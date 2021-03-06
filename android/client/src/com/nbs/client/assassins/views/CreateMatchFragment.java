package com.nbs.client.assassins.views;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.text.format.Time;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.maps.model.LatLng;
import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.TextChange;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.rest.RestService;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.controllers.CustomizeMatchActivity;
import com.nbs.client.assassins.controllers.MatchBoundsActivity;
import com.nbs.client.assassins.models.App;
import com.nbs.client.assassins.models.Match;
import com.nbs.client.assassins.models.Repository;
import com.nbs.client.assassins.models.User;
import com.nbs.client.assassins.network.CreateMatchRequest;
import com.nbs.client.assassins.network.HuntedRestClient;
import com.nbs.client.assassins.network.MatchResponse;
import com.nbs.client.assassins.utils.LocationUtils;
import com.nbs.client.assassins.views.DatePickerDialogFragment.OnDatePickedListener;
import com.nbs.client.assassins.views.TimePickerDialogFragment.OnTimePickedListener;

@EFragment(R.layout.create_match_fragment)
public class CreateMatchFragment extends SherlockFragment 
	implements OnDatePickedListener, OnTimePickedListener {

	private static final String TAG = "CreateMatchFragment";
	
    // Container Activity must implement this interface
    public interface OnMatchCreatedListener {
        public void onMatchCreated(boolean wasCreated);
    }

	private static final int MIN_PASSWORD_LEN = 6;
	private static final int MIN_MATCH_NAME_LEN = 6;

	private static final int GAMEPLAY_ACTIVITY_REQUEST = 0;
	private static final int BOUNDS_ACTIVITY_REQUEST = 1;

	public OnMatchCreatedListener mListener;
	@ViewById(R.id.create_match)
	public Button btnCreate;
	@ViewById(R.id.edit_match_name)
	public EditText matchName;
	@ViewById(R.id.manual_start)
	public CheckBox manualStart;
	@ViewById(R.id.join_on_create)
	public CheckBox joinOnCreate;
	@ViewById(R.id.edit_match_password)
	public EditText password;
	@ViewById(R.id.boundaries)
	public Button selectBoundaries;
	@ViewById(R.id.gameplay)
	public Button gameplaySettings;
	@ViewById(R.id.start_date)
	public Button startDate;
	@ViewById(R.id.start_time)
	public Button startTime;
	
	@RestService
	public HuntedRestClient restClient;
	
	public Integer minute;
	public Integer hourOfDay;
	public Integer monthDay;
	public Integer month;
	public Integer year;
	public LatLng nwCorner;
	public LatLng seCorner;
	public Double aRange;
	public Double hRange;
	public Integer tEscape;
	//@ViewById(R.id.join_when_create_match)
	//Switch join;

	//match parameters

	public CreateMatchFragment() {}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnMatchCreatedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnMatchCreatedListener");
        }
    }

	@TextChange({R.id.edit_match_name, R.id.edit_match_password})
	void onTextChangesOnSomeTextViews(TextView tv, CharSequence text) {	
	 
	}
	
	@Click(R.id.manual_start)
	void onManualStartChanged() {
		boolean isManualStart = manualStart.isChecked();
		int textColor = isManualStart ? R.color.gray : R.color.black;
		startTime.setEnabled(!isManualStart); startTime.setTextColor(textColor);
		startDate.setEnabled(!isManualStart); startDate.setTextColor(textColor);
	}
	
	@Click(R.id.boundaries)
	void onSelectBoundariesClicked() {
		Log.i(TAG, "onSelectBoundariesClicked()");
		
		hideKeyboard();
		
		Intent boundariesIntent = new Intent(getSherlockActivity(), MatchBoundsActivity.class);
        startActivityForResult(boundariesIntent, BOUNDS_ACTIVITY_REQUEST);
	}
	
	@Click(R.id.gameplay)
	void onGameplaySettingsClicked() {
		Log.i(TAG, "onGameplaySettingsClicked()");
		
		hideKeyboard();
		
		Intent intent = new Intent(getSherlockActivity(), CustomizeMatchActivity.class);
        startActivityForResult(intent, GAMEPLAY_ACTIVITY_REQUEST);
	}
	
	@Click(R.id.start_date)
	void onStartDateClicked() {
		Log.i(TAG, "onStartDateClicked()");
		
		hideKeyboard();
		
	    DialogFragment newFragment = 
	    		DatePickerDialogFragment.newInstance("Start Date");
	    ((DatePickerDialogFragment)newFragment).setOnDatePickedListener(this);
	    newFragment.show(getFragmentManager(), "start date dialog");
	}
	
	@Click(R.id.start_time)
	void onStartTimeClicked() {
		Log.i(TAG, "onStartTimeClicked()");
		
		hideKeyboard();
		
	    DialogFragment newFragment = 
	    		TimePickerDialogFragment.newInstance("Start Time");
	    ((TimePickerDialogFragment)newFragment).setOnTimePickedListener(this);
	    newFragment.show(getFragmentManager(), "start time dialog");
	}
	
	@Click(R.id.create_match)
	void onCreateMatchClicked() {

		//TODO: validate name and password before allowing button to be enabled
		//TODO: show visual indication if there are validation issues
		Log.i(TAG, password.getText().toString());
		
		if(hasValidSettings()) {
			InputMethodManager imm = (InputMethodManager)getSherlockActivity().getSystemService(
				      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
			btnCreate.setEnabled(false);
			
			//startTime remains null if manualStart is checked
			Time startTime = null;
			
			if(!manualStart.isChecked()) {
				startTime = new Time();
				startTime.set(0, minute, hourOfDay, monthDay, month, year);
			}
			String passwordStr = password.getText().toString();
			
			Repository model = App.getRepo();
			User user = model.getUser();
			CreateMatchRequest request = 
				new CreateMatchRequest(user.getToken(),
					new Match(matchName.getText().toString(), 
						passwordStr.length() >= MIN_PASSWORD_LEN ? passwordStr : null, 
					    user.getUsername(),
						((startTime != null) ? startTime.toMillis(false) : null), 
						nwCorner, seCorner, 
						aRange, hRange, tEscape));			
			
			request.joinOnCreate = joinOnCreate.isChecked();
			
			Log.i(TAG, "Creating Match " + request.toString());
			
			createMatchInBackground(request,
				ProgressDialog.show(getActivity(), "Please Wait", "Creating match...", true, false));
		}
	}

	private boolean hasValidSettings() {
		String passwordStr = password.getText().toString();
		String  validationMsg = "";
		
		if(passwordStr.length() < MIN_PASSWORD_LEN && passwordStr.length() != 0) {
			validationMsg += "Password at least " + MIN_PASSWORD_LEN + " chars ";
		}
		
		if(matchName.getText().toString().length() < MIN_MATCH_NAME_LEN) {
			validationMsg += "Match name at least " + MIN_MATCH_NAME_LEN +" chars "; 
		}
		
		if((minute == null || month == null) && !manualStart.isChecked()) {
			validationMsg += "Choose a start time or select manual start "; 
		}

		if(nwCorner == null) {
			validationMsg += "Choose boundaries ";
		}
		
		if(aRange == null) {
			validationMsg += "Set Gameplay settings";
		}
		
		boolean isValid = (validationMsg == "");
		
		if(!isValid) {
			Toast.makeText(getActivity(), validationMsg, Toast.LENGTH_LONG).show();
		}
		
		return isValid;
	}
	
	@Background
	void createMatchInBackground(CreateMatchRequest msg, ProgressDialog progress) {
		
		MatchResponse response = null;
		
		try {	
			response = restClient.createMatch(msg);		
		}
		catch(Exception e) {
			Log.i(TAG, "EXCEPTION: " + e.toString());
		}
		
		matchCreatedResult(response, progress);
	}
	
	@UiThread
	void matchCreatedResult(MatchResponse response, ProgressDialog progress) {
		progress.dismiss();
		if(response != null) {
			Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
			
			Log.d(TAG, response.toString());
			
			if(response.ok() && joinOnCreate.isChecked()) {
				Repository model = App.getRepo();
				model.createOrUpdateMatch(response.match);
				Log.d(TAG, "player from match: " + model.getMyPlayer(response.match.id));
				
			}
			
			mListener.onMatchCreated(response.ok());
			btnCreate.setEnabled(!response.ok());
			
		} else {
			btnCreate.setEnabled(true);	
			Toast.makeText(getActivity(), "Network error.", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult(result:"+resultCode+")");
		if(resultCode == Activity.RESULT_OK) {
			if(requestCode == BOUNDS_ACTIVITY_REQUEST) {
				String description = data.getStringExtra("description");
				Parcelable[] points = data.getParcelableArrayExtra("points");
				LatLng[] latLngPoints = new LatLng[points.length];
				
				for(int i = 0; i < points.length; i++) {
					latLngPoints[i] = ((LatLng)points[i]);
				}
				
				nwCorner = latLngPoints[0];
				seCorner = latLngPoints[1];
				
				Log.d(TAG, "Bounds [nw:" + nwCorner + "] [se:" +  seCorner + "]");
				
				String text = LocationUtils.getMilesAreaString(latLngPoints[0], latLngPoints[1]) +
						      ((description != null) ? " (" + description + ")" : "");
				selectBoundaries.setText(text);
				selectBoundaries.setTextColor(getResources().getColor(R.color.abs__bright_foreground_holo_light));
			} else if (requestCode == GAMEPLAY_ACTIVITY_REQUEST) {
				tEscape = data.getIntExtra("wait", -1);
				aRange = data.getDoubleExtra("aRange",-1.0d);
				hRange = data.getDoubleExtra("hRange",-1.0d);
				
				gameplaySettings.setText("hunt: "   + hRange + "mi, " +
										 "attack: " + aRange + "mi, " +
										 tEscape   + "s" );
				gameplaySettings.setTextColor(getResources()
						.getColor(R.color.abs__bright_foreground_holo_light));
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void hideKeyboard() {
		InputMethodManager inputManager = (InputMethodManager)
                getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 

		inputManager.hideSoftInputFromWindow(getSherlockActivity().getCurrentFocus().getWindowToken(),
		                   InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	public void onDatePicked(int year, int monthOfYear, int dayOfMonth) {
		startDate.setText(TimeUtils.formatDate(year, monthOfYear, dayOfMonth));
		startDate.setTextColor(getResources()
				.getColor(R.color.abs__bright_foreground_holo_light));
		this.year = year;
		this.monthDay = dayOfMonth;
		this.month = monthOfYear;
	}


	@Override
	public void onTimePicked(int hourOfDay, int minute) {
		startTime.setText(TimeUtils.formatTime(hourOfDay, minute));
		startTime.setTextColor(getResources()
				.getColor(R.color.abs__bright_foreground_holo_light));
		this.hourOfDay = hourOfDay;
		this.minute = minute;
	}


	
	@AfterInject
	public void afterInjection() {
		//subvert a bug in HttpUrlConnection
		//see: http://www.sapandiwakar.in/technical/eofexception-with-spring-rest-template-android/
		restClient.getRestTemplate().setRequestFactory(
				new HttpComponentsClientHttpRequestFactory());
	}
	
}
