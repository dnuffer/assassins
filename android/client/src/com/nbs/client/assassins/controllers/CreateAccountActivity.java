package com.nbs.client.assassins.controllers;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.googlecode.androidannotations.annotations.EActivity;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.views.CreateAccountFragment;
import com.nbs.client.assassins.views.CreateAccountFragment.OnAccountCreatedListener;
import com.nbs.client.assassins.views.CreateAccountFragment_;

@EActivity
public class CreateAccountActivity extends SherlockFragmentActivity implements OnAccountCreatedListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.setContentView(R.layout.fragment_container_layout);
        
        if (savedInstanceState == null) {
            CreateAccountFragment createAccountFragment = new CreateAccountFragment_();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.frag_container, createAccountFragment)
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
	public void onAccountCreated(boolean wasCreated) {
		if(wasCreated)
		{
			setResult(RESULT_OK);
			finish();
		}
	}
}
