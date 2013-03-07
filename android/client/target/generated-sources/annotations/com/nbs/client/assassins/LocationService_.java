//
// DO NOT EDIT THIS FILE, IT HAS BEEN GENERATED USING AndroidAnnotations.
//


package com.nbs.client.assassins;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import com.googlecode.androidannotations.api.BackgroundExecutor;

public final class LocationService_
    extends LocationService
{


    private void init_() {
        locationManager = ((LocationManager) this.getSystemService(Context.LOCATION_SERVICE));
        restClient = new HuntedRestClient_();
        doSomethingAfterInjection();
    }

    @Override
    public void onCreate() {
        init_();
        super.onCreate();
    }

    public static LocationService_.IntentBuilder_ intent(Context context) {
        return new LocationService_.IntentBuilder_(context);
    }

    @Override
    public void updateLocation(final Location l) {
        BackgroundExecutor.execute(new Runnable() {


            @Override
            public void run() {
                try {
                    LocationService_.super.updateLocation(l);
                } catch (RuntimeException e) {
                    Log.e("LocationService_", "A runtime exception was thrown while executing code in a runnable", e);
                }
            }

        }
        );
    }

    public static class IntentBuilder_ {

        private Context context_;
        private final Intent intent_;

        public IntentBuilder_(Context context) {
            context_ = context;
            intent_ = new Intent(context, LocationService_.class);
        }

        public Intent get() {
            return intent_;
        }

        public LocationService_.IntentBuilder_ flags(int flags) {
            intent_.setFlags(flags);
            return this;
        }

        public void start() {
            context_.startService(intent_);
        }

    }

}
