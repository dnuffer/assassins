package com.nbs.client.assassins.models;

import com.nbs.client.assassins.utils.Extras;
import android.os.Bundle;

public class MatchMapper {

	private static final String TAG = "MatchMapper";

	public static Match fromExtras(Bundle b) {
		Match m = new Match();
		m.id = b.getString("id");
		m.name = b.getString("name");
		m.startTime = Extras.getLong(b, "start_time");
		m.endTime = Extras.getLong(b, "end_time");
		m.winner = b.getString("winner");
		m.creator = b.getString("creator");
		m.countdownSec = Extras.getInt(b, "cnt_dwn_sec");
		m.attackRange = Extras.getDouble(b, "attack_range");
		m.huntRange = Extras.getDouble(b, "hunt_range");
		m.escapeTime = Extras.getInt(b, "escape_time");
		m.token = b.getString("token");
		m.nwCorner = new LatLngData(Extras.getDouble(b, "nw_lat"), Extras.getDouble(b, "nw_lng"));
		m.seCorner = new LatLngData(Extras.getDouble(b, "se_lat"), Extras.getDouble(b, "se_lng"));
		return m;
	}

	public static Bundle toExtras(Match m) {
		Bundle b = new Bundle();
		b.putString("id", m.id);
		b.putString("name", m.name);
		if(m.startTime != null) b.putLong("start_time", m.startTime);
		if(m.endTime != null) b.putLong("end_time", m.endTime);
		if(m.winner != null) b.putString("winner",m.winner);
		if(m.creator != null) b.putString("creator",m.creator);
		if(m.countdownSec != null) b.putInt("cnt_dwn_sec",m.countdownSec);
		if(m.attackRange != null)  b.putDouble("attack_range",m.attackRange);
		if(m.huntRange != null) b.putDouble("hunt_range",m.huntRange );
		if(m.escapeTime != null) b.putInt("escape_time",m.escapeTime);
		if(m.token != null) b.putString("token",m.token);
		if(m.nwCorner != null) {
			b.putDouble("nw_lat",m.nwCorner.lat); 
			b.putDouble("nw_lng",m.nwCorner .lng);
		}
		if(m.seCorner != null) {
			b.putDouble("se_lat",m.seCorner.lat);
			b.putDouble("se_lng",m.seCorner.lng);
		}
		return b;
	}
	


}
