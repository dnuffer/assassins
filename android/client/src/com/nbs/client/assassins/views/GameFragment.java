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
import com.nbs.client.assassins.models.App;
import com.nbs.client.assassins.models.Match;
import com.nbs.client.assassins.models.MatchMapper;
import com.nbs.client.assassins.models.Player;
import com.nbs.client.assassins.models.PlayerMapper;
import com.nbs.client.assassins.models.PlayerModel;
import com.nbs.client.assassins.models.Repository;
import com.nbs.client.assassins.models.User;
import com.nbs.client.assassins.sensors.BearingProvider;
import com.nbs.client.assassins.sensors.BearingProviderImpl;
import com.nbs.client.assassins.services.LocationService;
import com.nbs.client.assassins.services.PushNotifications;
import com.nbs.client.assassins.utils.Bus;

@EFragment(R.layout.game_fragment)
public class GameFragment extends SherlockFragment{

	private static final String TAG = "GameFragment";
	private static final String HUD_FRAGMENT = "HUDFragment";
	private static final String MAP_FRAG_ID = null;
	MapFragment_ mapFragment;
	HUDFragment_ hudFragment;

	@ViewById(R.id.toggle_compass)
	ImageView toggleCompass;
	
	private BearingProvider bearingSource;
	
	public GameFragment() { }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initMapFragment();
		registerReceivers();
	}

	private void initIntentFilters(Player player) {
		
		Context context = getActivity();

		unregisterReceivers();
		
		IntentFilter playerFilter = new IntentFilter();
		playerFilter.addAction(player.matchId + "." + player.username);
		Bus.register(context, focusedPlayerRcvr, playerFilter);
		
		IntentFilter gameFilter = new IntentFilter();
		gameFilter.addAction(player.matchId);
		Bus.register(context, focusedGameRcvr, gameFilter);
		
		IntentFilter userFilter = new IntentFilter();
        userFilter.addAction(LocationService.LOCATION_UPDATED);
        userFilter.addAction(User.LOGOUT_COMPLETE);
        Bus.register(context, userRcvr, userFilter);
        
		IntentFilter focusFilter = new IntentFilter();
        focusFilter.addAction(User.FOCUSED_GAME_CHANGED);
        Bus.register(context, focusedGameChanged, focusFilter);
	}
	
	private void registerReceivers() {
		Repository model = ((App)getActivity().getApplication()).getRepo();
        initIntentFilters(model.getMyFocusedPlayer());
	}
	
	private void unregisterReceivers() {
		Context context = getActivity();
		Bus.unregister(context, focusedPlayerRcvr);
		Bus.unregister(context, focusedGameRcvr);
		Bus.unregister(context, userRcvr);
		Bus.unregister(context, focusedGameChanged);
	}

	private void initMapFragment() {
		FragmentTransaction ft;
		mapFragment = new MapFragment_();
		ft = getActivity().getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.game_fragment_container, mapFragment, MAP_FRAG_ID);
		ft.commit();
		
		bearingSource = new BearingProviderImpl(getActivity());
		mapFragment.setBearingProvider(bearingSource);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Repository model = ((App)getActivity().getApplication()).getRepo();
		
		if(model.inActiveMatch()) {
			showHUD();
		} 
		
		schedulePendingMatchStartTimeAlarms();
		
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
	
	public void schedulePendingMatchStartTimeAlarms() {
		
		Context context = getActivity();
		Repository model = ((App)getActivity().getApplication()).getRepo();
		
		for (Match m : model.getPendingMatches()) {
			Log.d(TAG, "schedulingMatchStartTimeAlarm() " + m.startTime);
			AlarmManager alarmMngr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			//if the match has already begun,  it will fire immediately
			alarmMngr.set(AlarmManager.RTC_WAKEUP, m.startTime, 
				PendingIntent.getBroadcast(context, 0, 
						new Intent(PushNotifications.MATCH_START).putExtra("match_id", m.id), 
						PendingIntent.FLAG_UPDATE_CURRENT));
		}
	}
	
	@Override
	public void onPause() {
		registerReceivers();
		super.onPause();
	}

	@Override
	public void onResume() {
		bearingSource = new BearingProviderImpl(getActivity());
		mapFragment.setBearingProvider(bearingSource);
		//TODO: the bearing is messed up, fix bearing first
		//if(hudFragment != null) hudFragment.setBearingProvider(bearingSource);
	    
		unregisterReceivers();
		super.onResume();
	}

	@Click(R.id.toggle_compass)
	public void onToggleCompass() {
		mapFragment.toggleCompassMode();
		toggleCompass.setImageResource(
			mapFragment.getCompassMode() == MapFragment.MODE_BEARING ? 
					R.drawable.north : R.drawable.compass);
	}
	
	public void onTargetRangeChanged(String tRange) {
		if(hudIsShowing()) hudFragment.onTargetRangeChanged(tRange);
		if(tRange.equals(PlayerModel.HUNT_RANGE) || 
		   tRange.equals(PlayerModel.ATTACK_RANGE)) {
			Repository model = ((App)getActivity().getApplication()).getRepo();
			Player p = model.getMyFocusedPlayer();
			mapFragment.showTargetLocation(p.getTargetLatLng());
		} else {
			mapFragment.hideTargetLocation();
		}
	}
	
	public void onEnemyRangeChanged(String eRange) {
		if(hudIsShowing()) hudFragment.onEnemyRangeChanged(eRange);
	}
	
	public void onTargetBearingChanged(float tBearing) {
		mapFragment.onTargetBearingChanged(tBearing);
		if(hudIsShowing()) hudFragment.onTargetBearingChanged(tBearing);
	}
	
	public void onTargetLifeChanged(int tLife) {
		if(hudIsShowing()) hudFragment.onTargetLifeChanged(tLife);
	}
	
	public void onMyLifeChanged(int life) {
		if(hudIsShowing()) hudFragment.onMyLifeChanged(life);
	}
	
	public void updatePlayer(Player player) {
		onTargetBearingChanged(Math.round(player.targetBearing));

		mapFragment.onTargetLocationChanged(player.getTargetLatLng());
		
		onTargetLifeChanged(player.targetHealth);
		
		onTargetRangeChanged(player.targetRange);
		
		onMyLifeChanged(player.health);
		
		onEnemyRangeChanged(player.enemyRange);
	}
	
	public void onLocationChanged(LatLng location) {
		mapFragment.onLocationChanged(location);
	}
	
	private BroadcastReceiver focusedGameChanged = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			Repository model = ((App)getActivity().getApplication()).getRepo();
			Player p = model.getMyFocusedPlayer();
			
			initIntentFilters(p);

			hideHUD();
			initMapFragment();
			showHUD();
			
			updatePlayer(p);
			
		}
		
	};
	
	private BroadcastReceiver userRcvr = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
        	
			Repository model = ((App)getActivity().getApplication()).getRepo();

        	User user = model.getUser();
        	
			if(action.equals(LocationService.LOCATION_UPDATED)) {
    			if(model.inActiveMatch()) { 
    				initIntentFilters(model.getMyFocusedPlayer());
    			}
    			onLocationChanged(user.getLocation());
    		} else if(action.equals(User.LOGOUT_COMPLETE)) {
    			hideHUD();
    			mapFragment.onMatchEnd();
    		}
		}
		
	};
	
	private BroadcastReceiver focusedPlayerRcvr = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
        	String action = intent.getAction();
        	
        	Log.d(TAG, "broadcast received [" + action + "]");

        	Player player = PlayerMapper.fromExtras(intent.getExtras());
        	
        	updatePlayer(player);

		}
	};
	
	private BroadcastReceiver focusedGameRcvr = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	String action = intent.getAction();
        	
        	Log.d(TAG, "broadcast received [" + action + "]");
    		
        	Repository model = ((App)getActivity().getApplication()).getRepo();
        	
        	User user = model.getUser();

        	Match m = MatchMapper.fromExtras(intent.getExtras());
        	
        	if(m.winner != null) {
        		String winner = m.winner;
        		hideHUD();
        		if(m.winner.equals(user.getUsername())) {
    				winner = "you";
    			}
        		Toast.makeText(context, "The hunt is over. " + winner  + " won.", Toast.LENGTH_LONG).show();
    			mapFragment.onMatchEnd();
        	}
        }
	};
}