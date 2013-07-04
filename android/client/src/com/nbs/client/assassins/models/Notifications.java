package com.nbs.client.assassins.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.nbs.client.assassins.utils.SharedPref;

import android.content.Context;

public class Notifications {
	public static List<Notification> getAll(Context c) {
		Set<String> notificationIds = SharedPref.getStringSet(c, "notifications");

		List<Notification> nList = new ArrayList<Notification>();
		
		for(String id : notificationIds) {
			nList.add(new Notification(id, SharedPref.getString(c, id)));
		}
		
		return nList;
	}
	public static void delete(Context c, String id) {
		SharedPref.putString(c, id, null);
		SharedPref.removeStringFromSet(c, "notifications", id);
	}
	
	public static void add(Context c, Notification n) {
		SharedPref.addStringToSet(c, "notifications", n.id);
		SharedPref.putString(c, n.id, n.msg);
	}
	
	public static void deleteAll(Context c) {
		SharedPref.putStringSet(c, "notifications", null);
	}
	
	
}
