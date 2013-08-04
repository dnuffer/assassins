package com.nbs.client.assassins.controllers;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.googlecode.androidannotations.annotations.EActivity;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.views.JoinMatchFragment;
import com.nbs.client.assassins.views.JoinMatchFragment.OnMatchJoinedListener;
import com.nbs.client.assassins.views.JoinMatchFragment_;

@EActivity
public class JoinMatchActivity extends SherlockFragmentActivity implements OnMatchJoinedListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.setContentView(R.layout.fragment_container_layout);
        
        if (savedInstanceState == null) {
            JoinMatchFragment createMatchFragment = new JoinMatchFragment_();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.frag_container, createMatchFragment)
                    .commit();
        }
	}
	
    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
        	finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
	@Override
	public void onMatchJoined(boolean wasCreated) {
		if(wasCreated)
		{
			setResult(RESULT_OK);
			finish();
		}
	}
	
	
}