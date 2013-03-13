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
	@JsonIgnore
	public static final String OK = "ok";

	@JsonProperty("status")
	String status;
	
	@JsonProperty("message")
	String message;
	
	public boolean ok() {
		return status != null && status.equalsIgnoreCase(Response.OK);
	}
}
