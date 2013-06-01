package com.nbs.client.assassins.sensors;

import java.util.IdentityHashMap;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class BearingProviderImpl implements BearingProvider, SensorEventListener {

	private static final String TAG = "BearingProviderImpl";
	private SensorManager mSensorManager;
	private Sensor accelerometer;
	private Sensor magnetometer;
	private boolean registeredForSensorUpdates = false;
	
	private BearingSmoother bearingSmoother;

	private IdentityHashMap<BearingReceiver, BearingReceiver> bearingReceivers;	
	
	
	public BearingProviderImpl(Context c) {
		bearingReceivers = new IdentityHashMap<BearingReceiver, BearingReceiver>();
		bearingSmoother = new BearingSmoother(new SensorManagerStub());
		
		mSensorManager = (SensorManager)c.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	private void registerForSensorUpdates() {
		if(!registeredForSensorUpdates) {
			mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
			mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
			registeredForSensorUpdates = true;
		}
	}
	private void stopSensorUpdates() {
		if(registeredForSensorUpdates) {
			mSensorManager.unregisterListener(this);
			registeredForSensorUpdates = false;
		}
	}


    
	public void onBearingChanged(float bearing) {
		for(BearingReceiver receiver : bearingReceivers.values()) {
			receiver.onBearingChanged(bearing);
		}
		
	}

	@Override
	public void registerForBearingUpdates(BearingReceiver receiver) {
		bearingReceivers.put(receiver, receiver);
		registerForSensorUpdates();
	}


	@Override
	public void unregisterForBearingUpdates(BearingReceiver receiver) {
		bearingReceivers.remove(receiver.hashCode());
		if(bearingReceivers.isEmpty()) stopSensorUpdates();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Log.d(TAG, "SensorEvent at time: " + event.timestamp);
		int type = event.sensor.getType();
        
        if (type == Sensor.TYPE_ACCELEROMETER) {
           bearingSmoother.onAccelerometerChanged(event.values);
        } else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
           bearingSmoother.onMagnetometerChanged(event.values);
        } 
        
        onBearingChanged(bearingSmoother.getBearing());
	}
	
}
