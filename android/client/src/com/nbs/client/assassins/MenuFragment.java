package com.nbs.client.assassins;


import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockListFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class MenuFragment extends SherlockListFragment {
	
	public MenuFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.menu_list, null);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		
		super.onViewCreated(view, savedInstanceState);

		MenuRowData[] menuItems = new MenuRowData[] { 
			new MenuNavData("Profile", R.drawable.ic_launcher),
			new MenuHeaderData("Current Game"),
			new MenuEventData("Match Name", "Match type", R.drawable.ic_menu_mapmode),
			new MenuHeaderData("Players"),
			new MenuEventData("Player name", "rank", R.drawable.crosshairs),
			new MenuHeaderData("Notifications"),
			new MenuEventData("Event main", "event detail", R.drawable.crosshairs),
		};

		MenuAdapter adapter = new MenuAdapter(view.getContext(), menuItems);
     
		
		setListAdapter(adapter);
	}
}
