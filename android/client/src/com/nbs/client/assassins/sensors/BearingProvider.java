package com.nbs.client.assassins.sensors;


public interface BearingProvider {
	public void registerForBearingUpdates(BearingReceiver receiver);
	public void unregisterForBearingUpdates(BearingReceiver receiver);
}