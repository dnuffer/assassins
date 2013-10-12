package com.nbs.client.assassins.network;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.nbs.client.assassins.models.Player;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PlayerResponse extends Response {

	@JsonProperty("player")
	public Player player;
	
	@Override
	public String toString() {
		return "PlayerResponse [player=" + player + ", status=" + status
				+ ", message=" + message + ", time=" + time + "]";
	}
}
