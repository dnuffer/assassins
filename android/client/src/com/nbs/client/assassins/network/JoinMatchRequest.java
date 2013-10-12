package com.nbs.client.assassins.network;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class JoinMatchRequest {

	public JoinMatchRequest(String token, String pw, String match) {
		userToken = token; matchPassword = pw; matchName = match;
	}

	@JsonProperty("token")
	public String userToken;

	@JsonProperty("match_name")
	public String matchName;
	
	@JsonProperty("password")
	public String matchPassword;
}
