package com.nbs.client.assassins.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.nbs.client.assassins.R;
import com.nbs.client.assassins.controllers.MenuAdapter;
import com.nbs.client.assassins.models.Match;
import com.nbs.client.assassins.models.MatchMapper;
import com.nbs.client.assassins.models.Player;
import com.nbs.client.assassins.models.PlayerMapper;
import com.nbs.client.assassins.models.Repository;
import com.nbs.client.assassins.navigation.MenuEventData;
import com.nbs.client.assassins.navigation.MenuHeaderData;
import com.nbs.client.assassins.navigation.MenuRowData;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SideMenu extends ListView {

	private boolean isFilterRegistered;
	private Context context;
	
	public SideMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		this.context = context;
		setAdapter(new MenuAdapter(this.getContext()));
		registerFilterWithAction();
	}

	public SideMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SideMenu(Context context) {
		super(context);	
		init(context);
	}
	
	private void clear() {
		((MenuAdapter)getAdapter()).clear();
	}
	
	private void registerFilterWithAction() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Repository.NEW_MATCH);
		LocalBroadcastManager.getInstance(context).registerReceiver(rcvr, filter);
	}

	public void setMatches(List<Match> matches) {
		this.setAdapter(new GameMenuAdapter(context, matches));
	}
	
	
	private void addMatch(Match m) {
		((GameMenuAdapter)this.getAdapter()).addMatch(m);
	}

	private BroadcastReceiver rcvr = new BroadcastReceiver() {
		@Override
        public void onReceive(Context context, Intent intent) {
			addMatch(MatchMapper.fromExtras(intent.getExtras()));
		}


	};
}
