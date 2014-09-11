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
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ll_Inicio_Login extends Activity {
	
	public static final String DEVICE_EXTRA = "com.blueserial.SOCKET";
	public static final String DEVICE_UUID = "com.blueserial.uuid";
	public static final String BUFFER_SIZE = "com.blueserial.buffersize";
	
	public static final String TAG = "ISITE PROYECTO";
	public int mMaxChars   = 50000;//Default

	public BluetoothSocket mBTSocket;
	public ReadInput mReadThread = null;

	public boolean mIsUserInitiatedDisconnect = false;
	public Boolean Apuntamiento=false,Booteo=true,Habilitacion=false;;

	public UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID
	
	public int mBufferSize = 50000; //Default

	public Float NivelGlobal;
	public int NivelGlobalInt=0;
	public String strInputGlobal="";
	public boolean mIsBluetoothConnected = false;

	public BluetoothDevice mDevice;

	//////////////////////////////////////////////////////////////////
	public ProgressBar progressBarBoot;
	public ProgressDialog progressDialog;

	public Button btn_LogOut;
	public Spinner spin_TX,spin_RX,spin_Otros;
	public ArrayAdapter<String> TxAdapter,RxAdapter,OtrosAdapter;

	public Button btn_Ingresar,btn_Cargar_OPT,btn_SetFreq,btn_Reset,
	btn_Apuntamiento,btn_Prueba;
	public ToggleButton TB_Login,TB_CwOnOff,TB_Pointing;
	public TextView  TextFrecuenciaLeida,TextCWEstado,TextPointing,TextPrueba;
	public EditText EditFreq,EditPass,EditPrueba;
	
	public ProgressDialog progressDialogBooteo;
	public ProgressBar progressBar_Apuntamiento;
	public Handler puente;
	public MedirBaliza Apuntando;
	public VentanaDialogoNivel DialogoNivel;
	public Boolean Bool_pointing=false;
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

		Log.d(TAG, "OnCreate");
		Toast.makeText(getApplicationContext(), "OnCreate", Toast.LENGTH_LONG).show();
		LevantarXML();
		
		Botones();
		SetupUI();

		progressDialog = new ProgressDialog(ll_Inicio_Login.this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMessage("Detectando Nivel del Satelite...");
		progressDialog.setMax(100);
		progressDialog.setCancelable(true);
		progressDialog.show();	
	
		
		}
	
	/*
	 private void MensajeArranque() {
	
		
		progressDialog = new ProgressDialog(ll_Inicio_Login.this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("Equipo arrancando\nEspere 2 min aprox ...");
		progressDialog.setMax(10);
		progressDialog.setProgress(0);
		progressDialog.setCancelable(true);
	    progressDialog.show();	
		
	}
	*/
	
	private void SetupUI() {
		TB_Login.setChecked(true);
		progressBar_Apuntamiento.setMax(10);
		progressBar_Apuntamiento.setProgress(5);
	}

	private void Botones() {
		
	btn_Prueba.setOnClickListener(new OnClickListener() {
	
		@Override
		public void onClick(View v) {

			DialogoNivel= new VentanaDialogoNivel();
			DialogoNivel.execute();
			
		}
	});
		
		btn_SetFreq.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				FuncionEnviar("tx freq "+Float.parseFloat(EditFreq.getText().toString()));
			
				
			}
		});
		
		btn_Reset.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//FuncionEnviar("reset board");
				progressDialog.cancel();
			}
		});
				
		btn_Ingresar.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TB_Login.setChecked(true);	
			FuncionEnviar("telnet localhost");
				
			}
		});
				
		btn_Cargar_OPT.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				progressDialog.show();	
				Log.d(TAG, "boton opt");
				
			}
		});
		
		btn_Apuntamiento.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(btn_Apuntamiento.getText().toString().equals("Disable")){
					FuncionEnviar("rx pointing disable");
					btn_Apuntamiento.setText("Enable");
					
				}
				else{
					FuncionEnviar("rx pointing enable");
					btn_Apuntamiento.setText("Disable");
					
				}
			
			}
		});
		
		TB_Login.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				if (isChecked){
					
					
					Toast.makeText(getApplicationContext(), "Log Telnet ", Toast.LENGTH_SHORT).show();
				Habilitacion=isChecked;}
				else{Habilitacion=isChecked;
				Toast.makeText(getApplicationContext(), "Log Linux ", Toast.LENGTH_SHORT).show();}
				
			
			}
		});
		
		TB_CwOnOff.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked){
					FuncionEnviar("tx cw on");
					Toast.makeText(getApplicationContext(), "CW ON", Toast.LENGTH_SHORT).show();
					
				}
				else{
					FuncionEnviar("tx cw off");
				Toast.makeText(getApplicationContext(), "CW OFF", Toast.LENGTH_SHORT).show();}
			
			}
		});
		
		TB_Pointing.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked){
					FuncionEnviar("rx pointing on");
				}else{
					FuncionEnviar("rx pointing off");
				}
				
			}
		});
	
	}

	private void  LevantarXML() {
		
		TextFrecuenciaLeida=(TextView) findViewById(R.id.TextFrecuenciaLeida);
		TextCWEstado=(TextView) findViewById(R.id.TextCWEstado);
		TextPointing=(TextView) findViewById(R.id.TextPointing);
		TextPrueba=(TextView) findViewById(R.id.TextPrueba);
		
		btn_Ingresar=(Button) findViewById(R.id.btn_Ingresar);
		btn_SetFreq=(Button) findViewById(R.id.btn_SetFreq);
		btn_Reset=(Button) findViewById(R.id.btn_Reset);
		btn_Apuntamiento=(Button) findViewById(R.id.btnPointingEnable);
		btn_Cargar_OPT=(Button) findViewById(R.id.btn_CargarOPT);
		btn_Prueba=(Button) findViewById(R.id.btn_Prueba);

		TB_CwOnOff=(ToggleButton) findViewById(R.id.TB_CwOnOff);
		TB_Login=(ToggleButton) findViewById(R.id.TB_Login);
		TB_Pointing=(ToggleButton) findViewById(R.id.TB_Pointing);
		
		EditFreq=(EditText) findViewById(R.id.EditFreq);
		EditPass=(EditText) findViewById(R.id.EditPass);
		EditPrueba=(EditText) findViewById(R.id.EditPrueba);
		progressBar_Apuntamiento=(ProgressBar) findViewById(R.id.progressBar_Apuntamiento);
	}

	
	public void FuncionEnviar(String StringEnviado){
		
		try {
			mBTSocket.getOutputStream().write((StringEnviado+"\r").getBytes());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void FuncionDetectarComando(String detectorString,Boolean hab){
		Log.d(TAG, "Entrada General de Datos");
		
		
	if(hab){
		
			if (detectorString.contains("Username:")){
				Log.d(TAG, "Username:");
				FuncionEnviar("admin");		
			}
		
			if(detectorString.contains("Password:")){
				Log.d(TAG, "Password:");
				FuncionEnviar("P@55w0rd!");	
			}
			
			if(detectorString.contains(">")){
				Log.d(TAG, "telnet >"+strInputGlobal);
				
			}	
			if(detectorString.contains(("tx cw on"))||detectorString.contains("tx cw off")){
				FuncionEnviar("tx cw");
				
				Log.d(TAG, "CW solo"+strInputGlobal); // FRANCO GIOVANAZZI MAMA SOFIA DIEGO 
				}	
			
			if(detectorString.contains("cw =")){
				
					Log.d(TAG, "CW ="+strInputGlobal); // FRANCO GIOVANAZZI MAMA SOFIA DIEGO 
					
				TextCWEstado.post(new Runnable() {
						
				        public void run() {
				        
				        int posicion =strInputGlobal.indexOf("=");
				        TextCWEstado.setText("CW = "+strInputGlobal.substring(posicion+2,posicion+5));
				        
				        }
				    });
					
				}	
			
			if(detectorString.contains("pointing =")){
				TextCWEstado.post(new Runnable() {
					
			        public void run() {
			        
			        int posicion =strInputGlobal.indexOf("=");
			        TextPointing.setText("Point "+strInputGlobal.substring(posicion+2,posicion+5));
			        Log.d(TAG, "Pointing = "+strInputGlobal.substring(posicion+2,posicion+5)); // FRANCO GIOVANAZZI MAMA SOFIA DIEGO 
			     
			        }
			    });
				
			}	
			
			if(detectorString.contains("pointing = on")){
				
							
				Bool_pointing=true;
				
				
				}	
			if(detectorString.contains("pointing = off")){
				
				
				Bool_pointing=false;
				
				
				}	
			
			
			if(detectorString.contains("Tx Frequency")){
			//	Log.d(TAG, ""+strInputGlobal); // FRANCO GIOVANAZZI MAMA SOFIA DIEGO 
				
				TextFrecuenciaLeida.post(new Runnable() {
					
			        public void run() {
			        
			        int posicion =strInputGlobal.indexOf("=");
			        	TextFrecuenciaLeida.setText(strInputGlobal.substring(posicion+2,posicion+15));
			        
			        }
			    });
				}	
			}
			else
			{
			Log.d(TAG, "Linux");
			if (detectorString.contains("DRAM Test Successful")){
				Log.d(TAG, "DRAM Test Successful antes");
			}
			if(detectorString.contains("Uncompressing Linux")){
				Log.d(TAG, "Uncompressing Linux");
							}
			
			if (detectorString.contains("iDirect login:")){
			Log.d(TAG, "iDirect login:");
		
			FuncionEnviar("root");		
			}
			if(detectorString.contains("Password:")){
			Log.d(TAG, "Password:");
			
		
		FuncionEnviar("P@55w0rd!");	
		}
		if(detectorString.contains("#")){
			
		Log.d(TAG, "Linux  # "+hab);
		
		}
		}
	
	}

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
	/*	if (mBTSocket != null && mIsBluetoothConnected) {
			new DisConnectBT().execute();
		}*/
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
		if (mBTSocket != null && mIsBluetoothConnected) {
			new DisConnectBT().execute();
		}
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
						Log.d(TAG, "strInput: " + strInput);
						FuncionDetectarComando(strInput,Habilitacion);
						strInputGlobal=strInput;

						DialogoNivel.execute();
						}
					Thread.sleep(500);
				}
			} catch (IOException e) {
				
				e.printStackTrace();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}

		}

		public void stop() {
			bStop = true;
		}

	}

	////////////***   Bluetooth    FIN ******///////////////////////////////

	public class MedirBaliza extends AsyncTask<Void, Void, Void> {
		
	@Override 
		protected void onPreExecute() {
		
		//TextPrueba.setText(strInputGlobal);
		
		}

		@Override
		protected Void doInBackground(Void... devices) {
		
	
	//	progressBar_Apuntamiento.setProgress((int)(nivel*10.0));
		
	float nivel;
				try {
									
				//	nivel= Float.parseFloat(String.valueOf(strInputGlobal));
					//progressBar_Apuntamiento.setProgress((int)(nivel*10.0));
					Log.d(TAG, "progress bar apuntamiento strInputGlobal ="+strInputGlobal);
					Log.d(TAG, "progress bar apuntamiento nivel =");
					
					
				} catch (NumberFormatException nfe){
					Log.d(TAG, "progress bar apuntamiento mal="+strInputGlobal);
					
			}
			
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
		}

	}


	public class VentanaDialogoNivel extends AsyncTask<Void, Void, Void> {
		
	@Override 
		protected void onPreExecute() {
		String[] NivelesAlmacenados = strInputGlobal.split("\r");
		
		float nivelFlotante= Float.parseFloat(NivelesAlmacenados[0])*100;
		int NivelBaliza=(int)nivelFlotante;
		TextPrueba.setText("String: "+NivelesAlmacenados[0]+" float * 10:  "+nivelFlotante +" integer:  "+NivelBaliza);
		
		progressDialog.setProgress(NivelBaliza);
	
		}

		@Override
		protected Void doInBackground(Void... devices) {
		
	
	
			
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			
			
		}

	}


	}
	
	
	

