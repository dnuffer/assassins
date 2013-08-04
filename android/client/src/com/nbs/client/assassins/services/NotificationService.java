package com.nbs.client.assassins.services;

import java.util.UUID;

import com.googlecode.androidannotations.annotations.EService;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.controllers.MainActivity_;

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

	public static final String SET_MATCH_REMINDER_ALARMS = "com.nbs.client.assassins.SET_MATCH_REMINDER_ALARMS";
	public static final String CANCEL_MATCH_ALARMS = "com.nbs.client.assassins.CANCEL_MATCH_ALARMS";

	private void cancelMatchReminderAlarms(Context context) {
		AlarmManager alarmMngr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		alarmMngr.cancel(prepareNotificationServicePendingIntent(context, GCMMessages.MATCH_COUNTDOWN));
		alarmMngr.cancel(prepareLocationServicePendingIntent(context, GCMMessages.MATCH_COUNTDOWN));
	}
	
	private static PendingIntent prepareLocationServicePendingIntent(Context context, String action) {
		Intent matchReminderIntent = new Intent(context, LocationService_.class)
			.setAction(action);
		return PendingIntent.getService(context, 0, matchReminderIntent, 
												PendingIntent.FLAG_UPDATE_CURRENT);	
	}
	
	private static PendingIntent prepareNotificationServicePendingIntent(Context context, String action) {
		Intent matchStartingIntent = new Intent(context, NotificationService_.class)
			.setAction(action);
		return PendingIntent.getService(context, 0, matchStartingIntent, 
											PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	public static void setMatchReminderAlarms(Context context, Long matchStartTimeUTC) {	
		if(matchStartTimeUTC != null && matchStartTimeUTC > 0) {
			Log.d(TAG, "setMatchReminderAlarms(context, "+matchStartTimeUTC+")");
			long reportLocationReminderTime = matchStartTimeUTC-FIVE_MINUTES;
			long postNotifReminderTime = matchStartTimeUTC-ONE_MINUTE;
			AlarmManager alarmMngr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			//if the match has already begun,  it will fire immediately
			alarmMngr.set(AlarmManager.RTC_WAKEUP, reportLocationReminderTime, 
					prepareLocationServicePendingIntent(context, GCMMessages.MATCH_COUNTDOWN));
			alarmMngr.set(AlarmManager.RTC_WAKEUP, postNotifReminderTime, 
					prepareLocationServicePendingIntent(context, GCMMessages.MATCH_COUNTDOWN));
			Log.d(TAG, "registered alarm");
		}
	}
	
	public static void postNotification(Context c, int id, int res, String title, String message, Bundle extras)
	{
		try {
			//the intent to launch when the notification is touched
		    Intent notificationIntent = new Intent(c, MainActivity_.class);
		    //TODO why does this not start the app on notification pressed?
		    
		    notificationIntent.putExtras(extras);
		    
		    NotificationCompat.Builder builder = 
		    		new NotificationCompat.Builder(c)  
		            .setSmallIcon(res)  
		            .setContentTitle(title)  
		            .setContentText(message)
		        	.setAutoCancel(true)
	        		.setContentIntent(PendingIntent
	        			.getActivity(c, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT));

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
		String action = intent.getAction();

		if(action != null) {
			if(action.equals(GCMMessages.MATCH_COUNTDOWN)) {
				postNotification(this, UUID.randomUUID().hashCode(), R.drawable.crosshairs, 
						TAG, "Your match is about to begin.", intent.getExtras());
			}
			else if(action.equals(NotificationService.SET_MATCH_REMINDER_ALARMS)) {
				setMatchReminderAlarms(this, intent.getLongExtra("start_time", -1));
			}
			else if(action.equals(NotificationService.CANCEL_MATCH_ALARMS)) {
				cancelMatchReminderAlarms(this);
			}
			//TODO: handle other notifications through onStartService rather than
			//      calling the static postNotification method
		}

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

