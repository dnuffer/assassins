package assassins.ui.prototypes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bson.types.ObjectId;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

public class GameSnapshotManager {

	private Context context;
	private final ScheduledExecutorService locScheduler;
	private static final int NUM_THREADS = 1;
	private static final int MAX_SNAPSHOTS = 20;
	private ObjectId id;
	private ArrayList<GameSnapshot> snapshots;
	private boolean newTarget;
	private boolean attackWasSuccessful;
	private View matchView;
	private boolean wasAttacked;
	private Proximity myProximityToTarget;
	private Proximity enemyProximityToMe;
	private float oldBearing;
	private boolean targetSlain;
	private LocationManager mLocMgr;
	private Conspiracy currentMatch;
	private CurrentMatchTask currMatchTask;
	
	private LocationListener locListener = new LocationListener(){
		public void onProviderDisabled(String provider) {}
		public void onProviderEnabled(String provider) {}
		public void onLocationChanged(Location arg0) 
		{
			Runnable locUpdater = getLocationSender();
			locUpdater.run();
		}
		
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
	};
	private boolean attackAttempted;
	
	GameSnapshotManager(View m, Context context, ObjectId id)
	{
		this.context = context;
		this.matchView = m;
		this.id = id;
		
		newTarget = false;
		attackWasSuccessful = false;
		myProximityToTarget = Proximity.UNKNOWN_RANGE;
		enemyProximityToMe = Proximity.UNKNOWN_RANGE;
		oldBearing = 0.1f;
		snapshots = new ArrayList<GameSnapshot>();
		
		locScheduler = Executors.newScheduledThreadPool(NUM_THREADS);
	}
	
	public SendLocationPoster getLocationSender()
	{
		return new SendLocationPoster(id, matchView, this);
	}
	
	public void startSyncingWithWebService(long initialDelay, long delayBtwnUpdates)
	{
	    Runnable locUpdater = getLocationSender();
	    
	    try{
	    	//need the current match task to get attack delay and match params
	    	currMatchTask = new CurrentMatchTask(id, context, this);
	    	currMatchTask.execute();
	    }
	    catch(Exception e)
	    {
	    	Log.d("Project Assassins", "GameSnapshotManager.startSendingLocationToWebService(...)");
	    	Log.d("Project Assassins", "Caught exception: " + e);
	    }
	    	
	    try
	    {	
		    locScheduler.scheduleAtFixedRate(locUpdater, initialDelay, delayBtwnUpdates, TimeUnit.SECONDS);
	    }
	    catch(Exception e)
	    {
	    	Log.d("Project Assassins", "GameSnapshotManager.startSendingLocationToWebService(...)");
	    	Log.d("Project Assassins", "Caught exception: " + e);
	    	
	    }
	}
	
	public void startListeningForLocationChanges(long minTime, float minDistance)
	{
	    mLocMgr = (LocationManager) (context.getSystemService(Activity.LOCATION_SERVICE));
	    mLocMgr.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
	            minTime, minDistance, locListener);
	    mLocMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,
	            minTime, minDistance, locListener);
	    mLocMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
	            minTime, minDistance, locListener);
	}
	
	public void stopListeningForLocationChanges()
	{	
		mLocMgr.removeUpdates(locListener);
	}
	
	public void stopSendingLocationToWebService()
	{
		Log.d("Project Assassins", "GameSnapshotManager.stopSendingLocationToWebService()");
		
		if(locScheduler != null)
		{
			List cancelledTasks = locScheduler.shutdownNow();
			Log.d("Project Assassins", "Cancelled location tasks: " + cancelledTasks);
		}
		
		if(currMatchTask != null)
		{
			currMatchTask.cancel(true);
		}
	}
	
	public void updateSnapshot(GameSnapshot attackResults) {
		
		Log.d("Project Assassins", "LocationUpdater.updateSnapshot() with snapshot: " + attackResults);
		
		snapshots.add(attackResults);
		registerPlayerStateChanges();
		
		//keep a buffer of snapshots
		if(snapshots.size() > MAX_SNAPSHOTS)
		{
			snapshots.remove(0);
		}
		
		Log.d("Project Assassins", "LocationUpdater.updateSnapshot() snapshots size: " + snapshots.size());
		
		matchView.postInvalidate();
		
		Log.d("Project Assassins", "LocationUpdater.updateSnapshot() FINISHED");
	}
	
	private void registerPlayerStateChanges() {
		
		Log.d("Project Assassins", "LocationUpdater.registerPlayerStateChanges() BEGIN");
		
		if(snapshots.size() == 1) {
			
			GameSnapshot curr = snapshots.get(0);
			PlayerState myCurrState = curr.getMyState();

			if(myCurrState != null && myCurrState.getLife() == 0){
				notifyOfDeath();
			}
		
		} else if(snapshots.size() > 1) {
			GameSnapshot curr = snapshots.get(snapshots.size()-1);
			GameSnapshot prev = snapshots.get(snapshots.size()-2);
			
			if(curr != null && prev != null){
				
				PlayerState currTarget = curr.getTargetState();
				PlayerState prevTarget = prev.getTargetState();
				
				if(currTarget != null && prevTarget != null){
					
					if(currTarget.hasDifferentUserName(prevTarget)){	
						Notifier.sendStatusNotification(context, 
									"Assassins", "You have a new target.", R.drawable.assassin_icon);
						newTarget = true;
					}
					
					if(currTarget.compareLife(prevTarget) == -1){
						attackWasSuccessful = true;
					}
	
					if(currTarget.getLife() == 0 && targetSlain == false){
						targetSlain = true;
					}
				}
				
				PlayerState myCurrState = curr.getMyState();
				PlayerState myPrevState = prev.getMyState();
				
				if(myCurrState != null && myPrevState != null){
					
					if(myCurrState.compareLife(myPrevState) == -1){
						
						wasAttacked = true;
						
						if(myCurrState.getLife() == 0){
							notifyOfDeath();
						}else {
							Notifier.sendStatusNotification(context, 
									"Assassins", "You've been attacked.  Life Remaining: " + 
									Math.round(100*(((float)(myCurrState.getLife()))/3.0f)) + "%", 
									R.drawable.x_mark);
						}						
					}
					
					Proximity prevProximityToMe = enemyProximityToMe;
					enemyProximityToMe = myCurrState.getEnemyProximityToMe();
					Proximity prevProximityToTarget = myProximityToTarget;
					myProximityToTarget = myCurrState.getMyProximityToTarget();
					
					//check if player is winner
					if(prevProximityToMe != enemyProximityToMe && prevProximityToTarget != myProximityToTarget &&
						enemyProximityToMe == Proximity.NONE && myProximityToTarget == Proximity.NONE){
						
						notifyOfWin();		
						
					}else {
						//send status notifications for player or player's assassin mode change: search->hunt mode
						if(prevProximityToMe != enemyProximityToMe && enemyProximityToMe == Proximity.HUNT_RANGE){
							Notifier.sendStatusNotification(context, 
									"Assassins", "Your assassin approaches.", R.drawable.assassin_icon);
							stopListeningForLocationChanges();
							startListeningForLocationChanges((1*1000), 0);
						}
						
						if(prevProximityToTarget != myProximityToTarget && myProximityToTarget == Proximity.HUNT_RANGE){
							Notifier.sendStatusNotification(context, 
									"Assassins", "Your target is near.", R.drawable.assassin_icon);	
							stopListeningForLocationChanges();
							startListeningForLocationChanges((1*1000), 0);
						}	
					}
				}
			}
		}
		
		Log.d("Project Assassins", "LocationUpdater.registerPlayerStateChanges() FINISHED");
	}

	private void notifyOfWin() {
		Notifier.sendStatusNotification(context, 
				"Assassins", "You won the match.", R.drawable.assassin_icon);
		stopListeningForLocationChanges();
		
		try{
			AlertDialog alertDialog = new AlertDialog.Builder(context).create();
			alertDialog.setTitle("The Conspiracy is over.");
			alertDialog.setMessage("You are the master Assassin.");
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			   public void onClick(DialogInterface dialog, int which) {
					Bundle b = new Bundle();
					b.putString("profileId", id.toString());
					
					Intent i = new Intent(context, ProjectAssassins.class);
					i.putExtras(b);
					context.startActivity(i);
			   }
			});
			alertDialog.setIcon(R.drawable.assassin_icon);
			alertDialog.show();
			
		}
		catch(Exception e)
		{
			Log.d("Project Assassins", "caught expection when showing 'you win' dialog.");
		}
	}

	private void notifyOfDeath() {
		stopListeningForLocationChanges();
		stopSendingLocationToWebService();
		Notifier.sendStatusNotification(context, 
				"Assassins", "You've been slain.", 
				R.drawable.x_mark);
		try{

			AlertDialog alertDialog = new AlertDialog.Builder(context).create();
			alertDialog.setTitle("Your assassin dealt a fatal blow.");
			alertDialog.setMessage("You've been slain.");
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			   public void onClick(DialogInterface dialog, int which) {
					Bundle b = new Bundle();
					b.putString("profileId", id.toString());
					
					Intent i = new Intent(context, ProjectAssassins.class);
					i.putExtras(b);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(i);
			   }
			});
			alertDialog.setIcon(R.drawable.assassin_icon);
			alertDialog.show();
		}
		catch(Exception e)
		{
			Log.d("Project Assassins", "caught expection when showing 'you've been slain' dialog.");
		}
	}
	
	public void setCurrentMatch(Conspiracy match) {
		currentMatch = match;
	}
	
	public Conspiracy getCurrentMatch() {
		return currentMatch;
	}
	
	public Proximity getPlayerProximityToEnemy()
	{
		return myProximityToTarget;
	}
	
	public Proximity getEnemyProximityToPlayer()
	{
		return enemyProximityToMe;
	}
	
	public boolean targetChanged() {
		
		if(newTarget)
		{
			return true;
		}
		return false;
	}
	

	public void clearTargetSlainFlag() {
		targetSlain = false;
		
	}
	
	public void setTargetChanged(boolean targetChanged)
	{
		newTarget = targetChanged;
	}
	
	public float getOldBearingToTarget()
	{
		return oldBearing;
	}

	//Destructive
	public boolean attackSucceeded()
	{
		return attackWasSuccessful;
	}
	
	public void setAttackSucceeded(boolean attackSucceeded)
	{
		attackWasSuccessful = attackSucceeded;
	}
	
	public boolean isTargetSlain() {
		// TODO Auto-generated method stub
		return targetSlain;
	}
	
	public boolean wasAttacked()
	{
		return wasAttacked;
	}
	
	public void setAttacked(boolean wasAttacked)
	{
		this.wasAttacked = wasAttacked;
	}
	
	public String getTargetUsername()
	{
		PlayerState targetState = null;
		String name = null;
		
		if(snapshots.size() > 0 && snapshots.get(snapshots.size()-1) != null)
		{
			targetState = snapshots.get(snapshots.size()-1).getTargetState();
			if(targetState != null)
			{
				name = targetState.getUsername();
			}
		}
		
		return name;
	}
	
	public float getBearingToTarget() {
		float bearingToTarget = 0.0f;
		
		if(snapshots.size() > 0 && snapshots.get(snapshots.size()-1) != null)
		{
			bearingToTarget = snapshots.get(snapshots.size()-1).getBearingToTarget();
		}
		
		oldBearing = bearingToTarget;
		return bearingToTarget;
	}
	
	public GeoPoint getTargetLocation()
	{
		PlayerState targetState = null;
		GeoPoint location = null;
		
		Log.d("Project Assassins", "LocationUpdater.getTargetLocation() BEGIN");
		
		if(snapshots.size() > 0 && snapshots.get(snapshots.size()-1) != null)
		{
			targetState = snapshots.get(snapshots.size()-1).getTargetState();
			if(targetState != null)
			{
				location = targetState.getLocation();
			}
		}
		
		Log.d("Project Assassins", "LocationUpdater.getTargetLocation() FINISHED");
		return location;
	}

	public GeoPoint getMyLocation()
	{
		PlayerState myState = null;
		GeoPoint location = null;
		
		Log.d("Project Assassins", "LocationUpdater.getMyLocation() BEGIN");
		
		Log.d("Project Assassins", "Snapshots: "+snapshots.toString());
		
		if(snapshots.size() > 0 && snapshots.get(snapshots.size()-1) != null)
		{
			myState = snapshots.get(snapshots.size()-1).getMyState();
			Log.d("Project Assassins", "myState: "+myState);
			if(myState != null)
			{
				location = myState.getLocation();
			}
		}

		Log.d("Project Assassins", "LocationUpdater.getMyLocation() FINISHED");
		return location;
	}
	
	public int getTargetLife()
	{
		PlayerState targetState = null;
		int life = -1;
		
		if(snapshots.size() > 0 && snapshots.get(snapshots.size()-1) != null)
		{
			targetState = snapshots.get(snapshots.size()-1).getTargetState();
			if(targetState != null)
			{
				life = targetState.getLife();	
			}
		}
		
		return life;
	}
	
	public int getMyLife() {
		PlayerState myState = null;
		int life = -1;
		
		if(snapshots.size() > 0 && snapshots.get(snapshots.size()-1) != null)
		{
			myState = snapshots.get(snapshots.size()-1).getMyState();
			if(myState != null)
			{
				life = myState.getLife();	
			}
		}
		
		return life;
	}	

	public class SendLocationPoster implements Runnable
	{
		ObjectId id;
		View view;
		GameSnapshotManager gameMngr;
		
		SendLocationPoster(ObjectId id, View v, GameSnapshotManager gsm)
		{
			gameMngr = gsm;
			this.id = id;
			this.view = v;
		}
		
		public void run() {
			if(view != null && view.getHandler() != null && gameMngr != null && id != null){
				Log.d("Project Assassins", "SendLocationPoster.run() view handler: " + view.getHandler());
				view.getHandler().post(new SendLocationRunner(id, view, gameMngr));
			}
		}
		
	}
	
	public class SendLocationRunner implements Runnable {
		
		ObjectId id;
		View matchView;
		GameSnapshotManager gameMngr;
		
		SendLocationRunner(ObjectId id, View match, GameSnapshotManager gsm)
		{
			gameMngr = gsm;
			this.id = id;
			this.matchView = match;
			
		}
		
		public void run() {
			if(matchView != null && gameMngr != null){
				AsyncLocationTask alt = new AsyncLocationTask(id, matchView, gameMngr);
				alt.execute();
			}
			
		}
  }
	
	class CurrentMatchTask extends AsyncTask<Void, Integer, Conspiracy>{

		ObjectId id;
		Context context;
		GameSnapshotManager gameMngr;
		
		public CurrentMatchTask(ObjectId id, Context context, GameSnapshotManager gsm)
		{
			gameMngr = gsm;
			this.id = id;
			this.context = context;
			
		}

		@Override
		protected void onPostExecute(Conspiracy result) {
			Log.d("Project Assassins", "CurrentMatchTask.onPostExecute(...) " + result);
			//executes in UI thread
			gameMngr.setCurrentMatch(result);
			
			super.onPostExecute(result);
		}

		@Override
		protected Conspiracy doInBackground(Void... params) {

			AssassinsHttpClient httpC = new AssassinsHttpClient(context.getString(R.string.serviceUrl), context);
			Conspiracy match = null;
			
			try {		
				Log.d("Project Assassins", "Getting current match with profile id: "+id);
				
				match = httpC.getCurrentMatch(id);

				Log.d("Project Assassins", "Finished getting current match");
				
			} catch (AssassinsHttpException e) {
				Log.d("Project Assasins", "caught exception while getting current match: " + e.getMessage(), e.getCause());
			}	
			

			return match;
		}
		
	}

	public void setAttackAttemptInProgress(boolean attackAttempted) {
		this.attackAttempted = attackAttempted;
		
	}
	
	public boolean isAttackAttemptInProgress()
	{
		return attackAttempted;
	}


	




} 