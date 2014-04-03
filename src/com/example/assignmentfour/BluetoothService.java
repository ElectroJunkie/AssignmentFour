package com.example.assignmentfour;

import java.io.IOException;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

public class BluetoothService {
	
	private final BluetoothAdapter mBluetoothAdapter;
	private final Handler mHandler;
	
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	
	// Constructor
	public BluetoothService(Handler handler){
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mHandler = handler;
	}
	
	// Calls the connect thread.
	public void connect(BluetoothDevice device){
		
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
	}
	
	// Set up Bluetooth Socket for connection on a thread.
	private class ConnectThread extends Thread {
		
		private final BluetoothSocket mBluetoothSocket;
		private final BluetoothDevice mBluetoothDevice;
		
		// Constructor
		public ConnectThread (BluetoothDevice device) {
			
			BluetoothSocket tempSocket = null;
			mBluetoothDevice = device;
			
			try{
				Method m = device.getClass().getMethod("createRfcommSocket",  new Class[]{int.class});
				tempSocket = (BluetoothSocket) m.invoke(device, 1);
			}catch (Exception e){
				
			}
			mBluetoothSocket = tempSocket;
		}
		@Override
		public void run(){
			mBluetoothAdapter.cancelDiscovery();
			try{
				mBluetoothSocket.connect();
			}catch(IOException e){
				
			}
			connected(mBluetoothSocket, mBluetoothDevice);
		}
	}
	
	// Calls the connected thread.
	public void connected(BluetoothSocket socket, BluetoothDevice device){
		
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();
	}
	
	private class ConnectedThread extends Thread {
		
		private final BluetoothSocket mmSocket;
		
		// Constructor
		public ConnectedThread(BluetoothSocket socket){
			mmSocket = socket;
			
		}
		
		@Override
		public void run(){
			// I'm not doing anything just yet.
		}
	}
}