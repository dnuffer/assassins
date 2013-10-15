package com.nbs.client.assassins.views;

import java.util.List;

import com.nbs.client.assassins.controllers.MenuAdapter;
import com.nbs.client.assassins.models.Match;
import com.nbs.client.assassins.models.MatchMapper;

import com.nbs.client.assassins.models.Repository;

import com.nbs.client.assassins.utils.Bus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.util.AttributeSet;
import android.util.Log;

import android.widget.BaseAdapter;
import android.widget.ListView;


public class SideMenu extends ListView {

	private static final String TAG = "SideMenu";
	private Context context;
	
	public SideMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		this.context = context;
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
	
	private void registerFilterWithAction() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Repository.NEW_MATCH);
		Bus.register(context, rcvr, filter);
	}

	public void setMatches(List<Match> matches) {
		this.setAdapter(new GameMenuAdapter(context, matches));
		dirty();
	}
	
	private void addMatch(Match m) {
		((GameMenuAdapter)this.getAdapter()).addMatch(m);
		dirty();
	}

	private void dirty() {
		((GameMenuAdapter) this.getAdapter()).notifyDataSetChanged();
	}
	
	private BroadcastReceiver rcvr = new BroadcastReceiver() {
		@Override
        public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "SideMenu.onReceive("+intent+")");
			addMatch(MatchMapper.fromExtras(intent.getExtras()));
		}


	};
}
