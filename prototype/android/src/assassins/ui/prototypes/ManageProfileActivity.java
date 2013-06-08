package assassins.ui.prototypes;

import org.bson.types.ObjectId;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ManageProfileActivity extends Activity {

	Button createProfile;
	Button retryAuthentication;
	AsyncTaskCreateProfile createProfileTask;
	AsyncTaskAuthenticate authenticateTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume() {

		authenticateTask = new AsyncTaskAuthenticate(this);
		authenticateTask.execute();
		
		super.onResume();
	}

	private void createProfile()
	{
       	//extract profile info from text boxes
    	final EditText username = (EditText) findViewById(R.id.usernameText);
    	final EditText password = (EditText) findViewById(R.id.passwordText);
    	final EditText name = (EditText) findViewById(R.id.nameText);
    	final EditText email = (EditText) findViewById(R.id.emailText);
    	final EditText confirmPass = (EditText) findViewById(R.id.confirmText);
    	
    	String usernameStr = username.getText().toString();
    	String passwordStr = password.getText().toString();
    	String nameStr = name.getText().toString();
    	String emailStr = email.getText().toString();
    	String confirmPassStr = confirmPass.getText().toString();
    	Log.d("Project Assassins", usernameStr+" "+passwordStr+" "+nameStr+" "+emailStr+" "+confirmPassStr);

		//put new player profile on server
		Profile newPlayer = new Profile(nameStr, usernameStr, passwordStr, emailStr);
		
		String installId = Installation.id(this);
		Log.d("Project Assassins", "installId: " + installId);
		newPlayer.setInstallId(installId);
		
		
		AsyncTaskCreateProfile createProfileTask = new AsyncTaskCreateProfile(newPlayer, this);
		createProfileTask.execute();

	}

	public void handleAuthenticationResult(OperationResult<Profile> result) {
		
		
		Profile resultProfile = result.getResult();
		Exception authError = result.getException();
		
		if(resultProfile != null)		
		{
			//success
			Bundle b = new Bundle();
			b.putString("profileId", resultProfile.getObjectId().toString());
			Intent i = new Intent(ManageProfileActivity.this, ProjectAssassins.class);
			i.putExtras(b);
			startActivity(i);
			finish();
		}	
		else if(authError != null && 
				authError.getCause() != null &&
				authError.getCause().getClass() != assassins.ui.prototypes.AssassinsHttpException.class)
		{
			Toast.makeText(this, "There was a network problem.  Try again.", 3000).show();
			setContentView(R.layout.login_network_error);
			retryAuthentication = (Button) findViewById(R.id.profileErrorRetryButton);
			retryAuthentication.setOnClickListener(new View.OnClickListener() {
				public void onClick(View theButton) {
					theButton.setEnabled(false);
					authenticateTask = new AsyncTaskAuthenticate(ManageProfileActivity.this);
					authenticateTask.execute();
				}
			});	
		}
		else
		{
			//no profile yet
			setContentView(R.layout.manage_profile_layout);
			createProfile = (Button) findViewById(R.id.createProfileButton);
			createProfile.setOnClickListener(new View.OnClickListener() {
				public void onClick(View theButton) {
					theButton.setEnabled(false);
					createProfile();
				}
			});
		}
	}
	

	public void handleCreateProfileSuccess(Profile createdProfile) {

		//start up ProjectAssassins activity
		
		Log.d("Project Assassins", "handleCreateProfileSuccess() Profile: " + createdProfile);
		
		ObjectId profileId = createdProfile.getObjectId();
		
		Bundle b = new Bundle();
		b.putString("profileId", profileId.toString());
		Intent i = new Intent(ManageProfileActivity.this, ProjectAssassins.class);
		i.putExtras(b);
		startActivity(i);
		finish(); //clears this activity from the backstack

	}

	private class AsyncTaskCreateProfile extends AsyncTask<Void, Integer, OperationResult<Profile>>
	{
		Activity activity;
		ProgressDialog dialog;
		Profile profile;
			
		public AsyncTaskCreateProfile(Profile profile, Activity profileActivity)
		{
			activity = profileActivity;
			this.profile = profile;
		}
		
		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}
	
		@Override
		protected void onPostExecute(OperationResult<Profile> result) {
			
			if(dialog.isShowing())
				dialog.dismiss();
			
			Exception e = result.getException();
			
			if(result.getResult() != null)		
			{
				handleCreateProfileSuccess(result.getResult());
			}	
			else if(e != null && e.getCause().getClass() != assassins.ui.prototypes.AssassinsHttpException.class)
			{
				Toast.makeText(activity, "There was a network problem.  Try again.", 3000).show();
				createProfile.setEnabled(true);
			}
	
			
			super.onPostExecute(result);
		}
	
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(activity);
			dialog.setMessage("Creating profile...");
			dialog.show();
			
			super.onPreExecute();
		}
	
		@Override
		protected OperationResult<Profile> doInBackground(Void... params) {
			
			AssassinsHttpClient webservice = new AssassinsHttpClient(activity.getString(R.string.serviceUrl), activity);
			
			OperationResult<Profile> result = new OperationResult<Profile>();
			Profile newProfile = null;
			
			try {
				newProfile = webservice.createProfile(profile);
				Log.d("Project Assassins", "Created profile: " + newProfile);
	
			}
			catch(AssassinsHttpException e) {
	
				result.setException(e);
				//Log.d("Project Assassins", "exception class: "+e.getCause().getClass());
			}
			
			result.setResult(newProfile);
			
			return result;
		}
		
	}
	
	private class AsyncTaskAuthenticate extends AsyncTask<Void, Integer, OperationResult<Profile>>
	{
		Activity activity;
		ProgressDialog dialog;
		
		public AsyncTaskAuthenticate(Activity profileActivity)
		{
			activity = profileActivity;
			
		}
		
		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(OperationResult<Profile> result) {
			
			if(dialog.isShowing())
				dialog.dismiss();
			

			handleAuthenticationResult(result);

			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(activity);
			dialog.setMessage("Contacting assassin headquarters for any dirt we can find on you...");
			dialog.show();
			
			super.onPreExecute();
		}

		@Override
		protected OperationResult<Profile> doInBackground(Void... params) {
			
			AssassinsHttpClient webservice = new AssassinsHttpClient(activity.getString(R.string.serviceUrl), activity);
			
			OperationResult<Profile> result = new OperationResult<Profile>();
			Profile myProfile = null;
			
			try {
				myProfile = webservice.authenticatePlayer(activity);
				
				//this.getResources().
				Log.d("Project Assassins", "authenticated installation id");
				Log.d("Project Assassins", "ProfileId: "+myProfile.getObjectId().toString());
				
				
			}
			catch(AssassinsHttpException e) {
				
				result.setException(e);

				//Log.d("Project Assassins", "exception class: "+e.getCause().getClass());
			}			

			result.setResult(myProfile);

			return result;
		}
		
	}
	
}