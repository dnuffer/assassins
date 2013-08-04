package com.nbs.client.assassins.controllers;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.maps.model.LatLng;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.views.MatchBoundsFragment;
import com.nbs.client.assassins.views.MatchBoundsFragment.OnBoundsSelectedListener;
import com.nbs.client.assassins.views.MatchBoundsFragment_;

public class MatchBoundsActivity extends SherlockFragmentActivity implements OnBoundsSelectedListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        this.setContentView(R.layout.fragment_container_layout);
        
        if (savedInstanceState == null) {
            MatchBoundsFragment matchBoundsFragment = new MatchBoundsFragment_();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.frag_container, matchBoundsFragment)
                    .commit();
        }
	}
	
    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
        	setResult(RESULT_CANCELED);
        	finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
	@Override
	public void onBoundsSelected(String areaDescription, List<LatLng> points) {
			setResult(RESULT_OK, new Intent()
									.putExtra("points", points.toArray())
									.putExtra("description", areaDescription));
			finish();
	}
}
