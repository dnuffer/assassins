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
    private Context context;
    
    
    public MenuAdapter(Context context) {
    	this.context = context;
    	rows = new ArrayList<MenuListItem>();
    }
    
    public MenuAdapter(Context context, MenuRowData[] items) {
    	this.context = context;
        rows = new ArrayList<MenuListItem>();
        for (MenuRowData item : items) {
        	add(item);
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
    
    public void add(MenuRowData item) {
    	rows.add(inflate(item));
    }
    
    public void add(MenuRowData item, int index) {
    	rows.add(index, inflate(item));
    }
    
    private MenuListItem inflate(MenuRowData item) {
        MenuListItem inflated = null;
    	if(item instanceof MenuNavData)
        	inflated = new MenuNavItem(LayoutInflater.from(context), (MenuNavData)item);
        else if(item instanceof MenuHeaderData)
        	inflated = new MenuHeaderItem(LayoutInflater.from(context), (MenuHeaderData)item);
        else if(item instanceof MenuEventData)
        	inflated = new MenuEventItem(LayoutInflater.from(context), (MenuEventData)item);
        return inflated;
    }

	public void clear() {
		rows = new ArrayList<MenuListItem>();
	}
}
