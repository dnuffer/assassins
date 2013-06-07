package assassins.ui.prototypes;

//import android.app.Activity;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;


//public class SearchMode extends Activity {
public class HuntMode extends MapActivity {
	
	private MapView mapView;
	
    private static final int latitudeE6 = 37985339;
    private static final int longitudeE6 = 23716735;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	     setContentView(R.layout.hunt_mode);
	     
	     mapView = (MapView) findViewById(R.id.map_view);
	     mapView.setBuiltInZoomControls(true);
	     
	     List<Overlay> mapOverlays = mapView.getOverlays();
	     
	     Drawable drawable = this.getResources().getDrawable(R.drawable.icon);
	     CustomItemizedOverlay2 itemizedOverlay = new CustomItemizedOverlay2(drawable, this);
	     
	     GeoPoint point = new GeoPoint(latitudeE6, longitudeE6);
	     OverlayItem overlayitem = new OverlayItem(point, "Hello", "I'm in Athens, Greece!");
	     
	     itemizedOverlay.addOverlay(overlayitem);
	     mapOverlays.add(itemizedOverlay);
	     
	     MapController mapController = mapView.getController();
	     
	     mapController.animateTo(point);
	     mapController.setZoom(6);
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
