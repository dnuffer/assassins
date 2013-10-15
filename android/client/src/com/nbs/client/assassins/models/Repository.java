package com.nbs.client.assassins.models;

import java.util.List;

import android.os.Bundle;

public interface Repository {

	public static final String PLAYER_UPDATED = "PLAYER_UPDATED";
	public static final String MATCH_UPDATED = "MATCH_UPDATED";
	public static final String NEW_MATCH = "NEW_MATCH";
	public static final String NEW_PLAYER = "NEW_PLAYER";
	
	//EVENTS
	void onMatchEnd(Match match);

	//Session
	void onLogin(String username, String token);
	void onLogout();
	
	//USER
	User getUser();
	
	//LOCAL PLAYER
	Player getMyFocusedPlayer();
	Player getMyPlayer(String matchId);
	List<Player> getMyPlayers();
	
	Player getPlayer(long id);
	Player getPlayer(String matchId, String username);
	
	List<Player> getActivePlayers(String username);
	
	void createOrUpdatePlayer(Player player);
	void updatePlayer(Player p);
	
	//MATCH
	Match getFocusedMatch();
	void setFocusedMatch(String matchId);
	void createOrUpdateMatch(Match match);
	Match getMatch(String matchId);
	List<Match> getPendingMatches();
	List<Match> getActiveMatches();
	List<Match> getMatches();
	void updateMatch(Match m);

	boolean inMatch();
	boolean inPendingMatch();
	boolean inActiveMatch();
}
