package assassins.ui.prototypes;

import java.util.ArrayList;

import org.bson.types.ObjectId;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SecretConspiracyAccess extends Activity {
	
	private static final String TAG = "SecretConspiracyAccess";

	private Button viewGame;
	
	private EditText name;
	private EditText password;
	private TextView conspiracySummary;
	private ObjectId profileId;
	private String installId;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
     setContentView(R.layout.secret_conspiracy);
     
     String objIdStr = getIntent().getStringExtra("profileId");
     profileId = ObjectId.massageToObjectId(objIdStr);
     
     name = (EditText) findViewById(R.id.private_name);
     password = (EditText) findViewById(R.id.private_name);
     conspiracySummary = (TextView) findViewById(R.id.conspiracySummary);
     
     
     viewGame = (Button)findViewById(R.id.viewPrivateGames);
     viewGame.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View arg0) {
	    		    		
	    		if(!name.getText().equals("") && 
	    		   !password.getText().equals(""))
	    		{
		    		viewGame.setEnabled(false);
		    		AsyncTaskFindSecretMatch asyncFindTask = 
		    				new AsyncTaskFindSecretMatch(SecretConspiracyAccess.this, 
		    						name.getText().toString(), password.getText().toString());
		    		asyncFindTask.execute();
	    		}
	    	}

	    });
     
	}
	
	
	private void showMatchView(Conspiracy secretMatch)
	{
		Bundle bundle = new Bundle();
		bundle.putString("profileId", profileId.toString());
		bundle.putParcelable("match", secretMatch);
		Intent i = new Intent(SecretConspiracyAccess.this, MatchInfoActivity.class);
		i.putExtras(bundle);
        this.startActivity(i);	
	}
	
	private class AsyncTaskFindSecretMatch extends AsyncTask<Void, Integer, Conspiracy>
	{

		ProgressDialog progressDialog;	
		String matchName;
		String password;
		
		public AsyncTaskFindSecretMatch(Activity secretConspiracyActivity, String matchName, String password)
		{
			progressDialog = new ProgressDialog(secretConspiracyActivity);
			this.matchName = matchName;
			this.password = password;
		}
		
		
		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Conspiracy secretMatch) {
			// TODO Auto-generated method stub
			if(secretMatch != null)
			{
				if(progressDialog.isShowing())
					progressDialog.setMessage("Found secret match.");
				
				showMatchView(secretMatch);
			}
			else
			{
				if(progressDialog.isShowing())
					progressDialog.setMessage("Could not find match.");
			}
			
			if(progressDialog.isShowing())
				progressDialog.dismiss();
			viewGame.setEnabled(true);
			super.onPostExecute(secretMatch);
		}

		@Override
		protected void onPreExecute() {
			progressDialog.setMessage("Finding secret match...");
			progressDialog.show();
			
			super.onPreExecute();
		}

		@Override
		protected Conspiracy doInBackground(Void... params) {
			AssassinsHttpClient service = new AssassinsHttpClient(getString(R.string.serviceUrl), SecretConspiracyAccess.this);
    		
			Conspiracy secretMatch = null;
			
			try {
				
				secretMatch = service.getSecretMatch(profileId, matchName, password);
				
    		} catch (AssassinsHttpException e) {
				
				Log.d("Project Assassins", "AsyncTaskJoinMatch.doInBackground() caught expection: " + 
											e.getMessage(), e.getCause());
			}
			
			return secretMatch;
		}
		
	}
}
