package com.nbs.client.assassins.network;

import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.google.android.gms.maps.model.LatLng;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class UpdateLocationRequest {

	@JsonProperty("install_id")
	public String installId;
	
	@JsonProperty("lat")
	public Double latitude;
	
	@JsonProperty("lng")
	public Double longitude;
	
	public UpdateLocationRequest(LatLng location, String installId) {
		this.installId = installId;
		this.latitude = location.latitude;
		this.longitude = location.longitude;
	}

	@Override
	public String toString() {
		return "UpdateLocationRequest [installId=" + installId + ", latitude="
				+ latitude + ", longitude=" + longitude + "]";
	}
}