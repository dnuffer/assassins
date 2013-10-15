package com.nbs.client.assassins.models;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "", // will not be used
mailTo = "camsoupa@gmail.com")
public class App extends Application {
  
	private static Repository repo;
	
	@Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
        repo = new RepositoryImpl(this.getApplicationContext());
    }
    
	//for mocking
	//TODO: inject Db dependency when the 
	//      android annotations dependency is removed
	public void setRepo(Repository db) {
		this.repo = db;
	}
	
    public static synchronized Repository getRepo() {
    	return repo;
    }
}
