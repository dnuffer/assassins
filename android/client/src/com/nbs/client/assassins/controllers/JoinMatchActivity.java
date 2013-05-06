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

        this.setContentView(R.layout.join_match_layout);
        
        if (savedInstanceState == null) {
            //Bundle fragmentArgs = new Bundle();
            //fragmentArgs.putString(CreateMatchFragment.WHATEVER, whatever);
            JoinMatchFragment createMatchFragment = new JoinMatchFragment_();
            //createMatchFragment.setArguments(fragmentArgs);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.join_match_container, createMatchFragment)
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