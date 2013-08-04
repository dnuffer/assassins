package com.nbs.client.assassins.models;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.google.android.gms.maps.model.LatLng;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class LatLngData{

	@JsonProperty("lat")
	public double lat;
	
	@JsonProperty("lng")
	public double lng;
	
	public LatLngData() {}
	
	public LatLngData(LatLng latLng) {
		lat = latLng.latitude;
		lng = latLng.longitude;
	}
	
	public LatLng toLatLng() {
		return new LatLng(lat, lng);
	}

	@Override
	public String toString() {
		return "[lat=" + lat + ", lng=" + lng + "]";
	}
}
