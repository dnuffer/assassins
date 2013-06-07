package assassins.ui.prototypes;

import java.util.ArrayList;

import org.bson.types.ObjectId;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

public class AsyncPublicMatchesTask extends AsyncTask<Void, Integer, ArrayList<Conspiracy>> {

	ListActivity displayList;
	ObjectId id;
	ProgressDialog progressDialog;
	
	public AsyncPublicMatchesTask(ObjectId id, ListActivity l)
	{
		super();
		
		this.id = id;
		displayList = l;
		progressDialog = new ProgressDialog(l);
		
	}

	@Override
	protected void onPreExecute() {
		progressDialog.setMessage("Finding public matches...");
		progressDialog.show();
		super.onPreExecute();
	}
	@Override
	protected ArrayList<Conspiracy> doInBackground(Void... params) {
		AssassinsHttpClient httpC = new AssassinsHttpClient(displayList.getString(R.string.serviceUrl), displayList);
		ArrayList<Conspiracy> matchesList = null;
		
		try {		
			Log.d("Project Assassins", "Getting Public Matches - my id: "+id);
			
			matchesList = httpC.getPublicMatches(id);

			Log.d("Project Assassins", "FINISHED Getting Public Matches");
			
		} catch (AssassinsHttpException e) {
			Log.d("Project Assasins", "Retrieve Public Matches exception: " + e.getMessage(), e.getCause());
		}	
		

		return matchesList;
	}
	
	@Override
	protected void onPostExecute(ArrayList<Conspiracy> m)
	{
		Log.d("Project Assassins", "AsyncPublicMatchesTask onPostExecute called");
		if(m != null)
		{
			AvailableMatchesAdapter ama = new AvailableMatchesAdapter(displayList, R.layout.match_row_item, m);
			displayList.setListAdapter(ama);
			//displayList.getListView().invalidate();
			//ama.notifyDataSetChanged();
		}
		
		if(progressDialog.isShowing())
			progressDialog.dismiss();
		
	}


}
