/**
 * 
 */
package com.nbs.client.assassins;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;


import com.google.android.gcm.GCMRegistrar;

import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.FragmentById;


//import com.slidingmenu.lib.SlidingMenu;
//import com.slidingmenu.lib.app.SlidingFragmentActivity;

/* other info that may be relevant
String serial = android.os.Build.SERIAL;
TelephonyManager.getDeviceId();
WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
WifiInfo wInfo = wifiManager.getConnectionInfo();
String macAddress = wInfo.getMacAddress(); */

/**
 * @author cam
 *
 */

@EActivity
public class MainActivity extends SherlockFragmentActivity implements ActionBar.OnNavigationListener {
	
	//private MenuFragment menuFragment;
	
	@FragmentById(R.id.fragment_map)
	MapFragment map;
	
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
	
	
	MenuAdapter adapter;
	
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
		
		MenuRowData[] data = new MenuRowData[]{
			new MenuRowData(MenuItemType.MENU_NAV, "Play", R.drawable.ic_menu_mapmode, 0),
			new MenuRowData(MenuItemType.MENU_HEADER, "Loot", R.drawable.ic_coins_l, 1),
			new MenuRowData(MenuItemType.MENU_NAV, "Honors", R.drawable.ic_menu_myplaces, 2),
			new MenuRowData(MenuItemType.MENU_NAV, "Join", R.drawable.ic_menu_allfriends, 3),
			new MenuRowData(MenuItemType.MENU_HEADER, "Notifications", 4),
			new MenuRowData(MenuItemType.MENU_EVENT, "Something longer here.", "1/2/13", R.drawable.ic_audio_notification, 5),
			new MenuRowData(MenuItemType.MENU_EVENT, "Something else even lonnnngeeeeerrr...", "1/3/13", R.drawable.ic_audio_notification, 6),
		};
        
        adapter = new MenuAdapter(this, data);

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(adapter, this);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		setContentView(R.layout.activity_main);
		
    }
    
	
	@Override
	public void onBackPressed() {

		super.onBackPressed();
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		
		Object selectedItem = adapter.getItem(itemPosition);
		
		if(selectedItem instanceof MenuNavItem)
		{
			Log.i(TAG, Integer.toString(((MenuNavItem)selectedItem).getId()));
		}
		
		return false;
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


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		
		SubMenu more = menu.addSubMenu(Menu.NONE, 0, 0, "");
		
		more.setIcon(R.drawable.abs__ic_menu_moreoverflow_normal_holo_dark);
	    
		more.add("Create Account");
		more.add("Sign In");
	    more.add("Settings");
	    more.add("About");
	    more.add("Tutorial");

	    more.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
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
