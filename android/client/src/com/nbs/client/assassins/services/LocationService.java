package com.nbs.client.assassins.services;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationListener;
import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EService;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.annotations.rest.RestService;
import com.nbs.client.assassins.controllers.MainActivity;
import com.nbs.client.assassins.models.PlayerModel;
import com.nbs.client.assassins.models.PlayerState;
import com.nbs.client.assassins.models.UserModel;
import com.nbs.client.assassins.network.HuntedRestClient;
import com.nbs.client.assassins.network.LocationMessage;
import com.nbs.client.assassins.network.LocationResponse;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

@EService
public class LocationService extends Service {

	private static final String TAG = "LocationService";

	private static final int TWO_MINUTES = 1000 * 60 * 2;

	public static final String LOCATION_UPDATED = "com.nbs.android.client.LOCATION_UPDATED";
	public static final String STOP_UPDATES = "com.nbs.android.client.STOP_LOCATION_UPDATES";
	public static final String START_UPDATES = "com.nbs.android.client.STOP_LOCATION_UPDATES";
	public static final String SEND_LOCATION_NOW = "com.nbs.android.client.SEND_LOCATION_NOW";

	
	public static final float SEARCH_MIN_DISPLACEMENT = 100.0f;
	public static final float HUNT_MIN_DISPLACEMENT   = 10.0f;
	public static final float ATTACK_MIN_DISPLACEMENT = 0.5f;
	
	public static final int SEARCH_INTERVAL = 10000;
	public static final int HUNT_INTERVAL   = 2000;
	public static final int ATTACK_INTERVAL = 500;
	
	@RestService
	HuntedRestClient restClient;

	@SystemService
	LocationManager locationManager;

	LocationClient locationClient;
	LocationListener locationListener;
	Location current;
	
	ActivityRecognitionClient userActivityRecognitionClient;
	
	private IntentFilter intentFilter;
	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
    		
        	String action = intent.getAction();
        	Log.d(TAG, "received intent [" + action + "]");
        	
    		if(action.equals(GCMMessages.MATCH_REMINDER) || 
    		   action.equals(UserModel.USER_TOKEN_RECEIVED)) {
				
    			Location lastLocation = locationClient.getLastLocation();
				Log.d(TAG, "last location ["+lastLocation.toString()+"]");
				if(lastLocation != null) {
					updateLocation(lastLocation);
				}
    		}
    		else if(action.equals(PlayerModel.TARGET_RANGE_CHANGED) || 
    				action.equals(PlayerModel.ENEMY_RANGE_CHANGED)) {
    			
    			//TODO if the user is not moving, do not request location updates
    			
    			//throttle location updates based on the nearest of these two ranges
    			String tRange = PlayerModel.getTargetProximity(LocationService.this);
    			String eRange = PlayerModel.getEnemyProximity(LocationService.this);
    			
    			int   interval = SEARCH_INTERVAL;
    			float dist = SEARCH_MIN_DISPLACEMENT;
    			int   priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
    			
    			if(tRange.equals(PlayerModel.ATTACK_RANGE) || eRange.equals(PlayerModel.ATTACK_RANGE)) {
    				interval = ATTACK_INTERVAL; dist = ATTACK_MIN_DISPLACEMENT; priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
    			} else if(tRange.equals(PlayerModel.HUNT_RANGE) || eRange.equals(PlayerModel.HUNT_RANGE)) {
    				interval = HUNT_INTERVAL; dist = HUNT_MIN_DISPLACEMENT; priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
    			} 
    			requestLocationUpdates(interval, dist, priority);
    		}
    		else if(action.equals(PlayerModel.MATCH_END)) {
    			requestLocationUpdates(10000, 5.0f, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    		}
	    }
	};
	
	private void requestLocationUpdates(int intervalMillis, float minDistanceMeters, int priority) {
		//replaces previous requests for the same listener
		locationClient.requestLocationUpdates(
				new LocationRequest()
					.setInterval(intervalMillis)
					.setSmallestDisplacement(minDistanceMeters)
					.setPriority(priority),
				locationListener);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand("+intent+")");
		
		String action = intent.getAction();
		
		Log.d(TAG, "onStartCommand("+action+")");
		
		if(locationClient.isConnected()) {
			
			if(action != null && action.equals(LocationService.STOP_UPDATES)) {
				locationClient.removeLocationUpdates(locationListener);
			}
			else if(action != null && action.equals(LocationService.START_UPDATES)) {
				requestLocationUpdates(10000, 5.0f, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
			}
			else if(action.equals(GCMMessages.MATCH_REMINDER)) {
    			Location lastLocation = locationClient.getLastLocation();
				Log.d(TAG, "last location ["+lastLocation.toString()+"]");
				if(lastLocation != null) {
					updateLocation(lastLocation);
				}
    		}
			
			LocalBroadcastManager.getInstance(this).registerReceiver(intentReceiver, intentFilter);
			
		} else if (locationClient.isConnecting() && action != null && action.equals(LocationService.STOP_UPDATES)) {
			try {
				locationClient.disconnect();
			} catch(Exception e) {
				Log.d(TAG, e.getMessage());
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		locationClient.disconnect();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(intentReceiver);
	}

	@Override
	public void onCreate() {
		
		Log.d(TAG, "onCreate");
		
		intentFilter = new IntentFilter();
		intentFilter.addAction(GCMMessages.MATCH_REMINDER);
        intentFilter.addAction(PlayerModel.TARGET_RANGE_CHANGED); 
        intentFilter.addAction(PlayerModel.ENEMY_RANGE_CHANGED); 
        intentFilter.addAction(PlayerModel.MATCH_END);
        intentFilter.addAction(UserModel.USER_TOKEN_RECEIVED);
		
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				updateLocation(location);
			}
		};
		
		locationClient = new LocationClient(this, 
			new ConnectionCallbacks(){
				@Override
				public void onConnected(Bundle arg0) {
					Location lastLocation = locationClient.getLastLocation();
					
					Log.d(TAG, "LocationClient connected");
					
					if(lastLocation != null) {
						Log.d(TAG, "LocationClient connected, location ["+lastLocation.toString()+"]");
						updateLocation(lastLocation);
					}
					
					//TODO: if the app is hidden and the player is not in a match, stop the location updates
					//      the MainActivity should handle this in the onPause
					requestLocationUpdates(1000, 3.0f, LocationRequest.PRIORITY_HIGH_ACCURACY);
				}
				@Override
				public void onDisconnected() {
					Log.d(TAG, "LocationClient disconnected");
					
				}
			}, 
			new OnConnectionFailedListener(){
				@Override
				public void onConnectionFailed(ConnectionResult arg0) {
					Log.d(TAG, "LocationClient connection failed");
					locationClient.connect();
				}
		});
		
		locationClient.connect();
		
		
		/*	TODO: implement activity recognition PendingIntent that would broadcast
		 *        to stop requesting location updates when a user is still for instance
		 * userActivityRecognitionClient = new ActivityRecognitionClient(this, 
			new ConnectionCallbacks(){
				@Override
				public void onConnected(Bundle arg0) {
					userActivityRecognitionClient.requestActivityUpdates(5000, PendingIntent);
				}
				@Override
				public void onDisconnected() {
					// TODO Auto-generated method stub
					
				}
			}, 
			new OnConnectionFailedListener(){
				@Override
				public void onConnectionFailed(ConnectionResult arg0) {
					Log.d(TAG, "LocationClient connection failed");
					locationClient.connect();
				}
		});*/
	}
	
	
	@Background
	public void updateLocation(Location newLocation)
	{
		Log.d(TAG, "location [" + (newLocation != null ? newLocation.toString() : null) + "]");

		if(newLocation != null && UserModel.hasToken(this))
		{
			current = newLocation;

			final String regId = GCMRegistrar.getRegistrationId(this);
			if (regId.equals("")) {
				GCMRegistrar.register(this, GCMUtilities.SENDER_ID);
			} else {
				Log.v(TAG, "Already registered");

				LocationMessage msg = new LocationMessage(); 
				msg.latitude  = current.getLatitude();
				msg.longitude = current.getLongitude();
				msg.installId = UserModel.getInstallId(this);
				
				Log.v(TAG, msg.toString());
				
				LocationResponse response = restClient.updateLocation(
												UserModel.getToken(this), msg);
				
				Log.i(TAG,  response.toString());
				
				if(response != null && response.ok()) {
					Log.i(TAG,"location successfully sent to server.");
					UserModel.setLocation(this, response.latitude, response.longitude);
					
					if(UserModel.inMatch(this)) {
						PlayerModel.setPlayerState(this, response.playerState);
					}

		            LocalBroadcastManager.getInstance(this)
		            	.sendBroadcast(new Intent(LocationService.LOCATION_UPDATED));
				}
			}
		} else {
			//no token yet, but still want to draw user on map
			UserModel.setLocation(this, newLocation.getLatitude(), newLocation.getLongitude());
			LocalBroadcastManager.getInstance(this)
        		.sendBroadcast(new Intent(LocationService.LOCATION_UPDATED));
		}

	}
	

	@AfterInject
	public void afterInjection() {
		//subvert a bug in HttpUrlConnection
		//see: http://www.sapandiwakar.in/technical/eofexception-with-spring-rest-template-android/
		restClient.getRestTemplate().setRequestFactory(
				new HttpComponentsClientHttpRequestFactory());
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}

