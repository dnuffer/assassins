package com.nbs.client.assassins.controllers;

import java.util.ArrayList;
import java.util.List;

import com.nbs.client.assassins.navigation.MenuEventData;
import com.nbs.client.assassins.navigation.MenuEventItem;
import com.nbs.client.assassins.navigation.MenuHeaderData;
import com.nbs.client.assassins.navigation.MenuHeaderItem;
import com.nbs.client.assassins.navigation.MenuItemType;
import com.nbs.client.assassins.navigation.MenuListItem;
import com.nbs.client.assassins.navigation.MenuNavData;
import com.nbs.client.assassins.navigation.MenuNavItem;
import com.nbs.client.assassins.navigation.MenuRowData;

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
            if(item instanceof MenuNavData)
            	rows.add(new MenuNavItem(LayoutInflater.from(context), (MenuNavData)item));
            else if(item instanceof MenuHeaderData)
            	rows.add(new MenuHeaderItem(LayoutInflater.from(context), (MenuHeaderData)item));
            else if(item instanceof MenuEventData)
            	rows.add(new MenuEventItem(LayoutInflater.from(context), (MenuEventData)item));
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
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return rows.get(position).getView(convertView);
    }

}
