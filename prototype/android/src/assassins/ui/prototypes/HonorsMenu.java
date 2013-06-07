package assassins.ui.prototypes;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class HonorsMenu extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	     setContentView(R.layout.honors_menu);
	     
	     String[] achievements = getResources().getStringArray(R.array.achievements_array);
	     TypedArray achievementIcons = getResources().obtainTypedArray(R.array.achievement_icons);
	     
	     final ListView achievementsList = (ListView) findViewById(R.id.achievements_list);
	     final HonorsArrayAdapter adapter = new HonorsArrayAdapter(this, achievements, achievementIcons);
	     achievementsList.setAdapter(adapter);
	     
	     achievementsList.setOnItemClickListener(new OnItemClickListener() {
			    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			    	Object o = achievementsList.getAdapter().getItem(position);
			    	String keyword = o.toString();
			    	int picId = adapter.getImage(position);
			    	Bundle bundle = new Bundle();
			    	bundle.putString("type", keyword);
			    	bundle.putInt("pictureId", picId);
			    	bundle.putInt("pos", position);
			    	Intent i = new Intent(HonorsMenu.this, AchievementScreen.class);
			    	i.putExtras(bundle);
			        startActivityForResult(i,0);
			    }
			  });
	     
	}
}
