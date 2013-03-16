package com.nbs.client.assassins;


import java.util.ArrayList;
import java.util.Arrays;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.SherlockListFragment;

public class NotificationFragment extends SherlockListFragment {

	public NotificationFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.setHasOptionsMenu(true);
		
		/*
		MenuRowData[] items = new MenuRowData[] {
			new MenuEventData("Title 1", "Sub title 1", R.drawable.ic_coins_s),
			new MenuEventData("Title 2", "Sub title 2", R.drawable.ic_coins_s)
				
		};
			
		MenuAdapter adapter = new MenuAdapter(getActivity(), items);*/
		
		 // Create and populate a List of planet names.  
	    String[] planets = new String[] { "Mercury", "Venus", "Earth", "Mars",  
	                                      "Jupiter", "Saturn", "Uranus", "Neptune"};    
	    ArrayList<String> planetList = new ArrayList<String>();  
	    planetList.addAll( Arrays.asList(planets) );  
	      
	    // Create ArrayAdapter using the planet list.  
	    ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(getSherlockActivity(), 
	    		android.R.layout.simple_list_item_1, planetList);  
		
		this.setListAdapter(listAdapter);
		
		return inflater.inflate(R.layout.notif_list_fragment, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
	}

}
