package com.nbs.client.assassins.views;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gcm.GCMRegistrar;
import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.rest.RestService;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.models.User;
import com.nbs.client.assassins.network.HuntedRestClient;
import com.nbs.client.assassins.network.Response;
import com.nbs.client.assassins.network.UserLoginMessage;
import com.nbs.client.assassins.network.UserLoginResponse;

@EFragment(R.layout.create_account_fragment)
public class CreateAccountFragment extends SherlockFragment {

    // Container Activity must implement this interface
    public interface OnAccountCreatedListener {
        public void onAccountCreated(boolean wasCreated);
    }

	private static final String TAG = "CreateAccountFragment";
	
    OnAccountCreatedListener mListener;
	
	@ViewById(R.id.create_account)
	Button btnCreate;
	
	@ViewById(R.id.edit_username)
	EditText username;
	
	@ViewById(R.id.edit_password)
	EditText password;
	
	@RestService
	HuntedRestClient restClient;
	
	private ProgressDialog asyncProgress;
	
	public CreateAccountFragment() {
		
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnAccountCreatedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnAccountCreatedListener");
        }
    }
    
	@AfterInject
	public void afterInjection() {
		//subvert a bug in HttpUrlConnection
		//see: http://www.sapandiwakar.in/technical/eofexception-with-spring-rest-template-android/
		restClient.getRestTemplate().setRequestFactory(
				new HttpComponentsClientHttpRequestFactory());
	}

	@Click(R.id.create_account)
	void onCreateAccountClicked() {
		
		//TODO: validate username and password before allowing button to be enabled
		//TODO: show dialog if there are validation issues
		Log.i(TAG, password.getText().toString());

		if(password.getText().toString().length() > 5 && 
		   username.getText().toString().length() > 5) {
			
			InputMethodManager imm = (InputMethodManager)getSherlockActivity().getSystemService(
				      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

			btnCreate.setEnabled(false);
			
			UserLoginMessage msg = new UserLoginMessage();
			//TODO: what if they do not have a registrationId yet?
			msg.gcmRegId = GCMRegistrar.getRegistrationId(getActivity());
			msg.installId = User.getInstallId(getActivity());		
			
			msg.password = password.getText().toString();
			msg.username = username.getText().toString();

			asyncProgress = new ProgressDialog(getActivity());
			asyncProgress.setIndeterminate(true);
			asyncProgress.setTitle("Creating account...");
			asyncProgress.setCancelable(false);
			asyncProgress.show();
			
			createAccountInBackground(User.getToken(getActivity()), msg);
		}
		else {
			//TODO: provide earlier and better validation information to user
			Toast.makeText(
				getActivity(), 
				"Username at least 6 chars, Password at least 6 chars", 
				Toast.LENGTH_LONG
			).show();
		}
	}
	
	@Background
	void createAccountInBackground(String token, UserLoginMessage msg) {
		
		UserLoginResponse response = null;
		
		try {
		
		//TODO: handle exceptions
		if(token == null)
			response = restClient.registerUser(msg);
		else
			response = restClient.registerUser(token, msg);
		
		}
		catch(Exception e) {
			Log.i(TAG, e.getMessage());
		}
		
		accountCreatedResult(response);
	}
	
	@UiThread
	void accountCreatedResult(UserLoginResponse response) {
		
		asyncProgress.dismiss();
		
		if(response != null) {
			
			Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
			
			Log.d(TAG, response.toString());
			
			if(response.ok()) {
				
				User.setUsername(getActivity(), username.getText().toString());
				User.setToken(getActivity(), response.token);
				
				Log.d(TAG, User._toString(getActivity()));
				
				mListener.onAccountCreated(true);
				return;
			}
		} else {
			Toast.makeText(getActivity(), "Network error.", Toast.LENGTH_LONG).show();
		}
		
		btnCreate.setEnabled(true);	
	}
}
