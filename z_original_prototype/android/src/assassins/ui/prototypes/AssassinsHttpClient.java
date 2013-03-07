package assassins.ui.prototypes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.gson.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.bson.types.ObjectId;


public class AssassinsHttpClient {

	private String serviceURL;
	private Context context;
	
	public AssassinsHttpClient(String url, Context c) {
		
		serviceURL = url;
		context = c;
	}
	
	public Profile authenticatePlayer(Context c) throws AssassinsHttpException {
		
		String installId = Installation.id(c);
		//send installId to service to see if this device has a profile
		String response = get("/protected/authenticate/", installId);
		
		Log.d("Project Assassins", response);
		
		if(response.equals("null"))
			throw new AssassinsHttpException("Profile not found");
		
		Gson g = new GsonBuilder().registerTypeAdapter(Profile.class, new ProfileDeserializer()).setPrettyPrinting().create();
		
		Profile myProfile = g.fromJson(response, Profile.class);
		
		Log.d("Project Assassins", myProfile.toString());
		
		return myProfile;
	}
	
	
	/*
	 * FUNCTION: updateLocation
	 * PARAMS: ObjectId id - the local player's ObjectId (NOT installId)
	 * RETURNS: GameSnapshot myGame - contains bearing to target, target's state and player's state
	 */
	
	public GameSnapshot updateLocation(ObjectId id) throws AssassinsHttpException {

		GameSnapshot gs = null;
		
		GeoPoint geo = PlayerLocation.getMyLocationAsGeoPoint(context);
		
		if(geo != null)
		{	
			int lat = geo.getLatitudeE6();
			int lon = geo.getLongitudeE6();
			Log.d("Project Assassins", "my location in microdegrees: "+lat+" "+lon);
			
			String  response = post("/update/location/"+id.toString()+"/"+lat+"/"+lon, "");
	
			Log.d("ProjectAssassins", "update location response: "+response);
			
			//FIX ME!
			Gson g = new GsonBuilder().registerTypeAdapter(GameSnapshot.class, new GameSnapshotDeserializer())
									  .create();
			gs = g.fromJson(response, GameSnapshot.class);
			
		}
		
		return gs;
	}
	
	
	public GameSnapshot attackTarget(ObjectId profileId) throws AssassinsHttpException {
		
		GameSnapshot gs = null;
		
		GeoPoint geo = PlayerLocation.getMyLocationAsGeoPoint(context);

		if(geo != null)
		{	
			int lat = geo.getLatitudeE6();
			int lon = geo.getLongitudeE6();
			
			String attackResponse = post("/attack/"+profileId.toString()+"/"+lat+"/"+lon, "");
			Log.d("Project Assassins", "attack response: "+attackResponse);
			
			Gson g = new GsonBuilder().registerTypeAdapter(GameSnapshot.class, new GameSnapshotDeserializer())
													.create();
			
			gs = g.fromJson(attackResponse, GameSnapshot.class);
		
		}
		return gs;
	}


	private String geoPointToJson(String name, GeoPoint geo) {
		Gson g = new GsonBuilder()
					.registerTypeAdapter(GeoPoint.class, new GeoPointSerializer())
					.create();

		Log.d("ProjectAssassins", "geoPointToJson() name: "+name +" point: "+geo);
		JsonArray geoArray = (JsonArray) g.toJsonTree(geo, GeoPoint.class);
		Log.d("ProjectAssassins", "JsonArray of point: "+geoArray);
		JsonObject jo = new JsonObject();
		jo.add(name, geoArray);
		Log.d("ProjectAssassins", "geo json: "+jo.getAsString());
		return jo.getAsString();
	}

	
	public Conspiracy getCurrentMatch(ObjectId id) throws AssassinsHttpException {
				
		String response = get("/current/match/", id.toString());
		
		Gson g = new GsonBuilder().registerTypeAdapter(Conspiracy.class, new ConspiracyDeserializer())
				  .create();
		Conspiracy c = g.fromJson(response, Conspiracy.class);
		return c;
		
	}
	
	public Conspiracy getSecretMatch(ObjectId profileId, String matchName, String password) throws AssassinsHttpException {
		String response = get("/secret/match/", matchName+"/"+password+"/"+profileId.toString());
		
		Gson g = new GsonBuilder().registerTypeAdapter(Conspiracy.class, new ConspiracyDeserializer())
				  .create();
		Conspiracy c = g.fromJson(response, Conspiracy.class);
		return c;
	}
	
	public void importProfile(String phoneId) throws AssassinsHttpException {
		//UNIMPLEMENTED
	}
	
	
	public Conspiracy joinMatch(String installId, String matchId) throws AssassinsHttpException {
		
		String response = get("/protected/join/match/", matchId +"/"+installId);
		
		//FIX ME! clean this up
		if(response.equals("null"))
			throw new AssassinsHttpException("Match not found");
		
		Gson g = new GsonBuilder().registerTypeAdapter(Conspiracy.class, new ConspiracyDeserializer())
				  .create();
		Conspiracy c = g.fromJson(response, Conspiracy.class);
		
		return c;
	}
	
	public Profile createProfile(Profile p) throws AssassinsHttpException {
		
		Gson g = new GsonBuilder().registerTypeAdapter(Profile.class, new ProfileSerializer())
								  .create();
		
		String jsonPlayer = g.toJson(p);
		String response = post("/protected/profile", jsonPlayer);
		
		Profile newProfile = null;
		
		if(response != null)
		{
			Gson profileDeserializer = new GsonBuilder().registerTypeAdapter(Profile.class, new ProfileDeserializer())
					  .create();
			newProfile = profileDeserializer.fromJson(response, Profile.class);
		}
		
		Log.d("Project Assassins", "AssassinsHttpClient.createProfile() Profile: " + newProfile);
		
		return newProfile;
	}
	
	public void createMatch(Conspiracy c, ObjectId profileId) throws AssassinsHttpException {
		
		Gson g = new GsonBuilder().registerTypeAdapter(Conspiracy.class, new ConspiracySerializer())
								  .create();
		
		String jsonMatch = g.toJson(c, Conspiracy.class);
		post("/protected/match/"+profileId.toStringMongod(), jsonMatch);
	}
	
	public ArrayList<Conspiracy> getPublicMatches(ObjectId profileId) throws AssassinsHttpException {		
		
		GeoPoint geo = PlayerLocation.getMyLocationAsGeoPoint(context);
		//String json = geoPointToJson("location", geo);
		
		//FIX ME!  Need a location fix to get public matches in area
		if(geo == null)
		{
			Log.d("Project Assassins", "getPublicMatches(...) Could not get location fix.  Sending arbitrary location.");
			geo = new GeoPoint(-1,-1);
		}
		
		String response = get("/protected/public/matches/"+profileId.toStringMongod()+"/", 
										geo.getLatitudeE6()+"/"+geo.getLongitudeE6());
		
		Gson g = new GsonBuilder().registerTypeHierarchyAdapter(ArrayList.class, 
									new ConspiracyArrayDeserializer())
									.create();

		ArrayList<Conspiracy> matches = g.fromJson(response, ArrayList.class);
		Log.d("Project Assassins", "public matches: "+matches.toString());
		
		//Testing Code
//		ArrayList<Conspiracy> matches = new ArrayList<Conspiracy>();
//		
//		matches.add(new Conspiracy("Cam's Conspiracy", 1338521572));
//		matches.add(new Conspiracy("Dave's Conspiracy", 1338521577));
//		matches.add(new Conspiracy("Dan's Conspiracy", 1338521599));
		
		
		return matches;
	}
	
	private String post(String route, String json) throws AssassinsHttpException {
		 
		// Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpResponse response = null;
	    
	    BufferedReader in = null;
	    
	    Log.d("Project Assassins", serviceURL+route);
	    HttpPost httppost= new HttpPost(serviceURL+route);
	    
	    try {
	        // Add data
	        httppost.setEntity(new StringEntity(json));
	        Log.d("Project Assassins", httppost.toString());
	        // Execute HTTP Put Request
	        response = httpclient.execute(httppost);
	        
	        if(response.getStatusLine().getStatusCode() != 200) {
	        	String reasonPhrase = response.getStatusLine().getReasonPhrase();
	        	throw new AssassinsHttpException(reasonPhrase);
	        }
	        else{
	    	    
				 
	        	 in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				 StringBuffer sb = new StringBuffer("");
				 String line = "";
				 
				 while ((line = in.readLine()) != null) {
				     Log.d("Project Assassins", "Http Response Line: "+line);
					 sb.append(line);
				 }
				 
				 in.close();
				 String content = sb.toString();
				 return content;
            } 
	    } 
	    catch (ClientProtocolException e) {
	    	throw new AssassinsHttpException("ClientProtocolException", e);
	    } 
	    catch (IOException e) {
	    	throw new AssassinsHttpException("IO Exception", e);
	    }
	    catch (Exception e) {
	    	throw new AssassinsHttpException("Unknown Exception", e);
	    }
	}
	
	
	private void put(String route, String json) throws AssassinsHttpException {
		 
		// Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpResponse response = null;
	    
	    Log.d("Project Assassins", serviceURL+route+json);
	    HttpPut httpput= new HttpPut(serviceURL+route);
	    
	    try {
	        // Add data
	    	httpput.setEntity(new StringEntity(json));
	        Log.d("Project Assassins", httpput.toString());
	        // Execute HTTP Put Request
	        response = httpclient.execute(httpput);
	        
	        if(response.getStatusLine().getStatusCode() != 200) {
	        	String reasonPhrase = response.getStatusLine().getReasonPhrase();
	        	throw new AssassinsHttpException(reasonPhrase);
	        }
	    } 
	    catch (ClientProtocolException e) {
	    	throw new AssassinsHttpException("ClientProtocolException", e);
	    } 
	    catch (IOException e) {
	    	throw new AssassinsHttpException("IO Exception", e);
	    }
	    catch (Exception e) {
	    	throw new AssassinsHttpException("Unknown Exception", e);
	    }
	}
	
	
	
	private String get(String route, String params) throws AssassinsHttpException {
		 
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpResponse response = null;
	    
	    BufferedReader in = null;
	    
	    Log.d("Project Assassins", "HTTP GET: "+serviceURL+route+params);
	    HttpGet httpget= new HttpGet(serviceURL+route+params);
	    
	    try {
	        // Add data

	        Log.d("Project Assassins", httpget.toString());
	        // Execute HTTP Put Request
	        response = httpclient.execute(httpget);
	        
	        if(response.getStatusLine().getStatusCode() != 200){
	        	String reasonPhrase = response.getStatusLine().getReasonPhrase();
	        	throw new AssassinsHttpException(reasonPhrase);
	        }
	        else{
	    	    
				 
	        	 in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				 StringBuffer sb = new StringBuffer("");
				 String line = "";
				 
				 while ((line = in.readLine()) != null) {
				     Log.d("Project Assassins", "Line: "+line);
					 sb.append(line);
				 }
				 
				 in.close();
				 String content = sb.toString();
				 return content;
             } 

	    } 
	    catch (ClientProtocolException e) {
	    	throw new AssassinsHttpException("ClientProtocolException", e);
	    } 
	    catch (IOException e) {
	    	throw new AssassinsHttpException("IO Exception", e);
	    }
	    catch (Exception e) {
	    	throw new AssassinsHttpException("Unknown Exception", e);
	    }
	    finally {
            if (in != null) {
                try {
                    in.close();
                } 
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}



}


class LocationSerializer implements JsonSerializer<Location>
{

	public JsonElement serialize(Location arg0, Type arg1,
			JsonSerializationContext arg2) {

		JsonPrimitive lat = new JsonPrimitive(arg0.getLatitude());
		JsonPrimitive lon = new JsonPrimitive(arg0.getLongitude());
		
		JsonArray location = new JsonArray();
		
		location.add(lat);
		location.add(lon);

		return location;
	}
}


class GeoPointSerializer implements JsonSerializer<GeoPoint>
{
	public JsonElement serialize(GeoPoint arg0, Type arg1,
			JsonSerializationContext arg2) {

		JsonPrimitive lat = new JsonPrimitive(arg0.getLatitudeE6());
		JsonPrimitive lon = new JsonPrimitive(arg0.getLongitudeE6());
		
		JsonArray loc = new JsonArray();
		
		loc.add(lat);
		loc.add(lon);
		
		return loc;
	}
}

class ProfileSerializer implements JsonSerializer<Profile>{

	public JsonElement serialize(Profile p, Type t,
			JsonSerializationContext c) {

		JsonObject jo = new JsonObject();
		   jo.addProperty("name", p.getName());
		   jo.addProperty("install_id", p.getInstallId());
		   jo.addProperty("email", p.getEmail());
		   jo.addProperty("password", p.getPassword());
		   jo.addProperty("username", p.getUsername());
		   return jo;
	}
}


class ConspiracySerializer implements JsonSerializer<Conspiracy>{

	public JsonElement serialize(Conspiracy c, Type arg1,
			JsonSerializationContext arg2) {

		JsonObject jo = new JsonObject();
		jo.addProperty("name", c.getName());
		jo.addProperty("password", c.getPassword());
		jo.addProperty("type", c.getType().toString());

		Gson g = new GsonBuilder().registerTypeAdapter(GeoPoint.class, new GeoPointSerializer())
									.create();
		
		JsonArray nwCorner = (JsonArray) g.toJsonTree(c.getNorthwestCorner(), GeoPoint.class);
		JsonArray seCorner = (JsonArray) g.toJsonTree(c.getSoutheastCorner(), GeoPoint.class);

		jo.add("nw_corner", nwCorner);
		jo.add("se_corner", seCorner);
		jo.addProperty("start_time", c.getStartUTC());
		jo.addProperty("max_players", c.getMaxPlayers());
		jo.addProperty("is_public", c.isPublic());
		jo.addProperty("hunt_range", c.getHuntRange());
		jo.addProperty("attack_range", c.getAttackRange());
		jo.addProperty("attack_delay", c.getAttackDelay());
		return jo;
	}
}

class ProfileDeserializer implements JsonDeserializer<Profile>{

	public Profile deserialize(JsonElement arg0, Type arg1,
			JsonDeserializationContext arg2) throws JsonParseException {
			
		Log.d("Project Assassins", "deserializing profile: "+arg0);
		
			JsonObject profileJson = arg0.getAsJsonObject();

			Profile p = new Profile();
			
			p.setName(profileJson.get("name").getAsString());
			p.setUsername(profileJson.get("username").getAsString());
			p.setEmail(profileJson.get("email").getAsString());
			p.setInstallId(profileJson.get("install_id").getAsString());
			//FIX ME!  Handle null values for deserialized objects
			//p.setRank(profileJson.get("rank").getAsString());
			//p.setScore(profileJson.get("score").getAsInt());			
			p.setObjectId(ObjectId.massageToObjectId(profileJson.get("id").getAsString()));

			JsonArray hnrs = profileJson.get("achievements_completed").getAsJsonArray();
			
			ArrayList<Achievement> achievements = new ArrayList<Achievement>();
			
			for(int i = 0; i < hnrs.size(); i++)
			{
				achievements.add(new Achievement(hnrs.get(i).getAsString()));
			}
			
			p.setAchievementsCompleted(achievements);
			
			Gson g = new GsonBuilder().registerTypeAdapter(Conspiracy.class, new ConspiracyDeserializer()).create();
			
			p.setCurrentMatch(g.fromJson(profileJson.get("current_match"), Conspiracy.class));


			JsonElement stateIdAsJson = profileJson.get("playerstate_id");
			
			if(stateIdAsJson != JsonNull.INSTANCE)
			{
				p.setPlayerstateId(ObjectId.massageToObjectId(stateIdAsJson.getAsString()));
			}

		return p;
	}


}


class GameSnapshotDeserializer implements JsonDeserializer<GameSnapshot> {
	public GameSnapshot deserialize(JsonElement json, Type arg1,
			JsonDeserializationContext arg2) throws JsonParseException 
	{
		Log.d("Project Assassins", "deserializing gamesnapshot: "+json);
		
		JsonObject snapshotJson = json.getAsJsonObject();
	
		Gson g = new GsonBuilder().registerTypeAdapter(PlayerState.class, new PlayerStateDeserializer())
									.create();
		
		PlayerState myState = g.fromJson(snapshotJson.get("my_state"), PlayerState.class);
		
		JsonElement targetStateJson = snapshotJson.get("target_state");
	
		PlayerState targetState = null;
		
		if(targetStateJson != JsonNull.INSTANCE)
		{
			targetState = g.fromJson(targetStateJson, PlayerState.class);
		}
		
		//may want to wrap this in Bearing class... but can't see a need yet.
		float directionToTarget = snapshotJson.getAsJsonPrimitive("bearing_to_target").getAsFloat();

		GameSnapshot gs = new GameSnapshot(myState, targetState, directionToTarget);
		
		Log.d("Project Assassins", "deserialized gamesnapshot: "+gs.toString());
		
		return gs;
	}
}



class ProximityDeserializer implements JsonDeserializer<Proximity>{
	public Proximity deserialize(JsonElement proximityJson, Type arg1,
			JsonDeserializationContext arg2) throws JsonParseException {
				
		
		String proximityStr = proximityJson.getAsString();
		Log.d("Project Assassins", "deserializing proximity: "+proximityStr);
		
		Proximity p = Proximity.UNKNOWN_RANGE;
		
		if(proximityStr != null)
		{
			if(proximityStr.equals("attack_range"))
				p = Proximity.ATTACK_RANGE;
			else if(proximityStr.equals("hunt_range"))
				p = Proximity.HUNT_RANGE;
			else if(proximityStr.equals("alert_range"))
				p = Proximity.ALERT_RANGE;
			else if(proximityStr.equals("search_range"))
				p = Proximity.SEARCH_RANGE;
			else if(proximityStr.equals("no_target") || proximityStr.equals("no_assassin"))
				p = Proximity.NONE;
		}
		
		return p;
	
	}
}

class PlayerStateDeserializer implements JsonDeserializer<PlayerState> {
	public PlayerState deserialize(JsonElement stateJson, Type arg1,
			JsonDeserializationContext arg2) throws JsonParseException {
		Log.d("Project Assassins", "deserializing playerstate: "+stateJson.toString());
			
		JsonObject stateObj = stateJson.getAsJsonObject();
		
		String username = stateObj.get("username").getAsString();
		int life = stateObj.getAsJsonPrimitive("life").getAsInt();
		
		Gson proximityGson = new GsonBuilder().registerTypeAdapter(Proximity.class, 
										new ProximityDeserializer()).create();
		
		
		Proximity mineToTarget = proximityGson.fromJson(stateObj.get("proximity_to_target"), Proximity.class);
		Proximity enemyToMe    = proximityGson.fromJson(stateObj.get("enemy_proximity"), Proximity.class);
		
		
		Gson g = new GsonBuilder().registerTypeAdapter(GeoPoint.class, new GeoPointDeserializer())
									.create();
		
		JsonArray locAsJsonArray = stateObj.get("location").getAsJsonArray();
		
		GeoPoint location = null;
		
		if(locAsJsonArray.size() == 2)
		{
			location = g.fromJson(locAsJsonArray, GeoPoint.class);
		}


		return new PlayerState(username, life, location, mineToTarget, enemyToMe);
	}
}

class GeoPointDeserializer implements JsonDeserializer<GeoPoint> {

	public GeoPoint deserialize(JsonElement arg0, Type arg1,
			JsonDeserializationContext arg2) throws JsonParseException {
		Log.d("Project Assassins", "deserializing geopoint: "+arg0.toString());
		//json array containing two ints - lat and lon
		JsonArray locationArray = arg0.getAsJsonArray();

		return new GeoPoint(locationArray.get(0).getAsInt(), locationArray.get(1).getAsInt());
	}

}


class ObjectIdDeserializer implements JsonDeserializer<ObjectId> {

	public ObjectId deserialize(JsonElement arg0, Type arg1,
			JsonDeserializationContext arg2) throws JsonParseException {
		return ObjectId.massageToObjectId(arg0.getAsString());
	}
}


class ConspiracyDeserializer implements JsonDeserializer<Conspiracy> {
	public Conspiracy deserialize(JsonElement json, Type arg1,
			JsonDeserializationContext arg2) throws JsonParseException {
		
		Log.d("Project Assassins", "deserializing conspiracy: "+json.toString());
		
		JsonObject matchJson = json.getAsJsonObject();

		Conspiracy c = new Conspiracy();
		
		c.setObjectId(ObjectId.massageToObjectId(matchJson.get("id").getAsString()));
		
		c.setName(matchJson.get("name").getAsString());

		String creator = matchJson.get("creator").getAsString();
		
		c.setOwner(creator);

		float huntRange =matchJson.get("hunt_range").getAsFloat();
		float attackRange=matchJson.get("attack_range").getAsFloat();
		int attackDelay = matchJson.get("attack_delay").getAsInt();
		
		c.setHuntRange(huntRange);
		c.setAttackRange(attackRange);
		c.setAttackDelay(attackDelay);
		
		Gson pointGson = new GsonBuilder().registerTypeAdapter(GeoPoint.class, new GeoPointDeserializer()).create();

		JsonArray nwAsArray = matchJson.get("nw_corner").getAsJsonArray();
		JsonArray seAsArray = matchJson.get("se_corner").getAsJsonArray();
		
		GeoPoint nw = pointGson.fromJson(nwAsArray, GeoPoint.class);
		GeoPoint se = pointGson.fromJson(seAsArray, GeoPoint.class);
		
		c.setCorners(nw, se);

		Calendar startTime = new GregorianCalendar();
		Date d = new Date();
		d.setTime((matchJson.get("start_time").getAsLong()) * 1000);
		startTime.setTime(d);
		c.setStartTime(startTime);

		
		//add string conversion for type in conspiracy class
		c.setType(matchJson.get("type").getAsString());
		c.setPublic(matchJson.get("is_public").getAsBoolean());

		JsonArray thePlayers = matchJson.get("players").getAsJsonArray();

		Gson playersDeserializer = new GsonBuilder().registerTypeAdapter(PlayerState.class, new PlayerStateDeserializer()).create();

		for(int i = 0; i < thePlayers.size(); i++)
		{
			JsonElement playerElement = thePlayers.get(i);
			JsonObject playerObj = null;
			if(playerElement != null)
			{
				playerObj = playerElement.getAsJsonObject();
				if(playerObj != null)
					c.addPlayer(playersDeserializer.fromJson(playerObj, PlayerState.class));
			}
		}
		
		return c;
	}
}
class ConspiracyArrayDeserializer implements JsonDeserializer<ArrayList<Conspiracy>> {
	public ArrayList<Conspiracy> deserialize(JsonElement matches, Type arg1,
			JsonDeserializationContext arg2) throws JsonParseException {
		
		JsonArray conspiracies = matches.getAsJsonArray();
		ArrayList<Conspiracy> list = new ArrayList<Conspiracy>();
		
		for(int i = 0; i < conspiracies.size(); i ++)
		{
			Gson g = new GsonBuilder().registerTypeAdapter(Conspiracy.class, new ConspiracyDeserializer())
					.create();

			Conspiracy c = g.fromJson(conspiracies.get(i).getAsJsonObject(), Conspiracy.class);
			list.add(c);
		}
		return list;
	}
}




