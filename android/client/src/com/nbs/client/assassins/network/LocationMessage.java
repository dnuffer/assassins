/**
 * 
 */
package com.nbs.client.assassins.network;

import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.google.android.gms.maps.model.LatLng;


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
	
	public LocationMessage(LatLng location, String installId) {
		this.installId = installId;
		this.latitude = location.latitude;
		this.longitude = location.longitude;
	}

	@Override
	public String toString() {
		return "LocationMessage [installId=" + installId + ", latitude="
				+ latitude + ", longitude=" + longitude + "]";
	}
}