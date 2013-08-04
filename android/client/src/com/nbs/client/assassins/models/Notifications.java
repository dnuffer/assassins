package com.nbs.client.assassins.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.nbs.client.assassins.utils.KeyValueStore;

import android.content.Context;

public class Notifications extends KeyValueStore {
	public static List<Notification> getAll(Context c) {
		Set<String> notificationIds = getStringSet(c, "notifications");

		List<Notification> nList = new ArrayList<Notification>();
		
		for(String id : notificationIds) {
			nList.add(new Notification(id, getString(c, id)));
		}
		
		return nList;
	}
	public static void delete(Context c, String id) {
		putString(c, id, null);
		removeStringFromSet(c, "notifications", id);
	}
	
	public static void add(Context c, Notification n) {
		addStringToSet(c, "notifications", n.id);
		putString(c, n.id, n.msg);
	}
	
	public static void deleteAll(Context c) {
		putStringSet(c, "notifications", null);
	}
	
	
}
