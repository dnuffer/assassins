package assassins.ui.prototypes;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AvailableMatchesAdapter extends ArrayAdapter<Conspiracy> {
	
	private List<Conspiracy> conspiracies;
	
	public AvailableMatchesAdapter(Context context, int textViewResourceId, List<Conspiracy> conspiracies) {
		super(context, textViewResourceId,conspiracies);
		// TODO Auto-generated constructor stub
		
		this.conspiracies = conspiracies;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		
		View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.match_row_item, null);
        }
        Conspiracy c = conspiracies.get(position);
        if (c != null) {
                TextView topText = (TextView) v.findViewById(R.id.matchTitle);
                TextView bottomText = (TextView) v.findViewById(R.id.matchStartTime);
                if (topText != null) {
                      topText.setText(c.getName());                            }
                if(bottomText != null){
                      bottomText.setText(c.getStart().getTime().toString());
                }
        }
        Log.d("Project Assassins", "Available Matches Adapter getView called; position: " + position);
        return v;
	}

}
