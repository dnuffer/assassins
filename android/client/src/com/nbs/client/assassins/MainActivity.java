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
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import net.simonvt.menudrawer.MenuDrawer;

import com.google.android.gcm.GCMRegistrar;

import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.FragmentById;


//import com.slidingmenu.lib.SlidingMenu;
//import com.slidingmenu.lib.app.SlidingFragmentActivity;

/**
 * @author cam
 *
 */

@EActivity
public class MainActivity extends SherlockFragmentActivity implements OnItemClickListener {
	
	//private MenuFragment menuFragment;
	
	@FragmentById(R.id.fragment_map)
	MapFragment map;
	
	private MenuDrawer menuDrawer;
	
	private final String TAG = "MainActivity";
	
	public static final String ACTION = "some_action";
	public static final String ATTACKED = "attacked";
	public static final String MATCH_START = "match_start";
	public static final String MATCH_END = "match_end";
	public static final String MATCH_REMINDER = "match_reminder";
	public static final String INVITATION = "invitation";
	public static final String MATCH_EVENT = "match_event";
	public static final String TARGET_STATE_CHANGED = "a";
	public static final String ENEMY_STATE_CHANGED = "b";
	public static final String MY_STATE_CHANGED = "c";
	
	
	IntentFilter intentActionFilter;

	private OnSharedPreferenceChangeListener prefChangeListener =  new OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.i(TAG, "shared preference changed: " + key);
        }

	};

	
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

        
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.registerOnSharedPreferenceChangeListener(prefChangeListener);
        
        
        intentActionFilter = new IntentFilter();
        intentActionFilter.addAction(ACTION);      
        
        startService(new Intent(this, LocationService_.class));
        
		menuDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_CONTENT);
		menuDrawer.setContentView(R.layout.activity_main);
		menuDrawer.setMenuView(R.layout.menu_list);
		menuDrawer.setMenuSize(320);

		MenuFragment menu = (MenuFragment)getSupportFragmentManager().findFragmentById(R.id.menu_fragment);
		
		
		menu.getListView().setOnItemClickListener(this);
		

		
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
    }
    
	
	@Override
	public void onBackPressed() {
        final int drawerState = menuDrawer.getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            menuDrawer.closeMenu();
            return;
        }

		super.onBackPressed();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		menuDrawer.setActiveView(view);
		menuDrawer.closeMenu();
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
		unregisterReceiver(intentActionReceiver);
	    super.onPause();
	}
	

	@Override
	protected void onResume() 
	{
	    super.onResume();

	    registerReceiver(intentActionReceiver, intentActionFilter);
	}

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
        /* other info that may be relevant
        String serial = android.os.Build.SERIAL;
        TelephonyManager.getDeviceId();
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = wifiManager.getConnectionInfo();
		String macAddress = wInfo.getMacAddress(); */
        
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	    	menuDrawer.toggleMenu();
	        return true;
	    }
	    
		return super.onOptionsItemSelected(item);
	}
	

	
	   private BroadcastReceiver intentActionReceiver = new BroadcastReceiver() 
	    {
	        @Override
	        public void onReceive(Context context, Intent intent) 
	        {
	    		Log.v(TAG, "map " + map);
	        	
	        	if(map != null)
	    		{
/*		    		LocationMessage u = new LocationMessage();
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
		    		
		    		
		    		m.showInfoWindow();*/
	    		}

	        }
	    };

}
