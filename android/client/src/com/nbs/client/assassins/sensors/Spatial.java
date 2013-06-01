package com.nbs.client.assassins.sensors;

public interface Spatial {
	public abstract boolean getRotationMatrix (float[] R, float[] I, float[] gravity, float[] geomagnetic);
	public abstract float[] getOrientation (float[] R, float[] values);
}
