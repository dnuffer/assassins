package com.nbs.client.assassins;

import java.util.UUID;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

/* 
 * Inspired by Tim Bray's post @ http://android-developers.blogspot.com/2011/03/identifying-app-installations.html
 * This class generates and accesses a UUID for tracking unique installations of an application.
 *
 */

public class UserModel {
    private static final String INSTALLATION = "INSTALLATION";
    private static final String ID = "install_id";
	private static final String TOKEN = "token";
	private static final String USERNAME = "username";
	private static final String TAG = "UserModel";

    public synchronized static String getInstallId(Context context) {
        
    	if(context == null) return null;
    	SharedPreferences pref = context.getSharedPreferences(INSTALLATION, Context.MODE_PRIVATE);
        String installId = pref.getString(ID, null);
    	
    	if (installId == null) {
    		installId = UUID.randomUUID().toString();
            Editor editor = context.getSharedPreferences(INSTALLATION, Context.MODE_PRIVATE).edit();
            editor.putString(ID, installId);
            editor.commit();
        }
    	
        return installId;
    }
    
    public static String getUsername(Context context)
    {
    	if(context == null) return null;
    	SharedPreferences pref = context.getSharedPreferences(INSTALLATION, Context.MODE_PRIVATE);
	    return pref.getString(USERNAME, null);
    }
    
    public synchronized static void setUsername(Context context, String username)
    {
    	if(context == null) return;
    	Editor editor = context.getSharedPreferences(INSTALLATION, Context.MODE_PRIVATE).edit();
        editor.putString(USERNAME, username);
        editor.commit();
    }
    
    public static boolean hasUsername(Context context)
    {
    	return getUsername(context) != null;
    }
    
    public static String getToken(Context context)
    {
    	if(context == null) return null;
    	SharedPreferences pref = context.getSharedPreferences(INSTALLATION, Context.MODE_PRIVATE);
        return pref.getString(TOKEN, null);
    }
    
    public synchronized static void setToken(Context context, String token)
    {
    	if(context == null) return;
        Editor editor = context.getSharedPreferences(INSTALLATION, Context.MODE_PRIVATE).edit();
        editor.putString(TOKEN, token);
        editor.commit();
    }
    
    public static boolean hasToken(Context context)
    {
    	return getToken(context) != null;
    }

	public static String _toString(Context c) {
		
		return "[token=" + getToken(c) + ", username="+ getUsername(c) + ", install_id=" + getInstallId(c) +
				"match=" + getMatch(c) + ", " + "location=" + getLocation(c) + "]" ;
	}

	public static LatLng getLocation(Context c) {
		
		if(c == null) return null;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		
		String latStr = prefs.getString("my_lat", "");
		String lngStr = prefs.getString("my_lng", "");
		
		Log.d(TAG, "getting location preference [lat=" + latStr + ", lng=" + lngStr + "]");
		
		if(latStr != "" && lngStr != "") {
			double lat = Double.parseDouble(latStr);
			double lng = Double.parseDouble(lngStr);
	
			return new LatLng(lat,lng);
		}
		
		return null;
	}

	public synchronized static void setLocation(Context context, Location lastLocation) {
    	if(context == null || lastLocation == null) return;
    	Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    	editor.putString("my_lat", Double.toString(lastLocation.getLatitude()));
    	editor.putString("my_lng", Double.toString(lastLocation.getLongitude()));
        editor.commit();
		
	}

	public synchronized static void setMatch(Context context, Match match) {
		setMatchName(context, match.name);
		setMatchToken(context, match.token);
	}
	
	private static void setMatchToken(Context context, String matchId) {
    	if(context == null) return;
    	Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    	editor.putString("match_token", matchId);
        editor.commit();
		
	}

	private synchronized static void setMatchName(Context context, String name) {
    	if(context == null) return;
    	Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    	editor.putString("match_name", name);
        editor.commit();
	}

	public static Match getMatch(Context context) {
		Match match = new Match();
		match.name = getMatchName(context);
		match.token = getMatchToken(context);
		//TODO: store more of match's properties
		
		if(match.token == null) return null;
		
		return match;
	}
	
	private static String getMatchToken(Context context) {
    	if(context == null) return null;
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString("match_token", null);
	}

	private static String getMatchName(Context context) {
    	if(context == null) return null;
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString("match_name", null);
	}

	public static boolean hasMatch(Context context) {
		return (getMatch(context) != null);
	}
	
	
    
}
