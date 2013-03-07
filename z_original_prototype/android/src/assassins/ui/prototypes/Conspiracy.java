package assassins.ui.prototypes;

import java.lang.String;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.bson.types.ObjectId;

import com.google.android.maps.GeoPoint;
import com.google.gson.JsonElement;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import assassins.ui.prototypes.Profile;

public class Conspiracy extends MyParcelable {
	
	private ObjectId id;
	private String owner;
	private String name;
	private String password;
	private MatchType type;
	private GeoPoint[] area;
	private Calendar start_time; 
	private ArrayList<PlayerState> players;
	private int maxPlayers;
	private boolean isPublic;
	private float huntRange;
	private float attackRange;
	private int attackDelay;
	
	
    public static final Parcelable.Creator<Conspiracy> CREATOR
    = new Parcelable.Creator<Conspiracy>() {
	public Conspiracy createFromParcel(Parcel in) {
	    return new Conspiracy(in);
	}
	
	public Conspiracy[] newArray(int size) {
	    return new Conspiracy[size];
	}
	};
	private static final float FEET_PER_MILE = 5280;
	
	public Conspiracy()
	{
		owner = "";
		isPublic = false;
		type = MatchType.ASSASSINS;
		players = new ArrayList<PlayerState>();
		maxPlayers = 10;
		area = new GeoPoint[2];
		huntRange = -1.0f;
		attackRange = -1.0f;
		attackDelay = -1;
	}
	
	public Conspiracy(Parcel in)
	{
		owner = "";
		players = new ArrayList<PlayerState>();
		area = new GeoPoint[2];
		readFromParcel(in);
	}
	
	public Conspiracy(String n, long stime)
	{
		
		this.name = n;
		
		Calendar startTime = new GregorianCalendar();
		Date d = new Date();
		d.setTime(stime);
		startTime.setTime(d);
		start_time = startTime;

	}
	
	public int describeContents() {
		return 0;
	}

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(owner);
        dest.writeString(name);
        dest.writeString(password); //very secure :)
        dest.writeInt(matchTypeToInt(type));
        dest.writeInt(area[0].getLatitudeE6());
        dest.writeInt(area[0].getLongitudeE6());
        dest.writeInt(area[1].getLatitudeE6());
        dest.writeInt(area[1].getLongitudeE6());
        dest.writeLong(start_time.getTimeInMillis());
        dest.writeList(players);
        dest.writeInt(maxPlayers);
        boolean[] temp = new boolean[1]; //parcelable doesn't have a write boolean, just writeBooleanArray :(
        temp[0] = isPublic;
        dest.writeBooleanArray(temp);
        dest.writeFloat(huntRange);
        dest.writeFloat(attackRange);
        dest.writeInt(attackDelay);
		if(id != null)
			dest.writeString(id.toString());
        
    }
    
    public ArrayList<PlayerState> getPlayers()
    {
    	return players;
    }
 
    private int matchTypeToInt(MatchType t) {
    	
    	int matchTypeAsNum = -1;
    	
    	switch(t)
    	{
    		case ASSASSINS: matchTypeAsNum = 0;
    		break;
    		case BOUNTY: 	matchTypeAsNum = 1;
    		break;
    		case SCOURGE: 	matchTypeAsNum = 2;
    		break;
    		case THIEVES: 	matchTypeAsNum = 3;
    		break;
    	}
    	return matchTypeAsNum;
	}

	public void readFromParcel(Parcel in) {
		  	
			owner = in.readString();
			name = in.readString();
			password = in.readString(); //very secure :)
	        type = intToMatchType(in.readInt());
	        area[0] = new GeoPoint(in.readInt(), in.readInt());
	        area[1] = new GeoPoint(in.readInt(), in.readInt());
	        start_time = new GregorianCalendar();
	        start_time.setTimeInMillis(in.readLong());
	        players = new ArrayList<PlayerState>();
	        in.readList(players, PlayerState.class.getClassLoader());
	        maxPlayers = in.readInt();
	        boolean[] temp = new boolean[1]; //work around
	        in.readBooleanArray(temp);
	        isPublic = temp[0];
	        huntRange = in.readFloat();
	        attackRange = in.readFloat();
	        attackDelay = in.readInt(); 
			id = ObjectId.massageToObjectId(in.readString());
    }

	public float getHuntRange() {
		return huntRange;
	}

	public void setHuntRange(float huntRange) {
		this.huntRange = huntRange;
	}

	private MatchType intToMatchType(int t) {
		
		switch(t)
    	{
    		case 0: return MatchType.ASSASSINS;
    		case 1: return MatchType.BOUNTY;
    		case 2: return MatchType.SCOURGE;
    		case 3: return MatchType.THIEVES;
    	}
    	return null;
	}
	
	
	public void setPublic(boolean privacy) {
		isPublic = privacy;
	}
	
	public boolean isPublic() {
		return isPublic;
	}
	public int getPrivacy() {
		if(isPublic)
			return 1;
		
		return 0;
	}
	
	public void setCorners(GeoPoint a, GeoPoint b) {
		area[0] = a;
		area[1] = b;
	}
	
	public GeoPoint[] getArea() {
		return area;
	}
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLoc() {
		return area[0].toString() + " " + area[1].toString();
	}

	public MatchType getType() {
		return type;
	}
	
	public int getTypeInt() {
		
		int intType = -1;
		
		switch(type)
	      {
	      	case ASSASSINS:
	      		intType=0;
    		break;
	      	case BOUNTY:
	      		intType=1;
    		break;
	      	case SCOURGE:
	      		intType=2;
    		break;
	      	case THIEVES:
	      		intType=3;
    		break;
	      	default:
	      	break;
	      }
		return intType;
	}

	public void setType(MatchType type) {
		this.type = type;
	}
	
	public void setType(String typeAsStr) {
		if(typeAsStr.equalsIgnoreCase("ASSASSINS"))
			this.type = MatchType.ASSASSINS;
		else if(typeAsStr.equalsIgnoreCase("BOUNTY"))
			this.type = MatchType.BOUNTY;
		else if(typeAsStr.equalsIgnoreCase("SCOURGE"))
			this.type = MatchType.SCOURGE;
		else if(typeAsStr.equalsIgnoreCase("THIEVES"))
			this.type = MatchType.THIEVES;
		else
			Log.d("Project Assassins", "unrecognized matchtype: "+typeAsStr);
	}

	public void setStartTime(Calendar c) {
		start_time = c;
	}
	
	public Calendar getStart() {
		return start_time;
	}
	
	public long getStartUTC() {
		return start_time.getTimeInMillis()/1000;
	}
	
	public boolean addPlayer(PlayerState playerState) {
		if (players.size() < maxPlayers-1){
			players.add(playerState);
			return true;
		}
		else
			return false;
	}
	
	public void removePlayer(Profile newPlayer) {
		players.remove(newPlayer);
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}
	
	public boolean isComplete()
	{
		
		if((( !isPublic() && password != null && password.length() > 0 ) || isPublic() ) &&
			 start_time != null &&
			 area[0] !=null && 
			 name !=null && 
			 name.length() > 0 &&
			 attackDelay > 0)
		{
			return true;
		}	
		
		return false;
	}
	public String getNotCompletedString()
	{
		String notComplete = new String();
		notComplete+="Please Set:\n\n";
		if(name == null || name.length() == 0)
			notComplete += "Match Name\n";
		if(!isPublic() && (password == null || password.length() == 0))
			notComplete += "Password (if private)\n";
		if(area[0] == null || area[1] == null)
			notComplete += "Boundaries\n";
		if(start_time == null)
			notComplete += "Start Time\n";
		if(attackDelay == -1)
		{
			notComplete += "Gameplay Settings\n";
		}

		return notComplete;
	}
	
	@Override
	public String toString()
	{
		return "Creator: "+getOwner() + "\n" +
				"Match Name: "+getName() + "\n" +
				"Match Type: "+getType() + "\n" +
				"Location: "+getLoc() + "\n" +
				"Start Time: "+getStart().getTime().toString() + "\n";
		
	}

	public GeoPoint getNorthwestCorner() {
		return area[0];
	}



	public GeoPoint getSoutheastCorner() {
		return area[1];
	}

	public float getAttackRange() {
		return attackRange;
	}

	public void setAttackRange(float attackRange) {
		this.attackRange = attackRange;
	}

	public int getAttackDelay() {
		return attackDelay;
	}

	public void setAttackDelay(int attackDelay) {
		this.attackDelay = attackDelay;
	}

	public String getSettingsString() {
		return "" + (int)(huntRange*FEET_PER_MILE) + " ft, "+
				 "" + (int)(attackRange*FEET_PER_MILE) + " ft, "+
				 "" + attackDelay + " sec"; 
	}
	
	public ObjectId getObjectId()
	{
		return id;
	}
	
	public void setObjectId(ObjectId id)
	{
		this.id = id;
	}

}

