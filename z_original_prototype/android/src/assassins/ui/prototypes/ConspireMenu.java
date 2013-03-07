package assassins.ui.prototypes;

import java.util.ArrayList;

import org.bson.types.ObjectId;

import com.google.android.maps.GeoPoint;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ConspireMenu extends Activity {
	protected static final int SETTINGS_ACTIVITY_ID = 0;

	private ObjectId profileId;
	
	public static final String PREFS_NAME = "MyPrefsFile";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	     setContentView(R.layout.conspire_menu);

	     String objIdStr = getIntent().getStringExtra("profileId");
	     profileId = ObjectId.massageToObjectId(objIdStr); 
	     
	     
	    Button startConspiracy = (Button)findViewById(R.id.startNewConspiracy);
	    startConspiracy.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View arg0) {
	    		Intent i = new Intent(ConspireMenu.this, MatchSettingsView.class);
				Bundle b = new Bundle();
				b.putString("profileId", profileId.toString());
				i.putExtras(b);
	    		startActivityForResult(i, SETTINGS_ACTIVITY_ID);
	    	}
	    });
	    
	    Button secretConspiracy = (Button)findViewById(R.id.accessSecretConspiracy);
	    secretConspiracy.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View arg0) {
				Bundle b = new Bundle();
				b.putString("profileId", profileId.toString());
				Intent i = new Intent(ConspireMenu.this, SecretConspiracyAccess.class);
				i.putExtras(b);
				startActivity(i);
	    	}
	    });
	    
	    Button publicConspiracies = (Button)findViewById(R.id.seePublicConspiracies);
	    publicConspiracies.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View arg0) {
	    		Intent i = new Intent(ConspireMenu.this, AvailableMatchesActivity.class);
				Bundle b = new Bundle();
				b.putString("profileId", profileId.toString());
				i.putExtras(b);
	    		startActivity(i);
	    	}
	    });
    
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		Log.d("Project Assassins", "ConspireMenu onResume() called");
		
		// Restore preferences
	    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    if (settings.contains("profileId")) {
	    	profileId = ObjectId.massageToObjectId(settings.getString("profileId", ""));
	    }
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		Log.d("Project Assassins", "ConspireMenu onPaused() called");
		
		// We need an Editor object to make preference changes.
	    // All objects are from android.context.Context
	    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString("profileId", profileId.toString());
	
	    // Commit the edits!
	    editor.commit();
	}
	
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
	  super.onActivityResult(requestCode, resultCode, data); 
	  switch(requestCode) { 
	    case SETTINGS_ACTIVITY_ID : { 
	      if (resultCode == Activity.RESULT_OK) { 
	    	 Bundle b = data.getExtras();	    	 
	         //Conspiracy newGame = b.getParcelable("game");
	    	 String objIdStr = b.getString("profileId");
	    	 
	    	 if(objIdStr != null)
	    	 {
	    		 profileId = ObjectId.massageToObjectId(objIdStr); 
	    	 }
	    	 
	      } 
	      break; 
	    } 
	  } 
	}


}
