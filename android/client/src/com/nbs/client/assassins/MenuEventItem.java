package com.nbs.client.assassins;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuEventItem implements MenuListItem {

	private final LayoutInflater inflater;
	private final MenuEventData row;
	
	public MenuEventItem(LayoutInflater inflater, MenuEventData row)
	{
		this.inflater = inflater;
		this.row = row;
	}
	
	@Override
	public View getView(View convertView) {
		MenuEventItem.ViewHolder holder;
        View view;
        //we don't have a convertView so we'll have to create a new one
        if (convertView == null || !(convertView.getTag() instanceof MenuEventItem.ViewHolder)) {
            ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.menu_event_item, null);

            //use the view holder pattern to save already looked up subviews
            holder = new MenuEventItem.ViewHolder((ImageView)viewGroup.findViewById(R.id.event_icon),
                    (TextView)viewGroup.findViewById(R.id.primary_text),
                    (TextView)viewGroup.findViewById(R.id.sub_text));
            viewGroup.setTag(holder);

            view = viewGroup;
        } else {
            //get the holder back out
            holder = (MenuEventItem.ViewHolder)convertView.getTag();

            view = convertView;
        }

        //actually setup the view
        holder.imageView.setImageResource(row.getImageId());
        holder.titleTextView.setText(row.getMainText());
        holder.subTextView.setText(row.getSubText());

        return view;
	}

	@Override
	public int getViewType() {
		return MenuItemType.MENU_EVENT.ordinal();
	}
	
	

    private static class ViewHolder { 
        final ImageView imageView;
        final TextView titleTextView;
        final TextView subTextView;
        
		public ViewHolder(ImageView imageView, TextView titleTextView, TextView subTextView) {
			this.imageView = imageView;
			this.titleTextView = titleTextView;
			this.subTextView = subTextView;
		}
    }

}