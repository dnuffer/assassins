package com.nbs.client.assassins;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockListFragment;

public class NotificationFragment extends SherlockListFragment {

	public NotificationFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.setHasOptionsMenu(true);

		MenuRowData[] items = new MenuRowData[] {
			new MenuHeaderData("Notifications"),
			new MenuEventData("Title 1", "Sub title 1", R.drawable.ic_coins_s),
			new MenuEventData("Title 2", "Sub title 2", R.drawable.ic_coins_s)
				
		};
			
		MenuAdapter adapter = new MenuAdapter(getActivity(), items);
		
		this.setListAdapter(adapter);
		
		return inflater.inflate(R.layout.notif_list_fragment, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
	}

}
