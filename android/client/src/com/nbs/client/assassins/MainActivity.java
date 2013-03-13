/**
 * 
 */
package com.nbs.client.assassins;

import android.support.v4.app.FragmentTransaction;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;


import com.google.android.gcm.GCMRegistrar;

import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.FragmentById;
import com.nbs.client.assassins.CreateAccoutFragment.OnAccountCreatedListener;


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
public class MainActivity extends SherlockFragmentActivity implements OnAccountCreatedListener {
	
	//private MenuFragment menuFragment;
	
	@FragmentById(R.id.fragment_map)
	MapFragment mapFragment;
	
	CreateAccoutFragment createAccountFragment;
	
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

	/*options menu items*/
	private static final int JOIN_ID = 0;
	private static final int CREATE_ACCOUNT_ID = 1;
	private static final int SIGN_IN_ID = 2;
	private static final int MORE_ID = 3;
	private static final int SIGN_OUT_ID = 4;
	
	IntentFilter intentActionFilter;

	private boolean creatingAccount = false;

	private Menu optionsMenu;
	
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
        	Log.i(TAG, "registered GCM id.");
        } else
        {
        	GCMRegistrar.register(this, GCMUtilities.SENDER_ID);
        }

    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.registerOnSharedPreferenceChangeListener(prefChangeListener);

        intentActionFilter = new IntentFilter();
        intentActionFilter.addAction(ACTION);      
        
        startService(new Intent(this, LocationService_.class));
		
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		setContentView(R.layout.activity_main);
		
    }
    

	@Override
	public void onAccountCreated(boolean wasCreated) {
		
		removeCreateAccountFragment();
		
		removeNeedsAuthOptionsMenuItems();
		
		addLoggedInOptionsMenuItems(optionsMenu);
		
		this.supportInvalidateOptionsMenu();
	}

	private void removeCreateAccountFragment() {
		
		Log.i(TAG, "removing CreatAccountFragment");
		Log.i(TAG, "  creatingAccount: " + creatingAccount);
		Log.i(TAG, "  creatingAccountFragment: " + createAccountFragment);
		
		if(creatingAccount && createAccountFragment != null) {
		
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.show(mapFragment);
			ft.remove(createAccountFragment);
		    ft.commit();   
		    creatingAccount = false;
		    createAccountFragment = null;
		}
	}
	
	@Override
	public void onBackPressed() {

		if(creatingAccount) {
			removeCreateAccountFragment();
		} else {
			super.onBackPressed();
		}
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
		
		
		MenuItem join = menu.add(Menu.NONE, JOIN_ID, 0, "");
		join.setIcon(R.drawable.ic_menu_allfriends);
		join.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		if(!UserModel.hasToken(this) && !UserModel.hasUsername(this)) {
			addNeedsAuthOptionsMenuItems(menu);
		} else {
			addLoggedInOptionsMenuItems(menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	private void addNeedsAuthOptionsMenuItems(Menu menu) {
		SubMenu more = menu.addSubMenu(Menu.NONE, MORE_ID, 1, "");
		more.setIcon(R.drawable.abs__ic_menu_moreoverflow_normal_holo_dark);
		more.add(Menu.NONE, CREATE_ACCOUNT_ID, 2, "Create Account");
		more.add(Menu.NONE, SIGN_IN_ID, 3, "Sign In");

		more.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	}
	
	private void addLoggedInOptionsMenuItems(Menu menu) {
		SubMenu more = menu.addSubMenu(Menu.NONE, MORE_ID, 1, "");
		more.setIcon(R.drawable.abs__ic_menu_moreoverflow_normal_holo_dark);
		more.add(Menu.NONE, SIGN_OUT_ID, 2, "Sign Out");

		more.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	}
	
	private void removeNeedsAuthOptionsMenuItems() {
		optionsMenu.removeItem(SIGN_IN_ID);
		optionsMenu.removeItem(CREATE_ACCOUNT_ID);
		optionsMenu.removeItem(MORE_ID);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		this.optionsMenu = menu;
		
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	case android.R.id.home:
	    		return true;
	    	case CREATE_ACCOUNT_ID:
	    		creatingAccount = true;
	    		createAccountFragment = new CreateAccoutFragment_();
	    		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	    		ft.hide(mapFragment);
	    		ft.add(R.id.fragment_container, createAccountFragment);
	    	    //ft.show(createAccountFragment);
	    	    ft.commit();
    		break;
			default:
	    }
	    
		return super.onOptionsItemSelected(item);
	}
	

	
	   private BroadcastReceiver intentActionReceiver = new BroadcastReceiver() 
	    {
	        @Override
	        public void onReceive(Context context, Intent intent) 
	        {
	    		Log.v(TAG, "map " + mapFragment);
	        	
	        	if(mapFragment != null)
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
