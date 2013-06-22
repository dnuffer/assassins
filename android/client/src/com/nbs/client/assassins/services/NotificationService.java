package com.nbs.client.assassins.services;


import java.util.UUID;

import com.googlecode.androidannotations.annotations.EService;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.controllers.MainActivity_;
import com.nbs.client.assassins.models.UserModel;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

@EService
public class NotificationService extends Service {

	private static final String TAG = "NotifierService";

	private static final long ONE_MINUTE = 60*1000;
	private static final long FIVE_MINUTES = ONE_MINUTE*5;

	public static final String SET_MATCH_REMINDER_ALARMS = null;

	public static void setMatchReminderAlarms(Context context, Long matchStartTimeUTC) {	
		if(matchStartTimeUTC != null) {
			
			long reportLocationReminderTime = matchStartTimeUTC-FIVE_MINUTES;
			long postNotifReminderTime = matchStartTimeUTC-ONE_MINUTE;
			
			AlarmManager alarmMngr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			
			Intent matchReminderIntent = new Intent(context, LocationService_.class)
												.setAction(GCMMessages.MATCH_REMINDER);
			Intent matchStartingIntent = new Intent(context, NotificationService_.class)
												.setAction(GCMMessages.MATCH_REMINDER);
			PendingIntent startLocationService = PendingIntent.getService(context, 0, matchReminderIntent, 
					 						PendingIntent.FLAG_UPDATE_CURRENT);
		 
			PendingIntent startNotifService = PendingIntent.getService(context, 0, matchStartingIntent, 
						PendingIntent.FLAG_UPDATE_CURRENT);
			//if the match has already begun,  it will fire immediately
			alarmMngr.set(AlarmManager.RTC_WAKEUP, reportLocationReminderTime, startLocationService);
			alarmMngr.set(AlarmManager.RTC_WAKEUP, postNotifReminderTime, startNotifService);
			Log.d(TAG, "registered alarm");
		}
	}
	
	public static void postNotification(Context c, int id, int res, String title, String message, Bundle extras)
	{
		try {
			//the intent to launch when the notification is touched
		    Intent notificationIntent = new Intent(c, MainActivity_.class);
		    notificationIntent.putExtras(extras);
		    
		    NotificationCompat.Builder builder = 
		    		new NotificationCompat.Builder(c)  
		            .setSmallIcon(res)  
		            .setContentTitle(title)  
		            .setContentText(message)
		        	.setAutoCancel(true)
	        		.setContentIntent(PendingIntent.getActivity(
	        				c, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT));

	        // Add as notification  
	        NotificationManager manager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);  
	        manager.notify(id, builder.build());  
		}
		catch (IllegalArgumentException e) {
			Log.v(TAG, e.getMessage());
		}
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand("+intent+")");
		//TODO: handle all types of commands through startService, 
		//      not through calling static postNotification directly
		postNotification(this, UUID.randomUUID().hashCode(), R.drawable.crosshairs, 
					TAG, "Match about to start", intent.getExtras());
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
	}

	@Override
	public void onCreate() {	
		Log.d(TAG, "onCreate");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}

