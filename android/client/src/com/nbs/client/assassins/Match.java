package com.nbs.client.assassins;
import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.annotate.JsonSerialize;


/**
 * @author cam
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Match {

	@JsonProperty("id")
	String id;
	
	@JsonProperty("name")
	String name;
	
	@JsonProperty("players")
	Player[] players;
}
