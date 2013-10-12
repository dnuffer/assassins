package com.nbs.client.assassins.models;

import android.os.Bundle;

public class PlayerMapper {

	public static Player fromExtras(Bundle extras) {
		Player p = new Player();
		p.id = extras.getLong("id");
		p.matchId = extras.getString("match_id");
		p.username = extras.getString("username");
		p.status = extras.getString("status");
		p.health = extras.getInt("health");
		return p;
	}

	public static Bundle toExtras(Player player) {
		Bundle b = new Bundle();
		b.putLong("id", player.id);
		b.putString("username", player.username);
		b.putString("status", player.status);
		b.putInt("health",  player.health);
		b.putString("match_id", player.matchId);
		return b;
	}

}
