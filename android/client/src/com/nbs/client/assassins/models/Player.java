package com.nbs.client.assassins.models;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.google.android.gms.maps.model.LatLng;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Player {
	
	public static final String READY = "ready";

	//local database id - not serialized
	@JsonIgnore
	public Long id;
	
	@JsonIgnore
	public boolean isMe;
	
	@JsonProperty("status")
	public String status;
	
	@JsonProperty("username")
	public String username;
	
	@JsonProperty("target_life")
	public Integer targetHealth;
	
	@JsonProperty("match_id")
	public String matchId;
	
	@JsonProperty("role")
	public String role;
	
	@JsonProperty("team")
	public String team;
	
	@JsonProperty("target_bearing")
	public Float targetBearing;
	
	@JsonProperty("target_range")
	public String targetRange;
	
	@JsonProperty("my_life")
	public Integer health;
	
	@JsonProperty("enemy_range")
	public String enemyRange;
	
	@JsonProperty("target_lat")
	public Double targetLat;
	
	@JsonProperty("target_lng")
	public Double targetLng;
	
	@JsonProperty("lastAttackTime")
	public Long lastAttackTime;

	@Override
	public String toString() {
		return "PlayerStateResponse [targetLife=" + targetHealth
				+ ", targetbearing=" + targetBearing + ", targetRange="
				+ targetRange + ", myLife=" + health + ", enemyRange="
				+ enemyRange + ", targetLat=" + targetLat + ", targetLng="
				+ targetLng + "]";
	}

	public LatLng getTargetLatLng() {
		return targetLat != null && targetLng != null ?  new LatLng(targetLat, targetLng) : null;
	}

	public void setLastSuccessfulAttackTime(long currentTimeMillis) {
		this.lastAttackTime = currentTimeMillis;
	}

	public void setTargetLife(Integer targetLife) {
		this.targetHealth = targetLife;
		
	}

	public Integer getTargetLife() {
		return this.targetHealth;
	}
}
