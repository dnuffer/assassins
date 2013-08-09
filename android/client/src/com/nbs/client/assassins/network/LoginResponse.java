package com.nbs.client.assassins.network;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class LoginResponse extends Response {

	@JsonProperty("token")
	public String token;
	
	@Override
	public String toString() {
		return "UserLoginResponse [token=" + token + ", status=" + status
				+ ", message=" + message + "]";
	}
}
