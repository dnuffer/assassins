package com.nbs.client.assassins.views;

import android.text.format.Time;

public class TimeUtils {

 
	public static String format(Time t) {
		return formatDate(t.month+1, t.monthDay+1, t.year) + " " + formatTime(t.hour+1, t.minute+1);
	}

	public static String formatDate(int year, int monthOfYear, int dayOfMonth) {
		return monthOfYear + "/" + dayOfMonth + "/" + year;
	}
	
	public static String formatTime(int hourOfDay, int minute) {
		return hourOfDay%12 + ":" + minute + (hourOfDay >= 12 ? " pm" : " am");
	}
}