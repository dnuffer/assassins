package com.nbs.client.assassins.utils;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

public class SharedPref {
	
	private static final String LATITUDE_TRAILER = "_lat";
	private static final String LONGITUDE_TRAILER = "_lng";


	public static String getString(Context c, String key) {
		return PreferenceManager.getDefaultSharedPreferences(c)
				.getString(key, null);
	}
	
	public static long getLong(Context context, String key, int defaultValue) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getLong(key, defaultValue);
	}
	
	public static float getFloat(Context context, String key, float defaultValue) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getFloat(key, defaultValue);
	}
	
	public static int getInt(Context context, String key, int defaultValue) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getInt(key, defaultValue);
	}
	
	public static Set<String> getStringSet(Context context, String key) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getStringSet(key, new HashSet<String>());
	}
	
	public static LatLng getLatLng(Context context, String key) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if(pref.contains(key+LATITUDE_TRAILER))
        {
			double lat = pref.getFloat(key+LATITUDE_TRAILER, 181.0f);
	        double lng = pref.getFloat(key+LONGITUDE_TRAILER, 181.0f);
	        if(lat < 180 && lng < 180) return new LatLng(lat, lng);
        }
        return null;
	}

	public static void putStringSet(Context c, String k, Set<String> v) {
		if(v == null) {
			PreferenceManager.getDefaultSharedPreferences(c)		
			.edit()
			.remove(k)
	        .commit();
		}
		else {
			PreferenceManager.getDefaultSharedPreferences(c)		
			.edit()
			.putStringSet(k, v)
	        .commit();
		}
	}	
	
	public static void putLatLng(Context c, String k, LatLng v) {
		if(v == null) {
			PreferenceManager.getDefaultSharedPreferences(c)		
			.edit()
			.remove(k+LATITUDE_TRAILER)
	    	.remove(k+LONGITUDE_TRAILER)
	        .commit();
		}
		else {
			putDouble(c, k+LATITUDE_TRAILER, v.latitude);
			putDouble(c, k+LONGITUDE_TRAILER, v.latitude);
		}
	}
	
	public static void putLong(Context c, String k, Long v) {
		if(v == null) {
			PreferenceManager.getDefaultSharedPreferences(c)		
			.edit()
	    	.remove(k)
	        .commit();
		}
		else {
			long rawValue = v;
			PreferenceManager.getDefaultSharedPreferences(c)		
				.edit()
		    	.putLong(k, rawValue)
		        .commit();
		}
	}
	
	public static void putInt(Context c, String k, Integer v) {
		if(v == null) {
			PreferenceManager.getDefaultSharedPreferences(c)		
			.edit()
	    	.remove(k)
	        .commit();
		}
		else {
			int rawValue = v;
			PreferenceManager.getDefaultSharedPreferences(c)		
				.edit()
		    	.putInt(k, rawValue)
		        .commit();
		}
	}
	

	public static void putDouble(Context c, String k, Double v) {
		if(v == null) {
			putFloat(c,k,null);
		} else {
			double rawValue = v;
			putFloat(c,k,(float)rawValue);
		}
	}
	
	public static void putFloat(Context c, String k, Float v) {
		if(v == null) {
			PreferenceManager.getDefaultSharedPreferences(c)		
			.edit()
	    	.remove(k)
	        .commit();
		}
		else {
			Float rawValue = v;
			PreferenceManager.getDefaultSharedPreferences(c)		
				.edit()
		    	.putFloat(k, rawValue)
		        .commit();
		}
	}

	public static void putString(Context c, String k, String v) {
		if(v == null) {
			PreferenceManager.getDefaultSharedPreferences(c)		
			.edit()
	    	.remove(k)
	        .commit();
		}
		else {
			PreferenceManager.getDefaultSharedPreferences(c)		
				.edit()
		    	.putString(k, v)
		        .commit();
		}
	}

	public static void addStringToSet(Context c, String key, String toAdd) {
		Set<String> setMembers = getStringSet(c, key);
		setMembers.add(toAdd);
		putStringSet(c, key, setMembers);
	}

	public static void removeStringFromSet(Context c, String key, String toRemove) {
		Set<String> setMembers = getStringSet(c, key);
		setMembers.remove(toRemove);
		putStringSet(c, key, setMembers);
	}

}
