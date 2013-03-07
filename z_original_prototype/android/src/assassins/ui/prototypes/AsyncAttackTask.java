package assassins.ui.prototypes;

import org.bson.types.ObjectId;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

public class AsyncAttackTask extends AsyncTask<Void, Integer, GameSnapshot> {

	View view;
	ObjectId id;
	GameSnapshotManager gameMngr;
	
	public AsyncAttackTask(ObjectId id, View m, GameSnapshotManager gsm)
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
			Log.d("Project Assassins", "Atacking target - my id: "+id);
			
			newSnapshot = httpC.attackTarget(id);

			Log.d("Project Assassins", "FINISHED attack");
			
		} catch (AssassinsHttpException e) {
			Log.d("Project Assasins", "attack exception: " + e.getMessage(), e.getCause());
		}	
		

		return newSnapshot;
	}
	
	protected void onPostExecute(GameSnapshot gs)
	{
		if(gs != null)
		{
			gameMngr.updateSnapshot(gs);
			gameMngr.setAttackAttemptInProgress(false);
		}
		
	}


}
