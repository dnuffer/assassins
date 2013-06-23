package com.nbs.client.assassins.models;

import java.util.UUID;

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
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
	public  static final String USER_TOKEN_RECEIVED = "com.nbs.client.assassins.USER_TOKEN_CHANGED";

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
        
        if(token != null) {
			LocalBroadcastManager.getInstance(context)
				.sendBroadcast(new Intent().setAction(USER_TOKEN_RECEIVED));
        }
    }
    
    public static boolean hasToken(Context context)
    {
    	return getToken(context) != null;
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
	
	public synchronized static void setLocation(Context context,
			double lat, double lng) {
    	if(context == null) return;
    	Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    	editor.putString("my_lat", Double.toString(lat));
    	editor.putString("my_lng", Double.toString(lng));
        editor.commit();
	}  

	public static boolean loggedIn(Context context) {
		// TODO Auto-generated method stub
		return UserModel.hasToken(context) && UserModel.hasUsername(context);
	}
	
	
	public synchronized static void signOut(Context context) {
		MatchModel.setMatch(context, null);
		UserModel.setUsername(context, null);
		UserModel.setToken(context, null);
	}
	
	public static String _toString(Context c) {
		
		return "[token=" + getToken(c) + ", username="+ getUsername(c) + ", install_id=" + getInstallId(c) +
				"match=" + MatchModel.getMatch(c) + ", " + "location=" + getLocation(c) + "]" ;
	}


}
