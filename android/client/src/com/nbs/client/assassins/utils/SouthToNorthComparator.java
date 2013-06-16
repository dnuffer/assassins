package com.nbs.client.assassins.utils;

import java.util.Comparator;

import com.google.android.gms.maps.model.LatLng;

public class SouthToNorthComparator implements Comparator<LatLng> {

	@Override
	public int compare(LatLng lhs, LatLng rhs) {		
		double l = lhs.latitude; double r = rhs.latitude;
		return (l > r) ? -1 : (l == r) ? 0 : 1;
	}

}
