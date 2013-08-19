package com.nbs.client.assassins.models;

import java.util.UUID;
import com.google.android.gms.maps.model.LatLng;
import com.nbs.client.assassins.services.LocationService;
import com.nbs.client.assassins.utils.Bus;
import com.nbs.client.assassins.utils.KeyValueStore;
import com.nbs.client.assassins.utils.LocationUtils;
import android.content.Context;
import android.location.Location;

public class UserModel extends KeyValueStore {
    private static final String ID = "install_id";
	private static final String TOKEN = "token";
	private static final String USERNAME = "username";
	private static final String TAG = "UserModel";
	public  static final String USER_TOKEN_RECEIVED = "com.nbs.client.assassins.USER_TOKEN_CHANGED";
	public static final String LOGOUT_COMPLETE = "com.nbs.client.assassins.LOGOUT_COMPLETE";

    public synchronized static String getInstallId(Context c) {
        String installId = getString(c,ID);
    	if (installId == null) {
    		installId = UUID.randomUUID().toString();
            putString(c, ID, installId);
        }
        return installId;
    }
    
    public static String getUsername(Context c) {
	    return getString(c,USERNAME);
    }
    
    public synchronized static void setUsername(Context c, String username) {
    	putString(c,USERNAME, username);
    }
    
    public static boolean hasUsername(Context c) {
    	return getUsername(c) != null;
    }
    
    public static String getToken(Context c) {
        return getString(c,TOKEN);
    }
    
    public synchronized static void setToken(Context c, String token) {
        putString(c,TOKEN, token);
        if(token != null) {
			Bus.post(c,USER_TOKEN_RECEIVED);
        }
    }
    
    public static boolean hasToken(Context c) {
    	return getToken(c) != null;
    }

	public static LatLng getLocation(Context c) {
		return getLatLng(c, "my");
	}

	public synchronized static void setLocation(Context c, Location lastLocation) {
    	putLatLng(c, "my", LocationUtils.locationToLatLng(lastLocation));
	}
	
	public synchronized static void setLocation(Context c,
			double lat, double lng) {
		LatLng oldLoc = UserModel.getLocation(c);
		LatLng newLoc = new LatLng(lat,lng);
    	putLatLng(c,"my", newLoc);
    	
    	if(oldLoc == null || !oldLoc.equals(newLoc)) {
    		Bus.post(c,LocationService.LOCATION_UPDATED);
    	}
	}  

	public static boolean loggedIn(Context c) {
		return UserModel.hasToken(c) && UserModel.hasUsername(c);
	}
	
	public synchronized static void signOut(Context c) {
		Bus.post(c, UserModel.LOGOUT_COMPLETE);
		MatchModel.setMatch(c, null);
		UserModel.setUsername(c, null);
		UserModel.setToken(c, null);
	}
	
	public static String _toString(Context c) {
		return "[token=" + getToken(c) + ", username="+ getUsername(c) + ", install_id=" + getInstallId(c) +
				"match=" + MatchModel.getMatch(c) + ", " + "location=" + getLocation(c) + "]" ;
	}
}
