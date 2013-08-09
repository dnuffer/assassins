package com.nbs.client.assassins.network;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class LoginRequest {

	@JsonProperty("username")
	public String username;
	
	@JsonProperty("password")
	public String password;
	
	@JsonProperty("push_id")
	public String gcmRegId;
	
	@JsonProperty("install_id")
	public String installId;
}
