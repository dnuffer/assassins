package com.nbs.client.assassins.services;

import java.util.UUID;

import org.codehaus.jackson.map.ObjectMapper;
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
import com.nbs.client.assassins.models.App;
import com.nbs.client.assassins.models.Db;
import com.nbs.client.assassins.models.MatchMapper;
import com.nbs.client.assassins.models.Player;
import com.nbs.client.assassins.models.PlayerMapper;
import com.nbs.client.assassins.models.PlayerModel;
import com.nbs.client.assassins.models.Repository;
import com.nbs.client.assassins.models.User;
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
		
		Repository model = App.getRepo();
		
		if(type.equals(PushNotifications.PLAYER_EVENT)) {
			model.createOrUpdatePlayer(PlayerMapper.fromExtras(extras));
		} 
		else if(type.equals(PushNotifications.PLAYER_JOINED_MATCH)) {
			model.createOrUpdatePlayer(PlayerMapper.fromExtras(extras));
		} 
		else if(type.equals(PushNotifications.MATCH_END)) {
			model.onMatchEnd(MatchMapper.fromExtras(extras));
			Bus.post(c,PushNotifications.MATCH_END,extras);
		}
		else {
			Bus.post(c, type, extras);
		}

	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.i(TAG, "OnRegistered() received: " + registrationId);
		
		Repository model = App.getRepo();
		User user = model.getUser();
		
		//only send the new id if the user already has created an account
		if(model.getUser().hasToken()) {
			GCMRegistrationRequest msg = new GCMRegistrationRequest();
			msg.installId = user.getInstallId();
			msg.gcmRegId = registrationId;
			
			try {
				LoginResponse response = 
					restClient.updateGCMRegId(user.getToken(), msg);
				if(response != null && response.status != Response.ERROR) {
					Log.i(TAG, response.toString());
					user.setToken(response.token);
					GCMRegistrar.setRegisteredOnServer(context, true);
				}
			} catch(Exception e) {
				Log.e(TAG, e.getMessage());
				GCMRegistrar.setRegisteredOnServer(context, false);
			}
		}
		//provisional user
		else if(!user.hasUsername()) {
			LoginRequest msg = new LoginRequest();
			msg.installId = user.getInstallId();
			msg.gcmRegId = registrationId;
			
			try {
				LoginResponse response = 
					restClient.registerProvisionalUser(msg);
				if(response != null && response.status != Response.ERROR) {
					Log.i(TAG, response.toString());
					user.setToken(response.token);
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
        
		Repository model = App.getRepo();
		if(model.getUser().hasToken()) {
			GCMRegistrationRequest msg = new GCMRegistrationRequest();
			msg.installId = model.getUser().getInstallId();
			msg.gcmRegId = registrationId;
			restClient.unregisterGCMRegId(model.getUser().getToken(), msg);
			//TODO handle response for unregister and make sure it was successful
			GCMRegistrar.setRegisteredOnServer(context, false);
		}
	}
	
}
