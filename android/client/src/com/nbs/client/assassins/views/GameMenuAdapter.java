package com.nbs.client.assassins.views;

import java.util.ArrayList;
import java.util.List;

import com.nbs.client.assassins.R;
import com.nbs.client.assassins.models.Match;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

public class GameMenuAdapter extends BaseAdapter implements ListAdapter {

	private Context context;
	private List<MatchWrapper> matches = new ArrayList<MatchWrapper>();

	public GameMenuAdapter(Context context, List<Match> matches) {
		this.context = context;
		for(Match m : matches) {
			this.matches.add(new MatchWrapper(m));
		}
	}

	@Override
	public int getCount() {
		return matches.size();
	}

	@Override
	public Object getItem(int position) {
		return matches.get(position);
	}

	@Override
	public long getItemId(int position) {
		return matches.get(position).match.id.hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return matches.get(position).getView();
	}

	public void addMatch(Match m) {
		matches.add(new MatchWrapper(m));
	}
	
	private class MatchWrapper {
		public Match match;
		public GameStatus view;
		
		public MatchWrapper(Match match) {
			this.match = match;
		}

		public View getView() {
			if(view == null) {
				view = (GameStatus) LayoutInflater.from(context).inflate(R.layout.game_status, null);
				view.update(this.match);
			}
			return view;
		}
	}

}
