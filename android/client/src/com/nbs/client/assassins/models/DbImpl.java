package com.nbs.client.assassins.models;
 
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.codehaus.jackson.annotate.JsonProperty;
 
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
 
public class DbImpl extends SQLiteOpenHelper implements Db {
 
    // Logcat tag
    private static final String TAG = DbImpl.class.getName();
 
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "db_hunted";
 
    // Table Names
    private static final String TABLE_MATCHES = "matches";
    private static final String TABLE_PLAYERS = "players";
    private static final String TABLE_EVENTS = "events";
 
    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_TYPE = "type";
 
    // MATCHES Table - column names
    private static final String KEY_STATUS = "status";
	private static final String KEY_TOKEN = "token";
    private static final String KEY_CREATOR = "creator";
    private static final String KEY_NAME = "name";
    private static final String KEY_WINNER = "winner";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_NW_CORNER_LAT = "nw_lat";
    private static final String KEY_NW_CORNER_LNG = "mw_lng";
    private static final String KEY_SE_CORNER_LAT = "se_lat";
    private static final String KEY_SE_CORNER_LNG = "se_lng";
    private static final String KEY_ATTACK_RANGE = "attack_range";
    private static final String KEY_HUNT_RANGE = "hunt_range";
    private static final String KEY_ESCAPE_TIME = "escape_time";
    
    // PLAYERS Table - column names
    private static final String KEY_USERNAME = "username";
    private static final String KEY_HEALTH = "health";
    private static final String KEY_TEAM = "team";
    private static final String KEY_ROLE = "role";
	private static final String KEY_LAT = "lat";
	private static final String KEY_LNG = "lng";
	private static final String KEY_TARGET_LAT = "t_lat";
	private static final String KEY_TARGET_LNG = "t_lng";
	private static final String KEY_TARGET_BRG = "t_brg";
	private static final String KEY_TARGET_RNG = "t_rng";
	private static final String KEY_ENEMY_RNG = "e_rng";
    private static final String KEY_MATCH_ID = "match_id";
    
    // EVENTS Table - column names
    private static final String KEY_MESSAGE = "message";
 
    // Table Create Statements
    private static final String CREATE_TABLE_MATCHES = "CREATE TABLE "
    		+ TABLE_MATCHES + "(" + 
				KEY_ID            + " TEXT PRIMARY KEY," + 
				KEY_TYPE          + " TEXT," +
				KEY_STATUS        + " TEXT," +
				KEY_TOKEN         + " TEXT," +
				KEY_CREATOR       + " TEXT," +
				KEY_NAME          + " TEXT," +
				KEY_WINNER          + " TEXT," +
				KEY_START_TIME    + " DATETIME," +
				KEY_NW_CORNER_LAT + " REAL," +
				KEY_NW_CORNER_LNG + " REAL," +
				KEY_SE_CORNER_LAT + " REAL," +
				KEY_SE_CORNER_LNG + " REAL," +
				KEY_ATTACK_RANGE  + " REAL," +
				KEY_HUNT_RANGE    + " REAL," +
				KEY_ESCAPE_TIME   + " INT," +
				KEY_CREATED_AT    + " DATETIME" + 
    		")";
 
    private static final String CREATE_TABLE_PLAYERS = "CREATE TABLE " + 
    		TABLE_PLAYERS + "(" + 
    			KEY_ID         + " INTEGER PRIMARY KEY," + 
    		    KEY_MATCH_ID   + " TEXT," +
    			KEY_USERNAME   + " TEXT," + 
    			KEY_HEALTH     + " INT," + 
    			KEY_TEAM       + " TEXT," +
    			KEY_ROLE       + " TEXT," +
    			KEY_LAT        + " REAL," + 
    			KEY_LNG        + " REAL," + 
    			KEY_TARGET_LAT + " REAL," + 
    			KEY_TARGET_LNG + " REAL," + 
    			KEY_TARGET_BRG + " REAL," + 
    			KEY_TARGET_RNG + " TEXT," + 
    			KEY_ENEMY_RNG  + " TEXT," + 
    			KEY_CREATED_AT + " DATETIME" + 
    		")";
 
    private static final String CREATE_TABLE_EVENTS = "CREATE TABLE "
            + TABLE_EVENTS + "(" + 
    			KEY_ID         + " INTEGER PRIMARY KEY," + 
    			KEY_MATCH_ID   + " TEXT," +
    			KEY_MESSAGE    + " TEXT," + 
    			KEY_CREATED_AT + " DATETIME" + 
    		")";
    
    public DbImpl(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
 
        // creating required tables
        db.execSQL(CREATE_TABLE_MATCHES);
        db.execSQL(CREATE_TABLE_PLAYERS);
        db.execSQL(CREATE_TABLE_EVENTS);
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MATCHES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
 
        // create new tables
        onCreate(db);
    }
    
    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
 
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
 
    // ------------------------ "matches" table methods ----------------//
 
    /* (non-Javadoc)
	 * @see com.nbs.client.assassins.models.Db2#createMatch(com.nbs.client.assassins.models.Match)
	 */
    @Override
	public long createMatch(Match m) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, m.name);
        values.put(KEY_CREATED_AT, getDateTime());
        //TODO: enumerate match fields
 
        long id = db.insert(TABLE_MATCHES, null, values);
 
        for (Player p : m.players) {
            createPlayer(m.id, p);
        }
 
        return id;
    }
    
    /* (non-Javadoc)
	 * @see com.nbs.client.assassins.models.Db2#createPlayer(java.lang.String, com.nbs.client.assassins.models.Player)
	 */
    @Override
	public long createPlayer(String matchId, Player p) {
    	SQLiteDatabase db = this.getWritableDatabase();
    	
    	ContentValues values = new ContentValues();
        values.put(KEY_USERNAME,p.username);
        values.put(KEY_MATCH_ID, matchId);
        values.put(KEY_CREATED_AT, getDateTime());
        //TODO: enumerate player fields
        
        long id = db.insert(TABLE_PLAYERS, null, values);
        return id;
    }
 
    /* (non-Javadoc)
	 * @see com.nbs.client.assassins.models.Db2#getMatch(java.lang.String)
	 */
    @Override
	public Match getMatch(String matchId) {
        SQLiteDatabase db = this.getReadableDatabase();
 
        String selectQuery = "SELECT  * FROM " + TABLE_MATCHES + " WHERE "
                + KEY_ID + " = " + matchId;
 
        Log.e(TAG, selectQuery);
 
        Cursor c = db.rawQuery(selectQuery, null);
 
        if (c != null)
            c.moveToFirst();
        
        Match m = new Match();
        m.id = c.getString(c.getColumnIndex(KEY_ID));
        m.name = c.getString(c.getColumnIndex(KEY_NAME));
        //TODO: enumerate match fields
 
        return m;
    }
 
    /* (non-Javadoc)
	 * @see com.nbs.client.assassins.models.Db2#getAllMatches()
	 */
    @Override
	public List<Match> getAllMatches() {
        List<Match> matches = new ArrayList<Match>();
        String selectQuery = "SELECT  * FROM " + TABLE_MATCHES;
 
        Log.e(TAG, selectQuery);
 
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
            	Match m = new Match();
                m.id = c.getString(c.getColumnIndex(KEY_ID));
            	m.name = c.getString(c.getColumnIndex(KEY_NAME));
                m.players = getPlayersInMatch(m.id); 
                //TODO: enumerate match fields
                
                matches.add(m);
            } while (c.moveToNext());
        }
 
        return matches;
    }
 
    /* (non-Javadoc)
	 * @see com.nbs.client.assassins.models.Db2#getMatchesCount()
	 */
    @Override
	public int getMatchesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_MATCHES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }
 
    /* (non-Javadoc)
	 * @see com.nbs.client.assassins.models.Db2#updateMatch(com.nbs.client.assassins.models.Match)
	 */
    @Override
	public int updateMatch(Match m) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, m.name);
        //TODO: enumerate the match fields
 
        // updating row
        return db.update(TABLE_MATCHES, values, KEY_ID + " = ?",
                new String[] { m.id });
    }
 
    /* (non-Javadoc)
	 * @see com.nbs.client.assassins.models.Db2#deleteMatch(java.lang.String)
	 */
    @Override
	public void deleteMatch(String matchId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MATCHES, KEY_ID + " = ?",
                new String[] { matchId });
        db.delete(TABLE_PLAYERS, KEY_MATCH_ID + " = ?", 
        		new String[] { matchId });
    }
    
    /* (non-Javadoc)
	 * @see com.nbs.client.assassins.models.Db2#getPlayersInMatch(java.lang.String)
	 */
    @Override
	public Player[] getPlayersInMatch(String matchId) {
    	
        List<Player> players = new ArrayList<Player>();
        String selectQuery = "SELECT  * FROM " + TABLE_PLAYERS + " WHERE "
                + KEY_MATCH_ID + " = " + matchId;
 
        Log.e(TAG, selectQuery);
 
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
            	Player p = new Player();
                p.username = c.getString(c.getColumnIndex(KEY_USERNAME));
                p.id = c.getLong(c.getColumnIndex(KEY_ID));
                //TODO: enumerate player fields
                
                players.add(p);
            } while (c.moveToNext());
        }
 
    	return players.toArray(new Player[players.size()]);
    }
    
    /* (non-Javadoc)
	 * @see com.nbs.client.assassins.models.Db2#updatePlayer(com.nbs.client.assassins.models.Player)
	 */
    @Override
	public int updatePlayer(Player p) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_USERNAME, p.username);
 
        // updating row
        return db.update(TABLE_PLAYERS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(p.id) });
    }
    
    /* (non-Javadoc)
	 * @see com.nbs.client.assassins.models.Db2#deletePlayer(long)
	 */
    @Override
	public void deletePlayer(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLAYERS, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
    }

	@Override
	public Player getPlayer(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selectQuery = "SELECT  * FROM " + TABLE_PLAYERS + " WHERE "
                + KEY_ID + " = " + id;
 
        Log.e(TAG, selectQuery);
 
        Cursor c = db.rawQuery(selectQuery, null);
 
        if (c != null)
            c.moveToFirst();
        
        Player p = new Player();
        p.id = c.getLong(c.getColumnIndex(KEY_ID));
        p.username = c.getString(c.getColumnIndex(KEY_USERNAME));
        //TODO: enumerate match fields
 
        return p;
		
	}

	@Override
	public List<Player> getPlayers(String username) {
        List<Player> players = new ArrayList<Player>();
        String selectQuery = "SELECT  * FROM " + TABLE_PLAYERS + " WHERE "
                + KEY_USERNAME + " = " + username;
 
        Log.e(TAG, selectQuery);
 
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
            	Player p = new Player();
                p.username = c.getString(c.getColumnIndex(KEY_USERNAME));
                p.id = c.getLong(c.getColumnIndex(KEY_ID));
                //TODO: enumerate player fields
                
                players.add(p);
            } while (c.moveToNext());
        }
 
    	return players;
	}

	@Override
	public Player getPlayer(String matchId, String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selectQuery = "SELECT  * FROM " + TABLE_PLAYERS + " WHERE "
                + KEY_USERNAME + " = " + username + " and " + KEY_MATCH_ID + " = " + matchId;
 
        Log.e(TAG, selectQuery);
 
        Cursor c = db.rawQuery(selectQuery, null);
 
        if (c != null)
            c.moveToFirst();
        
        Player p = new Player();
        p.id = c.getLong(c.getColumnIndex(KEY_ID));
        p.username = c.getString(c.getColumnIndex(KEY_USERNAME));
        //TODO: enumerate match fields
 
        return p;
	}

	@Override
	public Match getFirstMatch() {
		List<Match> matches = this.getAllMatches();
		if(matches.size() > 0) {
			return matches.get(0);
		}
		return null;
	}
}