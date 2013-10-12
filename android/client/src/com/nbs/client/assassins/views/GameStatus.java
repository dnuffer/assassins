package com.nbs.client.assassins.views;

import com.nbs.client.assassins.R;
import com.nbs.client.assassins.models.App;
import com.nbs.client.assassins.models.Match;
import com.nbs.client.assassins.models.MatchMapper;
import com.nbs.client.assassins.models.Player;
import com.nbs.client.assassins.models.PlayerMapper;
import com.nbs.client.assassins.models.Repository;
import com.nbs.client.assassins.utils.Bus;
import com.nbs.client.assassins.views.PlayerStatus.PlayerReadyListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameStatus extends LinearLayout {

	private Match match;
	private Context context;
	private boolean isFilterRegistered = false;
	
	public GameStatus(Context context) {
		super(context);
		init(context);
	}

	public GameStatus(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		this.context = context;
	}

	public void update(Match m) {
		match = m;
		if(!isFilterRegistered) {
			this.registerFilterWithAction(m.id, rcvr);
			this.registerFilterWithAction(m.id + "." + Repository.NEW_PLAYER, playersRcvr);
			isFilterRegistered = true;
		}

		setName(m.name);
		setStatus(m.startTime);
	}
	
	private void registerFilterWithAction(String action, BroadcastReceiver br) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(action);
		LocalBroadcastManager.getInstance(context).registerReceiver(br, filter);
	}

	public void setName(String name) {		
		TextView t = (TextView)findViewById(R.id.match_name);
		t.setText(name);
		t.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Repository model = ((App) ((Activity)context).getApplication()).getRepo();
				model.getUser().setFocusedMatch(match.id);
			}
			
		});
	}
	
	public void setStatus(long startTime) {
		TextView t = (TextView)findViewById(R.id.match_status);
		t.setText(String.valueOf(startTime));
	}
	
	private void addPlayer(Player p) {
		PlayerStatus pStatus = (PlayerStatus)LayoutInflater.from(context).inflate(R.layout.player_status, null);
		pStatus.update(p);
		addView(pStatus);
	}

	private BroadcastReceiver rcvr = new BroadcastReceiver() {
		@Override
        public void onReceive(Context context, Intent intent) {
			update(MatchMapper.fromExtras(intent.getExtras()));
		}
	};
	
	private BroadcastReceiver playersRcvr = new BroadcastReceiver() {
		@Override
        public void onReceive(Context context, Intent intent) {
			addPlayer(PlayerMapper.fromExtras(intent.getExtras()));
		}
	};
	
	@Override
	public void finalize() {
		LocalBroadcastManager.getInstance(context).unregisterReceiver(rcvr);
		LocalBroadcastManager.getInstance(context).unregisterReceiver(playersRcvr);
		try {
			super.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
}
