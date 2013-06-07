package assassins.ui.prototypes;

import org.bson.types.ObjectId;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class SearchMode extends MapActivity {
	
	private MapView mapView;
	private MatchLayout matchLayout;
	private GameSnapshotManager manager;
    private ObjectId profileId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.search_mode);
	     
	    mapView = (MapView)findViewById(R.id.map_view);
	    setTitle("Active Conspiracy");
	    
	    String objIdStr = getIntent().getStringExtra("profileId");
	    profileId = ObjectId.massageToObjectId(objIdStr); 
	     
	    matchLayout = (MatchLayout)findViewById(R.id.active_match);
	    
		manager = new GameSnapshotManager(matchLayout, this, profileId);

		matchLayout.setGameSnapshotManager(manager);

		Button attack = (Button)findViewById(R.id.attack_button);
		attack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
	
				if(!manager.isAttackAttemptInProgress())
				{
					view.setEnabled(false);
					manager.setAttackAttemptInProgress(true);
					new AsyncAttackTask(profileId, matchLayout, manager).execute();
				}

			}
		});	 
	}
    
    protected void onResume() {
		super.onResume();
    	manager.startListeningForLocationChanges(20*1000/*minTime*/, 0/*minDistance*/);
    	manager.startSyncingWithWebService(1/*initial delay*/, 5/*delay btwn sends*/);
    }
    
    protected void onPause() {
    	super.onPause();
    	manager.stopSendingLocationToWebService();
    	manager.stopListeningForLocationChanges();
    }
    
    protected void onStop() {
    	super.onStop();
    	Log.d("Project Assassins", "Match activity onStop()");
    }
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
