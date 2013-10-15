package com.nbs.client.assassins.network;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.nbs.client.assassins.models.Match;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class CreateMatchRequest {
	
	@Override
	public String toString() {
		return "CreateMatchRequest [token=" + token + ", joinOnCreate="
				+ joinOnCreate + ", match=" + match + "]";
	}
	@JsonProperty("token")
	public String token;
	
	@JsonProperty("join_on_create")
	public boolean joinOnCreate;
	
	@JsonProperty("match")
	public Match match;
	
	public CreateMatchRequest() {}
	public CreateMatchRequest(String token, Match match) {
		this.token = token; this.match = match;
	}
	
}

