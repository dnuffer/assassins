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
import com.nbs.client.assassins.models.UserModel;
import com.nbs.client.assassins.network.HuntedRestClient;
import com.nbs.client.assassins.network.Response;
import com.nbs.client.assassins.network.UserLoginMessage;
import com.nbs.client.assassins.network.UserLoginResponse;

@EFragment(R.layout.login_fragment)
public class LoginFragment extends SherlockFragment {

    // Container Activity must implement this interface
    public interface OnLoginListener {
        public void onLogin(boolean success);
    }

	private static final String TAG = "CreateAccountFragment";
	
    OnLoginListener mListener;
	
	@ViewById(R.id.login)
	Button btnLogin;
	
	@ViewById(R.id.login_edit_username)
	EditText username;
	
	@ViewById(R.id.login_edit_password)
	EditText password;
	
	@RestService
	HuntedRestClient restClient;
	
	private ProgressDialog asyncProgress;
	
	public LoginFragment() {
		
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnLoginListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnLoginListener");
        }
    }
    
	@AfterInject
	public void afterInjection() {
		//subvert a bug in HttpUrlConnection
		//see: http://www.sapandiwakar.in/technical/eofexception-with-spring-rest-template-android/
		restClient.getRestTemplate().setRequestFactory(
				new HttpComponentsClientHttpRequestFactory());
	}

	@Click(R.id.login)
	void onLoginClicked() {
		
		//TODO: validate username and password before allowing button to be enabled
		//TODO: show dialog if there are validation issues
		Log.i(TAG, password.getText().toString());

		if(password.getText().toString().length() > 5 && 
		   username.getText().toString().length() > 5) {
			
			InputMethodManager imm = (InputMethodManager)getSherlockActivity().getSystemService(
				      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

			btnLogin.setEnabled(false);
			
			UserLoginMessage msg = new UserLoginMessage();
			//TODO: what if they do not have a registrationId yet?
			msg.gcmRegId = GCMRegistrar.getRegistrationId(getActivity());
			msg.installId = UserModel.getInstallId(getActivity());		
			
			msg.password = password.getText().toString();
			msg.username = username.getText().toString();

			asyncProgress = new ProgressDialog(getActivity());
			asyncProgress.setIndeterminate(true);
			asyncProgress.setTitle("Signing in...");
			asyncProgress.setCancelable(false);
			asyncProgress.show();
			
			loginInBackground(msg);
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
	void loginInBackground(UserLoginMessage msg) {
		UserLoginResponse response = null;
		try {
			response = restClient.login(msg);
		}
		catch(Exception e) {
			Log.i(TAG, e.getMessage());
		}
		loginResult(response);
	}
	
	@UiThread
	void loginResult(UserLoginResponse response) {
		
		asyncProgress.dismiss();
		
		if(response != null) {
			
			Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
			
			Log.d(TAG, response.toString());
			
			if(response.ok()) {
				
				UserModel.setUsername(getActivity(), username.getText().toString());
				UserModel.setToken(getActivity(), response.token);
				
				Log.d(TAG, UserModel._toString(getActivity()));
				
				mListener.onLogin(true);
				return;
			}
		} else {
			Toast.makeText(getActivity(), "Network error.", Toast.LENGTH_LONG).show();
			UserModel.setUsername(getActivity(), null);
			UserModel.setToken(getActivity(), null);
		}
		
		btnLogin.setEnabled(true);	
	}
}
