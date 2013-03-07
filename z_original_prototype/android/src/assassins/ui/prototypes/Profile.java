package assassins.ui.prototypes;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;
import org.bson.types.ObjectId;

public class Profile implements Parcelable{
	
	/*
	 * Enforce: unique username
	 * 
	 * phone_id and username are both unique.
	 * phone_id is a uniquely generated UUID at installation.
	 * 
	 * If a player installs the app on a new phone, a new UUID is generated.
	 * On app launch, the UUID is sent to the web service to verify the user's ID.
	 * In the case of a new install, the UUID is not recognized, so the player must authenticate
	 * with name and password.
	 * 
	 * The new UUID replaces the old UUID and an e-mail should be sent to the user indicating that
	 * their account is now bound to a new installation.
	 * 
	 */
	private ObjectId objectId;
	private String installId;
	private String name;
	private String username;
	private String password;
	private String email;
	private String rank;
	private int score;
	private ArrayList<Achievement> achievements;
	private Conspiracy currMatch;
	private ObjectId playerstateId;
	
	public Profile(){}
	
	public Profile(String username){ this.username = username; }

	//creating a new player
	public Profile(String name, String username, String password, String email)
	{
		this.name = name;
		this.username = username;
		this.password = password;
		this.email = email;
	}
	
	//an authenticated player
	public Profile(String name, String username, String password, String email, 
					String phoneId, String rank, int score, ArrayList<Achievement> achievements, 
					Conspiracy currentMatch, ObjectId gameState)
	{
		this.name = name;
		this.username = username;
		this.password = password;
		this.email = email;
		this.installId = phoneId;
		this.rank = rank;
		this.score = score;
		this.achievements = achievements;
		this.currMatch = currentMatch;
		this.playerstateId = gameState;
	}
	
	public Profile(Parcel in){
		readFromParcel(in);
	}
	
	private void readFromParcel(Parcel in) {
		name = in.readString();
		username = in.readString();
		password= in.readString();
		email = in.readString();
		installId = in.readString();
		rank = in.readString();
		score = in.readInt();
		//FIX ME! how to pass the array of achievements?
		achievements = new ArrayList<Achievement>();
		currMatch = null;
		objectId = ObjectId.massageToObjectId(in.readString());
		playerstateId = ObjectId.massageToObjectId(in.readString());
	}

	public ObjectId getObjectId()
	{
		return objectId;
	}
	
	public void setObjectId(ObjectId id)
	{
		objectId = id;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getInstallId() {
		return installId;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public ArrayList<Achievement> getAchievementsCompleted() {
		return achievements;
	}

	public void setAchievementsCompleted(
			ArrayList<Achievement> achievements_completed) {
		this.achievements = achievements_completed;
	}

	public Conspiracy getCurrentMatch() {
		return currMatch;
	}

	public void setCurrentMatch(Conspiracy c) {
		this.currMatch = c;
	}

	public ObjectId getPlayerstateId() {
		return playerstateId;
	}

	public void setPlayerstateId(ObjectId playerstateId) {
		this.playerstateId = playerstateId;
	}

	public ArrayList<Achievement> getHonors(){
		return achievements;
	}
	
	public String getRank(){
		return rank;
	}
	
	public void setRank(String rank){
		this.rank = rank;
	}
	
	public String getName(){
		return name;
	}

	public void setName(String newName) {
		name = newName;
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(username);
		dest.writeString(password);
		dest.writeString(email);
		dest.writeString(installId);
		dest.writeString(rank);
		dest.writeInt(score);
		//FIX ME! how to pass the array of achievements?
		//Could just use json to serialize here, 
		//otherwise, need a classloader to readFromParcel
		//the classloader is not very clear
		//dest.writeParcelable(currMatch, 0);
		if(objectId != null)
			dest.writeString(objectId.toString());
		if(playerstateId != null)
			dest.writeString(playerstateId.toString());
	}

	public void setInstallId(String id) {
		installId = id;		
	}
	
	public String toString()
	{
		return "Name: " + this.name + " Username " + this.username + "installId: "+ 
					this.installId +" objectId: " + this.objectId;
	}
}
