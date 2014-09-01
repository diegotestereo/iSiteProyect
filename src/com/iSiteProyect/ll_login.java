
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
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ll_login extends Activity {
	
	public static final String DEVICE_EXTRA = "com.blueserial.SOCKET";
	public static final String DEVICE_UUID = "com.blueserial.uuid";
	public static final String BUFFER_SIZE = "com.blueserial.buffersize";
	
	private static final String TAG = "BlueTest5-MainActivity";
	private int mMaxChars   = 50000;//Default
	private MiTareaAsincrona tarea;
	private BluetoothSocket mBTSocket;
	private ReadInput mReadThread = null;

	private boolean mIsUserInitiatedDisconnect = false;
 private Boolean Apuntamiento=false;
	// All controls here
	private TextView mTxtReceive;
	private EditText mEditSend;
	private ProgressBar pbarProgreso;
	private Button BotonBarra, mBtnDisconnect,mBtnSend,mBtnLoginTelnet,mBtnClearInput;
	 Button btn_LogOut;
	 Spinner spin_TX,spin_RX,spin_Otros;
	 ArrayAdapter<String> TxAdapter,RxAdapter,OtrosAdapter;
	private UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID
	// (http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createInsecureRfcommSocketToServiceRecord%28java.util.UUID%29)
    
	private int mBufferSize = 50000; //Default
	private ScrollView scrollView;
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
		setContentView(R.layout.ll_login);
		ActivityHelper.initialize(this);
		
		
		Intent intent = getIntent();
		Bundle b = intent.getExtras();
		mDevice = b.getParcelable(Homescreen.DEVICE_EXTRA);
		mDeviceUUID = UUID.fromString(b.getString(Homescreen.DEVICE_UUID));
		mMaxChars = b.getInt(Homescreen.BUFFER_SIZE);

		Log.d(TAG, "Ready");
		LevantarXML();
		SeteoUI();
		Spinners();
		Botones();
	
		mTxtReceive.setMovementMethod(new ScrollingMovementMethod());


	}

	


	private void Botones() {

		BotonBarra.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tarea = new MiTareaAsincrona();
		        tarea.execute();
			
			}
		});
		
		
		mBtnDisconnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mIsUserInitiatedDisconnect = true;
				new DisConnectBT().execute();
			}
		});

		
		btn_LogOut.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					mBTSocket.getOutputStream().write(("exit\r").getBytes());
					mEditSend.setText("");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		
		mBtnSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {
					mBTSocket.getOutputStream().write((mEditSend.getText().toString()+"\r").getBytes());
					mEditSend.setText("");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				if(Apuntamiento){
					Apuntamiento=false;
				}else{Apuntamiento=true;}
			}
		});
		
		mBtnLoginTelnet.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {
					mBTSocket.getOutputStream().write(("telnet localhost\r").getBytes());
				
					mEditSend.setText("");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Hilos();
			}
		});
		
		mBtnClearInput.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mTxtReceive.setText("");
			}
		});
		
	}



	private void Spinners() {
		spin_TX.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
						mEditSend.setText(TxAdapter.getItem(position).toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
		spin_RX.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mEditSend.setText(RxAdapter.getItem(position).toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
		spin_Otros.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mEditSend.setText(OtrosAdapter.getItem(position).toString());
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
	}

	private void SeteoUI() {
		String[] TxCadena=new String[]{"tx cw","tx enable","tx freq","tx ifl10","tx iflDC","tx BER","tx power"};
		String[] RxCadena=new String[]{"rx AGC","rx rnable","rx freq","rx ifl10","rx iflDC","rx pointing","rx power","rx SNR"};
		String[] OtrosCadena=new String[]{"sn","remotestate","versions_report"};
		
		
		TxAdapter= new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item,TxCadena );
		RxAdapter= new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item,RxCadena );
		OtrosAdapter= new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item,OtrosCadena );
		
		spin_TX.setAdapter(TxAdapter);
		spin_RX.setAdapter(RxAdapter);
		spin_Otros.setAdapter(OtrosAdapter);
	}

	private void  LevantarXML() {
		
		mBtnDisconnect = (Button) findViewById(R.id.btnDisconnect);
		mBtnSend = (Button) findViewById(R.id.btnSend);
		mBtnLoginTelnet = (Button) findViewById(R.id.btnLoginTelnet);
		BotonBarra=(Button) findViewById(R.id.btn_progress);
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
		pbarProgreso= (ProgressBar) findViewById(R.id.pbarProgreso);
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

						
							mTxtReceive.post(new Runnable() {
								@Override
								public void run() {
									mTxtReceive.append(strInput);
									strInputGlobal=strInput.substring(0,4);
									
									//Uncomment below for testing
								//	mTxtReceive.append("\n");
									//mTxtReceive.append("Chars: " + strInput.length() + " Lines: " + mTxtReceive.getLineCount() + "\n");
									
									/// DETECTA STRING
									FuncionComandos(strInput);
								
									
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

	private void FuncionComandos(String detectorString){
		
		if (detectorString.contains("iDirect login:")){
			
			Toast.makeText(getApplicationContext(), "login", Toast.LENGTH_SHORT).show();
			FuncionEnviar("root");		
			
			
		}else if(detectorString.contains("Password:")){
		
			Toast.makeText(getApplicationContext(), "P@55w0rd!", Toast.LENGTH_SHORT).show();
			FuncionEnviar("P@55w0rd!");	
		}else if(detectorString.contains("Username:")){
		
			Toast.makeText(getApplicationContext(), "admin", Toast.LENGTH_SHORT).show();
			FuncionEnviar("admin");	
		}
		else if(detectorString.contains("Unknown Command:")){
			
			Toast.makeText(getApplicationContext(), "Comando erroneo", Toast.LENGTH_SHORT).show();
				
		}else if(detectorString.contains(">")){
		
			Toast.makeText(getApplicationContext(), "telnet", Toast.LENGTH_SHORT).show();
		
		}else if(detectorString.contains("#")){
		
			Toast.makeText(getApplicationContext(), "Linux", Toast.LENGTH_SHORT).show();
			
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
			progressDialog = ProgressDialog.show(ll_login.this, "Hold on", "Connecting");// http://stackoverflow.com/a/11130220/1287554
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
            }
            
            
	        return true;
	    }
	 
	    @Override
	    protected void onProgressUpdate(Integer... values) {
	  
	    }
	 
	    @Override
	    protected void onPreExecute() {
	        pbarProgreso.setMax(200);
	        pbarProgreso.setProgress(0);
	    }
	 
	    @Override
	    protected void onPostExecute(Boolean result) {
	      
	            
	                 
	    }
	 
	    @Override
	    protected void onCancelled() {
	        
	    }
	}
	private void Hilos() {
		
		mEditSend.setText("");
		mEditSend.setText(strInputGlobal);
		
		
		
		
		new Thread(new Runnable() {
			
		    public void run() {
		    	
		    	
				
		  
		          pbarProgreso.post(new Runnable() {
		            public void run() {
		            
		            }
		            });
		          }
		    	
		
		}).start();
		
	}


}
