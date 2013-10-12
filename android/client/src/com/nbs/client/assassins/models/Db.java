package com.nbs.client.assassins.models;

import java.util.List;

public interface Db {

	public abstract long createMatch(Match m);

	public abstract long createPlayer(String matchId, Player p);

	public abstract Match getMatch(String matchId);

	public abstract List<Match> getAllMatches();

	public abstract int getMatchesCount();

	public abstract int updateMatch(Match m);

	public abstract void deleteMatch(String matchId);

	public abstract Player getPlayer(long id);
	
	public abstract Player[] getPlayersInMatch(String matchId);

	public abstract int updatePlayer(Player p);

	public abstract void deletePlayer(long id);

	public abstract List<Player> getPlayers(String username);

	public abstract Player getPlayer(String matchId, String username);

	public abstract Match getFirstMatch();

}