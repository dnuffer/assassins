package com.nbs.client.assassins.models;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "", // will not be used
mailTo = "camsoupa@gmail.com")
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ACRA.init(this);
    }
}
