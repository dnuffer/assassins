package com.nbs.client.assassins.models;

import com.nbs.client.assassins.utils.Extras;

import android.os.Bundle;

public class PlayerMapper {

	public static Player fromExtras(Bundle b) {
		Player p = new Player();
		p.id = Extras.getLong(b, "id");
		p.matchId = b.getString("match_id");
		p.username = b.getString("username");
		p.status = b.getString("status");
		p.health = Extras.getInt(b,"health");
		return p;
	}

	public static Bundle toExtras(Player p) {
		Bundle b = new Bundle();
		b.putLong("id", p.id);
		b.putString("username", p.username);
		b.putString("status", p.status);
		if(p.health != null) b.putInt("health",  p.health);
		b.putString("match_id", p.matchId);
		return b;
	}
}
