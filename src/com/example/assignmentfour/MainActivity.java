package com.example.assignmentfour;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ObjectUtils.Null;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.box.boxandroidlibv2.BoxAndroidClient;
import com.box.boxandroidlibv2.activities.FilePickerActivity;
import com.box.boxandroidlibv2.activities.FolderPickerActivity;
import com.box.boxandroidlibv2.activities.OAuthActivity;
import com.box.boxandroidlibv2.dao.BoxAndroidFile;
import com.box.boxandroidlibv2.dao.BoxAndroidFolder;
import com.box.boxandroidlibv2.dao.BoxAndroidOAuthData;
import com.box.boxjavalibv2.exceptions.AuthFatalFailureException;
import com.box.boxjavalibv2.requests.requestobjects.BoxFileUploadRequestObject;

public class MainActivity extends Activity {
	
	private final static int AUTH_REQUEST = 1;
	private final static int UPLOAD_REQUEST = 2;
	private final static int DOWNLOAD_REQUEST = 3;
	private static final int REQUEST_ENABLE_BT = 4;
	
	Button buttonStartService, buttonStopService;
	Button buttonWrite, buttonAuthenticate;
	Button buttonBoxUpload, buttonBoxDownload;
	Button buttonBluetoothOn, buttonBluetoothOff;
	Button buttonBluetoothDiscoverable, buttonBluetoothPair;
	Button buttonListPairedDevices, buttonSearchDevicesOrCancel;
	
	TextView accelDataTextView, textViewBluetooth;
	ServiceDataReceiver dataReceiver;
	float[] accelData;
	boolean serviceFlag = false;
	
	File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/accel_reading");
	File fileName = new File (dir, "acceldata.txt");
	FileOutputStream fStream;
	OutputStreamWriter oWriter;
	String dataString;
	
	// Bluetooth
	BluetoothAdapter mBluetoothAdapter;
	ArrayAdapter<String> BtArrayAdapter;
	Set<BluetoothDevice> pairedDevices;
	ListView listViewBluetoothDevices;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		// A note for future: If I put the setContentView in the end of onCreate, it doesn't work!
		
		// Initialize Buttons and TextViews
		buttonStartService = (Button)findViewById(R.id.button_start_service);
		buttonStopService = (Button)findViewById(R.id.button_stop_service);
		buttonWrite = (Button)findViewById(R.id.button_write);
		buttonBluetoothOn = (Button) findViewById(R.id.button_bluetooth_on);
		buttonBluetoothOff = (Button) findViewById(R.id.button_bluetooth_off);
		buttonBluetoothDiscoverable = (Button) findViewById(R.id.button_bluetooth_enable_discovery);
		buttonBluetoothPair = (Button) findViewById(R.id.button_bluetooth_pair);
		buttonListPairedDevices = (Button) findViewById(R.id.button_list_paried_devices);
		buttonSearchDevicesOrCancel = (Button) findViewById(R.id.button_search_devices_or_cancel);
		
		// Set up on click listeners
		buttonStartService.setOnClickListener(buttonStartServiceListener);
		buttonStopService.setOnClickListener(buttonStopServiceListener);
		buttonWrite.setOnClickListener(buttonWriteListener);
		buttonBluetoothOn.setOnClickListener(buttonBluetoothOnListener);
		buttonBluetoothOff.setOnClickListener(buttonBluetoothOffListener);
		buttonBluetoothDiscoverable.setOnClickListener(buttonBluetoothDiscoverableListener);
		buttonBluetoothPair.setOnClickListener(buttonBluetoothPairListener);
		buttonListPairedDevices.setOnClickListener(buttonListPairedDevicesListener);
		buttonSearchDevicesOrCancel.setOnClickListener(buttonSearchDevicesOrCancelListener);
		
		initializeBoxUI();
		//Toast.makeText(getApplicationContext(), dir.getAbsolutePath(), Toast.LENGTH_SHORT).show();
		
		// Bluetooth
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		// Check if Bluetooth is supported on the device.
		if(mBluetoothAdapter == null){
			// Maybe make a toast!
		}
		
		listViewBluetoothDevices = (ListView)findViewById(R.id.listview_bluetooth_devices);
		
		BtArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		listViewBluetoothDevices.setAdapter(BtArrayAdapter);
		
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(bluetoothReceiver, filter); //
	}
	
	/////////
	// Box //
	/////////
	private void initializeBoxUI(){
		initializeAuthenticateButton();
		initializeUploadButton();
		initializeDownloadButton();
	}
	private void initializeAuthenticateButton(){
		buttonAuthenticate = (Button) findViewById(R.id.button_authenticate);
		buttonAuthenticate.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				startAuthentication();
			}
		});
	}
	private void initializeUploadButton(){
		buttonBoxUpload = (Button) findViewById(R.id.button_box_upload);
		buttonBoxUpload.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				doUpload();
			}
		});
	}
	private void initializeDownloadButton(){
		buttonBoxDownload = (Button) findViewById(R.id.button_box_download);
		buttonBoxDownload.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				doDownload();
			}
		});
	}
	private void startAuthentication(){
		Intent intentBox = OAuthActivity.createOAuthActivityIntent(this, BoxApplication.CLIENT_ID,
				BoxApplication.CLIENT_SECRET, false, BoxApplication.REDIRECT_URL);
		this.startActivityForResult(intentBox,  AUTH_REQUEST);
	}
	private void doUpload(){
		try{
			BoxAndroidClient client = ((BoxApplication) getApplication()).getClient();
			Intent intent = FolderPickerActivity.getLaunchIntent(this, "0", (BoxAndroidOAuthData) client.getAuthData(),
					BoxApplication.CLIENT_ID, BoxApplication.CLIENT_SECRET);
			startActivityForResult(intent, UPLOAD_REQUEST);
		}
		catch (AuthFatalFailureException e){
			e.printStackTrace();
		}
	}
	private void doDownload(){
		try{
			BoxAndroidClient client = ((BoxApplication) getApplication()).getClient();
			Intent intent = FilePickerActivity.getLaunchIntent(this,  "0", (BoxAndroidOAuthData) client.getAuthData(),
					BoxApplication.CLIENT_ID, BoxApplication.CLIENT_SECRET);
			startActivityForResult(intent, DOWNLOAD_REQUEST);
		}
		catch (AuthFatalFailureException e){
			e.printStackTrace();
		}
	}
	// From below, I just copied.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTH_REQUEST) {
            onAuthenticated(resultCode, data);
        }
        else if (requestCode == UPLOAD_REQUEST) {
            onFolderSelected(resultCode, data);
        }
        else if (requestCode == DOWNLOAD_REQUEST) {
            onFileSelected(resultCode, data);
        }
        else if (requestCode == REQUEST_ENABLE_BT){
        	//Do something here?
    	}
    }
	
    private void onAuthenticated(int resultCode, Intent data) {
        if (Activity.RESULT_OK != resultCode) {
            Toast.makeText(this, "fail", Toast.LENGTH_LONG).show();
        }
        else {
            BoxAndroidOAuthData oauth = data.getParcelableExtra(OAuthActivity.BOX_CLIENT_OAUTH);
            BoxAndroidClient client = new BoxAndroidClient(BoxApplication.CLIENT_ID, BoxApplication.CLIENT_SECRET, null, null);
            client.authenticate(oauth);
            if (client == null) {
                Toast.makeText(this, "fail", Toast.LENGTH_LONG).show();
            }
            else {
                ((BoxApplication) getApplication()).setClient(client);
                Toast.makeText(this, "authenticated", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void onFileSelected(int resultCode, Intent data) {
        if (Activity.RESULT_OK != resultCode) {
            Toast.makeText(this, "fail", Toast.LENGTH_LONG).show();
        }
        else {
            final BoxAndroidFile file = data.getParcelableExtra(FilePickerActivity.EXTRA_BOX_ANDROID_FILE);
            AsyncTask<Null, Integer, Null> task = new AsyncTask<Null, Integer, Null>() {

                @Override
                protected void onPostExecute(Null result) {
                    Toast.makeText(MainActivity.this, "done downloading", Toast.LENGTH_LONG).show();
                    super.onPostExecute(result);
                }

                @Override
                protected void onPreExecute() {
                    Toast.makeText(MainActivity.this, "start downloading", Toast.LENGTH_LONG).show();
                    super.onPreExecute();
                }

                @Override
                protected Null doInBackground(Null... params) {
                    BoxAndroidClient client = ((BoxApplication) getApplication()).getClient();
                    try {
                        File f = new File(Environment.getExternalStorageDirectory(), file.getName());
                        System.out.println(f.getAbsolutePath());
                        client.getFilesManager().downloadFile(file.getId(), f, null, null);
                    }
                    catch (Exception e) {
                    }
                    return null;
                }
            };
            task.execute();

        }
    }
    private void onFolderSelected(int resultCode, Intent data) {
        if (Activity.RESULT_OK != resultCode) {
            Toast.makeText(this, "fail", Toast.LENGTH_LONG).show();
        }
        else {
            final BoxAndroidFolder folder = data.getParcelableExtra(FolderPickerActivity.EXTRA_BOX_ANDROID_FOLDER);
            AsyncTask<Null, Integer, Null> task = new AsyncTask<Null, Integer, Null>() {
            	
                @Override
                protected void onPostExecute(Null result) {
                    Toast.makeText(MainActivity.this, "done uploading", Toast.LENGTH_LONG).show();
                    super.onPostExecute(result);
                }
                
                @Override
                protected void onPreExecute() {
                    Toast.makeText(MainActivity.this, "start uploading", Toast.LENGTH_LONG).show();
                    super.onPreExecute();
                }
                
                @Override
                protected Null doInBackground(Null... params) {
                    BoxAndroidClient client = ((BoxApplication) getApplication()).getClient();
                    try {
                        //File mockFile = createMockFile();
                        //client.getFilesManager().uploadFile(
                        //    BoxFileUploadRequestObject.uploadFileRequestObject(folder.getId(), mockFile.getName(), mockFile, client.getJSONParser()));
                    	client.getFilesManager().uploadFile(
                                BoxFileUploadRequestObject.uploadFileRequestObject(folder.getId(), fileName.getName(), fileName, client.getJSONParser()));
                    }
                    catch (Exception e) {
                    }
                    return null;
                }
            };
            task.execute();
        }
    }
    private File createMockFile() {
        try {
            File file = File.createTempFile("tmp", ".txt");
            FileUtils.writeStringToFile(file, "string");
            return file;
        }
        catch (Exception e) {
            return null;
        }
    }
	
	////////////////////////////////
	// Implement button listeners //
	////////////////////////////////
	// Start service and register the listener
	public OnClickListener buttonStartServiceListener = new OnClickListener(){
		public void onClick(View view){
			if(serviceFlag == false){
				dataReceiver = new ServiceDataReceiver();
				IntentFilter intentFilter = new IntentFilter();
				intentFilter.addAction(AccelService.ACCELEROMETER_DATA);
				registerReceiver(dataReceiver, intentFilter);
				Intent intent_accel = new Intent(MainActivity.this, AccelService.class);
				startService(intent_accel);
				serviceFlag = true;
			}else{
				Toast.makeText(getApplicationContext(), "Service is already running.", Toast.LENGTH_SHORT).show();
			}
		}
	};
	// Stop service and unregister the listener
	private OnClickListener buttonStopServiceListener = new OnClickListener(){
		public void onClick(View view){
			if(serviceFlag == true){
				Intent intent_accel = new Intent(MainActivity.this, AccelService.class);
				stopService(intent_accel);
				unregisterReceiver(dataReceiver);
				serviceFlag = false;
				//try{bw.close();}catch(Exception e){}
			}else{
				Toast.makeText(getApplicationContext(), "Service is not running!", Toast.LENGTH_SHORT).show();
			}
		}
	};
	private OnClickListener buttonWriteListener = new OnClickListener(){
		public void onClick(View view){
			if(serviceFlag == true){
				try{
					if(!fileName.exists()){
						dir.mkdirs();
						fileName.createNewFile();
						Toast.makeText(getApplicationContext(), "making file", Toast.LENGTH_SHORT).show();
					}
					fStream = new FileOutputStream(fileName, true);
					oWriter = new OutputStreamWriter(fStream);
					oWriter.append(dataString);
					oWriter.close();
				}catch(Exception e){
					System.out.println("File cannot be created");
					e.printStackTrace();
				}
			}else{
				Toast.makeText(getApplicationContext(), "Service is not running!", Toast.LENGTH_SHORT).show();
			}
			
		}
	};
	private OnClickListener buttonBluetoothOnListener = new OnClickListener(){
		public void onClick(View view){
			if(!mBluetoothAdapter.isEnabled()){
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}else{
				//
			}
		}
	};
	private OnClickListener buttonBluetoothOffListener = new OnClickListener(){
		public void onClick(View view){
			mBluetoothAdapter.disable();
			// For this part to work, you need the BLUETOOTH_ADMIN permission on manifest.
		}
	};
	private OnClickListener buttonBluetoothDiscoverableListener = new OnClickListener(){
		public void onClick(View view){
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	};
	private OnClickListener buttonBluetoothPairListener = new OnClickListener(){
		public void onClick(View view){
			
		}
	};
	private OnClickListener buttonListPairedDevicesListener = new OnClickListener(){
		public void onClick(View view){
			pairedDevices = mBluetoothAdapter.getBondedDevices(); // Get paired devices
			for(BluetoothDevice device : pairedDevices)
				BtArrayAdapter.add(device.getName() + "\n" + device.getAddress());
		}
	};
	private OnClickListener buttonSearchDevicesOrCancelListener = new OnClickListener(){
		public void onClick(View view){
			if(mBluetoothAdapter.isDiscovering()){
				mBluetoothAdapter.cancelDiscovery();
				Toast.makeText(getApplicationContext(), "Cancel Searching", Toast.LENGTH_SHORT).show();
			}else{
				BtArrayAdapter.clear();
				mBluetoothAdapter.startDiscovery();
				Toast.makeText(getApplicationContext(), "Searching", Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	/////////////////////////////////////////////////////////////////
	// Implement message deliver mechanism - Use BroadcastReceiver //
	/////////////////////////////////////////////////////////////////
	
	private class ServiceDataReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent){
			accelData = intent.getFloatArrayExtra("Accelerometer data");
			accelDataTextView = (TextView)findViewById(R.id.textview_accel_data);
			StringBuilder accelDataToString = new StringBuilder();
			StringBuilder accelDataForFile = new StringBuilder();
			accelDataToString.append("x-axis: "+accelData[0]+"\n"+"y-axis: "+accelData[1]+"\n"+"z-axis: "+accelData[2]+"\n");
			accelDataForFile.append(accelData[0]+" "+accelData[1]+" "+accelData[2]+"\n");
			dataString = accelDataForFile.toString();
			String textToShow = accelDataToString.toString();
			accelDataTextView.setText(textToShow);
		}
	}
	
	final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver(){
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				BtArrayAdapter.add(device.getName() + "\n" + device.getAddress());
				BtArrayAdapter.notifyDataSetChanged();
			}
		}
	};
	
	/////////////////////////////
	// Bluetooth Server Thread //
	/////////////////////////////
	public class BluetoothServer extends Thread {
		
		private final BluetoothServerSocket mServerSocket;
		
		public BluetoothServer() {
			
			BluetoothServerSocket tempSocket = null;
			
			try{
				tempSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
			}
			catch (IOException e){}
			
			mServerSocket = tempSocket;
		}
		
		public void run() {
			
		}
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		unregisterReceiver(bluetoothReceiver);
	}
}
