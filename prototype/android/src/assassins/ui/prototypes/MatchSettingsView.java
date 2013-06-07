package assassins.ui.prototypes;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.bson.types.ObjectId;

import com.google.android.maps.GeoPoint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;


/**
 * A list view example where the 
 * data for the list comes from an array of strings.
 */
public class MatchSettingsView extends ListActivity {
	
	
	private ArrayList<MatchSettingsItem> _settings;
	private SettingsAdapter _settingsAdapter;	
	private Conspiracy newGame;
	
	public static final int BOUNDS_ACTIVITY_ID = 0;
	protected static final float FEET_PER_MILE = 5280;
	
	public final int PRIVATE=0, 
					PUBLIC=1;
	
	private final int SETTINGS_NUM = 8;
	public final int NAME=0,
					PRIVACY=1,
					PASSWORD=2,
					MATCHTYPE=3,
					MATCHTYPE_SETTINGS=4,
					BOUNDS=5,
					START=6,
					INCOMPLETE=7;
	
	private final int 	ASSASSINS=0,
						BOUNTY=1,
						SCOURGE=2,
						THIEVES=3;

	private int startYear;
    private int startMonth;
    private int startDay;
    private int startHour;
    private int startMin;
    
    private DatePickerDialog.OnDateSetListener mStartDateSetListener = new DatePickerDialog.OnDateSetListener() {
    	// onDateSet method
    	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
    		
    		//save these values off temporarily until a definite time is selected, then add them to the new game
    		startYear = year;
	        startMonth = monthOfYear;
	        startDay = dayOfMonth;
	        TimePickerDialog startTime = new TimePickerDialog(MatchSettingsView.this, mStartTimeSetListener, startHour, startMin, false);
	        startTime.show();
	        
    	}
	};
	
	private TimePickerDialog.OnTimeSetListener mStartTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
    	// onDateSet method

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			
			Calendar start = new GregorianCalendar(startYear, startMonth, startDay, hourOfDay, minute);
			newGame.setStartTime(start);
	        
	        //update the list view detail string
	        touchedSetting.setDetail(newGame.getStart().getTime().toLocaleString());
	        _settingsAdapter.notifyDataSetChanged();
		}
	};
    

	private boolean[] isSet;
	
	Button done;
	Button delete;

	MatchSettingsItem touchedSetting;
	private ObjectId profileId;
	
	AsyncTaskCreateMatch createMatchTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_match_settings);

	     String objIdStr = getIntent().getStringExtra("profileId");
	     profileId = ObjectId.massageToObjectId(objIdStr); 
	     
        
        
        // get the current date/time
        final Calendar c = Calendar.getInstance();
        startYear = c.get(Calendar.YEAR);
        startMonth = c.get(Calendar.MONTH);
        startDay = c.get(Calendar.DAY_OF_MONTH);
        startHour = c.get(Calendar.HOUR);
        startMin  = c.get(Calendar.AM_PM); //Fix Me!  Is there a problem here?
        
        touchedSetting = null;
        isSet = new boolean[SETTINGS_NUM];
        newGame = new Conspiracy();
        
        _settings = new ArrayList<MatchSettingsItem>();
        
        MatchSettingsItem cName = new MatchSettingsItem("Name", "set conspiracy name");
        _settings.add(cName);
        MatchSettingsItem cPrivacy = new MatchSettingsItem("Privacy", "private");
        _settings.add(cPrivacy);
        MatchSettingsItem cPassword = new MatchSettingsItem("Password", "set password");
        _settings.add(cPassword);
        MatchSettingsItem cMatchType = new MatchSettingsItem("Match Type", "Assassins");
        _settings.add(cMatchType );
        MatchSettingsItem cMatchTypeSettings = new MatchSettingsItem("Settings", "Adjust game parameters");
        _settings.add(cMatchTypeSettings );
        MatchSettingsItem cBounds = new MatchSettingsItem("Boundaries", "select boundaries");
        _settings.add(cBounds);
        MatchSettingsItem cStartTime = new MatchSettingsItem("Start Time", "set start time");
        _settings.add(cStartTime);
        _settingsAdapter = new SettingsAdapter(this,
                R.layout.settings_item, _settings);
        
        setListAdapter(_settingsAdapter);
        getListView().setTextFilterEnabled(true);
        
    	done = (Button)findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View arg0) {
        		

        		if(newGame.isComplete())
        		{
        			createMatchTask = new AsyncTaskCreateMatch(MatchSettingsView.this);
        			createMatchTask.execute();
        		}
        		else
        		{
        			Dialog dialog = onCreateDialog(INCOMPLETE);
        			 if(dialog != null)
        	    		dialog.show(); 
        		}
        		
        	}
        });

    }
    
    private class AsyncTaskCreateMatch extends AsyncTask<Void, Integer, Boolean>
    {
    	ProgressDialog progressDialog;
    	
    	public AsyncTaskCreateMatch(Activity matchCreationActivity)
    	{
    		progressDialog = new ProgressDialog(matchCreationActivity);
    	}
		
    	
    	@Override
		protected void onPreExecute() {
			progressDialog.setMessage("Creating Match...");
			progressDialog.show();
			super.onPreExecute();
		}
		
    	@Override
		protected void onPostExecute(Boolean createdMatch) {

    		Toast resultToast = null;
    		
    		if(createdMatch)
    		{
				Log.d("Project Assassins", "successfully created match");
				resultToast = Toast.makeText(getApplicationContext(), 
								"Created match: "+newGame.getName(), Toast.LENGTH_SHORT);
			
    		}
    		else
    		{
    			Log.d("Project Assassins", "Failed to create match");
    			resultToast = Toast.makeText(getApplicationContext(), 
    							"Failed to create match.", Toast.LENGTH_SHORT);
				
    		}
    		
    		if(progressDialog.isShowing())
				progressDialog.dismiss();
    		
    		resultToast.show();
    		
			super.onPostExecute(createdMatch);
			
			finishMatch();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			AssassinsHttpClient service = new AssassinsHttpClient(getString(R.string.serviceUrl), MatchSettingsView.this);
			
			boolean success = true;
			
			try {
				service.createMatch(newGame, profileId);
			}
			catch(AssassinsHttpException e) {
				success = false;
				Log.d("Project Assassins", 
						"caught exception in AsyncTaskCreateMatch.doInBackground(): " + 
								e.getMessage(), e.getCause());
				
			}
			
			
			return success;
		}
    	
    }
    
    private void finishMatch()
    {
		//send Conspiracy data back 
		Intent resultIntent = new Intent();
		Bundle b = new Bundle();
        
		//put parcel in bundle
		b.putParcelable("game", newGame);
        b.putString("profileId", profileId.toString());
		//put bundle in intent
		resultIntent.putExtras(b);
        
		//pass intent back to the previous activity
		setResult(Activity.RESULT_OK, resultIntent);
		finish(); 
    }
    
    protected void onListItemClick (ListView l, View v, int position, long id)
    {
    	touchedSetting = (MatchSettingsItem) getListView().getItemAtPosition(position);
    	Dialog dialog = onCreateDialog(position);
    	if(dialog != null)
    		dialog.show(); 	
    	isSet[position] = true;
    }
    
    @Override
	protected Dialog onCreateDialog(int id) {
		
    	switch(id)
    	{
    		case NAME:
    			//display a text input alert
	   			 LayoutInflater factory = LayoutInflater.from(this);
	   	            final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
	   	            final EditText matchNameText = (EditText) textEntryView .findViewById(R.id.username_edit);
	   	            return new AlertDialog.Builder(this)
   	                .setTitle("Set Match Name")
   	                .setView(textEntryView)
   	                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
   	                    public void onClick(DialogInterface dialog, int whichButton) {
   	                    	newGame.setName(matchNameText.getText().toString());
   	                    	touchedSetting.setDetail(newGame.getName());
   	                    	_settingsAdapter.notifyDataSetChanged();
   	                        
   	                    }
   	                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
   	                    public void onClick(DialogInterface dialog, int whichButton) {

   	                        /* User clicked cancel so do some stuff */
   	                    }
   	                }).create();
	   	            
    		case PRIVACY:
    			//display an alert with two privacy options
    			return new AlertDialog.Builder(this)
				.setTitle("Set Match Privacy")
				.setSingleChoiceItems(R.array.privacy_array, newGame.getPrivacy(), new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int id) {
			    
			      switch(id)
			      {
			      	case PRIVATE:
			      		touchedSetting.setDetail("private");
			      		newGame.setPublic(false);
		      		break;
			      	case PUBLIC:
			      		touchedSetting.setDetail("public");
			      		newGame.setPublic(true);
		      		break;
			      	default:
			      	break;
			      }
			    //password is only accessible if game is private - so disable password if public
			      if(newGame.isPublic())
			    	  setPasswordEnabled(false);
			      else
			    	  setPasswordEnabled(true);
			      
			    //update the listview's strings with new data
			      _settingsAdapter.notifyDataSetChanged();
			      dialog.dismiss();
			    }
				}).create();
    		

    		case PASSWORD:
    			//only allow setting password if match is private
    			if(!newGame.isPublic())
    			{
    				//display a password input and confirm alert
    				LayoutInflater passwordFactory = LayoutInflater.from(this);
    	            final View passwordView = passwordFactory.inflate(R.layout.alert_dialog_password_entry, null);
    	            final EditText passwordText = (EditText) passwordView .findViewById(R.id.password_edit);
    	            return new AlertDialog.Builder(this)
    	                .setTitle("Create a Password")
    	                .setView(passwordView)
    	                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    	                    public void onClick(DialogInterface dialog, int whichButton) {

    	                    	newGame.setPassword(passwordText.getText().toString());
       	                    	touchedSetting.setDetail(newGame.getPassword());
       	                    	_settingsAdapter.notifyDataSetChanged();
    	                    }
    	                })
    	                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	                    public void onClick(DialogInterface dialog, int whichButton) {

    	                        /* User clicked cancel so do some stuff */
    	                    }
    	                })
    	                .create();
    			}
    		break;
    		case MATCHTYPE:
    			//display an alert with match types
    			return new AlertDialog.Builder(this)
				.setTitle("Select a Match Type")
				.setSingleChoiceItems(R.array.matchtype_array, newGame.getTypeInt(), new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int id) {
			    
			      switch(id)
			      {
			      	case ASSASSINS:
			      		touchedSetting.setDetail("Assassins");
			      		newGame.setType(MatchType.ASSASSINS);
		      		break;
			      	case BOUNTY:
			      		touchedSetting.setDetail("Bounty");
			      		newGame.setType(MatchType.BOUNTY);
		      		break;
			      	case SCOURGE:
			      		touchedSetting.setDetail("Scourge");
			      		newGame.setType(MatchType.SCOURGE);
		      		break;
			      	case THIEVES:
			      		touchedSetting.setDetail("Thieves");
			      		newGame.setType(MatchType.THIEVES);
		      		break;
			      	default:
			      	break;
			      }
			      //update the listview's strings with new data
			      _settingsAdapter.notifyDataSetChanged();
			      dialog.dismiss();
			    }
				}).create();
    		case MATCHTYPE_SETTINGS:

                GameplayParamsDialog paramsDlg = new GameplayParamsDialog(this);

                ((AlertDialog)paramsDlg)
                	.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    	float huntRange = (float)((GameplayParamsDialog)dialog).getHuntRangeVal();
                    	float attackRange = ((GameplayParamsDialog)dialog).getAttackRangeVal();
                    	int attackDelay   = ((GameplayParamsDialog)dialog).getDelayAfterAttackVal();

                    	newGame.setHuntRange(huntRange);
                    	newGame.setAttackRange(attackRange);
                    	newGame.setAttackDelay(attackDelay);
                    	
                    	touchedSetting.setDetail(newGame.getSettingsString());
                    	_settingsAdapter.notifyDataSetChanged();

                    	
                    }
                });
                ((AlertDialog)paramsDlg)
                	.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	
                    }
                });
                 return paramsDlg;
          
    		case BOUNDS:
    			//display a webview with a callback to store bounds
    			Intent i = new Intent(MatchSettingsView.this, MatchBoundariesView.class);
    			startActivityForResult(i,BOUNDS_ACTIVITY_ID);
			break;
    		case START:
    			
    			//display an alert with a date and time picker
    			return new DatePickerDialog(this,  mStartDateSetListener,  startYear, startMonth, startDay);
    		case INCOMPLETE:	
    			return new AlertDialog.Builder(this).setTitle("Could not Finish Match").setMessage(newGame.getNotCompletedString())
    					.setCancelable(false)
    			       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			                dialog.dismiss();
    			           }
    			       }).create();
    		
    		default:
			break;
    	}
    	return null;
    }

	protected void setPasswordEnabled(boolean enabled) {
		// use a selector or alternate layout to simulate enable/disable functionality 
		// for the password setting list item
		//((View) getListView().getItemAtPosition(PASSWORD)).setEnabled(enabled);
		
	}
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
	  super.onActivityResult(requestCode, resultCode, data); 
	  switch(requestCode) { 
	    case BOUNDS_ACTIVITY_ID : { 
	      if (resultCode == Activity.RESULT_OK) { 
	    	  int[] geoCoords = data.getIntArrayExtra("geoCoords");
	    	  GeoPoint upperLeft = new GeoPoint(geoCoords[0], geoCoords[1]);
	    	  GeoPoint lowerRight = new GeoPoint(geoCoords[2], geoCoords[3]);
	    	  newGame.setCorners(upperLeft, lowerRight);
	    	  double x = ( 69.1 * ((upperLeft.getLatitudeE6()/1E6) - (lowerRight.getLatitudeE6()/1E6) )); 
	    	  double y = ( 53   * ((upperLeft.getLatitudeE6()/1E6) - (lowerRight.getLatitudeE6()/1E6) ));
	    	  touchedSetting.setDetail(round(x)+ " miles by "+ round(y) + " miles");
	    	  _settingsAdapter.notifyDataSetChanged();
	      } 
	      break; 
	    } 
	  } 
	}
    
	double round(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
    return Double.valueOf(twoDForm.format(d));
}
    
    
}



//sending an e-mail from the app
/*
Intent intent = null;
intent = new Intent(Intent.ACTION_SEND);
intent.setType("plain/text");
intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "jerome.locson@gmail.com" });
startActivity(Intent.createChooser(intent, ""));
*/