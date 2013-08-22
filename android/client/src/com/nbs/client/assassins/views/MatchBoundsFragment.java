package com.nbs.client.assassins.views;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
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
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.UiThread;
import com.nbs.client.assassins.utils.LocationUtils;

@EFragment
public class MatchBoundsFragment extends SherlockMapFragment {
	private static final String TAG = "MatchBoundsFragment";
	
	// Container Activity must implement this interface
    public interface OnBoundsSelectedListener {
        public void onBoundsSelected(String description, List<LatLng> points);
    }
    
	private OnBoundsSelectedListener mListener;
    
	private static final double DEFAULT_BOUNDS_HALF_WIDTH = 0.05;
	private static final int LATLNG_BOUNDS_PADDING = 80;

	private Marker corner1;
	private Marker corner2;
	
	private static final int RED = Color.argb(100, Color.red(Color.RED), Color.green(Color.RED), Color.blue(Color.RED));
	private static final int DONE_ID = 1;
	private Polygon matchBounds;

	private ProgressDialog progress;

 	public MatchBoundsFragment(){ }
    
 	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnBoundsSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnBoundsSelectedListener");
        }
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		setHasOptionsMenu(true);
		setMenuVisibility(true);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		
		getMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
		UiSettings uiSettings = getMap().getUiSettings();
		uiSettings.setCompassEnabled(true);
		uiSettings.setZoomGesturesEnabled(true);
		uiSettings.setZoomControlsEnabled(true);
		getMap().setMyLocationEnabled(true);

		progress = ProgressDialog.show(getSherlockActivity(), "Please Wait","Waiting for GPS...", true, false);
		
		getMap().setOnMarkerDragListener(new OnMarkerDragListener(){
			@Override
			public void onMarkerDrag(Marker m) {
				drawBounds();
			}

			@Override
			public void onMarkerDragEnd(Marker m) {}

			@Override
			public void onMarkerDragStart(Marker m) {}
		});
		
		getMap().setOnMyLocationChangeListener(new OnMyLocationChangeListener() {

			@Override
			public void onMyLocationChange(Location myLocation) {
				final double lat = myLocation.getLatitude();
				final double lng = myLocation.getLongitude();
				
				//default bounds are a 5 mile box around the user's location
				final LatLng cornerA = new LatLng(lat-DEFAULT_BOUNDS_HALF_WIDTH,lng-DEFAULT_BOUNDS_HALF_WIDTH);
				final LatLng cornerB = new LatLng(lat+DEFAULT_BOUNDS_HALF_WIDTH,lng+DEFAULT_BOUNDS_HALF_WIDTH);

				getMap().setOnMyLocationChangeListener(null);

				CameraUpdate camUpdate = CameraUpdateFactory
					.newLatLngBounds(new LatLngBounds(cornerA, cornerB), LATLNG_BOUNDS_PADDING);
				
				dismissGpsProgress();
				
				getMap().animateCamera(camUpdate, 1000, new CancelableCallback(){
					@Override
					public void onCancel() {
						dismissGpsProgress();
					}

					@Override
					public void onFinish() { 
						corner1 = getMap().addMarker(new MarkerOptions()
															.draggable(true)
															.position(cornerA));
						corner2 = getMap().addMarker(new MarkerOptions()
															.draggable(true)
															.position(cornerB));
						drawBounds();
						
					}
				});
			}});

		super.onViewCreated(view, savedInstanceState);
	}

	private void dismissGpsProgress() {
		if(progress != null) progress.dismiss();
	}
	private void drawBounds() {
		LatLng corner3 = new LatLng(corner2.getPosition().latitude, corner1.getPosition().longitude);
		LatLng corner4 = new LatLng(corner1.getPosition().latitude, corner2.getPosition().longitude);
		
		if(matchBounds == null) {		
			matchBounds = getMap().addPolygon(new PolygonOptions()
				.add(corner1.getPosition(), corner3, corner2.getPosition(), corner4)
				.strokeColor(RED));
		}
		else {
			matchBounds.setPoints(Arrays.asList(corner1.getPosition(), corner3, corner2.getPosition(), corner4));
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Log.d(TAG, "onCreateOptionsMenu()");
		menu.add(Menu.NONE, DONE_ID, Menu.FIRST, "Done")
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    	case DONE_ID:
	    		reverseGeocodeMatchArea( 
    				LocationUtils.midPoint(corner1.getPosition(), corner2.getPosition()),
    				ProgressDialog.show(getActivity(), "Please wait", "Computing bounds...", true, false));
	    	    return true;
	    }
		return super.onOptionsItemSelected(item);
	}
	
	@Background
	public void reverseGeocodeMatchArea(LatLng center, ProgressDialog progress) {
		Geocoder geo = new Geocoder(this.getSherlockActivity());
		List<Address> addresses = null;
		if(Geocoder.isPresent()) {
			try{
				addresses = geo.getFromLocation(center.latitude, center.longitude, 1);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		onGeocodeFinished(addresses, progress);
	}
	
	@UiThread
	public void onGeocodeFinished(List<Address> addresses, ProgressDialog progress) {
		progress.dismiss();
		
		String description = null;
		
		if(addresses != null && addresses.size() > 0) {
			String admin    = addresses.get(0).getAdminArea();
			String locality = addresses.get(0).getLocality();
			description = locality + ", " + admin;
			Log.d(TAG, description);
		}
		
		List<LatLng> s2N = LocationUtils.sortSouthToNorth(matchBounds.getPoints());

		//TODO: may need to change how we determine what is the 'most west/east'	
		//if your gameplay bounds cross the middle of the atlantic ocean, you will
		//experience unsatisfactory results!
		List<LatLng> e2W = LocationUtils.sortEastToWest(matchBounds.getPoints());

		//match bounds are stored as upper left and lower right corner
		LatLng n = s2N.get(s2N.size()-1);
		LatLng w = e2W.get(e2W.size()-1);
		LatLng s = s2N.get(0);
		LatLng e = e2W.get(0);

		mListener.onBoundsSelected(description, Arrays.asList(
				new LatLng(n.latitude, w.longitude), 
				new LatLng(s.latitude, e.longitude)));
	}

}
