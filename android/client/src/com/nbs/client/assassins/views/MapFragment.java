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
import com.nbs.client.assassins.models.App;
import com.nbs.client.assassins.models.Match;
import com.nbs.client.assassins.models.Player;
import com.nbs.client.assassins.models.Repository;
import com.nbs.client.assassins.models.User;
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
	
	BearingProvider bearingProvider;
	
	public static final int MODE_NORTH = 0;
	public static final int MODE_BEARING = 1;
	private int MODE = MODE_NORTH;

	private float bearing;
	
	private static final float DEFAULT_ZOOM = 18.0f;
	private static final float DEFAULT_TILT = 67.5f;

	private static final double METERS_PER_MILE = 1609.0d;
	private static final double RADIUS_OF_EARTH = 6371.0d;
	
	private GoogleMap map;
	
	private Marker targetLocationMarker;
	
	private Circle aRangeCircle;
	private Circle hRangeCircle;
	
	private Polygon boundsPolygon;
 	private Marker myLocationMarker;
 	
 	private int RED = Color.argb(100, 
 		Color.red(Color.RED), Color.green(Color.RED), Color.blue(Color.RED));
 	private int BLUE = Color.argb(100, 
 		Color.red(Color.BLUE), Color.green(Color.BLUE), Color.blue(Color.BLUE));
	
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
		
		Repository model = ((App)(getActivity().getApplication())).getRepo();
		User user = model.getUser();
		Player player = model.getMyFocusedPlayer();
		
		if(lastLocation == null) {
			lastLatLng = user.getLocation();
		} else {
			lastLatLng = new LatLng(lastLocation.getLatitude(), 
					lastLocation.getLongitude());
			
			if(user.getLocation() == null) {
				user.setLocation(lastLocation);
			}	
		}
		
		if(lastLatLng != null) showMyLocation(lastLatLng); 
		
		if(model.inActiveMatch()) {
			showGameBoundary();
			showRangeCircles(lastLatLng);
			showDirectionToTarget(tBearing);
			showTargetLocation(player.getTargetLatLng());
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
			Repository model = ((App)(getActivity().getApplication())).getRepo();
			User user = model.getUser();
			this.moveMapPositionTo(user.getLocation(), true, 800);
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
		if(bearingProvider != null) bearingProvider.unregisterForBearingUpdates(this);
	}

	private void registerForSensorUpdates() {
		if(bearingProvider != null) bearingProvider.registerForBearingUpdates(this);
	}
	public void onLocationChanged(LatLng location) {
		showMyLocation(location); 
		Repository model = ((App)(getActivity().getApplication())).getRepo();
		if(model.inActiveMatch()) {
			showRangeCircles(location);
			showDirectionToTarget(tBearing);
		} else {
			hideRangeCircles();
			hideTargetLocation();
			hideDirectionToTarget();
		}
		
		moveMapPositionTo(location);
	}
	
	public void moveMapPositionTo(LatLng location) {
		moveMapPositionTo(location, false, null);
	}
	
	public void moveMapPositionTo(LatLng location, boolean animate, Integer duration) {
		CameraPosition cam = map.getCameraPosition();
		moveMapPositionTo(location, cam.zoom, cam.tilt, animate, duration) ;
	}
	
	public void moveMapPositionTo(LatLng location, Float zoom, Float tilt, boolean animate, Integer duration) {

		if(!animating && location != null) {
			Log.d(TAG, "move map to [position=" + location.toString() + 
					", bearing=" + bearing + 
					", animate="+ animate + "]");

			CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(location)
				.zoom(zoom != null ? zoom : DEFAULT_ZOOM)              
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

	public void showMyLocation(LatLng location) {
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
				Repository model = ((App)(getActivity().getApplication())).getRepo();
				Player player = model.getMyFocusedPlayer();
				targetLocationMarker = getMap().addMarker(
			    		new MarkerOptions()
			    		.position(tLatLng)
			    		.title("target")
			    		.snippet(String.valueOf(player.getTargetLife()))
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

	public void showGameBoundary() {
		if(boundsPolygon == null) {
			boundsPolygon = map.addPolygon(new PolygonOptions()
				.zIndex(0)
				//TODO: show actual match bounds polygon
				.add(new LatLng(40.3, -111.6), new LatLng(40.4, -111.6), new LatLng(40.4, -111.7), new LatLng(40.3, -111.7))
			    .strokeColor(BLUE)
			    .fillColor(Color.TRANSPARENT));
		}
	}

	public void showRangeCircles(LatLng location) {
		Repository model = ((App)(getActivity().getApplication())).getRepo();
		Match match = model.getFocusedMatch();
		Double aRange = match.getAttackRange();
		
		//if in match with attack range, draw/update the circle position
		if(aRange != null) {
			if(aRangeCircle == null) {
				aRangeCircle = map.addCircle(new CircleOptions()
				     .center(location)
				     .radius(aRange*METERS_PER_MILE)
				     .strokeColor(RED)
				     .strokeWidth(5)
				     .fillColor(Color.TRANSPARENT));
			} else {
				aRangeCircle.setCenter(location);
			}
		} else if (aRangeCircle != null) {
			//if not in a match and showing an attack range circle, clear it
			aRangeCircle.remove();
		}
		
		Double hRange = match.getHuntRange();
		
		if(hRange != null) {
			if(hRangeCircle == null) {
				hRangeCircle = map.addCircle(new CircleOptions()
				     .center(location)
				     .radius(hRange*METERS_PER_MILE)
				     .strokeColor(BLUE)
				     .strokeWidth(5)
				     .fillColor(Color.TRANSPARENT));
			} else {
				hRangeCircle.setCenter(location);
			}
		} else if (hRangeCircle != null) {
			hRangeCircle.remove();
		}
		
	}

	private void showDirectionToTarget(Float tBearing) {
		
		Repository model = ((App)(getActivity().getApplication())).getRepo();
		Match match = model.getFocusedMatch();
		Double aRange = match.getAttackRange();
		
		if(tBearing != null && myLocationMarker != null && aRange != null) {
		
			LatLng myLocation = this.myLocationMarker.getPosition();
			
			tBearing = (float) Math.toRadians(tBearing);
			
			double dist = aRange/RADIUS_OF_EARTH;
			double lat1 = Math.toRadians(myLocation.latitude);
			double lon1 = Math.toRadians(myLocation.longitude);
	
			double lat2 = Math.asin( Math.sin(lat1)*Math.cos(dist) + Math.cos(lat1)*Math.sin(dist)*Math.cos(tBearing) );
			double a = Math.atan2(Math.sin(tBearing)*Math.sin(dist)*Math.cos(lat1), Math.cos(dist)-Math.sin(lat1)*Math.sin(lat2));
			double lon2 = lon1 + a;
			//Is this necessary?
			//lon2 = (lon2+ 3.0*Math.PI) % (2.0*Math.PI) - Math.PI;
			
			if(tBearingLine != null) {
				tBearingLine.remove();
			}
			
			tBearingLine = map.addPolyline(new PolylineOptions()
					.add(myLocation, new LatLng(Math.toDegrees(lat2), Math.toDegrees(lon2)))
					.color(RED)
					.width(5));
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
	
	public void onMatchEnd() {
		hideRangeCircles();
		hideDirectionToTarget();
		hideTargetLocation();
	}
	
	public void hideRangeCircles() {
		if(aRangeCircle != null) aRangeCircle.remove();
		if(hRangeCircle != null) hRangeCircle.remove();
	}
	
	public void hideDirectionToTarget() {
		if(tBearingLine != null) { tBearingLine.remove(); }
	}
	
	public void hideTargetLocation() {
		if(targetLocationMarker != null) targetLocationMarker.setVisible(false);
	}
	
	public void onTargetLocationChanged(LatLng tLoc) {
		if(tLoc != null) {
			showTargetLocation(tLoc);
		} else {
			hideTargetLocation();
		}
	}
	
}
