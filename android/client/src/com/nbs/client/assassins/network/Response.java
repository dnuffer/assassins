package com.nbs.client.assassins.network;

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
	public String status;
	
	@JsonProperty("message")
	public String message;
	
	@JsonProperty("time")
	public Long time;
	
	public boolean ok() {
		return status != null && status.equalsIgnoreCase(Response.OK);
	}
}
