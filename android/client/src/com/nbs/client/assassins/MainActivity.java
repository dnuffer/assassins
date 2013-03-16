/**
 * 
 */
package com.nbs.client.assassins;

import net.simonvt.menudrawer.MenuDrawer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.googlecode.androidannotations.annotations.FragmentById;
import com.googlecode.androidannotations.annotations.rest.RestService;
import com.nbs.client.assassins.CreateAccoutFragment.OnAccountCreatedListener;
import com.nbs.client.assassins.CreateMatchFragment.OnMatchCreatedListener;
import com.nbs.client.assassins.JoinMatchFragment.OnMatchJoinedListener;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorInflater;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

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
public class MainActivity extends SherlockFragmentActivity 
	implements OnAccountCreatedListener, OnMatchCreatedListener, OnMatchJoinedListener {
	
	//private MenuFragment menuFragment;
	
	@FragmentById(R.id.fragment_map)
	MapFragment mapFragment;
	
	@RestService
	HuntedRestClient restClient;
	
	CreateAccoutFragment createAccountFragment;
	CreateMatchFragment createMatchFragment;
	JoinMatchFragment joinMatchFragment;
	NotificationFragment notifFrag;
	Fragment dummyFragment;
	
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

	private boolean creatingAccount = false;
	private boolean creatingMatch = false;
	private boolean joiningMatch = false;
	private boolean viewingNotifications = false;
	
	private Menu optionsMenu;
	
	MenuDrawer mDrawer;

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

    	Log.d(TAG, "onCreate() UserModel" + UserModel._toString(this));
        
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
		

        mDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);
        mDrawer.setContentView(R.layout.activity_main);
        mDrawer.setMenuView(R.layout.menu_list);

 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
    }
    
    @Background
	public void registerGCMRegIdOnServerInBackground() {
    	Log.e(TAG, "Gcm Registered with google, but apparently not on server...");
	}

	@Override
	public void onAccountCreated(boolean wasCreated) {
		
		removeCreateAccountFragment();
		creatingAccount = false;
		supportInvalidateOptionsMenu();
	}
	
	@Override
	public void onMatchCreated(boolean wasCreated) {
		
		removeCreateMatchFragment();
		creatingMatch = false;
		supportInvalidateOptionsMenu();
	}
	
	@Override
	public void onMatchJoined(boolean wasCreated) {
		
		removeJoinMatchFragment();
		joiningMatch = false;
		supportInvalidateOptionsMenu();
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
	
	private void removeNotificationFragment() {
		
		if(viewingNotifications && notifFrag != null) {
		    
			/*dummyFragment = new Fragment();
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.setCustomAnimations(R.animator.object_slide_in_left, R.animator.object_slide_out_right);
			ft.replace(R.id.fragment_container, dummyFragment);
		    ft.commit();  */


			
			AnimatorSet set = new AnimatorSet();
			set.playTogether(
					ObjectAnimator.ofFloat(notifFrag.getListView(), 
							"x", notifFrag.getListView().getLeft(), notifFrag.getListView().getRight()));
			set.setDuration(500);
			set.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator animation) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					ft.remove(notifFrag);
				    ft.commit();
				    viewingNotifications = false;
				    supportInvalidateOptionsMenu();
				}

				@Override
				public void onAnimationCancel(Animator animation) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onAnimationRepeat(Animator animation) {
					// TODO Auto-generated method stub
					
				} });
		    set.start();
		}
		
	}
	
	private void removeCreateMatchFragment() {
		
		if(creatingMatch && createMatchFragment != null) {
		
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.show(mapFragment);
			ft.remove(createMatchFragment);
		    ft.commit();   
		    creatingMatch = false;
		    createMatchFragment = null;
		}
	}
	
	private void removeJoinMatchFragment() {

		if(joiningMatch && joinMatchFragment != null) {
		
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.show(mapFragment);
			ft.remove(joinMatchFragment);
		    ft.commit();   
		    joiningMatch = false;
		    joinMatchFragment = null;
		}
	}
	
	@Override
	public void onBackPressed() {

		if      (creatingAccount) { removeCreateAccountFragment(); } 
		else if (joiningMatch)    { removeJoinMatchFragment(); } 
		else if (creatingMatch)   { removeCreateMatchFragment(); }
		else    { super.onBackPressed(); }
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
		optionsMenu = menu;
		return super.onCreateOptionsMenu(menu);
	}
	
	private void addInMatchOptionsMenuItems(Menu menu) {
	}
	
	private void addNotInMatchOptionsMenuItems(Menu menu) {
		MenuItem join = menu.add(NOT_IN_MATCH_ITEMS, JOIN_ID, Menu.NONE, "");
		join.setIcon(R.drawable.social_group);
		join.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		MenuItem createMatch = menu.add(NOT_IN_MATCH_ITEMS, CREATE_MATCH_ID, Menu.NONE, "");
		createMatch.setIcon(R.drawable.content_new);
		createMatch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	}
	
	private void removeInMatchOptionsMenuItems(Menu menu) {
		
	}
	
	private void removeNotInMatchOptionsMenuItems(Menu menu) {
		menu.removeGroup(NOT_IN_MATCH_ITEMS);
	}
	
	

	private void addNewUserOptionsMenuItems(Menu menu) {
		SubMenu more = menu.addSubMenu(Menu.NONE, MORE_ID, 1, "");
		more.setIcon(R.drawable.abs__ic_menu_moreoverflow_normal_holo_dark);
		more.add(NEW_USER_ITEMS, CREATE_ACCOUNT_ID, 2, "Create Account");
		more.add(NEW_USER_ITEMS, SIGN_IN_ID, 3, "Sign In");

		more.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	}
	
	private void addLoggedInOptionsMenuItems(Menu menu) {
		SubMenu more = menu.addSubMenu(Menu.NONE, MORE_ID, 1, "");
		more.setIcon(R.drawable.abs__ic_menu_moreoverflow_normal_holo_dark);
		more.add(Menu.NONE, SIGN_OUT_ID, 2, "Sign Out");

		more.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	}
	
	private void removeNewUserOptionsMenuItems(Menu menu) {
		menu.removeItem(MORE_ID);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		if(creatingAccount || creatingMatch || joiningMatch || viewingNotifications)
		{
    		//on hide in MapFragment is too slow... need to remove earlier
    		menu.removeGroup(MapFragment.MAP_CONTROL_ITEMS);
			removeInMatchOptionsMenuItems(menu);
    		removeNotInMatchOptionsMenuItems(menu);
    		menu.removeGroup(NEW_USER_ITEMS);
		} else {
			if(UserModel.hasToken(this) && UserModel.hasUsername(this)) {
				addLoggedInOptionsMenuItems(menu);
				menu.removeGroup(NEW_USER_ITEMS);
			} else {
				addNewUserOptionsMenuItems(menu);
			}
			
			if(UserModel.hasMatch(this)) {
				Log.d(TAG, UserModel.getMatch(this).toString());
				addInMatchOptionsMenuItems(menu);
	    		removeNotInMatchOptionsMenuItems(menu);
			}
			else {
				addNotInMatchOptionsMenuItems(menu);
				removeInMatchOptionsMenuItems(menu);
			}
		}
		
		menu.add(Menu.NONE, NOTIF_ID, Menu.NONE, "")
			.setIcon(R.drawable.ic_audio_notification)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    
		FragmentTransaction ft;
		
		switch (item.getItemId()) {
	    	case android.R.id.home:
	    		return true;
	    	case CREATE_ACCOUNT_ID:
	    		creatingAccount = true;
    			supportInvalidateOptionsMenu();
	    		createAccountFragment = new CreateAccoutFragment_();
	    		ft = getSupportFragmentManager().beginTransaction();
	    		ft.hide(mapFragment);
	    		ft.add(R.id.fragment_container, createAccountFragment);
	    	    //ft.show(createAccountFragment);

	    	    ft.commit();
	    	    

	    	    return true;
	    	case JOIN_ID:
	    		joiningMatch = true;
	    		supportInvalidateOptionsMenu();
	    		joinMatchFragment = new JoinMatchFragment_();
	    		ft = getSupportFragmentManager().beginTransaction();
	    		ft.hide(mapFragment);
	    		ft.add(R.id.fragment_container, joinMatchFragment);
	    	    //ft.show(createAccountFragment);
	    	    ft.commit();

	    	    return true;
	    	case CREATE_MATCH_ID:
	    		creatingMatch = true;
	    		supportInvalidateOptionsMenu();
	    		createMatchFragment = new CreateMatchFragment_();
	    		ft = getSupportFragmentManager().beginTransaction();
	    		ft.hide(mapFragment);
	    		ft.add(R.id.fragment_container, createMatchFragment);
	    	    //ft.show(createAccountFragment);
	    	    ft.commit();
	    		return true;
	    	case NOTIF_ID:
	    		
	    		if(viewingNotifications) {
	    			removeNotificationFragment();
	    		} else {
	    		
		    		viewingNotifications = true;
		    		notifFrag = new NotificationFragment();
		    		ft = getSupportFragmentManager().beginTransaction();
		    		ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
		    		ft.replace(R.id.fragment_container, notifFrag);
		    	    //ft.show(createAccountFragment);
		    	    ft.commit();
	    	    
	    		}
	    	    return true;
			default:
	    }
	    
		return super.onOptionsItemSelected(item);
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





}
