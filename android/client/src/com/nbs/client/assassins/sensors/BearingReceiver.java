package com.nbs.client.assassins.sensors;

public interface BearingReceiver {
	public void setBearingProvider(BearingProvider provider);
	public void onBearingChanged(float bearing);
}
