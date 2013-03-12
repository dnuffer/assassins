/*
 * Based on the approach by Indrajit Khare described here:
 * http://logc.at/2011/10/10/handling-listviews-with-multiple-row-types/
 */

package com.nbs.client.assassins;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuNavItem implements MenuListItem {

	private final LayoutInflater inflater;
	private final MenuRowData row;
	
	public MenuNavItem(LayoutInflater inflater, MenuRowData row)
	{
		this.inflater = inflater;
		this.row = row;
	}
	
	@Override
	public View getView(View convertView) {
        ViewHolder holder;
        View view;
        //we have a don't have a convertView so we'll have to create a new one
        if (convertView == null) {
            ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.menu_nav_item, null);

            //use the view holder pattern to save already looked up subviews
            holder = new ViewHolder((ImageView)viewGroup.findViewById(R.id.icon),
                    (TextView)viewGroup.findViewById(R.id.text));
            viewGroup.setTag(holder);

            view = viewGroup;
        } else {
            //get the holder back out
            holder = (ViewHolder)convertView.getTag();

            view = convertView;
        }

        //actually setup the view
        holder.imageView.setImageResource(row.getImageId());
        holder.textView.setText(row.getMainText());

        return view;
	}

	@Override
	public int getViewType() {
		return MenuItemType.MENU_NOTIFICATION.ordinal();
	}

    private static class ViewHolder {
        final ImageView imageView;
        final TextView textView;

        private ViewHolder(ImageView imageView, TextView titleView) {
            this.imageView = imageView;
            this.textView = titleView;
        }
    }
}