/**
 * 
 */
package com.nbs.client.assassins;

import org.codehaus.jackson.annotate.*;


/**
 * @author cam
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

	@JsonProperty("gcm_reg_id")
	String gcmRegId;
	
	@JsonProperty("install_id")
	String installId;
	
	@JsonProperty("latitude")
	Double latitude = 0.0;
	
	@JsonProperty("longitude")
	Double longitude = 0.0;
	
	@Override
	public String toString() {
		return "User [gcmRegId=" + gcmRegId + ", installId=" + installId
				+ ", latitude=" + latitude + ", longitude=" + longitude + "]";
	}
}