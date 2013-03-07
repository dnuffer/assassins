package assassins.ui.prototypes;

import android.app.Activity;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HonorsArrayAdapter extends ArrayAdapter<String> {
	private final Activity context;
	private final String[] values;
	private final TypedArray icons;

	public HonorsArrayAdapter(Activity context, String[] values, TypedArray icons) {
		super(context, R.layout.row_layout, values);
		this.context = context;
		this.values = values;
		this.icons = icons;
	}
	
	// static to save the reference to the outer class and to avoid access to
	// any members of the containing class
	static class ViewHolder {
		public ImageView imageView;
		public TextView textView;
		public ImageView checkView;
	}
	
	public int getImage(int position) {
		return icons.getResourceId(position, -1);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		// ViewHolder will buffer the assess to the individual fields of the row
		// layout
		ViewHolder holder;
		// Recycle existing view if passed as parameter
		// This will save memory and time on Android
		// This only works if the base layout for all classes are the same
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.row_layout, null, true);
			holder = new ViewHolder();
			holder.textView = (TextView) rowView.findViewById(R.id.label);
			holder.imageView = (ImageView) rowView.findViewById(R.id.icon);
			holder.checkView = (ImageView) rowView.findViewById(R.id.check_view);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
		}

		holder.textView.setText(values[position]);
		holder.imageView.setImageResource(icons.getResourceId(position, -1));
		//can put int the check if the task is completed
		//holder.checkView.setImageResource(R.drawable.check_mark);

		return rowView;
	}
}
