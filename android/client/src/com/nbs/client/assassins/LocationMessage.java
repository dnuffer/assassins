/**
 * 
 */
package com.nbs.client.assassins;

import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.annotate.JsonSerialize;


/**
 * @author cam
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class LocationMessage {
	
	@JsonProperty("install_id")
	String installId;
	
	@JsonProperty("latitude")
	Double latitude = 0.0;
	
	@JsonProperty("longitude")
	Double longitude = 0.0;
}