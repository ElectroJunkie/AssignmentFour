package com.example.assignmentthree;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

public class AccelService extends Service implements SensorEventListener{
	
	private SensorManager sensorManager;
	final static String ACCELEROMETER_DATA = "com.example.AccelService.ACCELEROMETER_DATA";
	
	// onCreate is the very first function when Service is created
	@Override
	public void onCreate(){
		super.onCreate();
		Toast.makeText(getApplicationContext(), "Service Starting", Toast.LENGTH_SHORT).show();
	}
	// onStartCommand follows its execution after onCreate
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		initialize();
		return Service.START_NOT_STICKY;
	}
	@Override
	public IBinder onBind(Intent intent){
		return null;
	}
	@Override
	public void onDestroy(){
		Toast.makeText(getApplicationContext(), "Service Ending", Toast.LENGTH_SHORT).show();
		sensorManager.unregisterListener(this);
		super.onDestroy();
	}
	
	/////////////////////////////
	// Sensor Listener portion //
	/////////////////////////////
	@Override
	public void onSensorChanged(SensorEvent event){
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			Intent intent = new Intent();
			intent.setAction(AccelService.ACCELEROMETER_DATA);
			final float[] accel_data = event.values;
			
			// By using intent.putExtra, broadcast intent along with data
			intent.putExtra("Accelerometer data", accel_data);
			sendBroadcast(intent);
		}
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy){
		
	}
	public void initialize(){
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		
		// Check if accelerometer is detected. If so, initialize.
		if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
			//Toast.makeText(getApplicationContext(), "Accelerometer Detected", Toast.LENGTH_SHORT).show();
			
			sensorManager.registerListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					sensorManager.SENSOR_DELAY_NORMAL);
		}else{
			Toast.makeText(getApplicationContext(), "Accelerometer NOT Detected", Toast.LENGTH_SHORT).show();
		}
		/*
		// Check if External storage is detected.
		if(isExternalStorageWritable()==true){
			Toast.makeText(getApplicationContext(), "External SD Detected to write on", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(getApplicationContext(), "There is no external SD", Toast.LENGTH_SHORT).show();
		}*/
	}
	
	// Checks if external storage is available for read and write
	public boolean isExternalStorageWritable(){
		String state = Environment.getExternalStorageState();
		if(Environment.MEDIA_MOUNTED.equals(state)){
			return true;
		}
		return false;
	}
	// Checks if external storage is available to read
	public boolean isExternalStorageReadable(){
		String state = Environment.getExternalStorageState();
		if(Environment.MEDIA_MOUNTED.equals(state) ||
			Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
			return true;
		}
		return false;
	}
}