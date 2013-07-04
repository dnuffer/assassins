package com.nbs.client.assassins.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.maps.model.LatLng;
import com.nbs.client.assassins.utils.SharedPref;

public class MatchModel {

	public synchronized static void setMatch(Context context, Match match) {
		if(match == null) { match = new Match(); } 
		setName(context, match.name);
		setToken(context, match.token);
		setStartTime(context, match.startTime);
		setNWCorner(context, match.getNWCornerLatLng());
		setSECorner(context, match.getSECornerLatLng());
		setAttackRange(context, match.attackRange);
		setHuntRange(context, match.huntRange);
		setEscapeTime(context, match.escapeTime);
	}
	
	private synchronized static void setToken(Context context, String matchId) {
		PreferenceManager.getDefaultSharedPreferences(context).edit()
		.putString("match_token", matchId).commit();
	}

	private synchronized static void setName(Context context, String name) {
    	PreferenceManager.getDefaultSharedPreferences(context).edit()
    		.putString("match_name", name).commit();
	}

	public static Match getMatch(Context c) {
		return inMatch(c) ?  new Match(getToken(c), getName(c), null,
									getStartTime(c), getNWCorner(c), 
									getSECorner(c), getAttackRange(c), 
									getHuntRange(c), getEscapeTime(c)) : null;
	}
	
	private static String getToken(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context)
    			.getString("match_token", null);
	}

	private static String getName(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context)
    			.getString("match_name", null);
	}
	
	public static Long getStartTime(Context context) {
    	long startTime = SharedPref.getLong(context, "start_time", -1);
    	return startTime > 0 ? startTime : null;
	}


	public static LatLng getSECorner(Context context) {
    	return SharedPref.getLatLng(context, "se");
	}
	
	public static LatLng getNWCorner(Context context) {
		return SharedPref.getLatLng(context, "nw");
	}
	
	public static Double getAttackRange(Context context) {
		float aRange = SharedPref.getFloat(context, "attack_range", -1.0f);
        return aRange > 0 ? (double)aRange : null;
	}
	
	public static Double getHuntRange(Context context) {
		float hRange = SharedPref.getFloat(context, "hunt_range", -1.0f);
        return hRange > 0 ? (double)hRange : null;
	}
	
	public static Integer getEscapeTime(Context context) {
        int eTime = SharedPref.getInt(context, "escape_time", -1);
        return eTime > 0 ? eTime : null;
	}
	
	private synchronized static void setStartTime(Context c, Long sTime) {
		SharedPref.putLong(c, "start_time", sTime);
	}

	private synchronized static void setNWCorner(Context c, LatLng nwCorner) {
		SharedPref.putLatLng(c, "nw", nwCorner);
	}
	private synchronized static void setSECorner(Context c, LatLng seCorner) {
		SharedPref.putLatLng(c, "se", seCorner);
	}

	private synchronized static void setAttackRange(Context c, Double aRange) {
		SharedPref.putDouble(c, "attack_range", aRange);
	}
	private synchronized static void setHuntRange(Context c, Double hRange) {
		SharedPref.putDouble(c, "hunt_range", hRange);
	}
	private synchronized static void setEscapeTime(Context c, Integer eTime) {
		SharedPref.putInt(c, "escape_time", eTime);
	}

	public static boolean inMatch(Context c) {
		return getToken(c) != null;
	}

	public static boolean hasPendingMatch(Context context) {
		return MatchModel.inMatch(context) && MatchModel.getStartTime(context) > System.currentTimeMillis();
	}

	public static boolean inActiveMatch(Context context) {
		return MatchModel.inMatch(context) && MatchModel.getStartTime(context) < System.currentTimeMillis();
	}


}
