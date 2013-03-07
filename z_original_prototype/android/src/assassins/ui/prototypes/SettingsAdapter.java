package assassins.ui.prototypes;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SettingsAdapter extends ArrayAdapter<MatchSettingsItem> {

	private ArrayList<MatchSettingsItem> mySettings;
	
	public SettingsAdapter(Context context, int textViewResourceId,
			ArrayList<MatchSettingsItem> settings) {
		super(context, textViewResourceId, settings);
		
		mySettings = settings;	
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		
		 View v = convertView;
         if (v == null) {
             LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             v = vi.inflate(R.layout.settings_item, null);
         }
         MatchSettingsItem thisSetting = mySettings.get(position);
         if (thisSetting != null) {
                 TextView topText = (TextView) v.findViewById(R.id.settingTitle);
                 TextView bottomText = (TextView) v.findViewById(R.id.settingSubTitle);
                 if (topText != null) {
                       topText.setText(thisSetting.getName());                            }
                 if(bottomText != null){
                       bottomText.setText(thisSetting.getDetail());
                 }
         }
         return v;
	}

}
