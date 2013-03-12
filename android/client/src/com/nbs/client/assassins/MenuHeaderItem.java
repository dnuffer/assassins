package com.nbs.client.assassins;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MenuHeaderItem implements MenuListItem {

	private final LayoutInflater inflater;
	private final MenuRowData row;
	
	public MenuHeaderItem(LayoutInflater inflater, MenuRowData row)
	{
		this.inflater = inflater;
		this.row = row;
	}
	
	@Override
	public View getView(View convertView) {
        MenuHeaderItem.ViewHolder holder;
        View view;
        //we don't have a convertView so we'll have to create a new one
        if (convertView == null || !(convertView.getTag() instanceof MenuHeaderItem.ViewHolder)) {
            ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.menu_header_item, null);

            //use the view holder pattern to save already looked up subviews
            holder = new MenuHeaderItem.ViewHolder((TextView)viewGroup.findViewById(R.id.header_text));
            viewGroup.setTag(holder);

            view = viewGroup;
        } else {
            //get the holder back out
            holder = (MenuHeaderItem.ViewHolder)convertView.getTag();

            view = convertView;
        }

        //actually setup the view
        holder.textView.setText(row.getMainText());

        return view;
	}

	@Override
	public int getViewType() {
		return MenuItemType.MENU_HEADER.ordinal();
	}
	
	

    private static class ViewHolder {
        final TextView textView;

        private ViewHolder(TextView titleView) {
            this.textView = titleView;
        }
    }



	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return row.getId();
	}

}
