package com.nbs.client.assassins.views;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.maps.model.LatLng;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.models.MatchModel;
import com.nbs.client.assassins.models.PlayerModel;
import com.nbs.client.assassins.models.UserModel;
import com.nbs.client.assassins.sensors.BearingProvider;
import com.nbs.client.assassins.sensors.BearingProviderImpl;
import com.nbs.client.assassins.services.GCMMessages;
import com.nbs.client.assassins.utils.Bus;

@EFragment(R.layout.game_fragment)
public class GameFragment extends SherlockFragment{

	private static final String TAG = "GameFragment";
	private static final String HUD_FRAGMENT = "HUDFragment";
	MapFragment_ mapFragment;
	HUDFragment_ hudFragment;

	@ViewById(R.id.toggle_compass)
	ImageView toggleCompass;
	
	private BearingProvider bearingSource;
	private IntentFilter intentFilter;
	
	private IntentFilter matchStartFilter;
	
	public GameFragment() { }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FragmentTransaction ft;
		mapFragment = new MapFragment_();
		ft = getActivity().getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.game_fragment_container, mapFragment);
		ft.commit();
		
		bearingSource = new BearingProviderImpl(getActivity());
		mapFragment.setBearingProvider(bearingSource);
		
		matchStartFilter = new IntentFilter();
		matchStartFilter.addAction(GCMMessages.MATCH_START);
		
        intentFilter = new IntentFilter();
        intentFilter.addAction(PlayerModel.NEW_TARGET);   
        intentFilter.addAction(PlayerModel.TARGET_EVENT);
        intentFilter.addAction(PlayerModel.TARGET_BEARING_CHANGED); 
        intentFilter.addAction(PlayerModel.TARGET_LIFE_CHANGED); 
        intentFilter.addAction(PlayerModel.TARGET_LOCATION_CHANGED); 
        intentFilter.addAction(PlayerModel.TARGET_RANGE_CHANGED); 
        intentFilter.addAction(PlayerModel.ATTACKED); 
        intentFilter.addAction(PlayerModel.ENEMY_RANGE_CHANGED); 
        intentFilter.addAction(PlayerModel.MATCH_END);
        intentFilter.addAction(GCMMessages.MATCH_START);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		if(MatchModel.inActiveMatch(getSherlockActivity())) {
			showHUD();
		} else if(MatchModel.inMatch(getSherlockActivity()) && 
                !MatchModel.inActiveMatch(getSherlockActivity())) {
			scheduleMatchStartTimeAlarm();
        }
		super.onViewCreated(view, savedInstanceState);
	}

	private boolean hudIsShowing() {
		return getActivity().getSupportFragmentManager()
				.findFragmentByTag(HUD_FRAGMENT) != null;
	}
	
	private void showHUD() {
		if(!hudIsShowing()) {
			hudFragment = new HUDFragment_();
			hudFragment.setBearingProvider(bearingSource);
			getSherlockActivity()
				.getSupportFragmentManager()
					.beginTransaction()
					.add(R.id.game_fragment_container, hudFragment, HUD_FRAGMENT)
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.commit();
		}
	}
	
	private void hideHUD() {
		if(hudIsShowing()) {
			getSherlockActivity().getSupportFragmentManager()
				.beginTransaction()
				.remove(hudFragment)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.commit();
		}
	}
	
	public void scheduleMatchStartTimeAlarm() {
		
		Context context = getActivity();
		Log.d(TAG, "schedulingMatchStartTimeAlarm() " + MatchModel.getStartTime(context));
		AlarmManager alarmMngr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		//if the match has already begun,  it will fire immediately
		alarmMngr.set(AlarmManager.RTC_WAKEUP, MatchModel.getStartTime(context), 
			PendingIntent.getBroadcast(context, 0, new Intent(GCMMessages.MATCH_START), 
					PendingIntent.FLAG_UPDATE_CURRENT));
	}
	
	@Override
	public void onPause() {
		Bus.unregister(getSherlockActivity(),getBroadcastReceiver());
		super.onPause();
	}

	@Override
	public void onResume() {
		bearingSource = new BearingProviderImpl(getActivity());
		mapFragment.setBearingProvider(bearingSource);
		//TODO: the bearing is messed up, fix bearing first
		//if(hudFragment != null) hudFragment.setBearingProvider(bearingSource);
	    Bus.register(getSherlockActivity(),getBroadcastReceiver(), getIntentFilter());
		super.onResume();
	}
	
	@Click(R.id.toggle_compass)
	public void onToggleCompass() {
		mapFragment.toggleCompassMode();
		toggleCompass.setImageResource(
			mapFragment.getCompassMode() == MapFragment.MODE_BEARING ? 
					R.drawable.north : R.drawable.compass);
	}
	
	public void onTargetRangeChanged(Context c, String tRange) {
		Toast.makeText(c, "your enemy has entered " + tRange
				 + ".", Toast.LENGTH_SHORT).show();
		if(hudFragment != null) hudFragment.onTargetRangeChanged(tRange);
		if(tRange.equals(PlayerModel.HUNT_RANGE) || 
		   tRange.equals(PlayerModel.ATTACK_RANGE)) {
			mapFragment.showTargetLocation(PlayerModel.getTargetLocation(getSherlockActivity()));
		} else {
			mapFragment.hideTargetLocation();
		}
	}
	
	public void onEnemyRangeChanged(Context c, String eRange) {
		Toast.makeText(c, "your target is within " + eRange
				 + ".", Toast.LENGTH_SHORT).show();
		if(hudFragment != null) hudFragment.onEnemyRangeChanged(eRange);
	}
	
	public void onTargetBearingChanged(float tBearing) {
		mapFragment.onTargetBearingChanged(tBearing);
		if(hudFragment != null) hudFragment.onTargetBearingChanged(tBearing);
	}
	
	public void onTargetLifeChanged(int tLife) {
		if(hudFragment != null) hudFragment.onTargetLifeChanged(tLife);
	}
	
	public void onMyLifeChanged(int life) {
		if(hudFragment != null) hudFragment.onMyLifeChanged(life);
	}
	
	public void onLocationChanged(LatLng location) {
		mapFragment.onLocationChanged(location);
	}
	
	public BroadcastReceiver getBroadcastReceiver() {
		return gameBroadcastReceiver;
	}
	
	public IntentFilter getIntentFilter() {
		return intentFilter;
	}
	
	private BroadcastReceiver gameBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	String action = intent.getAction();
        	
        	Log.d(TAG, "received event [" + action + "]");
    		
    		if(action.equals(PlayerModel.NEW_TARGET)) {
    			Toast.makeText(context, "you have a new target.", Toast.LENGTH_SHORT).show();
    		}
    		else if(action.equals(PlayerModel.TARGET_BEARING_CHANGED)) {
    			float tBearing = PlayerModel.getTargetBearing(context);
    			onTargetBearingChanged(Math.round(tBearing));
    		}
    		else if(action.equals(PlayerModel.TARGET_LIFE_CHANGED)) {
    			onTargetLifeChanged(PlayerModel.getTargetLife(context));
    			Toast.makeText(context, "your target has suffered a blow.", Toast.LENGTH_SHORT).show();
    		}
    		else if(action.equals(PlayerModel.TARGET_RANGE_CHANGED)) {
    			onTargetRangeChanged(context, PlayerModel.getTargetProximity(context));
    		}
    		else if(action.equals(PlayerModel.ATTACKED)) {
    			onMyLifeChanged(PlayerModel.getMyLife(context));
    			Toast.makeText(context, "you were attacked.", Toast.LENGTH_SHORT).show();
    		}
    		else if(action.equals(PlayerModel.ENEMY_RANGE_CHANGED)) {
    			onEnemyRangeChanged(context, PlayerModel.getEnemyProximity(context));
    		}
    		else if(action.equals(PlayerModel.MATCH_END)) {
    			hideHUD();
    			String winner = intent.getStringExtra("winner");
    			if(winner != null && winner.equals(UserModel.getUsername(context))) {
    				winner = "you";
    			}
    			Toast.makeText(context, "The hunt is over. " + winner  + " won.", Toast.LENGTH_LONG).show();
    			MatchModel.setMatch(context, null);
    			PlayerModel.clearTarget(context);
    			PlayerModel.clearEnemy(context);
    		} else if(action.equals(GCMMessages.MATCH_START)) {
    			showHUD();
    		}
        }
	};
}