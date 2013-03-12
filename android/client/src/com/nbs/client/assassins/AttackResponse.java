package com.nbs.client.assassins;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class AttackResponse extends Response {
	@JsonProperty("hit")
	boolean hit;
	
	@JsonProperty("target_life")
	String targetLife;
}
