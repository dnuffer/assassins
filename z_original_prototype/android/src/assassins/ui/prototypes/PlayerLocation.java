package assassins.ui.prototypes;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.text.format.Time;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class PlayerLocation {

		PlayerLocation()
		{
			
		}
		
		public static GeoPoint getMyLocationAsGeoPoint(Context context) 
		{	
			Log.d("Project Assassins", "entering getMyLocationAsGeoPoint()");
			
			Location l = PlayerLocation.getMyLocation(context);
			if(l != null)
			{
				Log.d("Project Assassins", "my location in degrees: "+ l.getLatitude() +" "+ l.getLongitude());
				return PlayerLocation.locationToGeoPoint(l);
			}
			else
				return null;
			
		}
		
		public static Location getMyLocation(Context context)
		{
			Log.d("Project Assassins", "entering getMyLocation()");
			
			LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			LocationProvider p = lm.getProvider(LocationManager.GPS_PROVIDER);
			List<String> providers = lm.getProviders(false);
			
			Location best = null;
			float mostAccurate = 100000; //init to something ridiculously high
			
			Time t = new Time();
			t.setToNow();
			long currTime = t.toMillis(true);
			
			for(String provider : providers)
			{
				Location location = lm.getLastKnownLocation(provider);
				
				
				Log.d("ProjectAssassins", "Provider: "+provider);
				
				if(location != null)
				{
					long locationTime = location.getTime();
					float accuracy = location.getAccuracy();
					Log.d("ProjectAssassins", "   Accuracy: "+accuracy +"meters, Age: "+
							(currTime-locationTime)/1000 +" secs");
					
					//could also factor in age of location fix here
					if(accuracy < mostAccurate)
					{
						best = location;
						Log.d("ProjectAssassins", "BEST LOCATION FIX - "+provider);
					}
					
				}
			}

			return best;
		}
		
		
		public static int geoSpan(int a, int b)
		{
			int span = -1;
			
			span = (a > b) ? a-b : b-a;
			
			return span;
		}
		
		public static GeoPoint midPoint(GeoPoint a, GeoPoint b){

	        Log.d("Project Assassins", "GeoPoint alat: "+a.getLatitudeE6() + " alon: " + a.getLongitudeE6());
	        Log.d("Project Assassins", "GeoPoint blat: "+b.getLatitudeE6() + " blon: " + b.getLongitudeE6());
			
	        Location aLoc = PlayerLocation.geoPointToLocation(a);
	        Location bLoc = PlayerLocation.geoPointToLocation(b);
	        
	        Log.d("Project Assassins", "Location alat: "+aLoc.getLatitude() + " alon: " + aLoc.getLongitude());
	        Log.d("Project Assassins", "Location blat: "+bLoc.getLatitude() + " blon: " + bLoc.getLongitude());
			
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
		    
		    Location center = new Location(LocationManager.PASSIVE_PROVIDER);
		    center.setLatitude(lat3);
		    center.setLongitude(lon3);
		    
		    Log.d("Project Assassins", "midpoint as Location lat: "+center.getLatitude() + " lon: " + center.getLongitude());
		    
		    
		    GeoPoint mid = locationToGeoPoint(center);
		    
		    Log.d("Project Assassins", "midpoint as GeoPoint lat: "+mid.getLatitudeE6() + " lon: " + mid.getLongitudeE6());
		    
		    return mid;
		}
		
		public static GeoPoint locationToGeoPoint(Location loc)
		{	
			int lat = degreesToMicroDegrees(loc.getLatitude());
			int lon = degreesToMicroDegrees(loc.getLongitude());
			
			return new GeoPoint(lat, lon);		
		}
		
		public static Location geoPointToLocation(GeoPoint geo)
		{	
			double lat = microDegreesToDegrees(geo.getLatitudeE6());
			double lon = microDegreesToDegrees(geo.getLongitudeE6());
			
			Location loc = new Location(LocationManager.PASSIVE_PROVIDER);
			loc.setLatitude(lat);
			loc.setLongitude(lon);
			
			return loc;		
		}
		
		public static double roundDouble(double d) {
	        DecimalFormat twoDForm = new DecimalFormat("#.##");
	        return Double.valueOf(twoDForm.format(d));
		}
		
		
		public static String getMilesAreaString(GeoPoint corner1, GeoPoint corner2)
		{
			double x = ( 69.1 * ((corner1.getLatitudeE6()/1E6) - (corner2.getLatitudeE6()/1E6) )); 
			double y = ( 53   * ((corner1.getLatitudeE6()/1E6) - (corner2.getLatitudeE6()/1E6) ));
			return "" + roundDouble(x) + " miles by " + roundDouble(y) + " miles";
		}
		

		public static float getMetersBetween(GeoPoint me, GeoPoint them)
		{
			if(me == null || them == null)
			{
				return -1;
			}
			
			Location myLoc = geoPointToLocation(me);
			Location theirLoc = geoPointToLocation(them);
			
			float dist = myLoc.distanceTo(theirLoc);
			
			return dist;
		}
		
		public static float getBearingTo(GeoPoint me, GeoPoint them)
		{
			if(me == null || them == null)
			{
				return -1;
			}
			
			Location myLoc = geoPointToLocation(me);
			Location theirLoc = geoPointToLocation(them);
			
			float bearingToThem = myLoc.bearingTo(theirLoc);
			
			return bearingToThem;
		}
		
		public static int degreesToMicroDegrees(double degrees) {
		    
			int microDeg = (int)(degrees*1E6);
			
			Log.d("Project Assassins", "degreesToMicroDegrees - degrees '" + degrees + "' to  microdegrees '" +microDeg + "'");
			
			return microDeg;
		}
		
		public static double microDegreesToDegrees(int microDegrees) {
			
			double degrees = ((double) microDegrees) / (double)1E6;
			
			Log.d("Project Assassins", "microDegreesToDegrees - microDegrees '" + microDegrees + "' to degrees '" +degrees + "'");
			
		    return degrees;
		}
}
