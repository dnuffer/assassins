package assassins.ui.prototypes;

import org.bson.types.ObjectId;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ProjectAssassins extends Activity {
	
	private static final int IMPORT_ALERT = 1;
	private static final int DIALOG_LOGIN_ENTRY = 2;
	private static final int CREATE_PROFILE_ALERT = 3;
	
	private Profile myProfile;
	private String installId;
	private ObjectId profileId;
	private Context context;
	
	private Button a;
	private Button b;
	private Button c;
	private Button d;
	
	

	public static final String PREFS_NAME = "MyPrefsFile";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		
	    String objIdStr = getIntent().getStringExtra("profileId");
	    profileId = ObjectId.massageToObjectId(objIdStr); 
	  
		context = this.getApplicationContext();
		installId = Installation.id(this);
		
		a = (Button)findViewById(R.id.assassinate_button);
		a.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Bundle b = new Bundle();
				b.putString("profileId", profileId.toString());
				Intent i = new Intent(ProjectAssassins.this, SearchMode.class);
				i.putExtras(b);
				startActivity(i);
			}
		});

		b = (Button)findViewById(R.id.conspire_button);
		b.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Bundle b = new Bundle();
				b.putString("profileId", profileId.toString());
				Intent i = new Intent(ProjectAssassins.this, ConspireMenu.class);
				i.putExtras(b);
				startActivity(i);
			}
		});

		c = (Button)findViewById(R.id.honors_button);
		c.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Bundle b = new Bundle();
				b.putString("profileId", profileId.toString());
				Intent i = new Intent(ProjectAssassins.this, HonorsMenu.class);
				i.putExtras(b);
				startActivity(i);
			}
		});

		d = (Button)findViewById(R.id.book_of_hassan);
		d.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Intent i = new Intent(ProjectAssassins.this, BookOfHassanMenu.class);
				startActivity(i);
			} 
		});

    }
    
	@Override
	public void onResume() {
		super.onResume();
		
		Log.d("Project Assassins", "ProjectAssassins main menu onResume() called");
		
		// Restore preferences
	    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    if (settings.contains("profileId")) {
	    	profileId = ObjectId.massageToObjectId(settings.getString("profileId", ""));
	    }

	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		Log.d("Project Assassins", "ProjectAssassins main menu onPaused() called");
		
		if(profileId != null)
		{	
			// We need an Editor object to make preference changes.
		    // All objects are from android.context.Context
		    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		    SharedPreferences.Editor editor = settings.edit();
		    editor.putString("profileId", profileId.toString());
		
		    // Commit the edits!
		    editor.commit();
		}
	}
    
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		
		case IMPORT_ALERT:
            return new AlertDialog.Builder(ProjectAssassins.this)
            	//.setIconAttribute(android.R.attr.alertDialogIcon)
            	.setTitle(R.string.create_new_import_alert_title)
            	.setPositiveButton(R.string.create_profile_string, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    	 Dialog createUserWindwow = onCreateDialog(CREATE_PROFILE_ALERT);
                    	 createUserWindwow.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    	 createUserWindwow.show();
                    	
                    }  
                })/*
            	.setNegativeButton(R.string.import_profile_string, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    	//Tell the user to login
                        //loginDialogue();
                        Dialog loginWindow = onCreateDialog(DIALOG_LOGIN_ENTRY);
                        loginWindow.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                        loginWindow.show();
                    	
                    }  
                })
                .setCancelable(false)*/
            	.create();
			
        case DIALOG_LOGIN_ENTRY:
            // This example shows how to add a custom layout to an AlertDialog
            LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.alert_dialog_login_entry, null);
            return new AlertDialog.Builder(this)
                //.setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.alert_dialog_login_entry)
                .setView(textEntryView)
                .setPositiveButton(R.string.alert_dialog_submit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	
                    	final EditText username = (EditText) textEntryView.findViewById(R.id.usernameText);
                    	final EditText password = (EditText) textEntryView.findViewById(R.id.passwordText);
                    	String usernameString = username.getText().toString();
                    	String passwordString = password.getText().toString();
                    	String value = "Username: " + usernameString + "\nPassword: " + passwordString;
                    	Toast.makeText(getApplicationContext(), value, Toast.LENGTH_SHORT).show();
                    	
                    }
                })/*
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    	Toast.makeText(getApplicationContext(), "No Login Info Entered", Toast.LENGTH_SHORT).show();
                    	
                    	
                    }
                })*/
                .create();
            
        case CREATE_PROFILE_ALERT:
            // This example shows how to add a custom layout to an AlertDialog
            LayoutInflater factory2 = LayoutInflater.from(this);
            final View createProfileView = factory2.inflate(R.layout.create_profile_window, null);
            return new AlertDialog.Builder(this)
                //.setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.alert_dialog_login_create_profile)
                .setView(createProfileView)
                .setPositiveButton(R.string.alert_dialog_submit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	
                       	//extract profile info from text boxes
                    	final EditText username = (EditText) (createProfileView.findViewById(R.id.usernameText));
                    	final EditText password = (EditText) (createProfileView.findViewById(R.id.passwordText));
                    	final EditText name = (EditText) (createProfileView.findViewById(R.id.nameText));
                    	final EditText email = (EditText) (createProfileView.findViewById(R.id.emailText));
                    	final EditText confirmPass = (EditText) (createProfileView.findViewById(R.id.confirmText));
                    	
                    	String usernameStr = username.getText().toString();
                    	String passwordStr = password.getText().toString();
                    	String nameStr = name.getText().toString();
                    	String emailStr = email.getText().toString();
                    	String confirmPassStr = confirmPass.getText().toString();
                    	Log.d("Project Assassins", usernameStr+" "+passwordStr+" "+nameStr+" "+emailStr+" "+confirmPassStr);

                		//put new player profile on server
                		Profile newPlayer = new Profile(nameStr, usernameStr, passwordStr, emailStr);
                		
                		AsyncTaskCreateProfile createProfile = new AsyncTaskCreateProfile(newPlayer, ProjectAssassins.this);
                		createProfile.execute();
                    	//String value = "Username: " + usernameStr + "\nPassword: " + passwordStr;
                    	//Toast.makeText(getApplicationContext(), value, Toast.LENGTH_SHORT).show();       	
                    }
                })/*
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    	Toast.makeText(getApplicationContext(), "Invalid Profile", Toast.LENGTH_SHORT).show();
                    	
                    }
                })*/
                .create();
            
        }
        return null;
	}
	
	public void handleCreateProfileResponse(Profile result) {

		myProfile = result;
		
		if(myProfile == null)
		{
			a.setEnabled(false);
			b.setEnabled(false);
			c.setEnabled(false);
			d.setEnabled(false);
			Dialog createProfileDialog = onCreateDialog(CREATE_PROFILE_ALERT);
			createProfileDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			createProfileDialog.show();
		}
		else
		{
			if(myProfile.getCurrentMatch() != null)
			{
				a.setEnabled(true);
			}
			else
			{
				a.setEnabled(false);
			}
			
			b.setEnabled(true);
			c.setEnabled(true);
			d.setEnabled(true);
		}

	}

	public void handleAuthenticationResponse(Profile result) {
		
		if(result != null)
		{
			myProfile = result;
			
			if(myProfile.getCurrentMatch() != null)
			{
				a.setEnabled(true);
			}
			else
			{
				a.setEnabled(false);
			}
			
			b.setEnabled(true);
			c.setEnabled(true);
			d.setEnabled(true);
			
		}
		else if(myProfile == null)
		{
			Log.d("Project Assassins", "Failed to authenticate with server.");
			
			a.setEnabled(false);
			b.setEnabled(false);
			c.setEnabled(false);
			d.setEnabled(false);
			
			Dialog createProfileDialog = onCreateDialog(CREATE_PROFILE_ALERT);
			createProfileDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			createProfileDialog.show();
		}	
	}

	
	private class AsyncTaskCreateProfile extends AsyncTask<Void, Integer, Profile>
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
		protected void onPostExecute(Profile result) {
			
			if(dialog.isShowing())
				dialog.dismiss();
			
			handleCreateProfileResponse(result);
			
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
		protected Profile doInBackground(Void... params) {
			
			AssassinsHttpClient webservice = new AssassinsHttpClient(activity.getString(R.string.serviceUrl), activity);

			Profile newProfile = null;
			
			try {
				newProfile = webservice.createProfile(profile);
				Log.d("Project Assassins", "Created profile: " + newProfile);


			}
			catch(AssassinsHttpException e) {

				if(e.getCause().getClass() != assassins.ui.prototypes.AssassinsHttpException.class)
				{
					Toast.makeText(activity, "There was a network problem.  Try again.", 2000).show();
				}	

				Log.d("Project Assassins", "exception class: "+e.getCause().getClass());
			}
			return newProfile;
		}
		
	}
	
	private class AsyncTaskAuthenticate extends AsyncTask<Void, Integer, Profile>
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
		protected void onPostExecute(Profile result) {
			
			if(dialog.isShowing())
				dialog.dismiss();
			
			handleAuthenticationResponse(result);
			
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(activity);
			dialog.setMessage("Authenticating with server...");
			dialog.show();
			
			super.onPreExecute();
		}

		@Override
		protected Profile doInBackground(Void... params) {
			AssassinsHttpClient webservice = new AssassinsHttpClient(activity.getString(R.string.serviceUrl), activity);
		    
			Profile newProfile = null;
			try {
				newProfile = webservice.authenticatePlayer(activity);
				
				//this.getResources().
				Log.d("Project Assassins", "authenticated installation id");
				Log.d("Project Assassins", "ProfileId: "+newProfile.getObjectId().toString());
				
				
			}
			catch(AssassinsHttpException e) {
				
				if(e.getCause().getClass() != assassins.ui.prototypes.AssassinsHttpException.class)
				{
					Toast.makeText(activity, "There was a network problem.  Try again.", 2000).show();
				}	

				Log.d("Project Assassins", "exception class: "+e.getCause().getClass());
				Log.d("Project Assassins", e.getMessage());
			}
			return newProfile;
		}
		
	}
	


}
