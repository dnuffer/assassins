package com.nbs.client.assassins.network;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.nbs.client.assassins.models.Match;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class CreateMatchRequest {
	
	@JsonProperty("token")
	public String token;
	
	@JsonProperty("match")
	public Match match;
	
	public CreateMatchRequest() {}
	public CreateMatchRequest(String token, Match match) {
		this.token = token; this.match = match;
	}

	@Override
	public String toString() {
		return "CreateMatchRequest [token=" + token + ", match=" + match + "]";
	}
	
	
}

