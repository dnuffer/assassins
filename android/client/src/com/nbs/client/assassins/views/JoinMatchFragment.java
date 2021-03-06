package com.nbs.client.assassins.views;


import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.internal.m;
import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.rest.RestService;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.R.id;
import com.nbs.client.assassins.R.layout;
import com.nbs.client.assassins.models.App;
import com.nbs.client.assassins.models.Repository;
import com.nbs.client.assassins.models.User;
import com.nbs.client.assassins.network.HuntedRestClient;
import com.nbs.client.assassins.network.JoinMatchRequest;
import com.nbs.client.assassins.network.MatchResponse;
import com.nbs.client.assassins.network.Response;
import com.nbs.client.assassins.services.NotificationService;
import com.nbs.client.assassins.services.NotificationService_;

@EFragment(R.layout.join_match_fragment)
public class JoinMatchFragment extends SherlockFragment {

	private static final String TAG = "JoinMatchFragment";
	
    // Container Activity must implement this interface
    public interface OnMatchJoinedListener {
        public void onMatchJoined(boolean wasJoind);
    }

	private static final int MIN_PASSWORD_LEN = 6;
	private static final int MIN_MATCH_NAME_LEN = 6;
	
    OnMatchJoinedListener mListener;
	
	@ViewById(R.id.join_match)
	Button btnJoin;
	
	@ViewById(R.id.edit_join_match_name)
	EditText matchName;
	
	@ViewById(R.id.edit_join_match_password)
	EditText password;
	
	@RestService
	HuntedRestClient restClient;
	
	public JoinMatchFragment() {}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnMatchJoinedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnMatchJoindListener");
        }
    }

	@Click(R.id.join_match)
	void onJoinMatchClicked() {
		//TODO: validate name and password before allowing button to be enabled
		//TODO: show visual indication if there are validation issues
		Log.i(TAG, password.getText().toString());
		String passwordStr = password.getText().toString();
		if((passwordStr.length() >= MIN_PASSWORD_LEN || passwordStr.length() == 0)  && 
		   matchName.getText().toString().length() >= MIN_MATCH_NAME_LEN) {
			
			hideKeyboard();
			
			btnJoin.setEnabled(false);
			Repository model = ((App)(getActivity().getApplication())).getRepo();
			User user = model.getUser();
			
			JoinMatchRequest msg = new JoinMatchRequest(
				user.getToken(),
				(passwordStr.length() > 0 ? passwordStr : null),
				matchName.getText().toString()
			);
			
			joinMatchInBackground(msg, 
				ProgressDialog.show(getActivity(),"Please Wait","Joining match...", true, false));
		}
		else {
			//TODO: provide earlier and better validation information to user
			Toast.makeText(
				getActivity(), 
				"Match name at least " + MIN_MATCH_NAME_LEN +
				" chars, Password at least " + MIN_PASSWORD_LEN + " chars", 
				Toast.LENGTH_LONG
			).show();
		}
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager)getSherlockActivity().getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
	}
	
	@Background
	void joinMatchInBackground(JoinMatchRequest msg, ProgressDialog progress) {
		MatchResponse response = null;
		
		try {	
			response = restClient.joinMatch(msg.matchName, msg);	
			
			if(response.ok() && response.time != null && response.match != null) {
				//in order to have clients start at the same time, 
				//here we account for time discrepancy between server and client
				//the time it takes to send the message is still NOT accounted for
				//a server push notification on match start is probably a better solution...
				long now = System.currentTimeMillis();
				long serverTimeDifference = response.time - now;
				
				Log.d(TAG, "match start time ["+response.match.startTime+"]");
				
				if(response.match.startTime != null) {
					response.match.startTime += serverTimeDifference;
					Log.d(TAG, "time [local:" + now + "], [server:" + response.time + 
							"], fix local time ["+serverTimeDifference+"]");
					Log.d(TAG, "corrected match start time ["+response.match.startTime+"]");
				}
			}	
		}
		catch(Exception e) {
			Log.i(TAG, "EXCEPTION: " + e.toString());
		}
		
		matchJoinedResult(response, progress);
	}
	
	@UiThread
	void matchJoinedResult(MatchResponse response, ProgressDialog progress) {
		
		if(response != null) {
			Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
			Log.d(TAG, response.toString());
			
			if(response.ok()) {
				Repository model = App.getRepo();
				
				model.createOrUpdateMatch(response.match);

				Log.d(TAG, "starting notification service with start time ["+response.match.startTime+"]");
				
				if(response.match.startTime != null) {
					getActivity().startService(
							new Intent(getActivity(), NotificationService_.class)
								.setAction(NotificationService.SET_MATCH_REMINDER_ALARMS)
								.putExtra("start_time", response.match.startTime));
				} else {
					getActivity().startService(
							new Intent(getActivity(), NotificationService_.class)
								.setAction(NotificationService.WAIT_FOR_MATCH_START));
				}
				
				mListener.onMatchJoined(true);
				return;
			}
		} else {
			Toast.makeText(getActivity(), "Network error.", Toast.LENGTH_LONG).show();
		}
		
		progress.dismiss();
		btnJoin.setEnabled(true);
	}
    
	@AfterInject
	public void afterInjection() {
		//subvert a bug in HttpUrlConnection
		//see: http://www.sapandiwakar.in/technical/eofexception-with-spring-rest-template-android/
		restClient.getRestTemplate().setRequestFactory(
				new HttpComponentsClientHttpRequestFactory());
	}
}
