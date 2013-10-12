package com.nbs.client.assassins.models;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "", // will not be used
mailTo = "camsoupa@gmail.com")
public class App extends Application {
  
	private Repository repo;
	
	@Override
    public void onCreate() {
        super.onCreate();

        ACRA.init(this);
    }
    
	//for mocking
	//TODO: inject Db dependency when the 
	//      android annotations dependency is removed
	public void setRepo(Repository db) {
		this.repo = db;
	}
	
    public synchronized Repository getRepo() {
    	if (repo == null) {
    		repo = new RepositoryImpl(this.getApplicationContext());
    	}
    	
    	return repo;
    }
    
}
