package com.nbs.client.assassins.network;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.nbs.client.assassins.models.Match;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class CreateMatchMessage {
	
	@JsonProperty("token")
	public String token;
	
	@JsonProperty("match")
	public Match match;
	
	public CreateMatchMessage() {}
	public CreateMatchMessage(String token, Match match) {
		this.token = token; this.match = match;
	}

	@Override
	public String toString() {
		return "CreateMatchMessage [token=" + token + ", match=" + match + "]";
	}
	
	
}

