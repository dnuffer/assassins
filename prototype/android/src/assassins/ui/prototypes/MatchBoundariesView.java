package assassins.ui.prototypes;


import android.app.Activity;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;


public class MatchBoundariesView extends MapActivity {
	
	private MapView mapView;
	
	private Button cancel;
	private Button next;
	private Button back;
	private Button done;
	
	private Intent resultIntent;
	private int[] geoCoords;
	
	private LinearLayout mapButtons;
	
	private TransparentPanel adjustBoundsLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	     setContentView(R.layout.adjust_bounds);
	     
	     geoCoords = new int[4];
	     
	     mapButtons = (LinearLayout) findViewById(R.id.map_buttons);
	          
	     adjustBoundsLayout = (TransparentPanel) findViewById(R.id.transparent_panel);
	     adjustBoundsLayout.setVisibility(View.GONE);
	     
	     mapView = (MapView) findViewById(R.id.boundariesMapView);
	     mapView.setBuiltInZoomControls(false);
	     
	     MapController mapController = mapView.getController();
	     mapController.setZoom(6);

	     
	    	cancel = (Button)findViewById(R.id.cancel);
	        cancel.setOnClickListener(new View.OnClickListener() {
	        	public void onClick(View arg0) {
	        		//send coordinates back to MatchSettingsView
	        		 if(adjustBoundsLayout.getVisibility() == View.VISIBLE)
	        		 {
	        			 adjustBoundsLayout.setVisibility(View.GONE);
	        			 //adjustBoundsLayout.resetBounds(cancel.getContext());
	        		 }
	        		 else if (adjustBoundsLayout.getVisibility() == View.GONE)
	        		 {
	        			 
	        			 finish();
	        		 }
	        	}
	        });
	        
	        next = (Button)findViewById(R.id.next);
	        next.setOnClickListener(new View.OnClickListener() {
	        	public void onClick(View arg0) {
	     
        			 adjustBoundsLayout.resetBounds(next.getContext());
        			 //mapButtons.setVisibility(View.GONE);
        			 adjustBoundsLayout.setVisibility(View.VISIBLE); 
	        		
	        	}
	        });
	        
	        
	        done = (Button)findViewById(R.id.done);
	        done.setOnClickListener(new View.OnClickListener() {
	        	public void onClick(View arg0) {
	        		
	        		populateGeoCoordsArray();
	        		
	        		//need to get coords back to settings screen
	        		Intent resultIntent = new Intent();
	        		resultIntent.putExtra("geoCoords", geoCoords);
	        		setResult(Activity.RESULT_OK, resultIntent);
	        		finish(); 
	        	}
	        });
	        
	        back = (Button)findViewById(R.id.back);
	        back.setOnClickListener(new View.OnClickListener() {
	        	public void onClick(View arg0) {
	        		
	        		adjustBoundsLayout.setVisibility(View.GONE); 
	        		mapButtons.setVisibility(View.VISIBLE);
	        	}
	        });
	}
	
	private void populateGeoCoordsArray()
	{
		RectF boundsRect = adjustBoundsLayout.getRect();
		
		//get long/lat from bounds rect position
		int[] upperLeftLatLong = getGeoPointFromScreenCoord(boundsRect.left, boundsRect.top);
		int[] lowerRightLatLong = getGeoPointFromScreenCoord(boundsRect.right, boundsRect.bottom);
		
		geoCoords[0] = upperLeftLatLong[0];
		geoCoords[1] = upperLeftLatLong[1];
		geoCoords[2] = lowerRightLatLong[0];
		geoCoords[3] = lowerRightLatLong[1];
	}
	
	private int[] getGeoPointFromScreenCoord(float x, float y)
	{
		GeoPoint center = mapView.getMapCenter();
		 
		int screenX = (int)x;
		int screenY = (int)y;
		
		int viewHeight = mapView.getHeight();
		int viewWidth = mapView.getWidth();
		 
		int latitudeSpan = mapView.getLatitudeSpan();
		int longitudeSpan = mapView.getLongitudeSpan();
		 
		int microDegreePerPixelLatitude = latitudeSpan / viewHeight;
		int microDegreePerPixelLongitude = longitudeSpan / viewWidth;
		 
		int centerScreenX = viewWidth / 2;
		int centerScreenY = viewHeight / 2;
		 
		int deltaX = centerScreenX - screenX;
		int deltaY = centerScreenY - screenY;
		 
		int deltaLatitude = microDegreePerPixelLatitude * deltaY;
		int deltaLongitude = microDegreePerPixelLongitude * deltaX;
		 
		int computedLocationLatitude = center.getLatitudeE6() + deltaLatitude;
		 
		int computedLocationLongitude = center.getLongitudeE6() - deltaLongitude;
		
		int[] coord = new int[2];
		coord[0]=computedLocationLatitude;
		coord[1]=computedLocationLongitude;
		return coord;
		 
	}
	
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	

//	
//	@Override
//	protected boolean isRouteDisplayed() {
//		// TODO Auto-generated method stub
//		return false;
//	}

}
