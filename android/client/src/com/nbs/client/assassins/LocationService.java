package com.nbs.client.assassins;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import com.google.android.gcm.GCMRegistrar;
import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EService;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.annotations.rest.RestService;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

@EService
public class LocationService extends Service {

	private static final String TAG = "LocationService";

	@RestService
	HuntedRestClient restClient;

	@SystemService
	LocationManager locationManager;

	LocationListener locationListener;
	Location current;

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
		if(UserModel.hasToken(this) && isBetterLocation(newLocation, current))
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
		            Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		            editor.putString("my_lat", Double.toString(response.latitude));
		            editor.putString("my_lng", Double.toString(response.longitude));
		            editor.commit();

		            sendBroadcast(new Intent(MainActivity.LOCATION_UPDATED));
				}
			}
		}

	}

	@Override
	public void onCreate() {

		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				updateLocation(location);
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
		};

		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0/*ms*/, 5/*meters*/, locationListener);
	}

	@Override
	public void onDestroy() {
		locationManager.removeUpdates(locationListener);
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

