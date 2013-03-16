package com.nbs.client.assassins;
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
	String token;
	
	@JsonProperty("name")
	String name;
	
	@JsonProperty("players")
	Player[] players;
	
	@JsonProperty("password")
	String password;
	

	@Override
	public String toString() {
		return "Match [token=" + token + ", name=" + name + ", players="
				+ Arrays.toString(players) + ", password=" + password + "]";
	}
}
