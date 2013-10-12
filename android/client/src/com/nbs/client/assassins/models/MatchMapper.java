package com.nbs.client.assassins.models;

import android.os.Bundle;

public class MatchMapper {

	public static Match fromExtras(Bundle extras) {
		Match m = new Match();
		m.id = extras.getString("id");
		m.name = extras.getString("name");
		m.startTime = extras.getLong("start_time");
		return m;
	}

	public static Bundle toExtras(Match m) {
		Bundle b = new Bundle();
		b.putString("id", m.id);
		b.putString("name", m.name);
		b.putLong("start_time", m.startTime);
		return b;
	}

}
