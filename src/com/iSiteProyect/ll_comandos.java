package com.iSiteProyect;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ll_comandos extends Activity{

	private static final String TAG = "BlueTest5-MainActivity";
	private int mMaxChars = 50000;//Default
	private UUID mDeviceUUID;
	private BluetoothSocket mBTSocket;
	private ReadInput mReadThread = null;

	private boolean mIsUserInitiatedDisconnect = false;

	// All controls here
	private TextView mTxtReceive;
	private EditText mEditSend;
	private Button mBtnDisconnect,mBtnSend,mBtnClear,mBtnClearInput,btn_comandos;
	
	private ScrollView scrollView;
	

	private boolean mIsBluetoothConnected = false;

	private BluetoothDevice mDevice;

	private ProgressDialog progressDialog;

	
	TextView txtRecepcion;
@Override
protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	setContentView(R.layout.ll_comandos);
	Intent intent = getIntent();
	Bundle b = intent.getExtras();
	mDevice = b.getParcelable(ll_login.DEVICE_EXTRA);
	mDeviceUUID = UUID.fromString(b.getString(ll_login.DEVICE_UUID));
	mMaxChars = b.getInt(ll_login.BUFFER_SIZE);

	levantarXML();
	
	
}

private void levantarXML() {
	txtRecepcion=(TextView)findViewById(R.id.txtRecepcion);
	
}

/////////////////////////////////////////////////////////////////////////////////////


public class ReadInput implements Runnable {

	private boolean bStop = false;
	private Thread t;

	public ReadInput() {
		t = new Thread(this, "Input Thread");
		t.start();
	}

	public boolean isRunning() {
		return t.isAlive();
	}

	@Override
	public void run() {
		InputStream inputStream;

		try {
			inputStream = mBTSocket.getInputStream();
			while (!bStop) {
				byte[] buffer = new byte[256];
				if (inputStream.available() > 0) {
					inputStream.read(buffer);
					int i = 0;
					/*
					 * This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4 http://stackoverflow.com/a/8843462/1287554
					 */
					for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
					}
					final String strInput = new String(buffer, 0, i);

					/*
					 * If checked then receive text, better design would probably be to stop thread if unchecked and free resources, but this is a quick fix
					 */

					
						mTxtReceive.post(new Runnable() {
							@Override
							public void run() {
								mTxtReceive.append(strInput);
								//Uncomment below for testing
								//mTxtReceive.append("\n");
								//mTxtReceive.append("Chars: " + strInput.length() + " Lines: " + mTxtReceive.getLineCount() + "\n");
							    // FuncionComandos(strInput);
						
								int txtLength = mTxtReceive.getEditableText().length();  
								if(txtLength > mMaxChars){
									mTxtReceive.getEditableText().delete(0, txtLength - mMaxChars);
								}

									scrollView.post(new Runnable() { // Snippet from http://stackoverflow.com/a/4612082/1287554
												@Override
												public void run() {
													scrollView.fullScroll(View.FOCUS_DOWN);
												}
											});
							
							}
						});
					

				}
				Thread.sleep(500);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void stop() {
		bStop = true;
	}

}


private void FuncionEnviar(String StringEnviado){
	
	try {
		mBTSocket.getOutputStream().write((StringEnviado+"\r").getBytes());
		mEditSend.setText("");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	
}

public class DisConnectBT extends AsyncTask<Void, Void, Void> {

	@Override
	protected void onPreExecute() {
	}

	@Override
	protected Void doInBackground(Void... params) {

		if (mReadThread != null) {
			mReadThread.stop();
			while (mReadThread.isRunning())
				; // Wait until it stops
			mReadThread = null;

		}

		try {
			mBTSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		mIsBluetoothConnected = false;
		if (mIsUserInitiatedDisconnect) {
			finish();
		}
	}

}

private void msg(String s) {
	Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
}

@Override
protected void onPause() {
	if (mBTSocket != null && mIsBluetoothConnected) {
		new DisConnectBT().execute();
	}
	Log.d(TAG, "Paused");
	super.onPause();
}

@Override
protected void onResume() {
	if (mBTSocket == null || !mIsBluetoothConnected) {
		new ConnectBT().execute();
	}
	Log.d(TAG, "Resumed");
	super.onResume();
}

@Override
protected void onStop() {
	Log.d(TAG, "Stopped");
	super.onStop();
}

@Override
protected void onSaveInstanceState(Bundle outState) {
	// TODO Auto-generated method stub
	super.onSaveInstanceState(outState);
}

public class ConnectBT extends AsyncTask<Void, Void, Void> {
	private boolean mConnectSuccessful = true;

	@Override
	protected void onPreExecute() {
		progressDialog = ProgressDialog.show(ll_comandos.this, "Hold on", "Connecting");// http://stackoverflow.com/a/11130220/1287554
	}

	@Override
	protected Void doInBackground(Void... devices) {

		try {
			if (mBTSocket == null || !mIsBluetoothConnected) {
				mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
				BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
				mBTSocket.connect();
			}
		} catch (IOException e) {
			// Unable to connect to device
			e.printStackTrace();
			mConnectSuccessful = false;
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);

		if (!mConnectSuccessful) {
			Toast.makeText(getApplicationContext(), "Could not connect to device. Is it a Serial device? Also check if the UUID is correct in the settings", Toast.LENGTH_LONG).show();
			finish();
		} else {
			msg("Connected to device");
			mIsBluetoothConnected = true;
			mReadThread = new ReadInput(); // Kick off input reader
		}

		progressDialog.dismiss();
	}

}





}
