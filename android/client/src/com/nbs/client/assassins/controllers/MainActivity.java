/**
 * 
 */
package com.nbs.client.assassins.controllers;

import net.simonvt.menudrawer.MenuDrawer;
import android.support.v4.app.FragmentTransaction;
import android.app.ProgressDialog;
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

import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.rest.RestService;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.models.User;
import com.nbs.client.assassins.network.HuntedRestClient;
import com.nbs.client.assassins.services.GCMUtilities;
import com.nbs.client.assassins.services.LocationService_;
import com.nbs.client.assassins.views.CreateAccountFragment;
import com.nbs.client.assassins.views.CreateMatchFragment;
import com.nbs.client.assassins.views.JoinMatchFragment;
import com.nbs.client.assassins.views.MapFragment_;
import com.nbs.client.assassins.views.MenuFragment;
import com.nbs.client.assassins.views.NotificationFragment;

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
public class MainActivity extends SherlockFragmentActivity {
	
	//private MenuFragment menuFragment;

	MapFragment_ mapFragment;
	
	@RestService
	HuntedRestClient restClient;
	
	CreateAccountFragment createAccountFragment;
	CreateMatchFragment createMatchFragment;
	JoinMatchFragment joinMatchFragment;
	NotificationFragment notifFrag;
	MenuFragment menuFrag;
	
	private final String TAG = "MainActivity";
	
	public static final String LOCATION_UPDATED = "com.nbs.android.client.LOCATION_UPDATED";
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
	private static final int CREATE_MATCH_ID = 5;
	private static final int NOT_IN_MATCH_ITEMS = 6;

	private static final int NEW_USER_ITEMS = 7;

	private static final int NOTIF_ID = 8;

	IntentFilter intentActionFilter;
	IntentFilter intentLocationUpdateFilter;

	private boolean notificationsShowing = false;
	
	//MenuDrawer mDrawer;

	private OnSharedPreferenceChangeListener prefChangeListener =  new OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.i(TAG, "shared preference changed: " + key);
            
        }

	};

	private boolean sideNavMenuShowing;

	
	public MainActivity() {
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    	Log.d(TAG, "onCreate() UserModel" + User._toString(this));
        
        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        
        if(GCMRegistrar.isRegistered(this))
        {
        	Log.i(TAG, "registered GCM id.");
        	
        	if(!GCMRegistrar.isRegisteredOnServer(this))
        	{
        		registerGCMRegIdOnServerInBackground();
        	}
        	
        } else
        {
        	GCMRegistrar.register(this, GCMUtilities.SENDER_ID);
        }
        
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.registerOnSharedPreferenceChangeListener(prefChangeListener);

        intentActionFilter = new IntentFilter();
        intentActionFilter.addAction(ACTION);      
        
        intentLocationUpdateFilter = new IntentFilter();
        intentLocationUpdateFilter.addAction(LOCATION_UPDATED);   
        
        startService(new Intent(this, LocationService_.class));
		
        //TODO: once google map v2 bug is fixed, use MenuDrawer lib for side menu navigation
        //mDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);
        //mDrawer.setContentView(R.layout.activity_main);
        //mDrawer.setMenuView(R.layout.menu_list);
        setContentView(R.layout.activity_main);
        
		FragmentTransaction ft;
		mapFragment = new MapFragment_();
		ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fragment_container, mapFragment);
		ft.commit();
        
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
    }
    
    @Background
	public void registerGCMRegIdOnServerInBackground() {
    	Log.e(TAG, "Gcm Registered with google, but apparently not on server...");
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
		unregisterReceiver(locationUpdateReceiver);
	    super.onPause();
	}
	

	@Override
	protected void onResume() 
	{
	    super.onResume();

	    registerReceiver(intentActionReceiver, intentActionFilter);
	    registerReceiver(locationUpdateReceiver, intentLocationUpdateFilter);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		return super.onCreateOptionsMenu(menu);
	}
	
	private void addInMatchOptionsMenuItems(Menu menu) {
	}
	
	private void addNotInMatchOptionsMenuItems(Menu menu) {
		menu.add(NOT_IN_MATCH_ITEMS, JOIN_ID, Menu.NONE, "Join Game")
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		menu.add(NOT_IN_MATCH_ITEMS, CREATE_MATCH_ID, Menu.NONE, "Create Game")
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	}
	
	private void removeInMatchOptionsMenuItems(Menu menu) {
		
	}

	private void addNewUserOptionsMenuItems(Menu menu) {
		menu.add(NEW_USER_ITEMS, CREATE_ACCOUNT_ID, 2, "Create Account")
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(NEW_USER_ITEMS, SIGN_IN_ID, 3, "Sign In")
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	}
	
	private void addLoggedInOptionsMenuItems(Menu menu) {
		SubMenu more = menu.addSubMenu(Menu.NONE, MORE_ID, 2, "");
		more.setIcon(R.drawable.abs__ic_menu_moreoverflow_normal_holo_dark);
		more.add(Menu.NONE, SIGN_OUT_ID, 2, "Sign Out");

		more.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	}
	

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		if(User.loggedIn(this)) {
			
			addLoggedInOptionsMenuItems(menu);
			menu.removeGroup(NEW_USER_ITEMS);
			
			if(User.inMatch(this)) {
				Log.d(TAG, User.getMatch(this).toString());
				addInMatchOptionsMenuItems(menu);
				menu.removeGroup(NOT_IN_MATCH_ITEMS);
				
				menu.add(Menu.NONE, NOTIF_ID, 1, "")
				.setIcon(R.drawable.ic_action_hdpi_bulleted_list)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
				
			} else {
				addNotInMatchOptionsMenuItems(menu);
				removeInMatchOptionsMenuItems(menu);
			}
				
		} else {
			addNewUserOptionsMenuItems(menu);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
	    	case android.R.id.home:	 
	    		Log.d(TAG, "home pressed");
	    		toggleSideNavMenu();
	    		return true;	
	    	case CREATE_ACCOUNT_ID:
	    		showCreateAccountFragment();
	    	    return true;
	    	case JOIN_ID:
	    		showJoinMatchFragment();
	    	    return true;
	    	case CREATE_MATCH_ID:
	    		showCreateMatchFragment();
	    		return true;
	    	case NOTIF_ID:
	    		toggleNotificationFragment();
	    	    return true;
	    	case SIGN_OUT_ID:
	    		ProgressDialog asyncProgress = new ProgressDialog(this);
				asyncProgress.setIndeterminate(true);
				asyncProgress.setTitle("Signing out...");
				asyncProgress.setCancelable(false);
				asyncProgress.show();
	    		signOut(asyncProgress);
	    		return true;
			default:
	    }
	    
		return super.onOptionsItemSelected(item);
	}

	@Background
	public void signOut(ProgressDialog asyncProgress)
	{
		User.signOut(this);
		onSignOutFinished(asyncProgress);
	}
	
	@UiThread
	public void onSignOutFinished(ProgressDialog asyncProgress)
	{
		supportInvalidateOptionsMenu();
		asyncProgress.dismiss();
	}
	
	private void toggleNotificationFragment() {
		FragmentTransaction ft;
		if(notificationsShowing) {
			removeNotificationFragment();
		} else {
			notificationsShowing = true;
			notifFrag = new NotificationFragment();
			ft = getSupportFragmentManager().beginTransaction();
			//ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
			ft.add(R.id.fragment_container, notifFrag);
		    //ft.show(createAccountFragment);
		    ft.commit();
		}
	}

	private void showCreateMatchFragment() {
		Intent createMatchIntent = new Intent(this, CreateMatchActivity_.class);
        startActivityForResult(createMatchIntent, CREATE_MATCH_ID);
	}

	private void showJoinMatchFragment() {
		Intent joinMatchIntent = new Intent(this, JoinMatchActivity_.class);
		startActivityForResult(joinMatchIntent, JOIN_ID);
	}

	private void showCreateAccountFragment() {
		Intent createAccountIntent = new Intent(this, CreateAccountActivity_.class);
		startActivityForResult(createAccountIntent, CREATE_ACCOUNT_ID);
	}

	private void toggleSideNavMenu() {
		FragmentTransaction ft;
		if(sideNavMenuShowing) {
			hideSideNavMenuFragment();
		} else {
			sideNavMenuShowing = true;
			menuFrag = new MenuFragment();
			ft = getSupportFragmentManager().beginTransaction();
			//ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
			ft.add(R.id.fragment_container, menuFrag);
		    //ft.show(createAccountFragment);
		    ft.commit();
		}
	}
	
	private void hideSideNavMenuFragment() {
		if(sideNavMenuShowing) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.remove(menuFrag);
		    ft.commit();
		    sideNavMenuShowing = false;
		    supportInvalidateOptionsMenu();
		}
	}
	
	
	private void removeNotificationFragment() {
		
		if(notificationsShowing && notifFrag != null) {
			/*
			 * For 3.0+
			 * dummyFragment = new Fragment();
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.setCustomAnimations(R.animator.object_slide_in_left, R.animator.object_slide_out_right);
			ft.replace(R.id.fragment_container, dummyFragment);
		    ft.commit();  */

			
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.remove(notifFrag);
		    ft.commit();
		    notificationsShowing = false;
		    supportInvalidateOptionsMenu();
		}
		
	}
	

	private BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) 
        {
    		Log.d(TAG, "received LOCATION_UPDATED broadcast.");
    	
    		if(mapFragment != null)
    			mapFragment.updateMapPosition();
	    }
	};
	
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


	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		Log.i(TAG, "onActivityResult");
		supportInvalidateOptionsMenu();
		super.onActivityResult(arg0, arg1, arg2);
	}





}
