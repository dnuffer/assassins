package assassins.ui.prototypes;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MatchDetailAdapter extends ArrayAdapter<Object> {

	
	private final int HEADER = 0;
	private final int COUNTDOWN = 1;
	private final int BOUNDS = 2;
	private final int PLAYERS_TITLE = 3;
	//private final int PLAYER = 4;
	
	public MatchDetailAdapter(Context context, int textViewResourceId,
			ArrayList<Object> matchDetailItems) {
		
		super(context, textViewResourceId, matchDetailItems);
		// TODO Auto-generated constructor stub

	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View v = convertView;
		LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Object currMatchDetail = getItem(position);
		
        if (currMatchDetail != null)
        { 
			switch(position)
			{
				case HEADER:
			        if (v == null){
			            v = vi.inflate(R.layout.match_detail_header, null);
			        }
			        //set name
			        TextView name = (TextView) v.findViewById(R.id.headerMatchName);
			        //set image
			        ImageView img = (ImageView)v.findViewById(R.id.headerMatchTypeImage);
			        //set matchtype
			        TextView type = (TextView) v.findViewById(R.id.headerMatchType);
			        //set how many players are alive
			        TextView subTitle = (TextView) v.findViewById(R.id.headerMatchSubtitle);
			        
			        name.setText("name");
			        //img.setImageDrawable(Drawable.createFromResource());
			        type.setText("matchtype");
			        subTitle.setText("a bit of wisdom");
					break;
				case COUNTDOWN:
			        if (v == null) {
			            v = vi.inflate(R.layout.match_detail_countdown, null);
			        }
			        //set countdown title
			        //set clock
			        TextView countdown = (TextView) v.findViewById(R.id.countdown);
			        countdown.setText("countdown:");
					break;
				case BOUNDS:
			        if (v == null) {
			            v = vi.inflate(R.layout.match_detail_bounds, null);
			        }
			        //get the rectangle, center the mapview, draw the rect on the map
			        MapView theMap = (MapView) v.findViewById(R.id.matchDetailMapView);
			        MapController control = theMap.getController();
			        
			        //GeoPoint boundsCntr = new GeoPoint(position, position);
			        //theMap.getLatitudeSpan();
			        // theMap.getLongitudeSpan();
			        
			        //control.setCenter(boundsCntr);
			        
			        //get the orig map's latitude span and long span
			        //control.zoomToSpan();
			        
			     
			        //get orig map's screen coord boundsRect
			        TransparentPanel theBounds = (TransparentPanel)v.findViewById(R.id.matchDetailBounds);
			        //set the Rect

					break;
				case PLAYERS_TITLE:
			        if (v == null) {
			            v = vi.inflate(R.layout.match_detail_title, null);
			        }
			        //set the player title text
			        TextView playersTitle = (TextView) v.findViewById(R.id.title);
			        playersTitle.setText("Players:");
					break;
				default: //it must be a player
			        if (v == null) {
			            v = vi.inflate(R.layout.match_detail_player, null);
			        }
			        //set player name, player subtitle (alive, ghost or assassinated), player status pic
			        TextView playersID = (TextView) v.findViewById(R.id.playerTitle);
			        TextView playersStatus = (TextView) v.findViewById(R.id.playerSubTitle);
			        ImageView playersRankImage = (ImageView) v.findViewById(R.id.playerRankImage);
			        
			        playersID.setText("player");
			        playersStatus.setText("assassinated");
			        
					break;
			}	
		}

        return v;
	}
	
	

}
