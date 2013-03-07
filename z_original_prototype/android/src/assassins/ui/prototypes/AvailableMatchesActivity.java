package assassins.ui.prototypes;

import java.util.ArrayList;

import org.bson.types.ObjectId;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class AvailableMatchesActivity extends ListActivity {
	
	ArrayList<Conspiracy> _matches = new ArrayList<Conspiracy>();
	
	private ObjectId profileId;
	private AsyncPublicMatchesTask getPublicMatchesTask;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	     setContentView(R.layout.accessible_conspiracy_list);
	     
	     String objIdStr = getIntent().getStringExtra("profileId");
	     profileId = ObjectId.massageToObjectId(objIdStr);
	     
	     getPublicMatchesTask = new AsyncPublicMatchesTask(profileId, this);
	     getPublicMatchesTask.execute();

	     
	}
	
	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		
		Conspiracy touchedMatch = (Conspiracy) getListView().getItemAtPosition(position);
		Bundle bundle = new Bundle();
		bundle.putString("profileId", profileId.toString());
		bundle.putParcelable("match", touchedMatch);
		Intent i = new Intent(AvailableMatchesActivity.this, MatchInfoActivity.class);
		i.putExtras(bundle);
        this.startActivity(i);
        
	}
	
	
	     
}
