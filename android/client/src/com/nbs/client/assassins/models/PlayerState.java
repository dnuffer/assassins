package com.nbs.client.assassins.models;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PlayerState {
	@JsonProperty("target_life")
	public Integer targetLife;
	
	@JsonProperty("target_bearing")
	public Float targetBearing;
	
	@JsonProperty("target_range")
	public String targetRange;
	
	@JsonProperty("my_life")
	public Integer myLife;
	
	@JsonProperty("enemy_range")
	public String enemyRange;
	
	@JsonProperty("target_lat")
	public Double targetLat;
	
	@JsonProperty("target_lng")
	public Double targetLng;

	@Override
	public String toString() {
		return "PlayerStateResponse [targetLife=" + targetLife
				+ ", targetbearing=" + targetBearing + ", targetRange="
				+ targetRange + ", myLife=" + myLife + ", enemyRange="
				+ enemyRange + ", targetLat=" + targetLat + ", targetLng="
				+ targetLng + "]";
	}
}
