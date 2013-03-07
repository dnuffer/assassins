package assassins.ui.prototypes;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class AchievementScreen extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.achievement_screen);
	    
	    Bundle bundle = this.getIntent().getExtras();
	    ImageView icon = (ImageView) findViewById(R.id.icon);
	    TextView achievement = (TextView) findViewById(R.id.achievement);
	    TextView description = (TextView) findViewById(R.id.description);
	    
	    String[] descriptions = getResources().getStringArray(R.array.descriptions);
	    
	    icon.setImageResource(bundle.getInt("pictureId"));
	    achievement.setText(bundle.getString("type"));
	    description.setText(descriptions[bundle.getInt("pos")]);
	    
	}

}
