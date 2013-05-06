package com.nbs.client.assassins.controllers;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.googlecode.androidannotations.annotations.EActivity;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.views.CreateMatchFragment;
import com.nbs.client.assassins.views.CreateMatchFragment.OnMatchCreatedListener;
import com.nbs.client.assassins.views.CreateMatchFragment_;

@EActivity
public class CreateMatchActivity extends SherlockFragmentActivity implements OnMatchCreatedListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.setContentView(R.layout.create_match_layout);
        
        if (savedInstanceState == null) {
            //Bundle fragmentArgs = new Bundle();
            //fragmentArgs.putString(CreateMatchFragment.WHATEVER, whatever);
            CreateMatchFragment createMatchFragment = new CreateMatchFragment_();
            //createMatchFragment.setArguments(fragmentArgs);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.create_match_container, createMatchFragment)
                    .commit();
        }
	}
	
    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
        	finishActivity(0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
	@Override
	public void onMatchCreated(boolean wasCreated) {
		if(wasCreated)
		{
			setResult(RESULT_OK);
			finish();
		}
	}
	
	
}
