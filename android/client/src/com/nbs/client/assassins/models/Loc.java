package com.nbs.client.assassins.models;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.google.android.gms.maps.model.LatLng;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Loc{

	@JsonProperty("lat")
	public double lat;
	
	@JsonProperty("lng")
	public double lng;
	
	public Loc() {}
	
	public Loc(LatLng latLng) {
		lat = latLng.latitude;
		lng = latLng.longitude;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[lat=" + lat + ", lng=" + lng + "]";
	}
}
