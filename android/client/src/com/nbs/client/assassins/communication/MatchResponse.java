package com.nbs.client.assassins.communication;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.nbs.client.assassins.models.Match;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class MatchResponse extends Response {
	
	@JsonProperty("match")
	public Match match;

	@Override
	public String toString() {
		return "MatchResponse [match=" + match + ", status=" + status
				+ ", message=" + message + "]";
	}
}
