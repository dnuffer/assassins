/**
 * 
 */
package com.nbs.client.assassins.views;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.actionbarsherlock.app.SherlockMapFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.googlecode.androidannotations.annotations.EFragment;
import com.nbs.client.assassins.models.PlayerModel;
import com.nbs.client.assassins.models.UserModel;
import com.nbs.client.assassins.sensors.BearingProvider;
import com.nbs.client.assassins.sensors.BearingReceiver;

/**
 * @author cam
 *
 */

@EFragment
public class MapFragment extends SherlockMapFragment implements BearingReceiver {
	
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
	
	BearingProvider bearingProvider;
	
	public static final int MODE_NORTH = 0;
	public static final int MODE_BEARING = 1;
	private int MODE = MODE_NORTH;

	private float bearing;
	
	private static final float DEFAULT_ZOOM = 18.0f;
	private static final float DEFAULT_TILT = 67.5f;
	
	private GoogleMap map;
	
	private Marker targetLocationMarker;
	
	private Circle rangeCircle; 
	private Polygon boundsPolygon;
 	private Marker myLocationMarker;
 	private int circleColor = Color.argb(100, Color.red(Color.RED), Color.green(Color.RED), Color.blue(Color.RED));
 	private int boundsColor = Color.argb(100, Color.red(Color.BLUE), Color.green(Color.BLUE), Color.blue(Color.BLUE));
	
 	private boolean animating = false;

	private Float tBearing;

	private Polyline tBearingLine;
 	
 	public MapFragment(){ }

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		map = getMap();

		UiSettings uiSettings = map.getUiSettings();
		uiSettings.setCompassEnabled(true);
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		uiSettings.setScrollGesturesEnabled(false);
		uiSettings.setRotateGesturesEnabled(false);
		uiSettings.setZoomGesturesEnabled(true);
		uiSettings.setZoomControlsEnabled(true);
		
		Location lastLocation = getBestLastKnownLocation();
		LatLng lastLatLng;
		
		if(lastLocation == null) {
			lastLatLng = UserModel.getLocation(getSherlockActivity());
		} else {
			lastLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
			
			if(UserModel.getLocation(getSherlockActivity()) == null)
			{
				UserModel.setLocation(getSherlockActivity(), lastLocation);
			}	
		}
		
		
		showMyLocation(lastLatLng); 
		
		if(UserModel.inMatch(getSherlockActivity()))
		{
			showGameBoundary();
			showAttackRangeCircle(lastLatLng);
			showDirectionToTarget(tBearing);
			showTargetLocation(PlayerModel.getTargetLocation(getSherlockActivity()));
		}
		
		moveMapPositionTo(lastLatLng, DEFAULT_ZOOM, DEFAULT_TILT, true, 2000);
        
		super.onViewCreated(view, savedInstanceState);
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
	
	public int getCompassMode() {
		return this.MODE;
	}
	
	public void toggleCompassMode() {
		if(getCompassMode() == MODE_BEARING) {
			MODE = MODE_NORTH;
			stopSensorUpdates();
			this.moveMapPositionTo(UserModel.getLocation(getSherlockActivity()), true, 800);
			map.getUiSettings().setZoomControlsEnabled(true);
		} else  {
			MODE = MODE_BEARING;
			map.getUiSettings().setZoomControlsEnabled(false);
			if(!animating)
				registerForSensorUpdates();
		}
	}

	public void setBearingProvider(BearingProvider bp) {
		bearingProvider = bp;
	}
	
	private void stopSensorUpdates() {
		bearingProvider.unregisterForBearingUpdates(this);
	}

	private void registerForSensorUpdates() {
		bearingProvider.registerForBearingUpdates(this);
	}
	public void onLocationChanged(LatLng location) {
		showMyLocation(location); 
		showAttackRangeCircle(location);
		showDirectionToTarget(tBearing);
		moveMapPositionTo(location);
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

	private void showMyLocation(LatLng location) {
		if(myLocationMarker == null) {	
			myLocationMarker = map.addMarker(new MarkerOptions()
				.position(location)
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
		} else {
			myLocationMarker.setPosition(location);
		}
	}
	
	public void showTargetLocation(LatLng tLatLng) {
		if(tLatLng != null) {
			if(targetLocationMarker == null) {
				targetLocationMarker = getMap().addMarker(
			    		new MarkerOptions()
			    		.position(tLatLng)
			    		.title("target")
			    		.snippet(PlayerModel.getTargetLife(getActivity()).toString())
			    		.icon(BitmapDescriptorFactory
			    				.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
				targetLocationMarker.showInfoWindow();
			}
			else
			{
				targetLocationMarker.setPosition(tLatLng);
			}
			
			targetLocationMarker.setVisible(true);
		}
	}	
	
	public void hideTargetLocation() {
		if(targetLocationMarker != null) targetLocationMarker.setVisible(false);
	}

	private void showGameBoundary() {
		if(boundsPolygon == null) {
			boundsPolygon = map.addPolygon(new PolygonOptions()
				.zIndex(0)
				.add(new LatLng(40.3, -111.6), new LatLng(40.4, -111.6), new LatLng(40.4, -111.7), new LatLng(40.3, -111.7))
			    .strokeColor(boundsColor)
			    .fillColor(Color.TRANSPARENT));
		}
	}

	private void showAttackRangeCircle(LatLng location) {
		if(rangeCircle == null) {
			rangeCircle = map.addCircle(new CircleOptions()
			     .center(location)
			     .radius(50)
			     .strokeColor(circleColor)
			     .fillColor(Color.TRANSPARENT));
		} else {
			rangeCircle.setCenter(location);
		}
	}
	

	private void showDirectionToTarget(Float tBearing) {
		
		if(tBearing != null && myLocationMarker != null) {
		
			LatLng myLocation = this.myLocationMarker.getPosition();
			
			tBearing = (float) Math.toRadians(tBearing);
			
			double dist = 0.05/6371.0;
			double lat1 = Math.toRadians(myLocation.latitude);
			double lon1 = Math.toRadians(myLocation.longitude);
	
			double lat2 = Math.asin( Math.sin(lat1)*Math.cos(dist) + Math.cos(lat1)*Math.sin(dist)*Math.cos(tBearing) );
			double a = Math.atan2(Math.sin(tBearing)*Math.sin(dist)*Math.cos(lat1), Math.cos(dist)-Math.sin(lat1)*Math.sin(lat2));
			double lon2 = lon1 + a;
			//lon2 = (lon2+ 3.0*Math.PI) % (2.0*Math.PI) - Math.PI;
			
			if(tBearingLine != null) {
				tBearingLine.remove();
			}
			
			tBearingLine = map.addPolyline(new PolylineOptions()
					.add(myLocation, new LatLng(Math.toDegrees(lat2), Math.toDegrees(lon2)))
					.color(circleColor));
		}
		else if(tBearingLine != null) {
			tBearingLine.remove();
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

	


	@Override
	public void onBearingChanged(float bearing) {
		this.bearing = bearing;
		moveMapPositionTo(this.myLocationMarker.getPosition());
	}
	
	public void onTargetBearingChanged(float tBearing) {
		this.tBearing = tBearing;
		showDirectionToTarget(tBearing);
	}

}
