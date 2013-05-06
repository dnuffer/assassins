package com.nbs.client.assassins.communication;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class JoinMatchMessage {

	@JsonProperty("token")
	public String userToken;

	@JsonProperty("match_name")
	public String matchName;
	
	@JsonProperty("password")
	public String matchPassword;
}
