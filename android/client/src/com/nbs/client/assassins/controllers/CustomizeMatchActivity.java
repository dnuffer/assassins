package com.nbs.client.assassins.controllers;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.views.CustomizeMatchFragment;
import com.nbs.client.assassins.views.CustomizeMatchFragment.GameplaySettingsListener;
import com.nbs.client.assassins.views.CustomizeMatchFragment_;

public class CustomizeMatchActivity extends SherlockFragmentActivity implements GameplaySettingsListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        this.setContentView(R.layout.fragment_container_layout);
        
        if (savedInstanceState == null) {
            CustomizeMatchFragment gpFragment = new CustomizeMatchFragment_();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.frag_container, gpFragment)
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
	public void onGameplaySettingsFinished(int wait, double hRange, double aRange) {
		setResult(RESULT_OK, new Intent()
			.putExtra("wait", wait)
			.putExtra("hRange", hRange)
			.putExtra("aRange", aRange));
		finish();
	}

	@Override
	public void onGameplaySettingsCancelled() {
    	setResult(RESULT_CANCELED);
    	finish();
	}
}
