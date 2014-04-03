package com.example.assignmentfour;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class DeviceListActivity extends Activity {
	
	// Intent Keys
	public static String KEY_DEVICE_ADDRESS = "device_address";
	
	private Button buttonSearch;
	
	private BluetoothAdapter mBluetoothAdapter;
	private ArrayAdapter<String> mArrayAdapterPairedDevices, mArrayAdapterNewDevices;
	private Set<String> mSetNewDevices;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.layout_device_list);
		
		mArrayAdapterPairedDevices = new ArrayAdapter<String>(this, R.layout.device_name);
		mArrayAdapterNewDevices = new ArrayAdapter<String>(this, R.layout.device_name);
		mSetNewDevices = new HashSet<String>();
		
		// Set up ListView for paired devices
		ListView listViewPairedDevices = (ListView) findViewById(R.id.listview_paired_devices);
		listViewPairedDevices.setAdapter(mArrayAdapterPairedDevices);
		listViewPairedDevices.setOnItemClickListener(listViewDevicesListener);
		
		// Set up ListView for discovered devices
		ListView listViewNewDevices = (ListView) findViewById(R.id.listview_new_devices);
		listViewNewDevices.setAdapter(mArrayAdapterNewDevices);
		listViewNewDevices.setOnItemClickListener(listViewDevicesListener);
		
		// Set title visibility
		findViewById(R.id.textview_paired_devices_title).setVisibility(View.VISIBLE);
		findViewById(R.id.textview_new_devices_title).setVisibility(View.VISIBLE);
		
		// Set up search button with listener
		buttonSearch = (Button) findViewById(R.id.button_search);
		buttonSearch.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				discoverDevices();
				view.setVisibility(View.GONE); // The button will not be click-able while discovering
			}
		});
		
		// Register receivers for Bluetooth device discovery
		IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mDeviceReceiver, intentFilter);
		intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(mDeviceReceiver, intentFilter);
		
		// Setup Bluetooth device
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// Look up Bluetooth history and set the Paired Devices view
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		
		String notFound = getResources().getText(R.string.NOT_FOUND).toString();
		if(pairedDevices != null && !pairedDevices.isEmpty()){
			for(BluetoothDevice device : pairedDevices){
				mArrayAdapterPairedDevices.add(device.getName() + "\n" + device.getAddress());
			}
		}else{
			mArrayAdapterPairedDevices.add(notFound);
		}
		mArrayAdapterNewDevices.add(notFound);
	}
	
	private void discoverDevices(){
		
		mArrayAdapterNewDevices.clear();
		mSetNewDevices.clear();
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.SEARCHING);
		if (mBluetoothAdapter.isDiscovering()){
			mBluetoothAdapter.cancelDiscovery();
		}
		mBluetoothAdapter.startDiscovery();
	}
	
	private OnItemClickListener listViewDevicesListener = new OnItemClickListener(){
		
		@Override
		public void onItemClick(AdapterView<?> av, View devicename, int arg2, long arg3){
			
			// Just in case, it is still trying to discover. It costs.
			mBluetoothAdapter.cancelDiscovery();
			
			// Get the device MAC address: the last 17 chars in the TextView
			CharSequence device_info = ((TextView) devicename).getText();
			if(device_info != null){
				CharSequence device_address = device_info.toString().substring(device_info.length() - 17);
				Intent intent = new Intent();
				intent.putExtra(KEY_DEVICE_ADDRESS, device_address);
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		}
	};
	
	// Broadcast Receiver for Bluetooth discovery procedure
	private final BroadcastReceiver mDeviceReceiver = new BroadcastReceiver(){
		
		@Override
		public void onReceive(Context context, Intent intent){
			
			String action = intent.getAction();
			if(BluetoothDevice.ACTION_FOUND.equals(action)){ // If any device is found, add to the list
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				mSetNewDevices.add(device.getAddress());
				mArrayAdapterNewDevices.add(device.getName() + "\n" + device.getAddress());
			}else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){ // If finished, end
				setProgressBarIndeterminateVisibility(false);
				setTitle(R.string.SELECT_DEVICE);
				if(mSetNewDevices.isEmpty()){
					String notFound = getResources().getText(R.string.NOT_FOUND).toString();
					mArrayAdapterNewDevices.add(notFound);
				}
				buttonSearch.setVisibility(View.VISIBLE);
			}
		}
	};
	
	@Override
	protected void onDestroy(){
		
		super.onDestroy();
		
		if(mBluetoothAdapter != null){
			mBluetoothAdapter.cancelDiscovery();
		}
		unregisterReceiver(mDeviceReceiver);
	}
}