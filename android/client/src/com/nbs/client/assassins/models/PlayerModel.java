package com.nbs.client.assassins.models;

import com.google.android.gms.maps.model.LatLng;
import com.nbs.client.assassins.services.PushNotifications;
import com.nbs.client.assassins.utils.Bus;
import com.nbs.client.assassins.utils.KeyValueStore;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class PlayerModel extends KeyValueStore {
	
	private static final String TAG = "PlayerState";
	private static final String PACKAGE = "com.nbs.android.client.";
	public static final String TARGET_LIFE_CHANGED     = PACKAGE + "TARGET_LIFE_CHANGED";
	public static final String TARGET_LOCATION_CHANGED = PACKAGE + "TARGET_LOCATION_CHANGED";
	public static final String TARGET_BEARING_CHANGED  = PACKAGE + "TARGET_BEARING_CHANGED";
	public static final String TARGET_RANGE_CHANGED    = PACKAGE + "TARGET_RANGE_CHANGED";

	public static final String ENEMY_RANGE_CHANGED     = PACKAGE + "ENEMY_RANGE_CHANGED";
	public static final String ATTACKED                = PACKAGE + "ATTACKED";
	
	public static final String TARGET_EVENT = "target_event";
	public static final String ENEMY_EVENT  = "enemy_event";
	public static final String NEW_TARGET   = "new_target";
	public static final String NEW_ENEMY   = "new_enemy";
	
	public static final String  HUNT_RANGE = "hunt_range";
	public static final String  ATTACK_RANGE = "attack_range";

	public  static Integer getMyLife(Context c) {
    	return getInt(c,"my_life", -1);
	}

	public static synchronized void setMyLife(Context c, Integer myLife) {
    	Integer oldLife = getMyLife(c);
    	putInt(c,"my_life", myLife);
		if(myLife != null && myLife < oldLife) {
			Bus.post(c,ATTACKED);
		}
	}
	
	private static synchronized void setMyLife(Context c, String myLife) {
		try {
			setMyLife(c, Integer.parseInt(myLife));
		}
		catch(NumberFormatException exception) {
			Log.d(TAG, exception.getMessage());
		}
	}

	public  static Integer getTargetLife(Context c) {
    	return getInt(c,"target_life", -1);
	}

	public  static synchronized void setTargetLife(Context c, Integer targetLife) {
    	Integer oldLife = getTargetLife(c);
    	putInt(c,"target_life", targetLife);
		if(targetLife != null && targetLife < oldLife) {
			Bus.post(c,TARGET_LIFE_CHANGED);
		}
	}

	public  static Float getTargetBearing(Context c) {
    	return getFloat(c,"target_bearing", Float.NaN);
	}

	public static void setTargetBearing(Context c, String targetBearing) {
    	try {
    		setTargetBearing(c, Float.parseFloat(targetBearing));
    	}
    	catch(NumberFormatException exception) {
    		Log.d(TAG, exception.getMessage());
    	}
	}
	public  static void setTargetBearing(Context c, Float targetBearing) {
    	Float oldBearing = getTargetBearing(c);
    	putFloat(c,"target_bearing", targetBearing);
        if(oldBearing != targetBearing) {
			Bus.post(c,TARGET_BEARING_CHANGED);
        }
	}

	public static String getTargetProximity(Context c) {
    	return getString(c,"target_proximity");
	}

	public  static synchronized void setTargetProximity(Context c, String targetRange) {
    	String oldRange = getTargetProximity(c);
    	putString(c,"target_proximity", targetRange);
        if(targetRange != null && !targetRange.equals(oldRange)) {
			Bus.post(c,TARGET_RANGE_CHANGED);
        }
	}

	public static String getEnemyProximity(Context c) {
    	return getString(c,"enemy_proximity");
	}

	public static synchronized void setEnemyProximity(Context c, String enemyRange) {
    	String oldRange = getEnemyProximity(c);
    	putString(c,"enemy_proximity", enemyRange);
        if(enemyRange != null && !enemyRange.equals(oldRange)) {
			Bus.post(c,ENEMY_RANGE_CHANGED);
        }
	}

	public static LatLng getTargetLocation(Context c) {
    	return getLatLng(c, "target");
	}
	
	public static synchronized  void setTargetLocation(Context c,  LatLng newL) {
		LatLng oldL = getLatLng(c, "target");
        putLatLng(c, "target", newL);
        if(oldL == null || !oldL.equals(newL)) {
			Bus.post(c,TARGET_LOCATION_CHANGED);
        } 
	}
	
	//TODO the model should not know about a PlayerStateResponse...  try to combine PlayerState and PlayerStateResponse
	public static void setPlayerState(Context c, Player state) {
		if(state == null) { state = new Player(); }
		PlayerModel.setMyLife(c, state.health);
		PlayerModel.setEnemyProximity(c, state.enemyRange);
		PlayerModel.setTargetLife(c, state.targetHealth);
		PlayerModel.setTargetLocation(c, (state.targetLat == null ? null : new LatLng(state.targetLat, state.targetLng)));
		PlayerModel.setTargetBearing(c, state.targetBearing);
		PlayerModel.setTargetProximity(c, state.targetRange);
	}
	
	public synchronized static void clearTarget(Context c) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
		editor.remove("target_lat");
		editor.remove("target_lng");
		editor.remove("target_life");
		editor.remove("target_bearing");
		editor.remove("target_proximity");
		//the last attack time applies only to this target
		//for the 'assassins' game type
		editor.remove("last_attack_time");
		editor.commit();
	}
	
	public synchronized static void clearEnemy(Context c) {
		remove(c,"enemy_proximity");
	}

	public static synchronized void onNewTarget(Context c, Bundle extras) {
		clearTarget(c);
		onTargetEvent(c, extras);
		Bus.post(c,PlayerModel.NEW_TARGET);
	}

	public static synchronized void onTargetEvent(Context c, Bundle extras) {
		String tRange = extras.getString("target_range");
		String tBearing = extras.getString("target_bearing");
		String tLat = extras.getString("target_lat");
		String tLng = extras.getString("target_lng");
		String tLife = extras.getString("target_life");
		
		if(tRange != null) {
			setTargetProximity(c, tRange);
		}

		if(tBearing != null) {
			setTargetBearing(c, tBearing);
		}

		if(tLat != null && tLng != null) {
			try {
				setTargetLocation(c, 
					new LatLng(Double.parseDouble(tLat), 
							Double.parseDouble(tLng)));
			} catch (NumberFormatException e) {
				Log.e(TAG, e.getMessage());
			}
		}

		if(tLife != null) {
			setTargetLife(c, Integer.parseInt(tLife));
		}
		
		Log.d(TAG, "onTargetEvent");
		Bus.post(c,PlayerModel.TARGET_EVENT);
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
		PlayerModel.setPlayerState(c, null);
		
	}

	public static void setLastSuccessfulAttackTime(Context c, long time) {
		putLong(c, "last_attack_time", time);
	}
	
	public static long getLastSuccessfulAttackTime(Context c) {
		return getLong(c, "last_attack_time", -1);
	}
}
