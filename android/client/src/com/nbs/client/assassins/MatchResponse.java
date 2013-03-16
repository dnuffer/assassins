package com.nbs.client.assassins;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class MatchResponse extends Response {
	
	@JsonProperty("match")
	Match match;

	@Override
	public String toString() {
		return "MatchResponse [match=" + match + ", status=" + status
				+ ", message=" + message + "]";
	}
}
