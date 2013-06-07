package com.nbs.client.assassins.models;

import com.google.android.gms.maps.model.LatLng;
import com.nbs.client.assassins.controllers.MainActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class PlayerState {
	
	private static final String TAG = "PlayerState";
	
	public static final String NEW_TARGET              = "com.nbs.android.client.NEW_TARGET";	
	public static final String TARGET_LIFE_CHANGED     = "com.nbs.android.client.TARGET_LIFE_CHANGED";
	public static final String TARGET_LOCATION_CHANGED = "com.nbs.android.client.TARGET_LOCATION_CHANGED";
	public static final String TARGET_BEARING_CHANGED  = "com.nbs.android.client.TARGET_BEARING_CHANGED";
	public static final String TARGET_RANGE_CHANGED    = "com.nbs.android.client.TARGET_RANGE_CHANGED";
	public static final String TARGET_EVENT            = "com.nbs.android.client.TARGET_EVENT";
	public static final String MATCH_END               = "com.nbs.android.client.MATCH_END";
	public static final String ENEMY_RANGE_CHANGED     = "com.nbs.android.client.ENEMY_RANGE_CHANGED";
	public static final String ATTACKED                = "com.nbs.android.client.ATTACKED";
	
	public static final String  HUNT_RANGE = "hunt_range";
	public static final String  ATTACK_RANGE = "attack_range";

	public  static Integer getMyLife(Context c) {
    	if(c == null) return null;
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
    	return pref.getInt("my_life", -1);
	}

	public static synchronized void setMyLife(Context c, int myLife) {
    	if(c == null) return;
    	
    	Integer oldLife = getMyLife(c);
    	
    	Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
    	editor.putInt("my_life", myLife);
        editor.commit();
		
		if(myLife < oldLife)
		{
			LocalBroadcastManager.getInstance(c)
				.sendBroadcast(new Intent().setAction(ATTACKED));
		}
	}
	
	private static synchronized void setMyLife(Context c, String myLife) {
		try {
			Integer newLife = Integer.parseInt(myLife);
			setMyLife(c, newLife);
		}
		catch(NumberFormatException exception) {
			Log.d(TAG, exception.getMessage());
		}
	}

	public  static Integer getTargetLife(Context c) {
    	if(c == null) return null;
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
    	return pref.getInt("target_life", -1);
	}

	public  static synchronized void setTargetLife(Context c, int targetLife) {
    	if(c == null) return;
    	
    	Integer oldLife = getTargetLife(c);
    	
    	Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
    	editor.putInt("target_life", targetLife);
        editor.commit();
        
		if(targetLife < oldLife)
		{
			LocalBroadcastManager.getInstance(c)
				.sendBroadcast(new Intent().setAction(TARGET_LIFE_CHANGED));
		}
	}

	public  static Float getTargetBearing(Context c) {
    	if(c == null) return null;
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
    	
    	Float bearing = pref.getFloat("target_bearing", Float.NaN);

    	return bearing;
	}
	

	public static void setTargetBearing(Context c, String targetBearing) {
    	if(c == null) return;
    	
    	try {
    		setTargetBearing(c, Float.parseFloat(targetBearing));
    	}
    	catch(NumberFormatException exception) {
    		Log.d(TAG, exception.getMessage());
    	}
	}
	public  static void setTargetBearing(Context c, float targetBearing) {
    	if(c == null) return;
    	
    	Float oldBearing = getTargetBearing(c);
    	
    	Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
    	editor.putFloat("target_bearing", targetBearing);
        editor.commit();
        
        if(oldBearing != targetBearing)
        {
			LocalBroadcastManager.getInstance(c)
			.sendBroadcast(new Intent().setAction(TARGET_BEARING_CHANGED));
        }
	}

	public static String getTargetProximity(Context c) {
    	if(c == null) return null;
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
    	return pref.getString("target_proximity", null);
	}

	public  static synchronized void setTargetProximity(Context c, String targetRange) {
    	if(c == null) return;
    	
    	String oldRange = getTargetProximity(c);
    	
    	Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
    	editor.putString("target_proximity", targetRange);
        editor.commit();
        
        if(!targetRange.equals(oldRange))
        {
			LocalBroadcastManager.getInstance(c)
			.sendBroadcast(new Intent().setAction(TARGET_RANGE_CHANGED));
        }
	}

	public static String getEnemyProximity(Context c) {
    	if(c == null) return null;
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
    	return pref.getString("enemy_proximity", null);
	}

	public static synchronized void setEnemyProximity(Context c, String enemyRange) {
    	if(c == null) return;
    	
    	String oldRange = getEnemyProximity(c);
    	
    	Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
    	editor.putString("enemy_proximity", enemyRange);
        editor.commit();
        
        if(!enemyRange.equals(oldRange))
        {
			LocalBroadcastManager.getInstance(c)
			.sendBroadcast(new Intent().setAction(ENEMY_RANGE_CHANGED));
        }
	}

	public static LatLng getTargetLocation(Context c) {
    	if(c == null) return null;
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
    	
    	String latStr = pref.getString("target_lat", null);
    	String lngStr = pref.getString("target_lng", null);
    	
    	if(latStr == null || lngStr == null)
    	{
    		return null;
    	}

    	return new LatLng(Double.parseDouble(latStr), Double.parseDouble(lngStr));
	}

	public static synchronized  void setTargetLocation(Context c, double lat, double lng) {
    	if(c == null) return;
    	setTargetLocation(c, Double.toString(lat), Double.toString(lng));
	}
	
	public static synchronized  void setTargetLocation(Context c, String lat, String lng) {
    	if(c == null) return;
    	
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
    	
    	String oldLat = pref.getString("target_lat", null);
    	String oldLng = pref.getString("target_lng", null);
    	
    	Editor editor = pref.edit();
    	editor.putString("target_lat", lat);
    	editor.putString("target_lng", lng);
        editor.commit();
        
        if(!lat.equals(oldLat) || !lng.equals(oldLng))
        {
			LocalBroadcastManager.getInstance(c)
			.sendBroadcast(new Intent().setAction(TARGET_LOCATION_CHANGED));
        }
        
	}
	
	public synchronized static void clearTarget(Context c) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
		Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
		editor.remove("target_lat");
		editor.remove("target_lng");
		editor.remove("target_life");
		editor.remove("target_bearing");
		editor.remove("target_proximity");	
		editor.commit();
	}
	
	public synchronized static void clearEnemy(Context c) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
		Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
		editor.remove("enemy_proximity");
		editor.commit();
	}

	public static synchronized void onNewTarget(Context c, Bundle extras) {
		clearTarget(c);
		onTargetEvent(c, extras);
		LocalBroadcastManager.getInstance(c)
			.sendBroadcast(new Intent().setAction(NEW_TARGET));
	}

	public static synchronized void onTargetEvent(Context c, Bundle extras) {
		String tRange = extras.getString("target_range");
		
		if(tRange != null) {
			setTargetProximity(c, tRange);
		}
		String tBearing = extras.getString("target_bearing");
		
		if(tBearing != null) {
			setTargetBearing(c, tBearing);
		}

		String tLat = extras.getString("target_lat");
		String tLng = extras.getString("target_lng");
		
		if(tLat != null && tLng != null) {
			setTargetLocation(c, tLat, tLng);
		}
		
		String tLife = extras.getString("target_life");
		
		if(tLife != null) {
			setTargetLife(c, Integer.parseInt(tLife));
		}
		
		
		Log.d(TAG, "onTargetEvent");
		
		LocalBroadcastManager.getInstance(c)
			.sendBroadcast(new Intent().setAction(TARGET_EVENT));
	}

	public static void onEnemyEvent(Context c, Bundle extras) {
		
		String range = extras.getString("enemy_range");
		String myLife = extras.getString("my_life");
		
		if(range != null) {
			setEnemyProximity(c, range);
		}
		
		if(myLife != null) {
			setMyLife(c, myLife);
		}
	}
	
	public static void onMatchEnd(Context c, Bundle extras) {
		//TODO: set match state
		LocalBroadcastManager.getInstance(c)
			.sendBroadcast(new Intent().setAction(MATCH_END).putExtras(extras));
		
	}





}
