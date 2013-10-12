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
	
	@JsonProperty("health")
	public Integer health;
	
	@JsonProperty("enemy_range")
	public String enemyRange;
	
	@JsonProperty("target_lat")
	public Double targetLat;
	
	@JsonProperty("target_lng")
	public Double targetLng;
	
	@JsonProperty("lat")
	public Double lat;
	
	@JsonProperty("lng")
	public Double lng;
	
	@JsonProperty("last_attack")
	public Long lastAttackTime;

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
	
	@Override
	public String toString() {
		return "Player [id=" + id + ", isMe=" + isMe + ", status=" + status
				+ ", username=" + username + ", targetHealth=" + targetHealth
				+ ", matchId=" + matchId + ", role=" + role + ", team=" + team
				+ ", targetBearing=" + targetBearing + ", targetRange="
				+ targetRange + ", health=" + health + ", enemyRange="
				+ enemyRange + ", targetLat=" + targetLat + ", targetLng="
				+ targetLng + ", lat=" + lat + ", lng=" + lng
				+ ", lastAttackTime=" + lastAttackTime + "]";
	}
}
