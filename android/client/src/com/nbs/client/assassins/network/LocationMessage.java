/**
 * 
 */
package com.nbs.client.assassins.network;

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
	public String installId;
	
	@JsonProperty("latitude")
	public Double latitude;
	
	@JsonProperty("longitude")
	public Double longitude;
	
	@Override
	public String toString() {
		return "LocationMessage [installId=" + installId + ", latitude="
				+ latitude + ", longitude=" + longitude + "]";
	}
}