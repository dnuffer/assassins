package com.nbs.client.assassins.sensors;

public class BearingSmoother {

    //raw sensor data
	private float[] mAccelData = new float[3];
    private float[] mMagnetData = new float[3];
    
    //contains the results of getRotationMatrix
    private float[] mRotationMaxtrix = new float[16];
    private float[] mInclinationMatrix = new float[16];
    
    //contains the results of getOrientation: azimuth, pitch and roll
    //currently only using azimuth
    private float[] mOrientation = new float[3];
    private final float rad2deg = (float)(180.0f/Math.PI);
	
    //provides rotation matrix and orientation methods
    private Spatial spatial;
    
    //a time smoothed bearing
	private float bearing;
    
	/*
	 * time smoothing constant for low-pass filter
	 * 0 ≤ alpha ≤ 1 ; a smaller value basically means more smoothing
	 * See: http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
	 */
	private static final float ALPHA = 0.10f;
	
	public BearingSmoother(Spatial spatial) {
		this.spatial = spatial;
	}
	
	public float getBearing() {
		return bearing;
	}

	/* 
	 * @see http://www.codingforandroid.com/2011/01/using-orientation-sensors-simple.html 
	 * @see http://www.netmite.com/android/mydroid/cupcake/development/samples/
	 *      Compass/src/com/example/android/compass/CompassActivity.java
	 */
    
	public void onMagnetometerChanged(float[] values) {
		mMagnetData = lowPass( values.clone(), mMagnetData );
		computeBearing();
	}
	
	public void onAccelerometerChanged(float[] values) {
        mAccelData = lowPass( values.clone(), mAccelData );
        computeBearing();
    }
	
	private void computeBearing()
	{
        //TODO landscape mode is not working
		//also, when bearing crosses 0, there seems to be a problem
        if(spatial.getRotationMatrix(mRotationMaxtrix, mInclinationMatrix, mAccelData, mMagnetData))
        {
        	spatial.getOrientation(mRotationMaxtrix, mOrientation);
        	bearing = (mOrientation[0]*rad2deg); 		
        }
	}
	
	/**
	 * @see http://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
	 * @see http://developer.android.com/reference/android/hardware/SensorEvent.html#values
	 * @see http://blog.thomnichols.org/2012/06/smoothing-sensor-data-part-2
	 */
	private float[] lowPass( float[] newXYZ, float[] oldXYZ ) {
	    if ( oldXYZ == null ) return newXYZ;
	     
	    float[] filtered = new float[newXYZ.length];
	    for ( int i=0; i<newXYZ.length; i++ ) {
	    	filtered[i] = oldXYZ[i] + ALPHA * (newXYZ[i] - oldXYZ[i]);
	    }
	    return filtered;
	}
}
