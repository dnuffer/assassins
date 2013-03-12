package com.nbs.client.assassins;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockListFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MenuFragment extends SherlockListFragment {
	
	public MenuFragment() {
	}

	/*
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		List<MenuListItem> menuItems = new ArrayList<MenuListItem>();
		MenuAdapter menuAdapter = new MenuAdapter(menuItems);
		setListAdapter(menuAdapter);
		
		return inflater.inflate(R.layout.menu_list, null);
	}*/
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		
		super.onViewCreated(view, savedInstanceState);
		
/*		List<MenuListItem> menuItemViews = new ArrayList<MenuListItem>();
		
		MenuRowData[] menuItems = new MenuRowData[] { 
			new MenuRowData("Profile", null, R.drawable.ic_launcher),
			new MenuRowData("Tools", null, R.drawable.ic_launcher),
			new MenuRowData("Matches", null, R.drawable.ic_launcher)
		};
		
        for (MenuRowData item : menuItems) {
            menuItemViews.add(new MenuNavItem(LayoutInflater.from(view.getContext()), item));
        }*/
        
        ArrayAdapter<String> adapter = 
        		new ArrayAdapter<String>(getActivity(), R.layout.menu_nav_item_simple);

		adapter.add("Profile");
		adapter.add("Tools");
		adapter.add("Matches");
     
		
		setListAdapter(adapter);
	}
}
