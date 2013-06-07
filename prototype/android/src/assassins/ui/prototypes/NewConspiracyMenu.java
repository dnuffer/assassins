package assassins.ui.prototypes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class NewConspiracyMenu extends Activity{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	     setContentView(R.layout.new_conspiracy_menu);
	     
	     Spinner spinner = (Spinner) findViewById(R.id.spinnerMatchType);
	     ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	             this, R.array.matchtype_array, android.R.layout.simple_spinner_item);
	     adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	     spinner.setAdapter(adapter);
	}
}
