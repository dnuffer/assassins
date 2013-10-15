package com.nbs.client.assassins.models;
import java.util.Arrays;

import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.google.android.gms.maps.model.LatLng;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Match {

	@JsonProperty("_id")
	public String id;
	
	@JsonProperty("token")
	public String token;

	@JsonProperty("creator")
	public String creator;	
	
	@JsonProperty("name")
	public String name;
	
	@JsonProperty("winner")
	public String winner;
	
	@JsonProperty("countdown_sec")
	public Integer countdownSec;
	
	@JsonProperty("start_time")
	public Long startTime;
	
	@JsonProperty("end_time")
	public Long endTime;
	
	@JsonProperty("players")
	public Player[] players;
	
	@JsonProperty("password")
	public String password;
	
	@JsonProperty("nw_corner")
	public LatLngData nwCorner;
	
	@JsonProperty("se_corner")
	public LatLngData seCorner;
	
	@JsonProperty("attack_range")
	public Double attackRange;
	
	@JsonProperty("hunt_range")
	public Double huntRange;
	
	@JsonProperty("escape_time")
	public Integer escapeTime;
	
	public Match() {}
	
	public Match(String token, String name, String pw, String creator, Long tStart, 
			LatLng nw, LatLng se, double aRange, double hRange, int tEscape) {
		this(name, pw, creator, tStart, nw, se, aRange, hRange, tEscape);
		this.token = token;
	}
	
	public Match(String name, String pw, String creator, Long tStart, 
			LatLng nw, LatLng se, double aRange, double hRange, int tEscape) {
		this.name = name; 
		this.creator = creator;
		this.password = pw; 
		this.startTime = tStart;
		this.attackRange = aRange;   this.huntRange = hRange; this.escapeTime = tEscape;
		this.nwCorner = new LatLngData(nw); this.seCorner = new LatLngData(se);
	}

	public LatLng getSECornerLatLng() {
		return seCorner != null ? seCorner.toLatLng() : null;
	}

	public LatLng getNWCornerLatLng() {
		return nwCorner != null ? nwCorner.toLatLng() : null;
	}


	public Double getAttackRange() {
		return this.attackRange;
	}

	public Double getHuntRange() {
		return this.huntRange;
	}
	
	@Override
	public String toString() {
		return "Match [id=" + id + ", token=" + token + ", creator=" + creator
				+ ", name=" + name + ", winner=" + winner + ", countdownSec="
				+ countdownSec + ", startTime=" + startTime + ", players="
				+ Arrays.toString(players) + ", password=" + password
				+ ", nwCorner=" + nwCorner + ", seCorner=" + seCorner
				+ ", attackRange=" + attackRange + ", huntRange=" + huntRange
				+ ", escapeTime=" + escapeTime + ", endTime=" + endTime + "]";
	}
}
