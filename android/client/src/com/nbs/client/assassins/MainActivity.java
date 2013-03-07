/**
 * 
 */
package com.nbs.client.assassins;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.FragmentById;


//import com.slidingmenu.lib.SlidingMenu;
//import com.slidingmenu.lib.app.SlidingFragmentActivity;

/**
 * @author cam
 *
 */

@EActivity
public class MainActivity extends SherlockFragmentActivity {
	
	//private MenuFragment menuFragment;
	
	@FragmentById(R.id.fragment_map)
	MapFragment map;
	
	private final String TAG = "MainActivity";
	
	IntentFilter playerStateChangedFilter; 
	
	public MainActivity() {
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        
        if(GCMRegistrar.isRegistered(this))
        {
        	GCMRegistrar.unregister(this);//TODO: Just for initial testing remove this line!
        } else
        {
        	GCMRegistrar.register(this, GCMUtilities.SENDER_ID);
        }

        playerStateChangedFilter = new IntentFilter();
        playerStateChangedFilter.addAction("PLAYER_STATE_CHANGED");      
        
        startService(new Intent(this, LocationService_.class));

 		
 		setContentView(R.layout.activity_main);
    }
    
    @Override
    public void onDestroy() 
    {
        try {
        	GCMRegistrar.onDestroy(this);
        }
        catch(RuntimeException e) {
        	Log.v(TAG, e.getMessage());
        }
    	super.onDestroy();
    }

	@Override
	protected void onPause() 
	{
		unregisterReceiver(playerStateChangedReceiver);
	    super.onPause();
	}
	

	@Override
	protected void onResume() 
	{
	    super.onResume();

	    registerReceiver(playerStateChangedReceiver, playerStateChangedFilter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	        //toggle();
	        return true;
	    }
	    
		return super.onOptionsItemSelected(item);
	}
	
	
	   private BroadcastReceiver playerStateChangedReceiver = new BroadcastReceiver() 
	    {
	        @Override
	        public void onReceive(Context context, Intent intent) 
	        {
	    		Log.v(TAG, "map " + map);
	        	
	        	if(map != null)
	    		{
		    		User u = new User();
		    		u.installId = intent.getStringExtra("installId");
		    		u.latitude = intent.getDoubleExtra("latitude", 0);
		    		u.longitude = intent.getDoubleExtra("longitude", 0);

		    		Log.v(TAG, u.installId);
		    		Log.v(TAG, Installation.id(context));
		    		
		    		Float markerColor = u.installId == Installation.id(context) ? 
									    					BitmapDescriptorFactory.HUE_AZURE : 
									    					BitmapDescriptorFactory.HUE_GREEN;
		    		
		    		Marker m = map.getMap().addMarker(
		    		new MarkerOptions()
		    		.position(new LatLng(u.latitude, u.longitude))
		    		.title(u.installId)
		    		.snippet("snippet")
		    		.icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
		    		
		    		
		    		m.showInfoWindow();
	    		}

	        }
	    };
}
