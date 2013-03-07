package com.nbs.client.assassins;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.EService;
import com.googlecode.androidannotations.annotations.rest.RestService;


@EService
public class GCMIntentService extends GCMBaseIntentService {

	@RestService
	HuntedRestClient restClient;

	@AfterInject
	public void doSomethingAfterInjection() {
		//subvert a bug in HttpUrlConnection
		//see: http://www.sapandiwakar.in/technical/eofexception-with-spring-rest-template-android/
		restClient.getRestTemplate().setRequestFactory(
				new HttpComponentsClientHttpRequestFactory());
	}
	
	
	public GCMIntentService() {		
	}

	public GCMIntentService(String... senderIds) {
		super(GCMUtilities.SENDER_ID);
		
	}

	@Override
	protected void onError(Context arg0, String errorId) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onMessage(Context c, Intent intent) {
		Log.i(TAG, "received a GCM message.");
		
		Bundle b = intent.getExtras();
		
		for(String key : b.keySet())
		{
			Object o = b.get(key);
			Log.i(TAG, key + " : " + o.getClass() + " " + b.getString(key));
		}

		User u = new User();
		u.installId = intent.getStringExtra("install_id");
		u.latitude = Double.parseDouble(intent.getStringExtra("latitude"));
		u.longitude = Double.parseDouble(intent.getStringExtra("longitude"));
		

		sendPlayerStateChangedIntent(c, u);

	}

	@Override
	protected void onRegistered(Context c, String registrationId) {
		Log.i(TAG, registrationId);
		
		User user = new User();
		user.gcmRegId = registrationId;
		user.installId = Installation.id(c);
		
		
		restClient.registerUser(user);
	}

	@Override
	protected void onUnregistered(Context arg0, String registrationId) {
        
		//TODO: just for testing!
		GCMRegistrar.register(this, GCMUtilities.SENDER_ID);
	}
	
    private void sendPlayerStateChangedIntent(Context c, User u) 
    {
        Intent stateChangedIntent = new Intent();
        stateChangedIntent.setAction("PLAYER_STATE_CHANGED");
        stateChangedIntent.putExtra("installId", u.installId);
        stateChangedIntent.putExtra("latitude", u.latitude);
        stateChangedIntent.putExtra("longitude", u.longitude);
        
		try {
			c.sendBroadcast(stateChangedIntent);
		}
		catch (IllegalArgumentException e) {
			Log.v(TAG, e.getMessage());
		}
        
    }


	
}
