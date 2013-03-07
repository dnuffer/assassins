package assassins.ui.prototypes;


import android.app.Activity;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;


public class MatchInfoBoundsDisplay extends MapActivity {
	
	private final int SPAN_PADDING = 10000;
	
	private MapView mapView;
	private MatchInfoBoundsFrameLayout matchBoundsLayout;
	private TransparentPanelStatic viewBoundsLayout;

	private GeoPoint nw;
	private GeoPoint se;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	     setContentView(R.layout.view_bounds_static);
	     
	     int nwLat = getIntent().getIntExtra("nwLat", -1);
	     int nwLon = getIntent().getIntExtra("nwLon", -1);
	     int seLat = getIntent().getIntExtra("seLat", -1);
	     int seLon = getIntent().getIntExtra("seLon", -1);
	     nw = new GeoPoint(nwLat, nwLon);
	     se = new GeoPoint(seLat, seLon);

	     
	     mapView = (MapView) findViewById(R.id.view_bounds_static_mapview);
	     matchBoundsLayout = (MatchInfoBoundsFrameLayout) findViewById(R.id.view_bounds_static_frame);
	     matchBoundsLayout.initBounds(nw, se);

	}

	
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
