package com.nbs.client.assassins;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class GCMRegistrationMessage {
	
	@JsonProperty("push_id")
	String gcmRegId;
	
	@JsonProperty("install_id")
	String installId;
}
