package assassins.ui.prototypes;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;


public class MatchInfoBoundsFrameLayout extends FrameLayout {

	private MapView mapView;
	
	
	private GeoPoint nw;
	private GeoPoint se;
	private GeoPoint midpoint;

	private TransparentPanelStatic viewBoundsLayout;

	private GeoPoint ne;

	private GeoPoint sw;
	
	
	public MatchInfoBoundsFrameLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public MatchInfoBoundsFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public MatchInfoBoundsFrameLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void dispatchDraw(Canvas c)
	{
		super.dispatchDraw(c);
		
		drawMatchBounds();
		
	}
	
	
	private RectF getBoundingScreenRect(MapView v, GeoPoint nw, GeoPoint se)
	{
		int[] nwCoord = geoToScreenCoords(v, nw);
		int[] seCoord = geoToScreenCoords(v, se);
		RectF bounds = new RectF();
		bounds.left = nwCoord[0];
		bounds.top = nwCoord[1];
		bounds.right = seCoord[0];
		bounds.bottom = seCoord[1];
		
		Log.d("Project Assassins","left: "+bounds.left+" top: "+ bounds.top +
							"right: "+bounds.right+" bottom: "+ bounds.bottom);
		
		return bounds;
		
		
	}
	
	
	private int[] geoToScreenCoords(MapView v, GeoPoint corner)
	{
		GeoPoint center = v.getMapCenter();
		
		int viewHeight = v.getHeight();
		int viewWidth = v.getWidth();
		
		Log.d("Project Assassins", "View Height: "+viewHeight+ " View Width: "+viewWidth);
		 
		int latitudeSpan = v.getLatitudeSpan();
		int longitudeSpan = v.getLongitudeSpan();

		int microDegreePerPixelLatitude = latitudeSpan / viewHeight;
		int microDegreePerPixelLongitude = longitudeSpan / viewWidth;
		
		Log.d("Project Assassins", "lat/pxl: "+microDegreePerPixelLatitude + 
				"lon/pxl: "+microDegreePerPixelLongitude);
		 
		int centerScreenX = viewWidth / 2;
		int centerScreenY = viewHeight / 2;
		 
		Log.d("Project Assassins", "Screen center: "+centerScreenX+","+centerScreenY);
		
		int deltaLatitude = (center.getLatitudeE6() > 0) ? 
							 center.getLatitudeE6() - corner.getLatitudeE6() : 
							 corner.getLatitudeE6() - center.getLatitudeE6();
		int deltaLongitude = (center.getLongitudeE6() > 0) ? 
							  center.getLongitudeE6() - corner.getLongitudeE6() : 
							  corner.getLongitudeE6() - center.getLongitudeE6();
		
		Log.d("Project Assassins", "deltaLat: "+deltaLatitude+" deltaLon: "+deltaLongitude);
		
		int deltaY = deltaLatitude / microDegreePerPixelLatitude;
		int deltaX = deltaLongitude / microDegreePerPixelLongitude;
		
		int y = centerScreenY + deltaY;
		int x = centerScreenX + deltaX;
		
		Log.d("Project Assassins", "deltaX: "+deltaX+" deltaY: "+deltaY);
		
		int[] coords = new int[2];
		
		coords[0] = x;
		coords[1] = y;
		Log.d("Project Assassins", "Boundary coord: " + coords[0] + ", " + coords[1]);
		return coords;
		 
	}
	
	
	public void drawMatchBounds()
	{
		mapView = (MapView) findViewById(R.id.view_bounds_static_mapview);
		
/*		String title = "Title";
		String snippet = "Snippet";
		
		List<Overlay> mapOverlays = mapView.getOverlays();
		 
		Drawable overlayImg = this.getResources().getDrawable(R.drawable.ic_maps_indicator_current_position);
		CustomItemizedOverlay itemizedOverlay = new CustomItemizedOverlay(overlayImg, this.getContext());
		OverlayItem overlayitem1 = new OverlayItem(nw, title, snippet);
		OverlayItem overlayitem2 = new OverlayItem(se, title, snippet);
		OverlayItem overlayitem3 = new OverlayItem(ne, title, snippet);
		OverlayItem overlayitem4 = new OverlayItem(sw, title, snippet);
		OverlayItem overlayitem5 = new OverlayItem(midpoint, title, snippet);
		itemizedOverlay.addOverlay(overlayitem1);
		itemizedOverlay.addOverlay(overlayitem2);
		itemizedOverlay.addOverlay(overlayitem3);
		itemizedOverlay.addOverlay(overlayitem4);
		itemizedOverlay.addOverlay(overlayitem5);
		mapOverlays.add(itemizedOverlay); */
		
		
		viewBoundsLayout = (TransparentPanelStatic) findViewById(
				R.id.view_bounds_transparent_panel_static);
		viewBoundsLayout.setVisibility(View.VISIBLE); 
		
		viewBoundsLayout.setRect(getBoundingScreenRect(mapView, nw,se));
		viewBoundsLayout.invalidate();

	}

	public void initBounds(GeoPoint nw, GeoPoint se) {
		this.nw = nw;
		this.se = se;
		midpoint = PlayerLocation.midPoint(nw, se); 
		ne = new GeoPoint(nw.getLatitudeE6(), se.getLongitudeE6());
		sw = new GeoPoint(se.getLatitudeE6(), nw.getLongitudeE6());

		int latSpan = PlayerLocation.geoSpan(nw.getLatitudeE6(), se.getLatitudeE6());
		int lonSpan = PlayerLocation.geoSpan(nw.getLongitudeE6(), se.getLongitudeE6());
		mapView = (MapView)findViewById(R.id.view_bounds_static_mapview);
		mapView.getController().animateTo(midpoint, new MapZoomerRunnable(mapView, latSpan, lonSpan));
		mapView.setClickable(false);
	}

}

class MapZoomerRunnable implements Runnable
{

	private final int SPAN_PADDING = 10000;
	
	MapView mv;
	int latitudeSpan;
	int longitudeSpan;
	
	public MapZoomerRunnable(MapView mv, int latitudeSpan, int longitudeSpan)
	{
		this.mv = mv;
		this.longitudeSpan =longitudeSpan;
		this.latitudeSpan = latitudeSpan;
	}
	public void run() {

		mv.getController().zoomToSpan(latitudeSpan+SPAN_PADDING, longitudeSpan+SPAN_PADDING);
		
	}
}
