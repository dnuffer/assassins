/**
 * 
 */
package com.nbs.client.assassins.views;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.sensors.BearingProvider;
import com.nbs.client.assassins.sensors.BearingReceiver;

/**
 * @author cam
 *
 */
@EFragment(R.layout.hud)
public class HUDFragment extends SherlockFragment implements BearingReceiver {

	private static final String TAG = "HUDFragment";

	@ViewById(R.id.hud_tbearing)
	TextView tBearingView;
	
	@ViewById(R.id.hud_target_life)
	ProgressBar tLifeView;
	
	@ViewById(R.id.hud_my_life)
	ProgressBar lifeView;
	
	private BearingProvider bearingProvider;

	public HUDFragment() {}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onViewCreated(android.view.View, android.os.Bundle)
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		//stopSensorUpdates();
		super.onPause();
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		//registerForSensorUpdates();
		super.onResume();
	}
	
	private void stopSensorUpdates() {
		bearingProvider.unregisterForBearingUpdates(this);
	}

	private void registerForSensorUpdates() {
		bearingProvider.registerForBearingUpdates(this);
	}
	
	@Override
	public void setBearingProvider(BearingProvider provider) {
		bearingProvider = provider;	
	}

	@Override
	public void onBearingChanged(float bearing) {
		Log.d(TAG, Float.toString(bearing));
	}
	
	public void onTargetBearingChanged(float tBearing) {
		tBearingView.setText(Float.toString(tBearing));
	}
	
	public void onMyLifeChanged(int life) {
		lifeView.setProgress(life);
	}
	
	public void onTargetLifeChanged(int tLife) {
		tLifeView.setProgress(tLife);
	}
}
