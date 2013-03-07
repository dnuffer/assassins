package assassins.ui.prototypes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class BookOfHassanMenu extends Activity {
	//Your member variable declaration here
	ListView lv;
	
	// Create the array of Strings in the List
			 String[] topics = new String[] { "How to", "do", "that", "one", "thing", "I", 
					 "am", "just", "making", "crap", "up", "here",
					 "and", "I", "need", "to", "just", "fill", "some", "space", "for", "now"
			 };
			 String explanation = "This is a really long string intended to represent a really " +
					 " long explanation that would occur in a really large dialog box that we " +
					 " would use to read about a specific topic in our Book of Hassan Menu " +
					 " which is the place where we learn about the game and how it works." +
					 "This is a really long string intended to represent a really " +
					 " long explanation that would occur in a really large dialog box that we " +
					 " would use to read about a specific topic in our Book of Hassan Menu " +
					 " which is the place where we learn about the game and how it works." +
					 "This is a really long string intended to represent a really " +
					 " long explanation that would occur in a really large dialog box that we " +
					 " would use to read about a specific topic in our Book of Hassan Menu " +
					 " which is the place where we learn about the game and how it works." +
					 "This is a really long string intended to represent a really " +
					 " long explanation that would occur in a really large dialog box that we " +
					 " would use to read about a specific topic in our Book of Hassan Menu " +
					 " which is the place where we learn about the game and how it works." +
					 "This is a really long string intended to represent a really " +
					 " long explanation that would occur in a really large dialog box that we " +
					 " would use to read about a specific topic in our Book of Hassan Menu " +
					 " which is the place where we learn about the game and how it works." +
					 "This is a really long string intended to represent a really " +
					 " long explanation that would occur in a really large dialog box that we " +
					 " would use to read about a specific topic in our Book of Hassan Menu " +
					 " which is the place where we learn about the game and how it works." + 
					 "This is a really long string intended to represent a really " +
					 " long explanation that would occur in a really large dialog box that we " +
					 " would use to read about a specific topic in our Book of Hassan Menu " +
					 " which is the place where we learn about the game and how it works." +
					 "This is a really long string intended to represent a really " +
					 " long explanation that would occur in a really large dialog box that we " +
					 " would use to read about a specific topic in our Book of Hassan Menu " +
					 " which is the place where we learn about the game and how it works." +
					 "This is a really long string intended to represent a really " +
					 " long explanation that would occur in a really large dialog box that we " +
					 " would use to read about a specific topic in our Book of Hassan Menu " +
					 " which is the place where we learn about the game and how it works.";
			 
	// Called when the activity is first created.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 
		 
		 //Dialog boh_screen = onCreateDialog(IMPORT_ALERT);
	     //   accountWindow.show();
		 //Spinner spinner = (Spinner) findViewById(R.id.spinnerMatchType);
	     //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	     //        this, R.array.matchtype_array, android.R.layout.simple_spinner_item);
	     //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
	     setContentView(R.layout.boh_menu);
	     
		 //setListAdapter(new ArrayAdapter<String>(this, R.layout.boh_menu, topics));
	     lv=(ListView)findViewById(R.id.listView01);
	     
	     lv.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1 , topics));
	     //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.boh_topics, android.R.layout.simple_list_item_1);
		 //lv.setAdapter(adapter);
	     lv.setTextFilterEnabled(true);

		  lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view,
		        int position, long id) {
		    	Dialog boh_screen = onCreateDialog(position);
		    	boh_screen.show();
		    }
		  });
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		
		LayoutInflater inflater = LayoutInflater.from(this);
		final View explication = inflater.inflate(R.layout.boh_topic, null);
		//View layout = inflater.inflate(R.layout.boh_topic, 
		//		(ViewGroup) findViewById(R.id.layout_root));
		TextView text = (TextView) explication.findViewById(R.id.text);
		text.setText(explanation);
		//ImageView image = (ImageView) explication.findViewById(R.id.image);
		//image.setImageResource(R.drawable.icon);
		
		AlertDialog dialog = new AlertDialog.Builder(BookOfHassanMenu.this)
		.setNeutralButton("Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	dialog.cancel();
            }
        })
		.create();
		
		dialog.setTitle(topics[id]);
		dialog.setView(explication);
		
		return dialog;
	}
}