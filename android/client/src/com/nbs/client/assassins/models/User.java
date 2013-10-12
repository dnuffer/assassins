package com.nbs.client.assassins.models;

import java.util.UUID;
import com.google.android.gms.maps.model.LatLng;
import com.nbs.client.assassins.services.LocationService;
import com.nbs.client.assassins.utils.Bus;
import com.nbs.client.assassins.utils.KeyValueStore;
import com.nbs.client.assassins.utils.LocationUtils;
import android.content.Context;
import android.location.Location;

public class User extends KeyValueStore {
	private static final String TAG = "User";
    private static final String ID = "install_id";
	private static final String TOKEN = "token";
	private static final String USERNAME = "username";
	public  static final String LOGIN_COMPLETE = "com.nbs.client.assassins.USER_TOKEN_CHANGED";
	public static final String LOGOUT_COMPLETE = "com.nbs.client.assassins.LOGOUT_COMPLETE";
	public static final String FOCUSED_GAME_CHANGED = "com.nbs.client.assassins.FOCUSED_GAME_CHANGED";
	private Context c;

	public User(Context c) {
		this.c = c;
	}
	
    public synchronized String getInstallId() {
        String installId = getString(c,ID);
    	if (installId == null) {
    		installId = UUID.randomUUID().toString();
            putString(c, ID, installId);
        }
        return installId;
    }
    
    public String getUsername() {
	    return getString(c,USERNAME);
    }
    
    public synchronized void setUsername(String username) {
    	putString(c,USERNAME, username);
    }
    
    public boolean hasUsername() {
    	return getUsername() != null;
    }
    
    public String getToken() {
        return getString(c,TOKEN);
    }
    
    public synchronized void setToken(String token) {
        putString(c,TOKEN, token);
        if(token != null) {
			Bus.post(c,LOGIN_COMPLETE);
        }
    }
    
    public boolean hasToken() {
    	return getToken() != null;
    }

	public LatLng getLocation() {
		return getLatLng(c, "my");
	}

	public synchronized void setLocation(Location lastLocation) {
    	putLatLng(c, "my", LocationUtils.locationToLatLng(lastLocation));
	}
	
	public synchronized void setLocation(double lat, double lng) {
		LatLng oldLoc = getLocation();
		LatLng newLoc = new LatLng(lat,lng);
    	putLatLng(c,"my", newLoc);
    	
    	if(oldLoc == null || !oldLoc.equals(newLoc)) {
    		Bus.post(c,LocationService.LOCATION_UPDATED);
    	}
	}  

	public boolean isLoggedIn() {
		return hasToken() && hasUsername();
	}
	
	public synchronized void logout() {
		Bus.post(c, User.LOGOUT_COMPLETE);
		setUsername(null);
		setToken(null);
	}
	
	@Override
	public String toString() {
		return "[ token="       + getToken() + 
				", username="   + getUsername() + 
				", install_id=" + getInstallId() +
				", location="   + getLocation() + " ]" ;
	}

	public String getFocusedMatch() {
		return getString(c, "focused_match");
	}

	public void setFocusedMatch(String matchId) {
		putString(c, "focused_match", matchId);
		Bus.post(c, User.FOCUSED_GAME_CHANGED);
	}

	public void login(String username, String token) {
		setUsername(username);
		setToken(token);
		Bus.post(c, User.LOGIN_COMPLETE);
		
	}
}
