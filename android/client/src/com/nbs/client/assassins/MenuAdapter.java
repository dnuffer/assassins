package com.nbs.client.assassins;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class MenuAdapter extends BaseAdapter {

    private List<MenuListItem> rows;

	public MenuAdapter(Context context, MenuRowData[] items) {
        rows = new ArrayList<MenuListItem>();
        for (MenuRowData item : items) {
            if(item.getItemType() == MenuItemType.MENU_NAV)
            	rows.add(new MenuNavItem(LayoutInflater.from(context), item));
            else if(item.getItemType() == MenuItemType.MENU_HEADER)
            	rows.add(new MenuHeaderItem(LayoutInflater.from(context), item));
            else if(item.getItemType() == MenuItemType.MENU_EVENT)
            	rows.add(new MenuEventItem(LayoutInflater.from(context), item));
        }
        
        
	}

    @Override
    public int getViewTypeCount() {
        return MenuItemType.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).getViewType();
    }
    
    public int getCount() {
        return rows.size();
    }

 
    public Object getItem(int position) {
        return rows.get(position);
    }

    public long getItemId(int position) {
        return rows.get(position).getId();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return rows.get(position).getView(convertView);
    }

}
