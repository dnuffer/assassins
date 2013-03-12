package com.nbs.client.assassins;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Response {

	@JsonIgnore
	public static final String ERROR = "error";

	@JsonProperty("status")
	String type;
	
	@JsonProperty("message")
	String message;
	
}
