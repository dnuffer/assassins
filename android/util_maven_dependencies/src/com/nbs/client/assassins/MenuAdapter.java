package com.nbs.client.assassins;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class MenuAdapter extends BaseAdapter {

    private List<MenuListItem> rows;

	public MenuAdapter(List<MenuListItem> items) {
        rows = new ArrayList<MenuListItem>(items);//member variable
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
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return rows.get(position).getView(convertView);
    }

}
