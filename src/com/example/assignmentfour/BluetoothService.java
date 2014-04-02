package com.example.assignmentfour;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;

public class BluetoothService {
	
	private final BluetoothAdapter mBluetoothAdapter;
	private final Handler mHandler;
	
	// Constructor
	public BluetoothService(Handler handler){
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mHandler = handler;
	}
}