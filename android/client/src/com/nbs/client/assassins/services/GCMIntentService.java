package com.nbs.client.assassins.services;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.EService;
import com.googlecode.androidannotations.annotations.rest.RestService;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.controllers.MainActivity;
import com.nbs.client.assassins.models.User;
import com.nbs.client.assassins.network.GCMRegistrationMessage;
import com.nbs.client.assassins.network.HuntedRestClient;
import com.nbs.client.assassins.network.Response;
import com.nbs.client.assassins.network.UserLoginMessage;
import com.nbs.client.assassins.network.UserLoginResponse;


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
	
	
	public GCMIntentService() {		
	}

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
		Bundle b = intent.getExtras();
		
		for(String key : b.keySet())
		{
			Log.i(TAG, "  " + key + " : " + b.getString(key));
			
            //Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
            //editor.putString(key, b.getString(key));
            //editor.commit();
		}
		
		//String msgType = (String)b.get("type");
		
		//Log.i(TAG, "message type: " + msgType);
		
		
		//if(msgType.equals(GCMMessages.TARGET_EVENT))
		//{
			
		//}
		
		/*intent.setAction(MainActivity.ACTION);
		
		try {
			c.sendBroadcast(intent);
			
			//the intent to launch when the notification is touched
		    Intent notificationIntent = new Intent(this, MainActivity.class);
		    notificationIntent.putExtras(intent);
			
		    NotificationCompat.Builder builder = 
		    		new NotificationCompat.Builder(this)  
		            .setSmallIcon(R.drawable.ic_launcher)  
		            .setContentTitle(intent.getStringExtra("type"))  
		            .setContentText(intent.getStringExtra("message"))
		        	.setAutoCancel(true)
	        		.setContentIntent(PendingIntent.getActivity(
	        				this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT));

	        // Add as notification  
	        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);  
	        manager.notify(MainActivity.ACTION.hashCode(), builder.build());  
		}
		catch (IllegalArgumentException e) {
			Log.v(TAG, e.getMessage());
		}*/
	}


	
	@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.i(TAG, "OnRegistered() received: " + registrationId);
		
		//only send the new id if the user already has created an account
		if(User.hasToken(this)) {
			GCMRegistrationMessage msg = new GCMRegistrationMessage();
			msg.installId = User.getInstallId(context);
			msg.gcmRegId = registrationId;
			
			try {
				UserLoginResponse response = 
					restClient.updateGCMRegId(User.getToken(context), msg);
				if(response != null && response.status != Response.ERROR) {
					Log.i(TAG, response.toString());
					User.setToken(context, response.token);
					GCMRegistrar.setRegisteredOnServer(context, true);
				}
			} catch(Exception e) {
				Log.e(TAG, e.getMessage());
				GCMRegistrar.setRegisteredOnServer(context, false);
			}
		}
		//provisional user
		else if(!User.hasUsername(context))
		{
			UserLoginMessage msg = new UserLoginMessage();
			msg.installId = User.getInstallId(context);
			msg.gcmRegId = registrationId;
			
			try {
				UserLoginResponse response = 
					restClient.registerProvisionalUser(msg);
				if(response != null && response.status != Response.ERROR) {
					Log.i(TAG, response.toString());
					User.setToken(context, response.token);
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
        
		if(User.hasToken(context))
		{
			GCMRegistrationMessage msg = new GCMRegistrationMessage();
			msg.installId = User.getInstallId(context);
			msg.gcmRegId = registrationId;
			restClient.unregisterGCMRegId(User.getToken(context), msg);
			//TODO handle response for unregister and make sure it was successful
			GCMRegistrar.setRegisteredOnServer(context, false);
		}
	}
	
}
