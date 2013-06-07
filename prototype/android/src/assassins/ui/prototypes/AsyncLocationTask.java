package assassins.ui.prototypes;

import org.bson.types.ObjectId;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

public class AsyncLocationTask extends AsyncTask<Void, Integer, GameSnapshot> {

	View view;
	ObjectId id;
	GameSnapshotManager gameMngr;
	
	public AsyncLocationTask(ObjectId id, View m, GameSnapshotManager gsm)
	{
		super();
	
		gameMngr = gsm;
		this.id = id;
		view = m;
	}

	@Override
	protected GameSnapshot doInBackground(Void... params) {
		AssassinsHttpClient httpC = new AssassinsHttpClient(view.getContext().getString(R.string.serviceUrl), view.getContext());
		GameSnapshot newSnapshot = null;
		
		try {		
			Log.d("Project Assassins", "Updating location with id: "+id);
			
			newSnapshot = httpC.updateLocation(id);

			Log.d("Project Assassins", "FINISHED updating location");
			
		} catch (AssassinsHttpException e) {
			Log.d("Project Assasins", "update location exception: " + e.getMessage(), e.getCause());
		}	
		

		return newSnapshot;
	}
	
	protected void onPostExecute(GameSnapshot gs)
	{
		if(gs != null)
		{
			gameMngr.updateSnapshot(gs);
			view.invalidate();
		}
		
	}


}
