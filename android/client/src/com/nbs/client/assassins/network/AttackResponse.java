package com.nbs.client.assassins.network;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class AttackResponse extends PlayerResponse {

	@JsonProperty("hit")
	public boolean hit;

	@Override
	public String toString() {
		return "AttackResponse [hit=" + hit + ", player=" + player
				+ ", status=" + status + ", message=" + message + ", time="
				+ time + "]";
	}
}
