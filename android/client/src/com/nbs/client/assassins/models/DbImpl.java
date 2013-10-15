package com.nbs.client.assassins.models;
 
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.android.gms.maps.model.LatLng;
 
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
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
    private static final String KEY_STATUS = "status";
 
    // MATCHES Table - column names
	private static final String KEY_TOKEN = "token";
    private static final String KEY_CREATOR = "creator";
    private static final String KEY_NAME = "name";
    private static final String KEY_WINNER = "winner";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_CNTDWN_SEC = "cnt_dwn_sec";
    private static final String KEY_END_TIME = "end_time";
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
				KEY_WINNER        + " TEXT," +
				KEY_START_TIME    + " INT," +
				KEY_END_TIME      + " INT," +
				KEY_CNTDWN_SEC    + " INT," +
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
    			KEY_STATUS     + " TEXT," +
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

	private static final Integer INT_NULL_VALUE = Integer.MIN_VALUE;

	private static final Long LONG_NULL_VALUE = Long.MIN_VALUE;

	private static final Double DOUBLE_NULL_VALUE = Double.MIN_VALUE;

	private static final Float FLOAT_NULL_VALUE = Float.MIN_VALUE;
    
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
 
	private ContentValues matchToContentValues(Match m) {
		ContentValues values = new ContentValues();
        values.put(KEY_NAME, m.name);
        values.put(KEY_ID, m.id);           
        //values.put(KEY_TYPE, "assassins", "bounty", "thieves");         
        //values.put(KEY_STATUS, ?);        
        values.put(KEY_TOKEN,   m.token);       
        values.put(KEY_CREATOR, m.creator);            
		values.put(KEY_WINNER,  m.winner);      
		values.put(KEY_START_TIME,    toDbLong(m.startTime));
		values.put(KEY_END_TIME,      toDbLong(m.endTime));
		values.put(KEY_CNTDWN_SEC,    toDbInt(m.countdownSec));   
		values.put(KEY_NW_CORNER_LAT, toDbDouble(m.nwCorner.lat));
		values.put(KEY_NW_CORNER_LNG, toDbDouble(m.nwCorner.lng));
		values.put(KEY_SE_CORNER_LAT, toDbDouble(m.seCorner.lat));
		values.put(KEY_SE_CORNER_LNG, toDbDouble(m.seCorner.lng));
		values.put(KEY_ATTACK_RANGE,  toDbDouble(m.attackRange));
		values.put(KEY_HUNT_RANGE,    toDbDouble(m.huntRange));
		values.put(KEY_ESCAPE_TIME,   toDbInt(m.escapeTime));
        values.put(KEY_CREATED_AT,    getDateTime());
		return values;
	}

	private Match matchFromCursor(Cursor c) {
		Match m = new Match();
		m.id = c.getString(c.getColumnIndex(KEY_ID));
		m.name = c.getString(c.getColumnIndex(KEY_NAME));
		m.startTime = fromDbLong(c,KEY_START_TIME);
		m.endTime = fromDbLong(c,KEY_END_TIME);
		m.winner = c.getString(c.getColumnIndex(KEY_WINNER));
		m.creator = c.getString(c.getColumnIndex(KEY_CREATOR));
		m.countdownSec = fromDbInt(c,KEY_CNTDWN_SEC);
		m.attackRange = fromDbDouble(c,KEY_ATTACK_RANGE);
		m.huntRange = fromDbDouble(c,KEY_HUNT_RANGE);
		m.escapeTime = fromDbInt(c,KEY_ESCAPE_TIME);
		m.token = c.getString(c.getColumnIndex(KEY_TOKEN));
		m.players = getPlayersInMatch(m.id); 
		m.nwCorner = new LatLngData(fromDbDouble(c,KEY_NW_CORNER_LAT), 
				fromDbDouble(c,KEY_NW_CORNER_LNG));
		m.seCorner = new LatLngData(fromDbDouble(c,KEY_SE_CORNER_LAT), 
				fromDbDouble(c,KEY_SE_CORNER_LNG));
		return m;
	}
	
	private synchronized Integer toDbInt(Integer v) {
		return v == null ? INT_NULL_VALUE : v;
	}
	
	private synchronized Float toDbFloat(Float v) {
		return v == null ? FLOAT_NULL_VALUE : v;
	}

	private synchronized Double toDbDouble(Double v) {
		return v == null ? DOUBLE_NULL_VALUE : v;
	}

	private synchronized Long toDbLong(Long v) {
		return v == null ? LONG_NULL_VALUE : v;
	}
	
	private Double fromDbDouble(Cursor c, String key) {
		double v = c.getDouble(c.getColumnIndex(key));
		return v == DOUBLE_NULL_VALUE ? null : v;
	}

	private Integer fromDbInt(Cursor c, String key) {
		int v = c.getInt(c.getColumnIndex(key));
		return v == INT_NULL_VALUE ? null : v;
	}

	private Long fromDbLong(Cursor c, String key) {
		long v = c.getLong(c.getColumnIndex(key));
		return v == LONG_NULL_VALUE ? null : v;
	}
	
    private synchronized Float fromDbFloat(Cursor c, String key) {
		float v = c.getFloat(c.getColumnIndex(key));
		return v == FLOAT_NULL_VALUE ? null : v;
	}

	private ContentValues playerToContentValues(Player p) {
		ContentValues values = new ContentValues();
		values.put(KEY_USERNAME, p.username);
		values.put(KEY_MATCH_ID, p.matchId);
        values.put(KEY_HEALTH, toDbInt(p.health));
		values.put(KEY_STATUS, p.status);
        values.put(KEY_TEAM , p.team);         
		values.put(KEY_ROLE, p.role);          
		values.put(KEY_LAT, toDbDouble(p.lat));              
		values.put(KEY_LNG, toDbDouble(p.lng));             
		values.put(KEY_TARGET_LAT, toDbDouble(p.targetLat));      
		values.put(KEY_TARGET_LNG, toDbDouble(p.targetLng));      
		values.put(KEY_TARGET_BRG, toDbFloat(p.targetBearing));       
		values.put(KEY_TARGET_RNG, p.targetRange);      
		values.put(KEY_ENEMY_RNG, p.enemyRange);
		return values;
	}
	
	private Player playerFromCursor(Cursor c) {
		Player p = new Player();
		p.username = c.getString(c.getColumnIndex(KEY_USERNAME));
		p.id = fromDbLong(c,(KEY_ID));
		p.matchId = c.getString(c.getColumnIndex(KEY_MATCH_ID));
		p.health = fromDbInt(c,(KEY_HEALTH));
		p.status = c.getString(c.getColumnIndex(KEY_STATUS));
		p.team = c.getString(c.getColumnIndex(KEY_TEAM));
		p.role = c.getString(c.getColumnIndex(KEY_ROLE));
		p.targetLat = fromDbDouble(c,(KEY_TARGET_LAT));
		p.targetLng = fromDbDouble(c,(KEY_TARGET_LNG));
		p.targetBearing = fromDbFloat(c,(KEY_TARGET_BRG));
		p.targetRange = c.getString(c.getColumnIndex(KEY_TARGET_RNG));
		p.enemyRange = c.getString(c.getColumnIndex(KEY_ENEMY_RNG));
		return p;
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
 
        ContentValues values = matchToContentValues(m);
        long id = db.insert(TABLE_MATCHES, null, values);
 
        return id;
    }
    
    /* (non-Javadoc)
	 * @see com.nbs.client.assassins.models.Db2#createPlayer(java.lang.String, com.nbs.client.assassins.models.Player)
	 */
    @Override
	public long createPlayer(String matchId, Player p) {
    	SQLiteDatabase db = this.getWritableDatabase();
    	
    	p.matchId = matchId;
    	ContentValues values = playerToContentValues(p);
        
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
                + KEY_ID + " = '" + matchId + "'";
 
        Log.e(TAG, selectQuery);
 
        Cursor c = db.rawQuery(selectQuery, null);

        Match m = null;
        
        if (c != null && c.moveToFirst()) {
        	Bundle b = c.getExtras();
        	for(String key : b.keySet()) {
        		Log.d(TAG, "match cursor as extras");
        		Log.d(TAG, key + " : " + b.get(key));
        	}
        	
        	m = matchFromCursor(c);
        }
 
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
            	Match m = matchFromCursor(c);
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
 
        ContentValues values = matchToContentValues(m);
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
                + KEY_MATCH_ID + " = '" + matchId + "'";
 
        Log.e(TAG, selectQuery);
 
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
            	Player p = playerFromCursor(c);
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
        ContentValues values = playerToContentValues(p);  
        return db.update(TABLE_PLAYERS, values, KEY_MATCH_ID + " = ? AND " + KEY_USERNAME + " = ?",
                new String[] { p.matchId,  p.username });
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
 
        Player p = null;
        
        if (c != null && c.moveToFirst()) {
        	p = playerFromCursor(c);
        }
        if(p != null) Log.d(TAG, p.toString());
        return p;
		
	}

	@Override
	public List<Player> getPlayers(String username) {
        List<Player> players = new ArrayList<Player>();
        String selectQuery = "SELECT  * FROM " + TABLE_PLAYERS + " WHERE "
                + KEY_USERNAME + " = '" + username + "'";
 
        Log.e(TAG, selectQuery);
 
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
            	Player p = playerFromCursor(c);
                players.add(p);
            } while (c.moveToNext());
        }
 
    	return players;
	}

	@Override
	public Player getPlayer(String matchId, String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selectQuery = "SELECT  * FROM " + TABLE_PLAYERS + " WHERE "
                + KEY_USERNAME + " = '" + username + "' AND " + KEY_MATCH_ID + " = '" + matchId + "'";
 
        Log.e(TAG, selectQuery);
 
        Cursor c = db.rawQuery(selectQuery, null);
        
        Player p = null;
        
        try {
	        if (c != null && c.moveToFirst()) {
		        p = playerFromCursor(c);
	        }
        } catch (Exception e) {
        	Log.e(TAG, e.getMessage());
        }
        
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