package com.nbs.client.assassins.utils;

import java.util.Comparator;

import com.google.android.gms.maps.model.LatLng;

public class EastToWestComparator implements Comparator<LatLng> {

	@Override
	public int compare(LatLng lhs, LatLng rhs) {
		double l = lhs.longitude; double r = rhs.longitude;
		return (l > r) ? -1 : (l == r) ? 0 : 1; 
	}

}
