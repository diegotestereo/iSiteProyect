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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ll_Inicio_Login extends Activity {
	
	public static final String DEVICE_EXTRA = "com.blueserial.SOCKET";
	public static final String DEVICE_UUID = "com.blueserial.uuid";
	public static final String BUFFER_SIZE = "com.blueserial.buffersize";
	
	private static final String TAG = "ISITE PROYECTO";
	private int mMaxChars   = 50000;//Default
	private MiTareaAsincrona tarea;
	private BluetoothSocket mBTSocket;
	private ReadInput mReadThread = null;

	private boolean mIsUserInitiatedDisconnect = false;
 private Boolean Apuntamiento=false;
	// All controls here
//	 TextView mTxtReceive,TxtProgresoBarra;
//	private EditText mEditSend;
	private ProgressBar pbarProgreso;
//	private Button  mBtnDisconnect,mBtnSend,mBtnLoginTelnet,mBtnClearInput;
	 Button btn_LogOut;
	 Spinner spin_TX,spin_RX,spin_Otros;
	 ArrayAdapter<String> TxAdapter,RxAdapter,OtrosAdapter;
	 ToggleButton TB_Apuntamiento;
	private UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID
	// (http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createInsecureRfcommSocketToServiceRecord%28java.util.UUID%29)
    
	private int mBufferSize = 50000; //Default
//private ScrollView scrollView;
	 CheckBox chkScroll;
	 CheckBox chkReceiveText;
	 Float NivelGlobal;
	 int NivelGlobalInt=0;
	 String strInputGlobal="";
	private boolean mIsBluetoothConnected = false;

	private BluetoothDevice mDevice;

	private ProgressDialog progressDialog;

	//private String detectorString=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ll_inicio_login);
		ActivityHelper.initialize(this);
		
		
		Intent intent = getIntent();
		Bundle b = intent.getExtras();
		mDevice = b.getParcelable(Homescreen.DEVICE_EXTRA);
		mDeviceUUID = UUID.fromString(b.getString(Homescreen.DEVICE_UUID));
		mMaxChars = b.getInt(Homescreen.BUFFER_SIZE);

		Log.d(TAG, "Ready");
	//	LevantarXML();
	//	SeteoUI();
		//Botones();
	//	FuncionEnviar("");

	}

	private void Botones() {
	
	}

	private void SeteoUI() {
		/*TxtProgresoBarra.setText("0");
		
		String[] TxCadena=new String[]{"tx cw","tx enable","tx freq","tx ifl10","tx iflDC","tx iflDC on","tx iflDC off","tx BER","tx power"};
		String[] RxCadena=new String[]{"rx AGC","rx enable","rx disable","rx freq","rx ifl10","rx iflDC","rx iflDC on","rx iflDC off","rx pointing enable","rx pointing on","rx pointing off","rx power","rx SNR"};
		String[] OtrosCadena=new String[]{"sn","remotestate","versions_report","reset board","exit",};
		
		
		TxAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,TxCadena );
		RxAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,RxCadena );
		OtrosAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,OtrosCadena );
		
		spin_TX.setAdapter(TxAdapter);
		spin_RX.setAdapter(RxAdapter);
		spin_Otros.setAdapter(OtrosAdapter);*/
	}

	private void  LevantarXML() {
		
		/*
		TB_Apuntamiento=(ToggleButton) findViewById(R.id.toggleButtonApuntamiento);
		mBtnDisconnect = (Button) findViewById(R.id.btnDisconnect);
		mBtnSend = (Button) findViewById(R.id.btnSend);
		mBtnLoginTelnet = (Button) findViewById(R.id.btnLoginTelnet);
		TxtProgresoBarra=(TextView)findViewById(R.id.TxtProgresoBarra);

		btn_LogOut=(Button)findViewById(R.id.btn_LogOut);
		spin_TX=(Spinner)findViewById(R.id.spin_TX);
		spin_RX=(Spinner)findViewById(R.id.spin_RX);
		spin_Otros=(Spinner)findViewById(R.id.spin_Otros);
		mTxtReceive = (TextView) findViewById(R.id.txtReceive);
		mEditSend = (EditText) findViewById(R.id.editSend);
		scrollView = (ScrollView) findViewById(R.id.viewScrollcom);
		chkScroll = (CheckBox) findViewById(R.id.chkScroll);
		chkReceiveText = (CheckBox) findViewById(R.id.chkReceiveText);
		mBtnClearInput = (Button) findViewById(R.id.btnClearInput);
		pbarProgreso= (ProgressBar) findViewById(R.id.pbarProgreso);*/
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
						FuncionLogLinux(strInput);
						
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
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private void FuncionLogLinux(String detectorString){
		
		if (detectorString.contains("iDirect login:")){
			Log.d(TAG, "iDirect login:");
			FuncionEnviar("root");		
		}
		
		if(detectorString.contains("Password:")){
			Log.d(TAG, "Password:");
		FuncionEnviar("P@55w0rd!");	
		}
		if(detectorString.contains("#")){
	//	Toast.makeText(getApplicationContext(), "Linux", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "Linux  #");
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
			//new DisConnectBT().execute();desconecta bluetooth a pasar a segundo plano
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
			progressDialog = ProgressDialog.show(ll_Inicio_Login.this, "Espere un momento...", "Conectando");// http://stackoverflow.com/a/11130220/1287554
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

	
	private class MiTareaAsincrona extends AsyncTask<Void, Integer, Boolean> {
		 
	    @Override
	    protected Boolean doInBackground(Void... params) {
	    	while(Apuntamiento){
	   
        	if (strInputGlobal.equals(""))
    		{NivelGlobal=(float) 0;}
    		else{
    		
    		NivelGlobal=Float.parseFloat(strInputGlobal);
    					}
  		NivelGlobalInt=(int) (NivelGlobal*10);
    		pbarProgreso.setProgress(NivelGlobalInt);
    		
    		 runOnUiThread(new Runnable() {

    	         @Override
    	             public void run() {
    	        	// TxtProgresoBarra.setText(Integer.toString(NivelGlobalInt));      
    	         }
    	        });
        }
      	 
	        return true;
	    }
	 
	    @Override
	    protected void onProgressUpdate(Integer... values) {
	   	
	    }
	 
	    @Override
	    protected void onPreExecute() {
	        
	    }
	 
	    @Override
	    protected void onPostExecute(Boolean result) {
	           
	    }
	 
	    @Override
	    protected void onCancelled() {
	        
	    }
	}
	
	


}
