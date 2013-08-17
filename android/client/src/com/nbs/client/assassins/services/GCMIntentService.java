package com.nbs.client.assassins.services;

import java.util.UUID;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.maps.model.LatLng;
import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.EService;
import com.googlecode.androidannotations.annotations.rest.RestService;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.controllers.MainActivity;
import com.nbs.client.assassins.controllers.MainActivity_;
import com.nbs.client.assassins.models.MatchModel;
import com.nbs.client.assassins.models.Player;
import com.nbs.client.assassins.models.PlayerModel;
import com.nbs.client.assassins.models.UserModel;
import com.nbs.client.assassins.network.GCMRegistrationRequest;
import com.nbs.client.assassins.network.HuntedRestClient;
import com.nbs.client.assassins.network.Response;
import com.nbs.client.assassins.network.LoginRequest;
import com.nbs.client.assassins.network.LoginResponse;
import com.nbs.client.assassins.utils.Bus;


@EService
public class GCMIntentService extends GCMBaseIntentService {

	@RestService
	HuntedRestClient restClient;

	@AfterInject
	public void afterInjection() {
		//subvert a bug in HttpUrlConnection
		//see: http://www.sapandiwakar.in/technical/eofexception-with-spring-rest-template-android/
		restClient.getRestTemplate().setRequestFactory(
				new HttpComponentsClientHttpRequestFactory());
	}
	
	public GCMIntentService() {}

	public GCMIntentService(String... senderIds) {
		super(GCMUtilities.SENDER_ID);	
	}

	@Override
	protected void onError(Context context, String errorId) {
		Log.e(TAG, "ERROR: " + errorId);
	}

	@Override
	protected void onMessage(Context c, Intent intent) {
		Log.i(TAG, "received a GCM message.");
		Log.i(TAG, "  action: " + intent.getAction());
		Bundle extras = intent.getExtras();
		
		for(String key : extras.keySet()) {
			Log.i(TAG, "  " + key + " : " + extras.getString(key));
		}
		
		String type = extras.getString("type");
		
		if(type.equals(PushNotifications.NEW_TARGET)) {
			PlayerModel.onNewTarget(c, extras);
		}
		else if(type.equals(PushNotifications.TARGET_EVENT)) {
			PlayerModel.onTargetEvent(c, extras);
		}
		else if(type.equals(PushNotifications.ENEMY_EVENT)) {
			PlayerModel.onEnemyEvent(c, extras);
		} 
		else if(type.equals(PushNotifications.MATCH_END)) {
			MatchModel.onMatchEnd(c, extras);
			PlayerModel.onMatchEnd(c, extras);
			Bus.post(c,PushNotifications.MATCH_END,extras);
		}
		else {
			Bus.post(c, type, extras);
		}

	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.i(TAG, "OnRegistered() received: " + registrationId);
		
		//only send the new id if the user already has created an account
		if(UserModel.hasToken(this)) {
			GCMRegistrationRequest msg = new GCMRegistrationRequest();
			msg.installId = UserModel.getInstallId(context);
			msg.gcmRegId = registrationId;
			
			try {
				LoginResponse response = 
					restClient.updateGCMRegId(UserModel.getToken(context), msg);
				if(response != null && response.status != Response.ERROR) {
					Log.i(TAG, response.toString());
					UserModel.setToken(context, response.token);
					GCMRegistrar.setRegisteredOnServer(context, true);
				}
			} catch(Exception e) {
				Log.e(TAG, e.getMessage());
				GCMRegistrar.setRegisteredOnServer(context, false);
			}
		}
		//provisional user
		else if(!UserModel.hasUsername(context)) {
			LoginRequest msg = new LoginRequest();
			msg.installId = UserModel.getInstallId(context);
			msg.gcmRegId = registrationId;
			
			try {
				LoginResponse response = 
					restClient.registerProvisionalUser(msg);
				if(response != null && response.status != Response.ERROR) {
					Log.i(TAG, response.toString());
					UserModel.setToken(context, response.token);
					GCMRegistrar.setRegisteredOnServer(context, true);
				}
			} catch(Exception e) {
				Log.e(TAG, e.getMessage());
				GCMRegistrar.setRegisteredOnServer(context, false);
			}
		}
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
        
		if(UserModel.hasToken(context)) {
			GCMRegistrationRequest msg = new GCMRegistrationRequest();
			msg.installId = UserModel.getInstallId(context);
			msg.gcmRegId = registrationId;
			restClient.unregisterGCMRegId(UserModel.getToken(context), msg);
			//TODO handle response for unregister and make sure it was successful
			GCMRegistrar.setRegisteredOnServer(context, false);
		}
	}
	
}
