package assassins.ui.prototypes;

import com.google.android.maps.GeoPoint;

public class MatchBounds {
	
	private static final int MAX_POINTS = 2;
	GeoPoint[] area;
	
	MatchBounds()
	{
		init(); 
		
	}
	
	MatchBounds(int lat1, int lon1, int lat2, int lon2)
	{
		init();
		
	}
	
	private void init()
	{
		area = new GeoPoint[MAX_POINTS];
	}
	
	public GeoPoint[] getPointsArray()
	{
		return area; 
	}
	
	public void setPoint(int pointIndex, int lat, int lon)
	{
		if(pointIndex < MAX_POINTS)
			area[pointIndex] = new GeoPoint(lat, lon);
	}
	
	
}
