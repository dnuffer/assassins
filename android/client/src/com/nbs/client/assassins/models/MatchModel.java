package com.nbs.client.assassins.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

public class MatchModel {

	public synchronized static void setMatch(Context context, Match match) {
		if(match == null) {
			setName(context, null);
			setToken(context, null);
			setStartTime(context, null);
			setNWCorner(context, null);
			setSECorner(context, null);
			setAttackRange(context, null);
			setHuntRange(context, null);
			setEscapeTime(context, null);
			
		} else {
			setName(context, match.name);
			setToken(context, match.token);
			setStartTime(context, match.startTime);
			setNWCorner(context, match.getNWCornerLatLng());
			setSECorner(context, match.getSECornerLatLng());
			setAttackRange(context, match.attackRange);
			setHuntRange(context, match.huntRange);
			setEscapeTime(context, match.escapeTime);
		}	
	}
	
	private synchronized static void setToken(Context context, String matchId) {
    	if(context == null) return;
    	Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    	editor.putString("match_token", matchId);
        editor.commit();
		
	}

	private synchronized static void setName(Context context, String name) {
    	if(context == null) return;
    	Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    	editor.putString("match_name", name);
        editor.commit();
	}

	public static Match getMatch(Context context) {
		Match match = new Match();
		match.name = getName(context);
		match.token = getToken(context);
		//TODO: store more of match's properties
		
		return match.token == null ? null : match;
	}
	
	private static String getToken(Context context) {
    	if(context == null) return null;
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString("match_token", null);
	}

	private static String getName(Context context) {
    	if(context == null) return null;
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString("match_name", null);
	}
	
	public static Long getStartTime(Context context) {
    	if(context == null) return null;
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        long startTime = pref.getLong("start_time", -1);
        return startTime > 0 ? startTime : null;
	}
	public static LatLng getSECorner(Context context) {
    	if(context == null) return null;
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        double lat = pref.getFloat("se_lat", 181.0f);
        double lng = pref.getFloat("se_lng", 181.0f);
        return (lat < 180 && lng < 180) ? new LatLng(lat, lng) : null;
	}
	
	public static LatLng getNWCorner(Context context) {
    	if(context == null) return null;
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        double lat = pref.getFloat("nw_lat", 181.0f);
        double lng = pref.getFloat("nw_lng", 181.0f);
        return (lat < 180 && lng < 180) ? new LatLng(lat, lng) : null;
	}
	
	public static Double getAttackRange(Context context) {
    	if(context == null) return null;
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        double aRange = pref.getFloat("attack_range", -1.0f);
        return aRange > 0 ? aRange : null;
	}
	
	public static Double getHuntRange(Context context) {
    	if(context == null) return null;
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        double aRange = pref.getFloat("hunt_range", -1.0f);
        return aRange > 0 ? aRange : null;
	}
	
	public static Integer getEscapeTime(Context context) {
    	if(context == null) return null;
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        int eTime = pref.getInt("escape_time", -1);
        return eTime > 0 ? eTime : null;
	}
	
	private synchronized static void setStartTime(Context c, Long sTime) {
		PreferenceManager.getDefaultSharedPreferences(c)
			.edit()
	    	.putLong("start_time", sTime)
	        .commit();   
	}
	private synchronized static void setNWCorner(Context c, LatLng neCorner) {
		PreferenceManager.getDefaultSharedPreferences(c)
			.edit()
	    	.putFloat("nw_lat", (float)neCorner.latitude)
	    	.putFloat("nw_lng", (float)neCorner.longitude)
	        .commit();
	}
	private synchronized static void setSECorner(Context c, LatLng swCorner) {
		PreferenceManager.getDefaultSharedPreferences(c)		
			.edit()
	    	.putFloat("se_lat", (float)swCorner.latitude)
	    	.putFloat("se_lng", (float)swCorner.longitude)
	        .commit();
	}
	private synchronized static void setAttackRange(Context c, Double aRange) {
		if(aRange == null) {
			PreferenceManager.getDefaultSharedPreferences(c)		
			.edit()
	    	.remove("attack_range")
	        .commit();
		}
		else {
			double attackRange = aRange;
			PreferenceManager.getDefaultSharedPreferences(c)		
				.edit()
		    	.putFloat("attack_range", (float)attackRange)
		        .commit();
		}
	}
	private synchronized static void setHuntRange(Context c, Double hRange) {
		if(hRange == null) {
			PreferenceManager.getDefaultSharedPreferences(c)		
			.edit()
	    	.remove("hunt_range")
	        .commit();
		}
		else {
			double huntRange = hRange;
			PreferenceManager.getDefaultSharedPreferences(c)		
				.edit()
		    	.putFloat("hunt_range", (float)huntRange)
		        .commit();
		}
	}
	private synchronized static void setEscapeTime(Context c, Integer eTime) {
		if(eTime == null) {
			PreferenceManager.getDefaultSharedPreferences(c)		
			.edit()
	    	.remove("escape_time")
	        .commit();
		}
		else {
			int escapeTime = eTime;
			PreferenceManager.getDefaultSharedPreferences(c)		
				.edit()
		    	.putInt("escape_time", escapeTime)
		        .commit();
		}
	}

	public static boolean inMatch(Context context) {
		return getMatch(context) != null;
	}

	public static boolean hasPendingMatch(Context context) {
		return MatchModel.inMatch(context) && MatchModel.getStartTime(context) > System.currentTimeMillis();
	}


}
