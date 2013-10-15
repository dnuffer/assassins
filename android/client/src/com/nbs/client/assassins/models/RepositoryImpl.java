package com.nbs.client.assassins.models;

import java.util.List;
import com.nbs.client.assassins.utils.Bus;

import android.content.Context;
import android.util.Log;

public class RepositoryImpl implements Repository {
    
	private static final String TAG = "RepositoryImpl";
	private Db db;
	private Context context;
	private User user;
	
	public RepositoryImpl(Context c) {
		context = c;
		db = new DbImpl(this.context);
	}
	
	//for mocking
	//TODO: inject Db dependency once the 
	//      android annotations dependency is removed
	public void setDb(Db db) {
		this.db = db;
	}
	
    public Db getDb() {
    	if (db == null) {
    		db = new DbImpl(this.context);
    	}
    	return db;
    }

	@Override
	public void onMatchEnd(Match m) {
		db.updateMatch(m);
		Bus.post(context, m.id, MatchMapper.toExtras(m));
	}

	@Override
	public void onLogin(String username, String token) {
		User user = getUser();
		user.login(username, token);
	}

	@Override
	public void onLogout() {
		User user = getUser();
		user.logout();
	}

	@Override
	public User getUser() {
		if(user == null) {
			user = new User(context);
		}
		return user;
	}

	@Override
	public Player getMyFocusedPlayer() {
		String focusedMatchId = getUser().getFocusedMatch();
		Log.d(TAG, "getting focused match id: " + focusedMatchId);
		return getMyPlayer(focusedMatchId);
	}

	@Override
	public Player getMyPlayer(String matchId) {
		return db.getPlayer(matchId, getUser().getUsername());
	}

	@Override
	public List<Player> getMyPlayers() {
		return db.getPlayers(getUser().getUsername());
	}

	@Override
	public Player getPlayer(long id) {
		return db.getPlayer(id);
	}

	@Override
	public Player getPlayer(String matchId, String username) {
		return db.getPlayer(matchId, username);
	}

	@Override
	public List<Player> getActivePlayers(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createOrUpdatePlayer(Player player) {
		if(db.getPlayer(player.matchId, player.username) != null) {
			updatePlayer(player);
		} else {
			long pid = db.createPlayer(player.matchId, player);
			player.id = pid;
			Bus.post(context, player.matchId + "." + Repository.NEW_PLAYER, 
					PlayerMapper.toExtras(player));
		}
	}

	@Override
	public void updatePlayer(Player player) {
		long pid = db.updatePlayer(player);
		player.id = pid;
		Bus.post(context, player.matchId + "." + player.username, 
				PlayerMapper.toExtras(player));
	}

	@Override
	public Match getFocusedMatch() {
		String matchId = getUser().getFocusedMatch();
		
		if(matchId == null) {
			Match m = db.getFirstMatch();
			
			if(m != null) {
				getUser().setFocusedMatch(m.id);
				return m;
			}
		}
		
		return db.getMatch(matchId);
	}

	@Override
	public void setFocusedMatch(String matchId) {
		getUser().setFocusedMatch(matchId);
		
	}

	@Override
	public void createOrUpdateMatch(Match match) {
		
		if(db.getMatch(match.id) != null) {
			updateMatch(match);
		} else {
			db.createMatch(match);
			Bus.post(context, Repository.NEW_MATCH, MatchMapper.toExtras(match));
			
	        if(match.players != null) {
		        for (Player p : match.players) {
		            createOrUpdatePlayer(p);
		        }
	        }
		}
	}

	@Override
	public Match getMatch(String matchId) {
		return db.getMatch(matchId);
	}

	@Override
	public List<Match> getMatches() {
		return db.getAllMatches();
	}
	
	@Override
	public List<Match> getPendingMatches() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Match> getActiveMatches() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateMatch(Match m) {
		db.updateMatch(m);
		Bus.post(context, m.id, MatchMapper.toExtras(m));
		
		if(m.players != null) {
			for(Player p : m.players) {
				createOrUpdatePlayer(p);
			}
		}
	}

	@Override
	public boolean inMatch() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean inPendingMatch() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean inActiveMatch() {
		// TODO Auto-generated method stub
		return false;
	}
}
