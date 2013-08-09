package com.nbs.client.assassins.services;

import java.util.UUID;

import com.google.android.gms.location.LocationRequest;
import com.googlecode.androidannotations.annotations.EService;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.controllers.MainActivity_;
import com.nbs.client.assassins.models.PlayerModel;
import com.nbs.client.assassins.models.UserModel;
import com.nbs.client.assassins.utils.Bus;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.Log;

@EService
public class NotificationService extends Service {

	private static final String TAG = "NotificationService";

	private static final long ONE_MINUTE = 60*1000;
	private static final long FIVE_MINUTES = ONE_MINUTE*5;

	public static final String SET_MATCH_REMINDER_ALARMS = "com.nbs.client.assassins.SET_MATCH_REMINDER_ALARMS";
	public static final String CANCEL_MATCH_ALARMS = "com.nbs.client.assassins.CANCEL_MATCH_ALARMS";

	private IntentFilter intentFilter;
	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
    
        	if(intent != null) {
        		String type = intent.getAction();
        	
	        	if(type.equals(PushNotifications.PLAYER_JOINED_MATCH)) {
	    			NotificationService.postNotification(context, UUID.randomUUID().hashCode(), R.drawable.crosshairs, 
	    					TAG, PushNotifications.PLAYER_JOINED_MATCH, new Bundle());
	    		}
	    		else if(type.equals(PushNotifications.MATCH_START)) {
	    			NotificationService.postNotification(context, UUID.randomUUID().hashCode(), R.drawable.crosshairs, 
	    					TAG, PushNotifications.MATCH_START, new Bundle());
	    		}
	    		else if(type.equals(PushNotifications.MATCH_START)) {
	    			NotificationService.postNotification(context, UUID.randomUUID().hashCode(), R.drawable.crosshairs, 
	    					TAG, PushNotifications.MATCH_START, new Bundle());
	    		}
	    		else if(type.equals(PushNotifications.MATCH_END)) {
	    			NotificationService.postNotification(context, UUID.randomUUID().hashCode(), R.drawable.crosshairs, 
	    					TAG, PushNotifications.MATCH_END, new Bundle());
	    		}
	    		else if(type.equals(PushNotifications.PLAYER_ELIMINATED)) {
	    			NotificationService.postNotification(context, UUID.randomUUID().hashCode(), R.drawable.crosshairs, 
	    					TAG, PushNotifications.PLAYER_ELIMINATED, new Bundle());
	    		}
	    		else if(type.equals(PushNotifications.INVITE)) {
	    			NotificationService.postNotification(context, UUID.randomUUID().hashCode(), R.drawable.crosshairs, 
	    					TAG, PushNotifications.INVITE, new Bundle());
	    		}
	    		else if(type.equals(PushNotifications.ACHIEVEMENT)) {
	    			NotificationService.postNotification(context, UUID.randomUUID().hashCode(), R.drawable.crosshairs, 
	    					TAG, PushNotifications.ACHIEVEMENT, new Bundle());
	    		}
        	}
        }
	};
	
	@Override
	public void onCreate() {	
		Log.d(TAG, "onCreate");
		intentFilter = new IntentFilter();
		intentFilter.addAction(PushNotifications.PLAYER_JOINED_MATCH);
		intentFilter.addAction(PushNotifications.MATCH_START);
		intentFilter.addAction(PushNotifications.MATCH_START);
		intentFilter.addAction(PushNotifications.MATCH_END);
		intentFilter.addAction(PushNotifications.PLAYER_ELIMINATED);
		intentFilter.addAction(PushNotifications.INVITE);
		intentFilter.addAction(PushNotifications.ACHIEVEMENT);
		Bus.register(this, intentReceiver, intentFilter);
		
	}

	private void cancelMatchReminderAlarms(Context context) {
		AlarmManager alarmMngr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		alarmMngr.cancel(prepareNotificationServicePendingIntent(context, PushNotifications.MATCH_COUNTDOWN));
		alarmMngr.cancel(prepareLocationServicePendingIntent(context, PushNotifications.MATCH_COUNTDOWN));
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
			
			Time t = new Time(); t.set(matchStartTimeUTC);
			Log.d(TAG, "setMatchReminderAlarms(context, "+t.toString()+")");
			
			long postNotifReminderTime = matchStartTimeUTC-ONE_MINUTE;
			
			AlarmManager alarmMngr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			//if the match has already begun,  it will fire immediately
			alarmMngr.set(AlarmManager.RTC_WAKEUP, postNotifReminderTime, 
					prepareLocationServicePendingIntent(context, PushNotifications.MATCH_COUNTDOWN));
			
			alarmMngr.set(AlarmManager.RTC_WAKEUP, postNotifReminderTime, 
					prepareNotificationServicePendingIntent(context, PushNotifications.MATCH_COUNTDOWN));
			
			alarmMngr.set(AlarmManager.RTC_WAKEUP, matchStartTimeUTC, 
					prepareNotificationServicePendingIntent(context, PushNotifications.MATCH_START));
			
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
		String action = intent != null ? intent.getAction() : null;

		if(action != null) {
			if(action.equals(PushNotifications.MATCH_START)) {
				Bus.post(this, PushNotifications.MATCH_START);
			}
			else if(action.equals(PushNotifications.MATCH_COUNTDOWN)) {
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
		Bus.unregister(this, intentReceiver);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}

