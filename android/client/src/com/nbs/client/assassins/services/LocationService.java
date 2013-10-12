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
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.rest.RestService;
import com.nbs.client.assassins.models.App;
import com.nbs.client.assassins.models.Player;
import com.nbs.client.assassins.models.PlayerModel;
import com.nbs.client.assassins.models.Repository;
import com.nbs.client.assassins.models.User;
import com.nbs.client.assassins.network.HuntedRestClient;
import com.nbs.client.assassins.network.UpdateLocationRequest;
import com.nbs.client.assassins.network.LocationResponse;
import com.nbs.client.assassins.utils.Bus;
import com.nbs.client.assassins.utils.LocationUtils;

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
import android.widget.Toast;

@EService
public class LocationService extends Service {

	private static final String TAG = "LocationService";

	private static final int TWO_MINUTES = 1000 * 60 * 2;

	public static final String LOCATION_UPDATED = "com.nbs.android.client.LOCATION_UPDATED";
	public static final String STOP_UPDATES = "com.nbs.android.client.STOP_LOCATION_UPDATES";
	public static final String START_UPDATES = "com.nbs.android.client.START_LOCATION_UPDATES";
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
	
	ActivityRecognitionClient userActivityRecognitionClient;
	
	private IntentFilter intentFilter;
	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	String action = intent.getAction();
        	Log.d(TAG, "received intent [" + action + "]");
        	
        	if(action.equals(PushNotifications.MATCH_COUNTDOWN) || 
    		   action.equals(User.LOGIN_COMPLETE) || 
    		   action.equals(PushNotifications.MATCH_START)) {
				sendLocationToServer(locationClient.getLastLocation());
    		}
    		else if(action.equals(PlayerModel.TARGET_RANGE_CHANGED) || 
    				action.equals(PlayerModel.ENEMY_RANGE_CHANGED)) {
    			
    			//TODO if the user is not moving, do not request location updates
    			//use activity recognition (in comments below)
    			
    			//throttle location updates based on the nearest of these two ranges
    			String tRange = PlayerModel.getTargetProximity(LocationService.this);
    			String eRange = PlayerModel.getEnemyProximity(LocationService.this);
    			
    			int   interval = SEARCH_INTERVAL;
    			float dist = SEARCH_MIN_DISPLACEMENT;
    			int   priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
    			
    			if(tRange != null && eRange != null) {
	    			if(tRange.equals(PlayerModel.ATTACK_RANGE) || eRange.equals(PlayerModel.ATTACK_RANGE)) {
	    				interval = ATTACK_INTERVAL; 
	    				dist = ATTACK_MIN_DISPLACEMENT; 
	    				priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
	    			} else if(tRange.equals(PlayerModel.HUNT_RANGE) || eRange.equals(PlayerModel.HUNT_RANGE)) {
	    				interval = HUNT_INTERVAL; 
	    				dist = HUNT_MIN_DISPLACEMENT; 
	    				priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
	    			} 
    			}
    			requestLocationUpdates(interval, dist, priority);
    		}
    		else if(action.equals(PushNotifications.MATCH_END)) {
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
		String action = intent != null ? intent.getAction() : null;
		
		if(locationClient.isConnected()) {
			if(action != null && action.equals(LocationService.STOP_UPDATES)) {
				locationClient.removeLocationUpdates(locationListener);
			} else if(action != null && action.equals(LocationService.START_UPDATES)) {
				requestLocationUpdates(10000, 5.0f, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
			} else if(action != null && action.equals(PushNotifications.MATCH_COUNTDOWN)) {
    			sendLocationToServer(locationClient.getLastLocation());
    		}
			Bus.register(this,intentReceiver, intentFilter);	
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
		Bus.unregister(this,intentReceiver);
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		intentFilter = new IntentFilter();
		intentFilter.addAction(PushNotifications.MATCH_COUNTDOWN);
        intentFilter.addAction(PlayerModel.TARGET_RANGE_CHANGED); 
        intentFilter.addAction(PlayerModel.ENEMY_RANGE_CHANGED); 
        intentFilter.addAction(PushNotifications.MATCH_END);
        intentFilter.addAction(PushNotifications.MATCH_START);
        intentFilter.addAction(User.LOGIN_COMPLETE);
		
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				sendLocationToServer(location);
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
						sendLocationToServer(lastLocation);
					}
					
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
	public void sendLocationToServer(Location l)
	{
		Log.d(TAG, "location [" + (l != null ? l.toString() : null) + "]");

		if(l == null) return;
		
		Repository model = ((App)getApplication()).getRepo();
		User user = model.getUser();
		
		if(user.hasToken())
		{
			final String regId = GCMRegistrar.getRegistrationId(this);
			if (regId.equals("")) {
				GCMRegistrar.register(this, GCMUtilities.SENDER_ID);
			} else {
				Log.v(TAG, "GCM already registered. Sending location to server.");

				UpdateLocationRequest msg = 
					new UpdateLocationRequest(LocationUtils.locationToLatLng(l), 
							user.getInstallId());
				
				Log.v(TAG, msg.toString());
				
				LocationResponse response = restClient.updateLocation(user.getToken(), msg);
				
				Log.i(TAG, response.toString());
				
				if(response != null) { 
					if(response.ok()) {
						Log.i(TAG,"location successfully sent to server.");
						user.setLocation(response.latitude, response.longitude);
					}
					else {
						showToastOnUiThread(response.message);
					}
					
					//even if the location response is not 'ok' (i.e. out of bounds)
					//there will be a player state if in an active match
					for(Player p : response.players) {
						model.updatePlayer(p);
					}
				}
			}
		} else {
			//no token yet, but still broadcast for provisional-user functionality
			user.setLocation(l.getLatitude(), l.getLongitude());
			Bus.post(this,LocationService.LOCATION_UPDATED);
		}

	}
	
	@UiThread
	public  void showToastOnUiThread(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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

