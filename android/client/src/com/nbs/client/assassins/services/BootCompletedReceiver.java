package com.nbs.client.assassins.services;

import com.nbs.client.assassins.models.UserModel;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {

	private static final int FIVE_MINUTES = 60*5*1000;
	private static final String TAG = "BootCompletedReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive");
		
		context.startService(new Intent(context, LocationService_.class));
	
		Long matchStartTimeUTC = UserModel.getMatchStartTimeUTC(context);
		
		if(matchStartTimeUTC != null) {
			
			matchStartTimeUTC -= FIVE_MINUTES;
			AlarmManager alarmMngr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			
			Intent matchReminderIntent = new Intent(context, LocationService_.class)
												.setAction(GCMMessages.MATCH_REMINDER);
			PendingIntent startService = PendingIntent.getService(context, 0, matchReminderIntent, 
					 						PendingIntent.FLAG_UPDATE_CURRENT);
			//if the match has already begun,  it will fire immediately
			alarmMngr.set(AlarmManager.RTC_WAKEUP, matchStartTimeUTC, startService);
			Log.d(TAG, "registered alarm for time: " + matchStartTimeUTC);
		}
	}

}
