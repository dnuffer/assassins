package assassins.ui.prototypes;

import java.util.ArrayList;

import org.bson.types.ObjectId;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MatchDetailView extends MapActivity {
	
	private ObjectId profileId;
	
	private ListView playersView;
	private PlayersAdapter playersAdapter;
	
	 @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_status_view); 
        
		Conspiracy c = getIntent().getParcelableExtra("match");
		ArrayList<PlayerState> plyrsArr = c.getPlayers();
		
		String objIdStr = getIntent().getStringExtra("profileId");
		profileId = ObjectId.massageToObjectId(objIdStr);

        playersView = (ListView)findViewById(R.id.match_detail_players_list);
        
        ((TextView)findViewById(R.id.match_detail_match_name)).setText("Conspiracy: " + c.getName());
        ((TextView)findViewById(R.id.match_detail_match_subtitle)).setText("Master: " + c.getOwner());
        ((TextView)findViewById(R.id.match_detail_match_type)).setText("Type: " + c.getTypeInt());
        ((TextView)findViewById(R.id.match_detail_time_label)).setText("Start Time:");
        ((TextView)findViewById(R.id.match_detail_start_time)).setText(c.getStart().getTime().toString());
        
        MapView boundsMap = ((MapView)findViewById(R.id.match_detail_map_view));
        
        GeoPoint nw = c.getNorthwestCorner();
        GeoPoint se = c.getSoutheastCorner();

        GeoPoint mid = PlayerLocation.midPoint(nw, se);
        
        int latSpan = PlayerLocation.geoSpan(nw.getLatitudeE6(), nw.getLongitudeE6());
        int lonSpan = PlayerLocation.geoSpan(se.getLatitudeE6(), se.getLongitudeE6());
        
        boundsMap.getController().zoomToSpan(latSpan, lonSpan);
        
        boundsMap.getController().setCenter(mid);
        playersAdapter = new PlayersAdapter(this,
                R.layout.player_item, plyrsArr);
        
        playersView.setAdapter(playersAdapter);
        
        playersView.setTextFilterEnabled(true);
        Utility.setListViewHeightBasedOnChildren(playersView);
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}

class PlayersAdapter extends ArrayAdapter<PlayerState> {

	public PlayersAdapter(Context context, int resource,
			ArrayList<PlayerState> players) {
		super(context, resource, players);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		 View v = convertView;
         if (v == null) {
             LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             v = vi.inflate(R.layout.player_item, null);
         }
		
          PlayerState p = this.getItem(position);
        		  
		  if (p != null) {
                 TextView topText = (TextView) v.findViewById(R.id.player_item_title);
                 TextView bottomText = (TextView) v.findViewById(R.id.player_item_subtitle);
                 if (topText != null) {
                       topText.setText(p.getUsername());                            
                 }
                 if(bottomText != null){
                       bottomText.setText(p.getLife());
                 }
         }
         return v;
		
	}
}

class Utility {
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
	
