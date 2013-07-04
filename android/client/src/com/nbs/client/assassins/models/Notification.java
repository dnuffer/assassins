package com.nbs.client.assassins.models;

public class Notification {
	
	String msg;
	String id;
	
	public Notification(String id, String msg) {
		this.msg = msg;
		this.id = "notification_"+id;
	}
}
