//
// DO NOT EDIT THIS FILE, IT HAS BEEN GENERATED USING AndroidAnnotations.
//


package com.nbs.client.assassins;

import android.content.Context;
import android.content.Intent;

public final class GCMIntentService_
    extends GCMIntentService
{


    private void init_() {
        restClient = new HuntedRestClient_();
    }

    @Override
    public void onCreate() {
        init_();
        super.onCreate();
    }

    public static GCMIntentService_.IntentBuilder_ intent(Context context) {
        return new GCMIntentService_.IntentBuilder_(context);
    }

    public static class IntentBuilder_ {

        private Context context_;
        private final Intent intent_;

        public IntentBuilder_(Context context) {
            context_ = context;
            intent_ = new Intent(context, GCMIntentService_.class);
        }

        public Intent get() {
            return intent_;
        }

        public GCMIntentService_.IntentBuilder_ flags(int flags) {
            intent_.setFlags(flags);
            return this;
        }

        public void start() {
            context_.startService(intent_);
        }

    }

}
