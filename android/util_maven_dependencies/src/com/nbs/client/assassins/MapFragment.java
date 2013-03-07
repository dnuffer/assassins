/**
 * 
 */
package com.nbs.client.assassins;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockMapFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.googlecode.androidannotations.annotations.EFragment;

/**
 * @author cam
 *
 */

@EFragment
public class MapFragment extends SherlockMapFragment {

    private GoogleMap map;
    
    public MapFragment(){}
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    View root = super.onCreateView(inflater, container, savedInstanceState);
	    map = getMap();
	    
	    map.getUiSettings().setCompassEnabled(true);
	    
	    CameraPosition cameraPosition = new CameraPosition.Builder()
	    .target(new LatLng(0,0))
	    .zoom(25)              
	    .bearing(0)    
	    .tilt(45)                
	    .build();                   
	    
	    
	    
	    
	    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null);
	    return root;
        
    }

}
