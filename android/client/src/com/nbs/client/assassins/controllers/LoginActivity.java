package com.nbs.client.assassins.controllers;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.googlecode.androidannotations.annotations.EActivity;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.views.LoginFragment;
import com.nbs.client.assassins.views.LoginFragment_;
import com.nbs.client.assassins.views.LoginFragment.OnLoginListener;

@EActivity
public class LoginActivity extends SherlockFragmentActivity implements OnLoginListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.setContentView(R.layout.fragment_container_layout);
        
        if (savedInstanceState == null) {
            //Bundle fragmentArgs = new Bundle();
            //fragmentArgs.putString(LoginFragment.WHATEVER, whatever);
            LoginFragment loginFragment = new LoginFragment_();
            //createMatchFragment.setArguments(fragmentArgs);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.frag_container, loginFragment)
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
	public void onLogin(boolean success) {
		if(success)
		{
			setResult(RESULT_OK);
			finish();
		}
	}
}
