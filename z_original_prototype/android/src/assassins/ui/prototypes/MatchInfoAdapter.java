package assassins.ui.prototypes;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MatchInfoAdapter extends ArrayAdapter<ListItem> {

	public MatchInfoAdapter(Context context, int resource,
			List<ListItem> matchInfoItems) {
		super(context, resource, matchInfoItems);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		 ListItem item = this.getItem(position);

		 Log.d("Project Assassins", "MatchInfoAdapter::getView() position: " + position+ " item: " + this.getItem(position).toString()); 

		 
		 if (item != null) {  

             LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             convertView = vi.inflate(item.getLayout(), null, true);

			
             switch(item.getLayout())
             {
             	case R.layout.match_info_item:
             		 Log.d("Project Assassins", "Match Info Item");
	    			 TextView topText = (TextView) convertView.findViewById(R.id.match_info_item_title);
	    			 TextView bottomText = (TextView) convertView.findViewById(R.id.match_info_item_subtitle);
	    			 ImageView img = (ImageView)convertView.findViewById(R.id.match_info_item_img);
	    			 
	    			 if (topText != null) {
	    			       topText.setText(item.getTitle());                            
	    			 }
	    			 if(bottomText != null){
	    			       bottomText.setText(item.getSubTitle());
	    			 }
	    			 if(img !=null && item.getImgResource() != -1)
	    			 {
	    				 img.setImageResource(item.getImgResource());
	    			 }
            	 
            	break;
             	case R.layout.list_divider_titled:
             		Log.d("Project Assassins", "Divider");
             		TextView dividerTitle = (TextView) convertView.findViewById(R.id.list_divider_title_text);
             		
             		if(dividerTitle != null)
             		{
             			dividerTitle.setText(item.getTitle());
             		}
             		
             	break;
             
             }

        }
		 parent.invalidate();
        return convertView;
		
	}

}
