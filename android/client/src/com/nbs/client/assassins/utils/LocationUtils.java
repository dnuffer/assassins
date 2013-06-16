package com.nbs.client.assassins.utils;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class LocationUtils {
		
		private static final String TAG = "LocationUtils";

		public static int geoSpan(int a, int b)
		{
			int span = -1;
			
			span = (a > b) ? a-b : b-a;
			
			return span;
		}
		
		public static LatLng midPoint(LatLng a, LatLng b){

	        Log.d(TAG, "LatLng alat: "+a.latitude + " alon: " + a.longitude);
	        Log.d(TAG, "LatLng blat: "+b.latitude + " blon: " + b.longitude);
			
	        Location aLoc = LocationUtils.latLngToLocation(a);
	        Location bLoc = LocationUtils.latLngToLocation(b);
	        
	        Log.d(TAG, "Location alat: "+aLoc.getLatitude() + " alon: " + aLoc.getLongitude());
	        Log.d(TAG, "Location blat: "+bLoc.getLatitude() + " blon: " + bLoc.getLongitude());
			
	        double lat1 = aLoc.getLatitude();
			double lon1 = aLoc.getLongitude();
			double lat2 = bLoc.getLatitude();
			double lon2 = bLoc.getLongitude();
			
			double dLon = Math.toRadians(lon2 - lon1);

		    //convert to radians
		    lat1 = Math.toRadians(lat1);
		    lat2 = Math.toRadians(lat2);
		    lon1 = Math.toRadians(lon1);
		    
		    double Bx = Math.cos(lat2) * Math.cos(dLon);
		    double By = Math.cos(lat2) * Math.sin(dLon);
		    double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
		    double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

		    lat3 = Math.toDegrees(lat3);
		    lon3 = Math.toDegrees(lon3);
		    
		    Location center = newLocation(lat3, lon3);
		    
		    Log.d(TAG, "midpoint as Location lat: "+center.getLatitude() + " lon: " + center.getLongitude());
		    
		    
		    LatLng mid = locationToLatLng(center);
		    
		    Log.d(TAG, "midpoint as LatLng lat: "+mid.latitude + " lon: " + mid.longitude);
		    
		    return mid;
		}
		
		public static LatLng locationToLatLng(Location loc)
		{	
			return new LatLng(loc.getLatitude(), loc.getLongitude());		
		}
		
		public static Location latLngToLocation(LatLng geo)
		{	
			Location loc = newLocation(geo.latitude, geo.longitude);
			return loc;		
		}

		private static Location newLocation(double lat, double lng) {
			Location loc = new Location(LocationManager.PASSIVE_PROVIDER);
			loc.setLatitude(lat);
			loc.setLongitude(lng);
			return loc;
		}
		
		public static double roundDouble(double d) {
	        DecimalFormat dFormat = new DecimalFormat("#.##");
	        return Double.valueOf(dFormat.format(d));
		}
		
		
		public static String getMilesAreaString(LatLng corner1, LatLng corner2)
		{
			double x = ( 69.1 * ((corner1.latitude) - (corner2.latitude) )); 
			double y = ( 53   * ((corner1.latitude) - (corner2.latitude) ));
			return "" + Math.abs(roundDouble(x)) + " mi. by " + Math.abs(roundDouble(y)) + " mi.";
		}
		
		public static float getMetersBetween(LatLng me, LatLng them)
		{
			if(me == null || them == null)
			{
				return -1;
			}
			
			Location myLoc = latLngToLocation(me);
			Location theirLoc = latLngToLocation(them);
			
			float dist = myLoc.distanceTo(theirLoc);
			
			return dist;
		}
		
		public static float getBearingTo(LatLng me, LatLng them)
		{
			if(me == null || them == null)
			{
				return -1;
			}
			
			Location myLoc = latLngToLocation(me);
			Location theirLoc = latLngToLocation(them);
			
			float bearingToThem = myLoc.bearingTo(theirLoc);
			
			return bearingToThem;
		}

		public static List<LatLng> sortSouthToNorth(List<LatLng> points) {
			Collections.sort(points, new SouthToNorthComparator());
			return points;
		}

		public static List<LatLng> sortEastToWest(List<LatLng> points) {
			Collections.sort(points, new EastToWestComparator());
			return points;
		}
}