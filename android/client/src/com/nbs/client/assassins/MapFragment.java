/**
 * 
 */
package com.nbs.client.assassins;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockMapFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.SystemService;


/**
 * @author cam
 *
 */

@EFragment
public class MapFragment extends SherlockMapFragment implements SensorEventListener {

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
	}



	protected static final String TAG = "MapFragment";

	private GoogleMap map;

	private SensorManager mSensorManager;

	private Sensor accelerometer;
	private Sensor magnetometer;

	private float azimuth;

	//replace with a broadcast receiver
	private OnSharedPreferenceChangeListener prefChangeListener =  new OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			Log.i(TAG, "shared preference changed: " + key);
		}
	};

	public MapFragment(){ }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = super.onCreateView(inflater, container, savedInstanceState);
		map = getMap();

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(inflater.getContext());
		sp.registerOnSharedPreferenceChangeListener(prefChangeListener);

		map.getUiSettings().setCompassEnabled(true);
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		map.setIndoorEnabled(false);
		
		setHasOptionsMenu(true);   

		mSensorManager = (SensorManager)getSherlockActivity().getSystemService(Context.SENSOR_SERVICE);
		accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
		return root;

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		
		Location lastLocation = getBestLastKnownLocation();
		LatLng lastLatLng;
		
		if(lastLocation == null) {
			lastLatLng = UserModel.getLocation(getSherlockActivity());
		} else {
			lastLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
		}
		
		Log.i(TAG, "registering sensor listener");
		mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
		
		this.moveMapFocusTo(lastLatLng, true);
		
		super.onViewCreated(view, savedInstanceState);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	public void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		mSensorManager.unregisterListener(this);
	}

	/* Courtest: http://www.codingforandroid.com/2011/01/using-orientation-sensors-simple.html */
	float[] mGravity = new float[]{0.0f,0.0f, 0.0f};
	float[] mGeomagnetic = new float[]{0.0f,0.0f, 0.0f};
	@Override
	public void onSensorChanged(SensorEvent event) {

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			mGravity = event.values;
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			mGeomagnetic = event.values;
		
		if (mGravity != null && mGeomagnetic != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				azimuth = orientation[0]; // orientation contains: azimuth, pitch and roll
				
				Log.i(TAG, "Sensor [azimuth=" + azimuth + ", activty=" + getSherlockActivity() + "]");
				moveMapFocusTo(UserModel.getLocation(getSherlockActivity()), false);
			}
		}
	}

	public void updateMapPosition() {
		moveMapFocusTo(UserModel.getLocation(getSherlockActivity()), false);
	}
	
	public float getBearing() {
		return (float)Math.toDegrees(azimuth);
	}
	
	public void moveMapFocusTo(LatLng location, boolean animate) {

		if(location != null) {
			Log.d(TAG, "move map to [position=" + location.toString() + 
					", azimuth=" + azimuth + 
					", bearing=" + getBearing() + 
					", animate="+ animate + "]");
			CameraPosition cameraPosition = new CameraPosition.Builder()
			.target(location)
			.zoom(18)              
			.bearing(getBearing())
			.tilt(67.5f)                
			.build(); 
	
			CameraUpdate camUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
			
			if(animate)
				map.animateCamera(camUpdate, 2000, null);
			else
				map.moveCamera(camUpdate);
		}
	}
	
	private Location getBestLastKnownLocation() {
		LocationManager locationManager = (LocationManager) (getSherlockActivity().getSystemService(Context.LOCATION_SERVICE));
		
		Location location = null;
		
		if((location = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null)
			return location;
		else if((location = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null)
			return location;
		else if ((location = locationManager
				.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)) != null)
			return location;
		
		return location;
	}
}
