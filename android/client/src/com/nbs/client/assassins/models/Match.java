package com.nbs.client.assassins.models;
import java.util.Arrays;

import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.google.android.gms.maps.model.LatLng;


/**
 * @author cam
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Match {

	public Match(String name, String pw, LatLng nw, LatLng se, double aRange, double hRange, int tEscape) {
		this.name = name; this.password = pw; 
		this.attackRange = aRange; this.huntRange = hRange; this.escapeTime = tEscape;
		this.nwCorner = new Loc(nw); this.seCorner = new Loc(se);
	}
	
	public Match() {
	}

	@JsonProperty("token")
	public String token;
	
	@JsonProperty("name")
	public String name;
	
	@JsonProperty("players")
	public Player[] players;
	
	@JsonProperty("password")
	public String password;
	
	@JsonProperty("nw_corner")
	public Loc nwCorner;
	
	@JsonProperty("se_corner")
	public Loc seCorner;
	
	@JsonProperty("attack_range")
	public double attackRange;
	
	@JsonProperty("hunt_range")
	public double huntRange;
	
	@JsonProperty("escape_time")
	public int escapeTime;

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Match [token=" + token + ", name=" + name + ", players="
				+ Arrays.toString(players) + ", password=" + password
				+ ", nwCorner=" + nwCorner + ", seCorner=" + seCorner
				+ ", attackRange=" + attackRange + ", huntRange=" + huntRange
				+ ", escapeTime=" + escapeTime + "]";
	}
	
}
