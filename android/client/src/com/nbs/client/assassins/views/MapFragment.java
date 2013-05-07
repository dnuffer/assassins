/**
 * 
 */
package com.nbs.client.assassins.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.graphics.Color;
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
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.googlecode.androidannotations.annotations.EFragment;
import com.nbs.client.assassins.models.User;


/**
 * @author cam
 *
 */

@EFragment
public class MapFragment extends SherlockMapFragment implements SensorEventListener {

	protected static final String TAG = "MapFragment";
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		
		if(hidden) {
			stopSensorUpdates();
		}
		
		super.onHiddenChanged(hidden);	
	}
	
	public static final int MODE_NORTH = 0;
	public static final int MODE_BEARING = 1;
	private int MODE = MODE_NORTH;

	private static final float DEFAULT_ZOOM = 18.0f;
	private static final float DEFAULT_TILT = 67.5f;
	
	private GoogleMap map;
	private SensorManager mSensorManager;
	private Sensor accelerometer;
	private Sensor magnetometer;
	private float bearing;
	private boolean registeredForSensorUpdates = false;
	
	private Circle rangeCircle; 
	private Polygon boundsPolygon;
 	private Marker myLocationMarker;
 	private int circleColor = Color.argb(100, Color.red(Color.RED), Color.green(Color.RED), Color.blue(Color.RED));
 	private int boundsColor = Color.argb(100, Color.red(Color.BLUE), Color.green(Color.BLUE), Color.blue(Color.BLUE));
	
 	private boolean animating = false;
	
 	public MapFragment(){ }
 	

	public int getCompassMode() {
		return this.MODE;
	}
	
	public void toggleCompassMode() {
		if(getCompassMode() == MODE_BEARING) {
			MODE = MODE_NORTH;
			stopSensorUpdates();
			this.moveMapPositionTo(User.getLocation(getSherlockActivity()), true, 800);
			map.getUiSettings().setZoomControlsEnabled(true);
		} else  {
			MODE = MODE_BEARING;
			map.getUiSettings().setZoomControlsEnabled(false);
			if(!animating)
				registerForSensorUpdates();
		}
	}
 	
	//replace with a broadcast receiver
	private OnSharedPreferenceChangeListener prefChangeListener =  new OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			Log.i(TAG, "shared preference changed: " + key);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = super.onCreateView(inflater, container, savedInstanceState);
		
		return root;

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		
		//setHasOptionsMenu(true);
		
		map = getMap();

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
		sp.registerOnSharedPreferenceChangeListener(prefChangeListener);

		UiSettings uiSettings = map.getUiSettings();
		uiSettings.setCompassEnabled(true);
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		uiSettings.setScrollGesturesEnabled(false);
		uiSettings.setRotateGesturesEnabled(false);
		uiSettings.setZoomGesturesEnabled(true);
		uiSettings.setZoomControlsEnabled(true);

		mSensorManager = (SensorManager)getSherlockActivity().getSystemService(Context.SENSOR_SERVICE);
		accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
		Location lastLocation = getBestLastKnownLocation();
		LatLng lastLatLng;
		
		if(lastLocation == null) {
			lastLatLng = User.getLocation(getSherlockActivity());
		} else {
			lastLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
			
			if(User.getLocation(getSherlockActivity()) == null)
			{
				User.setLocation(getSherlockActivity(), lastLocation);
			}	
		}
		
		moveMapPositionTo(lastLatLng, DEFAULT_ZOOM, DEFAULT_TILT, true, 2000);
		
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
		
		if(MODE == MODE_BEARING) {
			registerForSensorUpdates();
		}
		else if (MODE == MODE_NORTH) {
			stopSensorUpdates();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		stopSensorUpdates();
		
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	private void registerForSensorUpdates() {
		if(!registeredForSensorUpdates) {
			mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
			mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
			registeredForSensorUpdates = true;
		}
	}
	private void stopSensorUpdates() {
		if(registeredForSensorUpdates) {
			mSensorManager.unregisterListener(this);
			registeredForSensorUpdates = false;
		}
	}

    private float[] mGData = new float[3];
    private float[] mMData = new float[3];
    private float[] mR = new float[16];
    private float[] mI = new float[16];
    private float[] mOrientation = new float[3];
    private final float rad2deg = (float)(180.0f/Math.PI);

	/* 
	 * @see http://www.codingforandroid.com/2011/01/using-orientation-sensors-simple.html 
	 * @see http://www.netmite.com/android/mydroid/cupcake/development/samples/
	 *      Compass/src/com/example/android/compass/CompassActivity.java
	 */
    public void onSensorChanged(SensorEvent event) {

        int type = event.sensor.getType();
        
        if (type == Sensor.TYPE_ACCELEROMETER) {
            mGData = lowPass( event.values.clone(), mGData );
        } else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
        	mMData = lowPass( event.values.clone(), mMData );
        } else {
            // we should not be here.
            return;
        }
        //TODO landscape mode is not working here
        SensorManager.getRotationMatrix(mR, mI, mGData, mMData);
        SensorManager.getOrientation(mR, mOrientation);
            
        bearing = (mOrientation[0]*rad2deg); // orientation contains: azimuth, pitch and roll		
		Log.i(TAG, "Sensor [bearing=" + bearing + "]");
		
		moveMapPositionTo(User.getLocation(getSherlockActivity()));
    }
    
	/*
	 * time smoothing constant for low-pass filter
	 * 0 ≤ alpha ≤ 1 ; a smaller value basically means more smoothing
	 * See: http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
	 */
	private static final float ALPHA = 0.10f;

	/**
	 * @see http://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
	 * @see http://developer.android.com/reference/android/hardware/SensorEvent.html#values
	 * @see http://blog.thomnichols.org/2012/06/smoothing-sensor-data-part-2
	 */
	private float[] lowPass( float[] newXYZ, float[] oldXYZ ) {
	    if ( oldXYZ == null ) return newXYZ;
	     
	    float[] filtered = new float[newXYZ.length];
	    for ( int i=0; i<newXYZ.length; i++ ) {
	    	filtered[i] = oldXYZ[i] + ALPHA * (newXYZ[i] - oldXYZ[i]);
	    }
	    return filtered;
	}

	public void updateMapPosition() {
		moveMapPositionTo(User.getLocation(getSherlockActivity()));
	}
	
	public void moveMapPositionTo(LatLng location) {
		moveMapPositionTo(location, false, null);
	}
	
	public void moveMapPositionTo(LatLng location, boolean animate, Integer duration) {
		CameraPosition currentCamera = map.getCameraPosition();
		float zoom = currentCamera.zoom;
		float tilt = currentCamera.tilt;
		moveMapPositionTo(location, zoom, tilt, animate, duration) ;
	}
	
	public void moveMapPositionTo(LatLng location, Float zoom, Float tilt, boolean animate, Integer duration) {

		if(!animating && location != null) {
			Log.d(TAG, "move map to [position=" + location.toString() + 
					", bearing=" + bearing + 
					", animate="+ animate + "]");
		
			CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(location)
				.zoom(zoom != null ? zoom : 19)              
				.bearing(MODE == MODE_NORTH  ? 0 : bearing)
				.tilt(tilt != null ? tilt : DEFAULT_TILT)                
				.build(); 	
			
			if(rangeCircle == null) {
				rangeCircle = map.addCircle(new CircleOptions()
				     .center(location)
				     .radius(50)
				     .strokeColor(circleColor)
				     .fillColor(Color.TRANSPARENT));
			} else {
				rangeCircle.setCenter(location);
			}
			
			if(boundsPolygon == null) {
				boundsPolygon = map.addPolygon(new PolygonOptions()
					.zIndex(0)
					.add(new LatLng(40.3, -111.6), new LatLng(40.4, -111.6), new LatLng(40.4, -111.7), new LatLng(40.3, -111.7))
				    .strokeColor(boundsColor)
				    .fillColor(Color.TRANSPARENT));
			}
			
			
			if(myLocationMarker == null) {	
				myLocationMarker = map.addMarker(new MarkerOptions()
		    		.position(location)
		    		.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
			} else {
				myLocationMarker.setPosition(location);
			} 

			CameraUpdate camUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
			
			if(animate) {
				stopSensorUpdates();
				animating = true;
				map.animateCamera(camUpdate, duration != null ? duration : 1000, 
					new CancelableCallback(){
						@Override
						public void onCancel() {
							animating = false;
							if(MODE == MODE_BEARING)
								registerForSensorUpdates();
						}
						@Override
						public void onFinish() {
							animating = false;
							if(MODE == MODE_BEARING)
								registerForSensorUpdates();
						}
				});
			} else {
				//map.stopAnimation();
				map.moveCamera(camUpdate);
			}
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
