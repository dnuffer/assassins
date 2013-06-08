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
import com.nbs.client.assassins.models.PlayerState;
import com.nbs.client.assassins.models.User;
import com.nbs.client.assassins.network.HuntedRestClient;
import com.nbs.client.assassins.network.LocationMessage;
import com.nbs.client.assassins.network.LocationResponse;
import com.nbs.client.assassins.network.PlayerStateResponse;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

@EService
public class LocationService extends Service {

	private static final String TAG = "LocationService";

	@RestService
	HuntedRestClient restClient;

	@SystemService
	LocationManager locationManager;

	LocationClient locationClient;
	LocationListener locationListener;
	Location current;
	
	ActivityRecognitionClient userActivityRecognitionClient;

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

	@Background
	public void updateLocation(Location newLocation)
	{
		if(User.hasToken(this)/* && isBetterLocation(newLocation, current)*/)
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
				msg.installId = User.getInstallId(this);
				
				Log.v(TAG, msg.toString());
				
				LocationResponse response = restClient.updateLocation(
												User.getToken(this), msg);
				
				Log.i(TAG,  response.toString());
				
				if(response != null && response.ok()) {
					Log.i(TAG,"location successfully sent to server.");
					User.setLocation(this, response.latitude, response.longitude);
					
					PlayerStateResponse state = response.playerState;
					
					if(state != null)
					{
						if(state.myLife != null) {
							PlayerState.setMyLife(this, state.myLife);
						}
						if(state.enemyRange != null) {
							PlayerState.setEnemyProximity(this, state.enemyRange);
						}
						if(state.targetLife != null) {
							PlayerState.setTargetLife(this, state.targetLife);
						}
						if(state.targetLat != null && state.targetLng != null) {
							PlayerState.setTargetLocation(this, state.targetLat, state.targetLng);
						}
						if(state.targetBearing != null) {
							PlayerState.setTargetBearing(this, state.targetBearing);
						}
						if(state.targetRange != null) {
							PlayerState.setTargetProximity(this, state.targetRange);
						}
					}
					
		            LocalBroadcastManager.getInstance(this)
		            	.sendBroadcast(new Intent(MainActivity.LOCATION_UPDATED));
				}
			}
		} else {
			//no token yet, but still want to draw user on map
			User.setLocation(this, newLocation.getLatitude(), newLocation.getLongitude());
			LocalBroadcastManager.getInstance(this)
        		.sendBroadcast(new Intent(MainActivity.LOCATION_UPDATED));
		}

	}

	@Override
	public void onCreate() {
		
		Log.d(TAG, "onCreate");
		
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
					
					Log.d(TAG, "LocationClient connected, location ["+lastLocation.toString()+"]");
					
					if(lastLocation != null) {
						updateLocation(lastLocation);
					}
					
					locationClient.requestLocationUpdates(
							new LocationRequest()
								.setInterval(3000)
								.setSmallestDisplacement(1.0f)
								.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY),
							locationListener);
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
		});
		
		locationClient.connect();
		
		
/*	TODO: implement activity recognition PendingIntent that would broadcast a 
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

	@Override
	public void onDestroy() {
		locationClient.disconnect();
	}

	@Override
	public void onStart(Intent intent, int startid) {

	}

	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/** Determines whether one Location reading is better than the current Location fix
	 * @param location  The new Location that you want to evaluate
	 * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	 */
	private boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}



}

