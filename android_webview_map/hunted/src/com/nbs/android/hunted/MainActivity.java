package com.nbs.android.hunted;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Menu;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity implements LocationListener {

	private static final String MAP_URL = "file:///android_asset/webView.html";
	private WebView webView;
	private Location mostRecentLocation;
	@Override
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getLocation();
		setupWebView();
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void setupWebView(){
		webView = (WebView) findViewById(R.id.mapWebView);
		webView.getSettings().setJavaScriptEnabled(true);
		//Wait for the page to load then send the location information
		webView.setWebChromeClient(new WebChromeClient(){
			public void onConsoleMessage(String message, int lineNumber, String sourceID) {
				Log.d("WebView Console: ", message + " -- line "
						+ lineNumber + " of "
						+ sourceID);
			}
		});
		webView.addJavascriptInterface(new JavaScriptInterface(), "android");
		webView.loadUrl(MAP_URL);
		/** Allows JavaScript calls to access application resources **/	
	}
	/** Sets up the interface for getting access to Latitude and Longitude data from device
	 **/
	private class JavaScriptInterface {
		public double getLatitude(){
			return mostRecentLocation.getLatitude();
		}
		public double getLongitude(){
			return mostRecentLocation.getLongitude();
		}
	}
	private void getLocation() {
		LocationManager locationManager =
				(LocationManager)getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		String provider = locationManager.getBestProvider(criteria,true);
		//In order to make sure the device is getting the location, request updates.
		locationManager.requestLocationUpdates(provider, 1, 0, this);
		mostRecentLocation = locationManager.getLastKnownLocation(provider);
	}

	@Override
	public void onLocationChanged(Location location) {
		final String centerURL = "javascript:centerAt(" +
				location.getLatitude() + "," +
				location.getLongitude()+ ")";
		webView.loadUrl(centerURL);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

}
