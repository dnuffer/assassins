package com.nbs.client.assassins.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.maps.model.LatLng;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.models.PlayerModel;
import com.nbs.client.assassins.sensors.BearingProvider;
import com.nbs.client.assassins.sensors.BearingReceiver;
import com.nbs.client.assassins.sensors.BearingProviderImpl;

@EFragment(R.layout.game_fragment)
public class GameFragment extends SherlockFragment{

	private static final String TAG = null;
	MapFragment_ mapFragment;
	HUDFragment_ hudFragment;

	@ViewById(R.id.toggle_compass)
	ImageView toggleCompass;
	
	private BearingProvider bearingSource;
	
	
	public GameFragment() { }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FragmentTransaction ft;
		mapFragment = new MapFragment_();
		ft = getActivity().getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.game_fragment_container, mapFragment);
		ft.commit();
		
		hudFragment = new HUDFragment_();
		ft = getActivity().getSupportFragmentManager().beginTransaction();
		ft.add(R.id.game_fragment_container, hudFragment);
		ft.commit();
		
		bearingSource = new BearingProviderImpl(getActivity());
		mapFragment.setBearingProvider(bearingSource);
		hudFragment.setBearingProvider(bearingSource);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);

	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		bearingSource = new BearingProviderImpl(getActivity());
		mapFragment.setBearingProvider(bearingSource);
		hudFragment.setBearingProvider(bearingSource);
		super.onResume();
	}
	
	public void onGameEvent(Intent intent) {
		
	}
	
	@Click(R.id.toggle_compass)
	public void onToggleCompass() {
		mapFragment.toggleCompassMode();
		toggleCompass.setImageResource(
			mapFragment.getCompassMode() == MapFragment.MODE_BEARING ? 
					R.drawable.north : R.drawable.compass);
	}
	
	public void onTargetRangeChanged(String tRange) {
		if(tRange.equals(PlayerModel.HUNT_RANGE) || 
		   tRange.equals(PlayerModel.ATTACK_RANGE)) {
			mapFragment.showTargetLocation(PlayerModel.getTargetLocation(getSherlockActivity()));
		} else {
			mapFragment.hideTargetLocation();
		}
	}
	
	public void onTargetBearingChanged(float tBearing) {
		mapFragment.onTargetBearingChanged(tBearing);
		hudFragment.onTargetBearingChanged(tBearing);
	}
	
	public void onTargetLifeChanged(int tLife) {
		hudFragment.onTargetLifeChanged(tLife);
	}
	
	public void onMyLifeChanged(int life) {
		hudFragment.onMyLifeChanged(life);
	}
	
	public void onLocationChanged(LatLng location) {
		mapFragment.onLocationChanged(location);
	}
}
