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
import com.nbs.client.assassins.models.App;
import com.nbs.client.assassins.models.Match;
import com.nbs.client.assassins.models.Repository;
import com.nbs.client.assassins.models.User;
import com.nbs.client.assassins.network.HuntedRestClient;
import com.nbs.client.assassins.network.LoginRequest;
import com.nbs.client.assassins.network.LoginResponse;

@EFragment(R.layout.login_fragment)
public class LoginFragment extends SherlockFragment {

	private static final String TAG = "CreateAccountFragment";
	
    // Container Activity must implement this interface
    public interface OnLoginListener {
        public void onLogin(boolean success);
    }
	
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
	
	public LoginFragment() {}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnLoginListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnLoginListener");
        }
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

			Repository model = ((App)(getActivity().getApplication())).getRepo();
			User user = model.getUser();
			btnLogin.setEnabled(false);
			
			LoginRequest msg = new LoginRequest();
			//TODO: what if they do not have a registrationId yet?
			msg.gcmRegId = GCMRegistrar.getRegistrationId(getActivity());
			msg.installId = user.getInstallId();		
			
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
	void loginInBackground(LoginRequest msg) {
		LoginResponse response = null;
		try {
			response = restClient.login(msg);
		}
		catch(Exception e) {
			Log.i(TAG, e.getMessage());
		}
		loginResult(response);
	}
	
	@UiThread
	void loginResult(LoginResponse response) {
		
		asyncProgress.dismiss();
		Repository model = App.getRepo();
		User user = model.getUser();
		
		if(response != null) {
			
			Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
			
			Log.d(TAG, response.toString());
			
			if(response.ok()) {
				
				user.setUsername(username.getText().toString());
				user.setToken(response.token);
				
				if(response.matches != null) {
					for(Match m : response.matches) {
						model.createOrUpdateMatch(m);
					}
				}

				Log.d(TAG, model.getUser().toString());
				
				mListener.onLogin(true);
				return;
			}
		} else {
			Toast.makeText(getActivity(), "Network error.", Toast.LENGTH_LONG).show();
			user.setUsername(null);
			user.setToken( null);
		}
		
		btnLogin.setEnabled(true);	
	}
    
	@AfterInject
	public void afterInjection() {
		//subvert a bug in HttpUrlConnection
		//see: http://www.sapandiwakar.in/technical/eofexception-with-spring-rest-template-android/
		restClient.getRestTemplate().setRequestFactory(
				new HttpComponentsClientHttpRequestFactory());
	}

}
