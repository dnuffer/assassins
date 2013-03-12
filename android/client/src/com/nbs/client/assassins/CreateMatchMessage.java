package com.nbs.client.assassins;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class CreateMatchMessage {

	@JsonProperty("token")
	String token;
	
	@JsonProperty("match")
	Match match;
}

