package assassins.ui.prototypes;

import java.util.ArrayList;

import org.bson.types.ObjectId;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MatchInfoActivity extends ListActivity {
	
	private ObjectId profileId;
	private String installId;
	private Conspiracy c;
	private Button btnJoin;
	private ArrayList<ListItem> matchItems;
	private AsyncTaskJoinMatch asyncJointask;
	
	private final int LOCATION_ITEM_POSITION = 7;
	
	
	 @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_info_list);

		String objIdStr = getIntent().getStringExtra("profileId");
		profileId = ObjectId.massageToObjectId(objIdStr);
		
		installId = Installation.id(this);
		
		c = getIntent().getParcelableExtra("match");
		setMatch(c);
		
		btnJoin = (Button)findViewById(R.id.join);
	     btnJoin.setOnClickListener(new View.OnClickListener() {
		    	public void onClick(View arg0) {

		    		AsyncTaskJoinMatch asyncJoinTask = new AsyncTaskJoinMatch(MatchInfoActivity.this);
		    		asyncJoinTask.execute();
		    		

		    	}

		    });
		
		
	 }
	 
	 public void setMatch(Conspiracy updatedMatch)
	 {
		c = updatedMatch;
		matchItems = MatchToListAdapter.toList(c);
		
		MatchInfoAdapter adapter = new MatchInfoAdapter(this, 
				R.layout.match_info_item, matchItems);
			
		setListAdapter(adapter);
	 }
	 
		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			
			switch(position)
			{
				case LOCATION_ITEM_POSITION:
					Bundle bundle = new Bundle();
					bundle.putInt("nwLat", c.getNorthwestCorner().getLatitudeE6());
					bundle.putInt("nwLon", c.getNorthwestCorner().getLongitudeE6());
					bundle.putInt("seLat", c.getSoutheastCorner().getLatitudeE6());
					bundle.putInt("seLon", c.getSoutheastCorner().getLongitudeE6());
					Intent i = new Intent(MatchInfoActivity.this, MatchInfoBoundsDisplay.class);
					i.putExtras(bundle);
			        this.startActivity(i);
				break;
			}

	        
		}

		
	private class AsyncTaskJoinMatch extends AsyncTask<Void, Integer, Conspiracy>
	{
		ProgressDialog progressDlg;
		
		public AsyncTaskJoinMatch(ListActivity matchInfoList)
		{
			progressDlg = new ProgressDialog(matchInfoList);
		}
		
		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}
		
		@Override
	    protected void onPreExecute() {
			btnJoin.setEnabled(false); 
			progressDlg.setMessage("Joining match...");
	    	progressDlg.show();
	    }

		@Override
		protected void onPostExecute(Conspiracy joinedMatch) {
			
			if(joinedMatch != null)
			{
				if (progressDlg.isShowing())
					progressDlg.setMessage("Successfully joined match.");
				
				Log.d("Project Assassins", "Successfully joined match: "+joinedMatch);
				setMatch(joinedMatch);
				Toast.makeText(getApplicationContext(), "Joined match: "+joinedMatch.getName(), Toast.LENGTH_SHORT).show();
			}
			else
			{
				btnJoin.setEnabled(true);
				Log.d("Project Assassins", "Failed to join match: " + joinedMatch);
				Toast.makeText(getApplicationContext(), "Failed to join match.", Toast.LENGTH_SHORT).show();
			}
			
            if (progressDlg.isShowing())
            	progressDlg.dismiss();
			
			super.onPostExecute(joinedMatch);
		}

		@Override
		protected Conspiracy doInBackground(Void... params) {
    		
			AssassinsHttpClient service = new AssassinsHttpClient(getString(R.string.serviceUrl), MatchInfoActivity.this);
    		
			Conspiracy joinedMatch = null;
			
			try {
				
				joinedMatch = service.joinMatch(installId, c.getObjectId().toString());
				
    		} catch (AssassinsHttpException e) {
				
				Log.d("Project Assassins", "AsyncTaskJoinMatch.doInBackground() caught expection: " + 
											e.getMessage(), e.getCause());
			}
			
			return joinedMatch;
		}
		
	}
	
}

class MatchToListAdapter
{

	public static ArrayList<ListItem> toList(Conspiracy c)
	{
		ArrayList<ListItem> matchItems = new ArrayList<ListItem>();
		
		matchItems.add(new ListItem("Match Details", "", -1, R.layout.list_divider_titled));
		matchItems.add(new ListItem("Name", c.getName(), -1, R.layout.match_info_item));
		matchItems.add(new ListItem("Creator", c.getOwner(), -1, R.layout.match_info_item));
		matchItems.add(new ListItem("Type", c.getType().toString(), R.drawable.assassin_icon, R.layout.match_info_item));
		matchItems.add(new ListItem("Settings", c.getSettingsString(), -1
				, R.layout.match_info_item));
		matchItems.add(new ListItem("Start Time", c.getStart().getTime().toLocaleString(), -1, R.layout.match_info_item));
		matchItems.add(new ListItem("Visiblity", c.isPublic() ? "public":"private", -1, R.layout.match_info_item));
		matchItems.add(new ListItem("Location", PlayerLocation.getMilesAreaString(c.getNorthwestCorner(), c.getSoutheastCorner()), 
									R.drawable.tattered_map_with_arrow, R.layout.match_info_item));
		matchItems.add(new ListItem("Conspirators", "", -1, R.layout.list_divider_titled));
		
		if(c.getPlayers().size() < 1)
		{
			matchItems.add(new ListItem("No Conspirators yet", "", 
					R.drawable.x_mark, R.layout.match_info_item));
		}
		else
		{
			for(PlayerState ps : c.getPlayers())
			{
				matchItems.add(new ListItem(ps.getUsername(), "add rank here", 
						R.drawable.assassin_icon, R.layout.match_info_item));
			}
		}

		return matchItems;
	}
}




