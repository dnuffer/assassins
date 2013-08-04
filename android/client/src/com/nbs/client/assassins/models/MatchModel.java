package com.nbs.client.assassins.models;

import android.content.Context;
import com.google.android.gms.maps.model.LatLng;
import com.nbs.client.assassins.utils.KeyValueStore;

public class MatchModel extends KeyValueStore {

	public synchronized static void setMatch(Context c, Match match) {
		if(match == null) { match = new Match(); } 
		setName(c, match.name);
		setToken(c, match.token);
		setStartTime(c, match.startTime);
		setNWCorner(c, match.getNWCornerLatLng());
		setSECorner(c, match.getSECornerLatLng());
		setAttackRange(c, match.attackRange);
		setHuntRange(c, match.huntRange);
		setEscapeTime(c, match.escapeTime);
	}
	
	private synchronized static void setToken(Context c, String matchId) {
		putString(c, "match_token", matchId);
	}

	private synchronized static void setName(Context c, String name) {
		putString(c, "match_name", name);
	}

	public static Match getMatch(Context c) {
		return inMatch(c) ?  new Match(getToken(c), getName(c), null,
									getStartTime(c), getNWCorner(c), 
									getSECorner(c), getAttackRange(c), 
									getHuntRange(c), getEscapeTime(c)) : null;
	}
	
	private static String getToken(Context c) {
    	return getString(c,"match_token");
	}

	private static String getName(Context c) {
    	return getString(c, "match_name");
	}
	
	public static Long getStartTime(Context c) {
    	long startTime = getLong(c, "start_time", -1);
    	return startTime > 0 ? startTime : null;
	}


	public static LatLng getSECorner(Context c) {
    	return getLatLng(c, "se");
	}
	
	public static LatLng getNWCorner(Context c) {
		return getLatLng(c, "nw");
	}
	
	public static Double getAttackRange(Context c) {
		float aRange = getFloat(c, "attack_range", -1.0f);
        return aRange > 0 ? (double)aRange : null;
	}
	
	public static Double getHuntRange(Context c) {
		float hRange = getFloat(c, "hunt_range", -1.0f);
        return hRange > 0 ? (double)hRange : null;
	}
	
	public static Integer getEscapeTime(Context c) {
        int eTime = getInt(c, "escape_time", -1);
        return eTime > 0 ? eTime : null;
	}
	
	private synchronized static void setStartTime(Context c, Long sTime) {
		putLong(c, "start_time", sTime);
	}

	private synchronized static void setNWCorner(Context c, LatLng nwCorner) {
		putLatLng(c, "nw", nwCorner);
	}
	private synchronized static void setSECorner(Context c, LatLng seCorner) {
		putLatLng(c, "se", seCorner);
	}

	private synchronized static void setAttackRange(Context c, Double aRange) {
		putDouble(c, "attack_range", aRange);
	}
	private synchronized static void setHuntRange(Context c, Double hRange) {
		putDouble(c, "hunt_range", hRange);
	}
	private synchronized static void setEscapeTime(Context c, Integer eTime) {
		putInt(c, "escape_time", eTime);
	}

	public static boolean inMatch(Context c) {
		return getToken(c) != null;
	}

	public static boolean hasPendingMatch(Context c) {
		return MatchModel.inMatch(c) && MatchModel.getStartTime(c) > System.currentTimeMillis();
	}

	public static boolean inActiveMatch(Context c) {
		return MatchModel.inMatch(c) && MatchModel.getStartTime(c) < System.currentTimeMillis();
	}


}
