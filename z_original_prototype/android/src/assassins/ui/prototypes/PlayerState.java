package assassins.ui.prototypes;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.maps.GeoPoint;

public class PlayerState extends MyParcelable {
	
	private int life;
	private GeoPoint location;
	private String username;
	private Proximity myProximityToTarget;
	private Proximity enemyProximityToMe;
	
    public static final Parcelable.Creator<PlayerState> CREATOR
    = new Parcelable.Creator<PlayerState>() {
	public PlayerState createFromParcel(Parcel in) {
	    return new PlayerState(in);
	}
	
	public PlayerState[] newArray(int size) {
	    return new PlayerState[size];
	}
	};
	
	
	public PlayerState(Parcel in)
	{
		readFromParcel(in);
	}
	
	
	private void readFromParcel(Parcel in) {
		life = in.readInt();
		username = in.readString();
		int lat = in.readInt();
		int lon = in.readInt();
		if(lat != -1 && lon != -1)
		{
			location = new GeoPoint(lat,lon);
		}
		else
		{
			location = null;
		}
		myProximityToTarget = Enum.valueOf(Proximity.class, in.readString());
		enemyProximityToMe = Enum.valueOf(Proximity.class, in.readString());
		
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(life);
		dest.writeString(username);
		if(location != null)
		{
			dest.writeInt(location.getLatitudeE6());
			dest.writeInt(location.getLongitudeE6());
		}
		else
		{
			dest.writeInt(-1);
			dest.writeInt(-1);
		}
		dest.writeString(myProximityToTarget.toString());
		dest.writeString(enemyProximityToMe.toString());
	}

	public PlayerState(String username, int life, GeoPoint location, Proximity mine, Proximity enemy) {
		this.setUsername(username);
		this.life =  life;
		this.location = location;
		this.myProximityToTarget = mine;
		this.enemyProximityToMe = enemy;
	}
	
	public PlayerState(String username, int life, GeoPoint location) {
		this.setUsername(username);
		this.life =  life;
		this.location = location;
		this.myProximityToTarget = Proximity.UNKNOWN_RANGE;
		this.enemyProximityToMe = Proximity.UNKNOWN_RANGE;
	}
	
	public int getLife() {
		return life;
	}
	
	public GeoPoint getLocation() {
		return location;
	}

	public void setLife(int i) {
		life = i;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public boolean hasDifferentUserName(PlayerState other)
	{
		if(other == null)
			return false;
		
		return (this.getUsername() == other.getUsername());
	}
	
	public int compareLife(PlayerState other)
	{
		if(other != null)
		{
			if(other.getLife() > this.getLife())
			{
				return -1;
			}
			else if(other.getLife() < this.getLife())
			{
				return 1;
			}
		}
		return 0;
	}

	public Proximity getMyProximityToTarget() {
		return myProximityToTarget;
	}

	public void setMyProximityToTarget(Proximity myProximityToTarget) {
		this.myProximityToTarget = myProximityToTarget;
	}

	public Proximity getEnemyProximityToMe() {
		return enemyProximityToMe;
	}

	public void setEnemyProximityToMe(Proximity enemyProximityToMe) {
		this.enemyProximityToMe = enemyProximityToMe;
	}

	
	
	
}
