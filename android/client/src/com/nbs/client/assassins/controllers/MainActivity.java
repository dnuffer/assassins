/**
 * 
 */
package com.nbs.client.assassins.controllers;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import com.google.android.gcm.GCMRegistrar;
import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.rest.RestService;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.models.App;
import com.nbs.client.assassins.models.Player;
import com.nbs.client.assassins.models.Repository;
import com.nbs.client.assassins.models.User;
import com.nbs.client.assassins.network.HuntedRestClient;
import com.nbs.client.assassins.network.MatchResponse;
import com.nbs.client.assassins.network.Response;
import com.nbs.client.assassins.services.GCMUtilities;
import com.nbs.client.assassins.services.LocationService;
import com.nbs.client.assassins.services.LocationService_;
import com.nbs.client.assassins.services.NotificationService;
import com.nbs.client.assassins.services.NotificationService_;
import com.nbs.client.assassins.services.PushNotifications;
import com.nbs.client.assassins.utils.Bus;
import com.nbs.client.assassins.views.GameFragment;
import com.nbs.client.assassins.views.GameFragment_;
import com.nbs.client.assassins.views.MenuFragment;
import com.nbs.client.assassins.views.NotificationFragment;
import com.nbs.client.assassins.views.PlayerStatus;
import com.nbs.client.assassins.views.PlayerStatus.PlayerReadyListener;
import com.nbs.client.assassins.views.SideMenu;

/* other phone hardware info that may be relevant to make cheating/abuse difficult
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
public class MainActivity extends SherlockFragmentActivity implements PlayerReadyListener {
	
	private final String TAG = "MainActivity";
	
	@RestService
	HuntedRestClient restClient;
	
	NotificationFragment notifFrag;
	MenuFragment menuFrag;
	GameFragment gameFragment;
	
	@ViewById(R.id.left_drawer)
	SideMenu mNavDrawer;
	
	@ViewById(R.id.right_drawer)
	ListView mEventDrawer;
	
	private final String mEventDrawerTitle = "notifications";
	private String mTitle;
	
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

	IntentFilter intentFilter;
	
	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		
    		Log.d(TAG, "onReceive("+intent+")");
    		
//    		if(action != null) {
//    			Repository model = ((App)getApplication()).getRepo();
//    			if(action.equals(Repository.PLAYER_UPDATED)) {
//    				Player player = model.getPlayer(intent.getLongExtra("player_id", -1));
//	    			mNavDrawer.updatePlayer(player);
//	    		} else if(action.equals(Repository.MATCH_UPDATED)) {
//	    			Match match = model.getMatch(intent.getStringExtra("match_id"));
//	    			mNavDrawer.updateMatch(match);
//	    			supportInvalidateOptionsMenu();
//	    		} else if(action.equals(Repository.NEW_PLAYER)) {
//	    			Player player = model.getPlayer(intent.getLongExtra("player_id", -1));
//	    			mNavDrawer.addPlayer(player);
//	    		} else if(action.equals(Repository.NEW_MATCH)) {
//	    			Match match = model.getMatch(intent.getStringExtra("match_id"));
//	    			mNavDrawer.addMatch(match);
//	    			supportInvalidateOptionsMenu();
//	    		}
//    		}
	    }
	};
	
	/*
	private class NavDrawerItemClickListener implements OnItemClickListener {
	    @Override
	    public void onItemClick(AdapterView parent, View view, int position, long id) {
	        Log.d("NavDrawerItemClickListener", "OnItemClick("+position+")");
	    }
	}
	*/
	
	private class EventDrawerItemClickListener implements OnItemClickListener {
	    @Override
	    public void onItemClick(AdapterView parent, View view, int position, long id) {
	        Log.d("EventDrawerItemClickListener", "OnItemClick("+position+")");
	    }
	}
	
	private DrawerLayout mDrawerLayout;
	
	public MainActivity() {}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    	ProgressDialog progress = ProgressDialog.show(this, "Please wait...", "Waiting for GPS...");
        
        registerForPushNotifications();

        intentFilter = new IntentFilter(); 
        intentFilter.addAction(PushNotifications.MATCH_END);
        
        startService(new Intent(this, LocationService_.class));

        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(new ActionBarDrawerToggle(this, mDrawerLayout,
        	R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_closed) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
            	getSupportActionBar().setTitle(drawerView.equals(mNavDrawer) ? 
            			mTitle : mEventDrawerTitle);
                supportInvalidateOptionsMenu();
            }
        });
        
        
        Repository model = ((App)getApplication()).getRepo();
        mNavDrawer.setMatches(model.getMatches());
        
    	FragmentTransaction ft;
    	gameFragment = new GameFragment_();
    	ft = getSupportFragmentManager().beginTransaction();
    	ft.replace(R.id.fragment_container, gameFragment);
    	ft.commit();
 		
 		progress.dismiss();
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
	
	private void toggleNotificationFragment() {
	}
	
	private void toggleSideNavMenu() {
	}
	
    @Override
    public void onDestroy() {
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
		Bus.unregister(this,intentReceiver);
		Repository model = ((App)getApplication()).getRepo();
		if(!model.inMatch()) {
			startService(new Intent(this, LocationService_.class).setAction(LocationService.STOP_UPDATES));
		}
		
	    super.onPause();
	}
	
	@Override
	protected void onResume()  {
	    Bus.register(this,intentReceiver, intentFilter);
	    startService(new Intent(this, LocationService_.class).setAction(LocationService.START_UPDATES));
	    super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		return super.onCreateOptionsMenu(menu);
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
		
		// If the nav drawer is open, hide action items related to the content view
        boolean navDrawerOpen = mDrawerLayout.isDrawerOpen(mNavDrawer);
        boolean eventDrawerOpen = mDrawerLayout.isDrawerOpen(mEventDrawer);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		
        Repository model = ((App)getApplication()).getRepo();
        User user = model.getUser();
		if(user.isLoggedIn()) {
			addLoggedInOptionsMenuItems(menu);
			menu.removeGroup(NEW_USER_ITEMS);
			if(model.inMatch()) {
				Log.d(TAG, model.getFocusedMatch().toString());
				addInMatchOptionsMenuItems(menu);
				menu.removeGroup(NOT_IN_MATCH_ITEMS);
				//menu.add(Menu.NONE, NOTIF_ID, 1, "Messages").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
				
			} else {
				addNotInMatchOptionsMenuItems(menu);
				menu.removeGroup(IN_MATCH_ITEMS);
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
	    		signOut(ProgressDialog.show(this, "Please Wait", "Signing out...", true, false));
	    		return true;
			default:
	    }
	    
		return super.onOptionsItemSelected(item);
	}

	@Background
	public void signOut(ProgressDialog asyncProgress) {
		Response response = null;
		
		try {
			Repository model = ((App)getApplication()).getRepo();
			User user = model.getUser();
			response = restClient.logout(user.getToken());
			
			Log.d(TAG, response.toString());

			model.onLogout();
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
	public void onSignOutFinished(ProgressDialog asyncProgress, Response response) {
		supportInvalidateOptionsMenu();
		asyncProgress.dismiss();
		String message = response == null ? "Network error." : response.message;
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
	
	private void showCreateMatchFragment() {
        startActivityForResult(new Intent(this, CreateMatchActivity_.class), CREATE_MATCH_ID);
	}

	private void showJoinMatchFragment() {
		startActivityForResult(new Intent(this, JoinMatchActivity_.class), JOIN_ID);
	}

	private void showCreateAccountFragment() {
		startActivityForResult(new Intent(this, CreateAccountActivity_.class), CREATE_ACCOUNT_ID);
	}
	
	private void showSignInFragment() {
		startActivityForResult(new Intent(this, LoginActivity_.class), SIGN_IN_ID);
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
		
		if((requestCode == JOIN_ID || requestCode == CREATE_MATCH_ID) && 
			resultCode == Activity.RESULT_OK) {

			mDrawerLayout.openDrawer(mNavDrawer);
		}
		super.onActivityResult(requestCode, resultCode, arg2);
	}

	@Override
	public void ready(PlayerStatus view, String matchToken) {
		sendPlayerReadyStatusToServer(view, matchToken);
	}

	@Background
	public void sendPlayerReadyStatusToServer(PlayerStatus view, String matchId) {
		MatchResponse response = null;
		try {
			Repository model = ((App)getApplication()).getRepo();
			String userToken = model.getUser().getToken();
			response = restClient.readyForMatch(matchId, userToken);
			
			if(response.ok()) {
				model.updateMatch(response.match);
				for(Player p : response.match.players) {
					model.updatePlayer(p);
				}
			}
		} catch(Exception e) {
			Log.e(TAG, e.getMessage());
		}
		onSendPlayerStatusComplete(view, response);
	}
	
	@UiThread
	public void onSendPlayerStatusComplete(PlayerStatus view, MatchResponse response) {
		if(response == null || !response.ok()) {
			view.enableReadyListener();
			Toast.makeText(this, (response != null ? response.message : "request failed. try again."), 1000).show();
		}
	}
	
}
