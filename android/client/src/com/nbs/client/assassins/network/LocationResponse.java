package com.nbs.client.assassins.network;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.nbs.client.assassins.models.Player;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class LocationResponse extends Response {

	@JsonProperty("latitude")
	public double latitude;
	
	@JsonProperty("longitude")
	public double longitude;
	
	@JsonProperty("players")
	public Player[] players;

	@Override
	public String toString() {
		return "LocationResponse [latitude=" + latitude + ", longitude="
				+ longitude + ", players=" + players + ", status="
				+ status + ", message=" + message + ", ok()=" + ok() + "]";
	}
}
