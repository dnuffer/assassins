/**
 * 
 */
package com.nbs.client.assassins.controllers;

import java.util.Timer;
import java.util.TimerTask;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import net.simonvt.menudrawer.MenuDrawer;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.rest.RestService;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.models.MatchModel;
import com.nbs.client.assassins.models.PlayerModel;
import com.nbs.client.assassins.models.UserModel;
import com.nbs.client.assassins.network.AttackResponse;
import com.nbs.client.assassins.network.HuntedRestClient;
import com.nbs.client.assassins.network.LocationMessage;
import com.nbs.client.assassins.network.LocationResponse;
import com.nbs.client.assassins.network.Response;
import com.nbs.client.assassins.services.GCMUtilities;
import com.nbs.client.assassins.services.LocationService;
import com.nbs.client.assassins.services.LocationService_;
import com.nbs.client.assassins.services.NotificationService;
import com.nbs.client.assassins.services.NotificationService_;
import com.nbs.client.assassins.views.CreateAccountFragment;
import com.nbs.client.assassins.views.CreateMatchFragment;
import com.nbs.client.assassins.views.GameFragment;
import com.nbs.client.assassins.views.GameFragment_;
import com.nbs.client.assassins.views.HUDFragment_;
import com.nbs.client.assassins.views.JoinMatchFragment;
import com.nbs.client.assassins.views.MapFragment;
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
	
	private final String TAG = "MainActivity";
	
	@RestService
	HuntedRestClient restClient;
	
	NotificationFragment notifFrag;
	MenuFragment menuFrag;
	GameFragment gameFragment;
	
	/*options menu items*/
	private static final int JOIN_ID = 0;
	private static final int CREATE_ACCOUNT_ID = 1;
	private static final int SIGN_IN_ID = 2;
	private static final int MORE_ID = 3;
	private static final int SIGN_OUT_ID = 4;
	private static final int CREATE_MATCH_ID = 5;
	private static final int NOT_IN_MATCH_ITEMS = 6;

	private static final int NEW_USER_ITEMS = 7;
	
	private static final int IN_MATCH_ITEMS = 8;
	private static final int NOTIF_ID = 10;

	IntentFilter intentLocationUpdateFilter;

	private boolean notificationsShowing = false;
	
	//MenuDrawer mDrawer;

	private boolean sideNavMenuShowing;
	
	public MainActivity() {
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    	Log.d(TAG, "onCreate() UserModel" + UserModel._toString(this));
        
        registerForPushNotifications();

        intentLocationUpdateFilter = new IntentFilter();
        intentLocationUpdateFilter.addAction(LocationService.LOCATION_UPDATED);   
        
        startService(new Intent(this, LocationService_.class));
		
        //TODO: once google map v2 bug is fixed, use MenuDrawer lib for side menu navigation
        //mDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);
        //mDrawer.setContentView(R.layout.activity_main);
        //mDrawer.setMenuView(R.layout.menu_list);
        setContentView(R.layout.activity_main);

    	FragmentTransaction ft;
    	gameFragment = new GameFragment_();
    	ft = getSupportFragmentManager().beginTransaction();
    	ft.replace(R.id.fragment_container, gameFragment);
    	ft.commit();

 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
    }
    
	@AfterInject
	public void afterInjection() {
		//subvert a bug in HttpUrlConnection
		//see: http://www.sapandiwakar.in/technical/eofexception-with-spring-rest-template-android/
		restClient.getRestTemplate().setRequestFactory(
				new HttpComponentsClientHttpRequestFactory());
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent arg2) {
		Log.i(TAG, "onActivityResult");
		supportInvalidateOptionsMenu();
		super.onActivityResult(requestCode, resultCode, arg2);
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
	protected void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(locationUpdateReceiver);
		
		if(!MatchModel.inMatch(this)) {
			startService(new Intent(this, LocationService_.class).setAction(LocationService.STOP_UPDATES));
		}
		
	    super.onPause();
	}
	
	@Override
	protected void onResume() 
	{
	    LocalBroadcastManager.getInstance(this)
	    	.registerReceiver(locationUpdateReceiver, intentLocationUpdateFilter);
	    
	    startService(new Intent(this, LocationService_.class).setAction(LocationService.START_UPDATES));
	    
	    super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		return super.onCreateOptionsMenu(menu);
	}

	private void registerForPushNotifications() {
        
    	GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
		
		if(GCMRegistrar.isRegistered(this)) {
        	Log.i(TAG, "registered GCM id.");
        	
        	if(!GCMRegistrar.isRegisteredOnServer(this)) {
        		registerGCMRegIdOnServerInBackground();
        	}
        	
        } else {
        	GCMRegistrar.register(this, GCMUtilities.SENDER_ID);
        }
	}
    
    @Background
	public void registerGCMRegIdOnServerInBackground() {
    	Log.e(TAG, "Gcm Registered with google, but apparently not on server...");
	}
	
	private void addInMatchOptionsMenuItems(Menu menu) {
	}
	
	private void addNotInMatchOptionsMenuItems(Menu menu) {
		if(menu.findItem(CREATE_MATCH_ID) == null) {
			menu.add(NOT_IN_MATCH_ITEMS, CREATE_MATCH_ID, Menu.NONE, "Create Game")
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		if(menu.findItem(JOIN_ID) == null) {
			menu.add(NOT_IN_MATCH_ITEMS, JOIN_ID, Menu.NONE, "Join Game")
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
	}
	
	private void removeInMatchOptionsMenuItems(Menu menu) {
		menu.removeGroup(IN_MATCH_ITEMS);
	}

	private void addNewUserOptionsMenuItems(Menu menu) {
		if(menu.findItem(CREATE_ACCOUNT_ID) == null) {
			menu.add(NEW_USER_ITEMS, CREATE_ACCOUNT_ID, 2, "Create Account")
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		if(menu.findItem(SIGN_IN_ID) == null) {
			menu.add(NEW_USER_ITEMS, SIGN_IN_ID, 3, "Sign In")
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
	}
	
	private void addLoggedInOptionsMenuItems(Menu menu) {
		if(menu.findItem(MORE_ID) == null) {
			SubMenu more = menu.addSubMenu(Menu.NONE, MORE_ID, 2, "");
			more.setIcon(R.drawable.abs__ic_menu_moreoverflow_normal_holo_dark);
			more.add(Menu.NONE, SIGN_OUT_ID, 2, "Sign Out");
			more.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		if(UserModel.loggedIn(this)) {
			
			addLoggedInOptionsMenuItems(menu);
			menu.removeGroup(NEW_USER_ITEMS);
			
			if(MatchModel.inMatch(this)) {
				Log.d(TAG, MatchModel.getMatch(this).toString());
				addInMatchOptionsMenuItems(menu);
				menu.removeGroup(NOT_IN_MATCH_ITEMS);
				
				menu.add(Menu.NONE, NOTIF_ID, 1, "Messages")
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
	    	case SIGN_IN_ID:
	    		showSignInFragment();
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
		Response response = null;
		
		try {
			response = restClient.logout(UserModel.getToken(this));
			
			Log.d(TAG, response.toString());

			UserModel.signOut(this);
			this.startService(new Intent(this, NotificationService_.class)
									.setAction(NotificationService.CANCEL_MATCH_ALARMS));
			GCMRegistrar.setRegisteredOnServer(this, false);
			GCMRegistrar.unregister(this);

		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		
		onSignOutFinished(asyncProgress, response);
	}
	
	@UiThread
	public void onSignOutFinished(ProgressDialog asyncProgress, Response response)
	{
		supportInvalidateOptionsMenu();
		asyncProgress.dismiss();
		
		String message = response == null ? "Network error." : response.message;
		
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
	
	private void toggleNotificationFragment() {
		FragmentTransaction ft;
		if(notificationsShowing) {
			removeNotificationFragment();
			//this.toggleCompass.setVisibility(ImageView.VISIBLE);
		} else {
			hideSideNavMenuFragment();
			//this.toggleCompass.setVisibility(ImageView.INVISIBLE);
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
	
	private void showSignInFragment() {
		Intent loginIntent = new Intent(this, LoginActivity_.class);
		startActivityForResult(loginIntent, SIGN_IN_ID);
	}

	private void toggleSideNavMenu() {
		FragmentTransaction ft;
		if(sideNavMenuShowing) {
			hideSideNavMenuFragment();
		} else {
			removeNotificationFragment();
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
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.remove(notifFrag);
		    ft.commit();
		    notificationsShowing = false;
		    supportInvalidateOptionsMenu();
		}	
	}

	private BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
    		Log.d(TAG, "received LOCATION_UPDATED broadcast.");
    		gameFragment.onLocationChanged(UserModel.getLocation(context));
	    }
	};

}
