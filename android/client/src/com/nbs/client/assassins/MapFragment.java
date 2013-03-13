/**
 * 
 */
package com.nbs.client.assassins;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.googlecode.androidannotations.annotations.EFragment;


/**
 * @author cam
 *
 */

@EFragment
public class MapFragment extends SherlockMapFragment {

	protected static final String TAG = "MapFragment";

	private GoogleMap map;
    
    private final static int CREATE_ACCOUNT_ID = Menu.FIRST;
    private final static int SIGN_IN_ID = Menu.FIRST+1;
    
	
	//replace with a broadcast receiver
	private OnSharedPreferenceChangeListener prefChangeListener =  new OnSharedPreferenceChangeListener() {

        private boolean myLat = false;
        private boolean myLng = false;
		
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.i(TAG, "shared preference changed: " + key);
    	    
            if(key == "my_lat") myLat = true;
            else if(key == "my_lng") myLng = true;
            
            if(myLat && myLng)
            {            	
            	
            	myLat = false;
            	myLng = false;
            	
            	double lat = Double.parseDouble(sharedPreferences.getString("my_lat", ""));
            	double lng = Double.parseDouble(sharedPreferences.getString("my_lng", ""));
            	
            	Log.i(TAG, "animating map to: " + lat + ", " + lng);
            	
                CameraPosition cameraPosition = new CameraPosition.Builder()
        	    .target(new LatLng(lat,lng))
        	    .zoom(25)              
        	    .bearing(0)    
        	    .tilt(45)                
        	    .build(); 
        	    
        	    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000, null);
            	
            }

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
	    
	    setHasOptionsMenu(true);
	    
	    return root;
        
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

}
