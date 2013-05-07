/**
 * 
 */
package com.nbs.client.assassins.services;

import android.content.Context;

import com.google.android.gcm.GCMBroadcastReceiver;


public class GCMBroadcastReceiverImpl extends GCMBroadcastReceiver { 
	  @Override
	  protected String getGCMIntentServiceClassName(Context context) { 
		  //android annotations generate source files with trailing underscore
		  //android by default expects class to be named GCMIntentService if this
		  //method is not overridden
		  return "com.nbs.client.assassins.services.GCMIntentService_"; 
	  } 
	}
