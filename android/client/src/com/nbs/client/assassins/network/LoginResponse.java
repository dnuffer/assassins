package com.nbs.client.assassins.network;

import java.util.Arrays;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.nbs.client.assassins.models.Match;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class LoginResponse extends Response {

	@JsonProperty("token")
	public String token;
	
	@JsonProperty("matches")
	public Match[] matches;

	@Override
	public String toString() {
		return "LoginResponse [token=" + token + ", matches="
				+ Arrays.toString(matches) + ", status=" + status
				+ ", message=" + message + ", time=" + time + "]";
	}
}
