package com.nbs.client.assassins.views;

import java.sql.Date;
import java.util.HashMap;

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
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameStatus extends LinearLayout {

	protected static final String TAG = "GameStatus";
	private Match match;
	
	HashMap<String, PlayerStatus> players = new HashMap<String, PlayerStatus>();
	
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
			registerFilterWithAction(m.id, rcvr);
			registerFilterWithAction(m.id + "." + Repository.NEW_PLAYER, playersRcvr);
			isFilterRegistered = true;
		}
		
		if(match.players != null) {
			for(Player p : match.players) {
				PlayerStatus status = players.get(p.username);
				if(status != null) {
					status.update(p);
				} else {
					addPlayer(p);
				}
			}
		}
		
		setName(m.name);
		setStatus(m.startTime);
	}
	
	private void registerFilterWithAction(String action, BroadcastReceiver br) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(action);
		Bus.register(context, br, filter);
	}

	public void setName(String name) {		
		TextView t = (TextView)findViewById(R.id.match_name);
		t.setText(name);
		t.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Repository model = App.getRepo();
				Log.d(TAG, "setting focused match id to " + match.id);
				model.getUser().setFocusedMatch(match.id);
			}
		});
	}
	
	public void setStatus(Long startTime) {
		long sysTime = System.currentTimeMillis();
		String status = "";
		if(startTime == null) {
			status = "(starts when all players are ready)";
		} else {
			Time t = new Time();
			t.set(startTime*1000);
			String formattedTime = TimeUtils.format(t);
			Log.d(TAG, "syst: " + sysTime + " startt: " + startTime);
			status = "(" + (sysTime > startTime ? 
					"started at " + formattedTime : "starts at " + formattedTime) + ")";
		}
		TextView t = (TextView)findViewById(R.id.match_status);
		t.setText(status);
	}
	
	private void addPlayer(Player p) {
		PlayerStatus pStatus = (PlayerStatus)LayoutInflater
				.from(context).inflate(R.layout.player_status, null);
		players.put(p.username, pStatus);
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
		Bus.unregister(context, rcvr);
		Bus.unregister(context, playersRcvr);
		try {
			super.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
}
