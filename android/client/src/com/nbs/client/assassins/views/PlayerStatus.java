package com.nbs.client.assassins.views;

import com.nbs.client.assassins.R;
import com.nbs.client.assassins.models.Player;
import com.nbs.client.assassins.models.PlayerMapper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlayerStatus extends LinearLayout {
	
	public interface PlayerReadyListener {
		void ready(PlayerStatus view, String matchId);
	}
	
	private PlayerReadyListener readyListener;
	
	
	private Context context;
	private boolean isFilterRegistered = false;
	private Player player;
	
	public PlayerStatus(Context context) {
		super(context);
		init(context);
	}

	public PlayerStatus(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		this.context = context;
		
		if(!(context instanceof PlayerReadyListener)) {
			throw new ClassCastException("PayerStatus.context is not instance of PlayerReadyListener");
		}
	}

	public void update(Player p) {
		this.player = p;
		if(!isFilterRegistered)
			registerFilterWithAction(p.matchId + "." + p.username);
		setName(p.username);
		
		if(p.isMe && p.status != player.READY) {
			setStatus("ready?");
			enableReadyListener();
		} else {
			setStatus(p.status);
			disableReadyListener();
		}
	}

	public void disableReadyListener() {
		TextView t = (TextView)findViewById(R.id.player_status);
		t.setClickable(false);
	}

	public void enableReadyListener() {
		TextView t = (TextView)findViewById(R.id.player_status);
		t.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				arg0.setClickable(false);
				((PlayerReadyListener)context).ready(PlayerStatus.this, player.matchId);
			}
			
		});
		t.setClickable(true);
	}

	private void registerFilterWithAction(String action) {
		filter.addAction(action);
		LocalBroadcastManager.getInstance(context).registerReceiver(rcvr, filter);
		isFilterRegistered = true;
	}

	public void setName(String name) {		
		TextView t = (TextView)findViewById(R.id.player_username);
		t.setText(name);	
	}
	
	public void setStatus(String status) {
		TextView t = (TextView)findViewById(R.id.player_status);
		t.setText(status);
	}

	private IntentFilter filter = new IntentFilter();
	private BroadcastReceiver rcvr = new BroadcastReceiver() {
		@Override
        public void onReceive(Context context, Intent intent) {
			update(PlayerMapper.fromExtras(intent.getExtras()));
		}
	};
	
	@Override
	public void finalize() {
		LocalBroadcastManager.getInstance(context).unregisterReceiver(rcvr);
		try {
			super.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
