package com.iSiteProyect;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.UUID;

import com.iSiteProyect.ll_Inicio_Login.ConnectBT;
import com.iSiteProyect.ll_Inicio_Login.DisConnectBT;
import com.iSiteProyect.ll_Inicio_Login.ReadInput;

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
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;



public class ll_Principal extends Activity{
	private static final String DEVICE_EXTRA = "com.blueserial.SOCKET";
	private static final String DEVICE_UUID = "com.blueserial.uuid";
	private static final String BUFFER_SIZE = "com.blueserial.buffersize";
	
	private static final String TAG = "ISITE PROYECTO";
	private int mMaxChars   = 50000;//Default

	private BluetoothSocket mBTSocket;
	private ReadInput mReadThread = null;

	private boolean mIsUserInitiatedDisconnect = false;
	private Boolean Apuntamiento=false;

	private Button btn_LogOut;
	private Spinner spin_TX,spin_RX,spin_Otros;
	private ArrayAdapter<String> TxAdapter,RxAdapter,OtrosAdapter;
	private ToggleButton TB_Apuntamiento;
	private UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID
	
	private int mBufferSize = 50000; //Default

	private Float NivelGlobal;
	private int NivelGlobalInt=0;
	private String strInputGlobal="";
	private boolean mIsBluetoothConnected = false;

	private BluetoothDevice mDevice;

	private ProgressDialog progressDialog;

	//////////////////////////////////////////////////////////////////
	Button btn_Ingresar,btn_Cargar_OPT;
	Boolean Habilitacion=false;
	
	
	
	
	
	Button btn_Cw,btn_Opt,btn_Otros,btn_Info;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ll_principal);
		LenvantarXML();
		botones();
	}

	private void botones() {
		btn_Cw.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					mBTSocket.getOutputStream().write(("rx iflDC off\r").getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			
			
		});
		
		
			btn_Otros.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					mBTSocket.getOutputStream().write(("rx iflDC on\r").getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			
			
		});
		
		
		
		
	}

	private void LenvantarXML() {
		// TODO Auto-generated method stub
		btn_Cw=(Button) findViewById(R.id.btn_Cw);
		btn_Otros=(Button) findViewById(R.id.btn_Otros);
		btn_Info=(Button) findViewById(R.id.btn_Info);
		btn_Opt=(Button) findViewById(R.id.btn_Opt);
	}

	
	
	////////////////////////    FUNCIONES       ///////////////////////////////////////
	
	public void FuncionEnviar(String StringEnviado){
		
		try {
			mBTSocket.getOutputStream().write((StringEnviado+"\r").getBytes());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	/////////////////////////////////////////////////////////////////
	
	
	
	
	
	
	
	
	////////////***   Bluetooth    INICIO ******///////////////////////////////
	
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
	
	public void msg(String s) {
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
			progressDialog = ProgressDialog.show(ll_Principal.this, "Espere un momento...", "iSite Conectado !!!");// http://stackoverflow.com/a/11130220/1287554
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
						Log.d(TAG, "eNTRO DATO");
						
						//FuncionDetectar(strInput,Habilitacion);
						
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

	////////////***   Bluetooth    FIN ******///////////////////////////////
	
	
	
}
