package com.nbs.client.assassins.utils;

import android.os.Bundle;
import android.util.Log;

public class Extras {
	private static final String TAG = "Extras";

	public static Long getLong(Bundle b, String key){
		Object value = b.get(key);

		if(value != null) {
			if(value instanceof Long) {
				return (Long)value;
			} else if (value instanceof String) {
				try{
					return Long.parseLong((String)value);
				}
				catch(NumberFormatException e) {
					Log.e(TAG, e.getMessage());
				}
			} else {
				Log.e(TAG, "Expected Long extra for key: " + key);
			}
		}
		return null;
	}
	
	public static Double getDouble(Bundle b, String key) {
		Object value = b.get(key);
		if(value != null) {
			if(value instanceof Double) {
				return (Double)value;
			} else if (value instanceof String) {
				try{
					return Double.parseDouble((String)value);
				}
				catch(NumberFormatException e) {
					Log.e(TAG, e.getMessage());
				}
			} else {
				Log.e(TAG, "Expected Double extra for key: " + key);
			}
		}
		return null;
	}
	
	public static Integer getInt(Bundle b, String key) {
		Object value = b.get(key);
		if(value != null) {
			if(value instanceof Integer) {
				return (Integer)value;
			} else if (value instanceof String) {
				try{
					return Integer.parseInt((String)value);
				}
				catch(NumberFormatException e) {
					Log.e(TAG, e.getMessage());
				}
			} else {
				Log.e(TAG, "Expected Integer extra for key: " + key);
			}
		}
		return null;
	}
}
