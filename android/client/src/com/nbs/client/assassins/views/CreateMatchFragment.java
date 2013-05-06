package com.nbs.client.assassins.views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.rest.RestService;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.R.id;
import com.nbs.client.assassins.R.layout;
import com.nbs.client.assassins.communication.CreateMatchMessage;
import com.nbs.client.assassins.communication.HuntedRestClient;
import com.nbs.client.assassins.communication.MatchResponse;
import com.nbs.client.assassins.communication.Response;
import com.nbs.client.assassins.models.Match;
import com.nbs.client.assassins.models.UserModel;

@EFragment(R.layout.create_match_fragment)
public class CreateMatchFragment extends SherlockFragment {

    // Container Activity must implement this interface
    public interface OnMatchCreatedListener {
        public void onMatchCreated(boolean wasCreated);
    }

	private static final String TAG = "CreateMatchFragment";

	private static final int MIN_PASSWORD_LEN = 6;
	private static final int MIN_MATCH_NAME_LEN = 6;
	
    OnMatchCreatedListener mListener;
	
	@ViewById(R.id.create_match)
	Button btnCreate;
	
	@ViewById(R.id.edit_match_name)
	EditText matchName;
	
	@ViewById(R.id.edit_match_password)
	EditText password;
	
	//@ViewById(R.id.join_when_create_match)
	//Switch join;
	
	@RestService
	HuntedRestClient restClient;
	
	private ProgressDialog asyncProgress;
	
	public CreateMatchFragment() {
		
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnMatchCreatedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnMatchCreatedListener");
        }
    }

	@Click(R.id.create_match)
	void onCreateMatchClicked() {

		//TODO: validate name and password before allowing button to be enabled
		//TODO: show visual indication if there are validation issues
		Log.i(TAG, password.getText().toString());
		String passwordStr = password.getText().toString();
		if((passwordStr.length() >= MIN_PASSWORD_LEN || passwordStr.length() == 0)  && 
		   matchName.getText().toString().length() >= MIN_MATCH_NAME_LEN) {
			
			
			InputMethodManager imm = (InputMethodManager)getSherlockActivity().getSystemService(
				      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
			btnCreate.setEnabled(false);
			
			CreateMatchMessage msg = new CreateMatchMessage();
			msg.match = new Match();			
			msg.match.password = passwordStr.length() >= MIN_PASSWORD_LEN ? passwordStr : null;
			msg.match.name = matchName.getText().toString();
			msg.token = UserModel.getToken(getActivity());
			
			asyncProgress = new ProgressDialog(getActivity());
			asyncProgress.setIndeterminate(true);
			asyncProgress.setTitle("Please Wait");
			asyncProgress.setMessage("Creating match...");
			asyncProgress.setCancelable(false);
			asyncProgress.show();
			
			createMatchInBackground(msg);
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
	
	@Background
	void createMatchInBackground(CreateMatchMessage msg) {
		
		MatchResponse response = null;
		
		try {	
			//TODO: handle exceptions
			response = restClient.createMatch(msg);		
		}
		catch(Exception e) {
			Log.i(TAG, e.getMessage());
		}
		
		matchCreatedResult(response);
	}
	
	@UiThread
	void matchCreatedResult(MatchResponse response) {

		asyncProgress.dismiss();
		
		if(response != null) {
			
			Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
			
			Log.d(TAG, response.toString());
			Log.d(TAG, "statuses match: " + (response.status == Response.OK));
			
			if(response.ok()) {
				
				//only save if set to join on create
				//UserModel.setMatch(getActivity(), response.match);
				//Log.d(TAG, UserModel._toString(getActivity()));
				
				mListener.onMatchCreated(true);
				return;
			}
		} else {
			
			btnCreate.setEnabled(true);	
			Toast.makeText(getActivity(), "Network error.", Toast.LENGTH_LONG).show();
		}
	}
}
