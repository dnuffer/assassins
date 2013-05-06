package com.nbs.client.assassins.models;
import java.util.Arrays;

import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.annotate.JsonSerialize;


/**
 * @author cam
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Match {

	@JsonProperty("token")
	public String token;
	
	@JsonProperty("name")
	public String name;
	
	@JsonProperty("players")
	public Player[] players;
	
	@JsonProperty("password")
	public String password;
	

	@Override
	public String toString() {
		return "Match [token=" + token + ", name=" + name + ", players="
				+ Arrays.toString(players) + ", password=" + password + "]";
	}
}
