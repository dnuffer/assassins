package com.nbs.client.assassins.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class Bus {
	private static final String TAG = "Bus";

	public static void post(Context c, String event) {
		LocalBroadcastManager.getInstance(c).sendBroadcast(new Intent().setAction(event));
	}
	
	public static void post(Context c, String event, Bundle extras) {
		Log.d(TAG, event + " " + extras);
		LocalBroadcastManager.getInstance(c).sendBroadcast(new Intent().setAction(event).putExtras(extras));
	}
	
	public static void register(Context c, BroadcastReceiver r, IntentFilter i) {
		LocalBroadcastManager.getInstance(c).registerReceiver(r, i);
	}
	
	public static void unregister(Context c, BroadcastReceiver r) {
		LocalBroadcastManager.getInstance(c).unregisterReceiver(r);
	}
}
