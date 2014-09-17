package com.iSiteProyect;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;










import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
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
	public Boolean Apuntamiento=false,Booteo=true,Habilitacion=false;

	public UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID
	
	public int mBufferSize = 50000; //Default

	public Float NivelGlobal;
	public int NivelGlobalInt=0;
	public String strInputGlobal="";
	public boolean mIsBluetoothConnected = false;

	public BluetoothDevice mDevice;

	//////////////////////////////////////////////////////////////////
	// dialogos en progreso
	public ProgressBar progressBarBoot;
	public ProgressDialog progressDialog,progressDialog2;
	public ProgressDialog progressDialogInicio;
	public ProgressBar progressBar_Apuntamiento;
	

	public Button btn_LogOut;
	public Spinner spin_TX,spin_RX,spin_Otros;
	public ArrayAdapter<String> TxAdapter,RxAdapter,OtrosAdapter;

	public Button btn_Ingresar,btn_Cargar_OPT,btn_SetFreq,btn_Reset,btn_Browser,btn_SetPower;
	public ToggleButton TB_Login,TB_CwOnOff,TB_Pointing;
	public TextView  TextPointing,TextPrueba,TextNivel;
	public EditText EditFreq,EditPass,EditPrueba,EditTxPower;
		
	// hilos
	
	public Handler puente;
	public VentanaDialogoNivel DialogoNivel;
	public Boolean Lectura_pointing=false,boolPassword=true, telnet=true;
	;
	public Thread th1;
	//
	////// opt 
	
	private static final int REQUEST_PATH = 1;
	 
	String curFileName,curFilePath;
	
	EditText EditPath;
	
	/// archivo procesamiento
	 File f;
	 FileReader lectorArchivo;
	
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
		
		LevantarXML();
		SetupUI();
	
		Botones();
		Log.d(TAG, "OnCreate");
		}
	
	
	private void SetupUI() {
		TB_Login.setChecked(false);
		progressBar_Apuntamiento.setMax(100);
		progressBar_Apuntamiento.setProgress(0);
	
	}

	private void Botones() {
		
		btn_Browser.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
		
			Intent intent1 = new Intent(getApplicationContext(), FileChooser.class);
	        startActivityForResult(intent1,REQUEST_PATH);
		
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
				FuncionEnviar("reset board");
				progressDialog.cancel();
			}
		});
				
		btn_Ingresar.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				FuncionEnviar("telnet localhost");
				
			}
		});
				
		btn_Cargar_OPT.setOnClickListener(new OnClickListener() {
			 String p;
			 String[] CadenaPartida;
			 int longitudArchivo; 
			
			@Override
			public void onClick(View v) {
			
				Log.d("OPT", "boton opt");
				Log.d("OPT", EditPath.getText().toString());
				p=LeerArchivo(EditPath.getText().toString());
				CadenaPartida = p.split("\n");
				longitudArchivo=CadenaPartida.length;
				Log.d("OPT", "lineas= "+longitudArchivo);
				for(int i=0;i<longitudArchivo;i++){
					Log.d("OPT cargado: ",CadenaPartida[i]+" Linea N� "+i);
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
					FuncionEnviar("rx iflDC off");
					FuncionEnviar("tx cw on");
					//Toast.makeText(getApplicationContext(), "CW ON", Toast.LENGTH_SHORT).show();
					
					
				}
				else{
					FuncionEnviar("tx cw off");
					FuncionEnviar("rx iflDC on");
				
				//Toast.makeText(getApplicationContext(), "CW OFF", Toast.LENGTH_SHORT).show();
				}
			
			}
		});
	
		btn_SetPower.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(EditTxPower.getText().toString().equals(""))
				{
					Toast.makeText(getApplicationContext(), "Ingrese potencia !!", Toast.LENGTH_SHORT).show();
					
				}
				else{FuncionEnviar("tx power -"+EditTxPower.getText().toString());}
					
					
					
					
			
			}
		});
		
		TB_Pointing.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
		
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				if (isChecked){
					
					FuncionEnviar("rx pointing enable");// habiulita el comando de ponting
					FuncionEnviar("rx pointing on");
					Lectura_pointing=true;
					Hilo();
				    th1.start();
					
				}else{
				
					FuncionEnviar("rx pointing off");// deshabilita el comando de ponting
					FuncionEnviar("rx pointing disable");
					progressBar_Apuntamiento.setProgress(0);
					Lectura_pointing=false;
					
				}
				
			}
		});
	
	}

	
	private void  LevantarXML() {
	
		TextPointing=(TextView) findViewById(R.id.TextPointing);
		TextPrueba=(TextView) findViewById(R.id.TextPrueba);
		TextNivel=(TextView) findViewById(R.id.TextNivel);
		
		btn_Ingresar=(Button) findViewById(R.id.btn_Ingresar);
		btn_SetFreq=(Button) findViewById(R.id.btn_SetFreq);
		btn_Reset=(Button) findViewById(R.id.btn_Reset);
		btn_Cargar_OPT=(Button) findViewById(R.id.btn_CargarOPT);
		btn_Browser=(Button) findViewById(R.id.btn_Browser);
		btn_SetPower=(Button) findViewById(R.id.btn_SetPower);
	
		TB_CwOnOff=(ToggleButton) findViewById(R.id.TB_CwOnOff);
		TB_Login=(ToggleButton) findViewById(R.id.TB_Login);
		TB_Pointing=(ToggleButton) findViewById(R.id.TB_Pointing);
		
		EditFreq=(EditText) findViewById(R.id.EditFreq);
		EditPass=(EditText) findViewById(R.id.EditPass);
		EditPath=(EditText) findViewById(R.id.EditPath);
		EditTxPower=(EditText) findViewById(R.id.EditTxPower);
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
		
		
		Log.d("FuncionDetectarComando", "detector string: "+detectorString+" Boolean: "+hab);
		String[] CadenaPartida = detectorString.split("\r");
		int longitud =CadenaPartida.length;
		Log.d("FuncionDetectarComando","Esta es la longitud  "+longitud);

		for(int i=0;i<longitud;i++){
		Log.d("FuncionDetectarComando","Esta es la cadena "+i+": "+CadenaPartida[i]+"-");
				}

		
	if(hab){	
		
		  
		
			if (detectorString.contains("Username:")){
			
				FuncionEnviar("admin");		
			}
			
			if(detectorString.contains("Password:")){
				
				final String pass=EditPass.getText().toString();
				
				 runOnUiThread(new Runnable() {
				       
							        public void run() {
							        	Log.d("FuncionDetectarComando","PASS EDIT: "+pass);
							        	Toast.makeText(getApplicationContext(), "Password: "+pass, Toast.LENGTH_SHORT).show();
										  }
							    });
				 FuncionEnviar(pass);	
						}	
					
							
			if(detectorString.contains(">")&telnet){
				
				telnet=false;
				 runOnUiThread(new Runnable() {
				       
							        public void run() {
										  Toast.makeText(getApplicationContext(), " Logueado en Telnet", Toast.LENGTH_SHORT).show();
									       }
							    });
			
				}	
			if(detectorString.contains(("tx cw on"))||detectorString.contains("tx cw off")){
				FuncionEnviar("tx cw");
				}	
			
			if(detectorString.contains("cw =")){
				 	  runOnUiThread(new Runnable() {
		        int posicion =strInputGlobal.indexOf("=");
					        public void run() {
								  Toast.makeText(getApplicationContext(), " Clean Carrier = "+strInputGlobal.substring(posicion+2,posicion+5), Toast.LENGTH_LONG).show();
							       }
					    });
					
				}	
			
			if(detectorString.contains("pointing =")){
				 runOnUiThread(new Runnable() {
		        int posicion =strInputGlobal.indexOf("=");
					        public void run() {
					        	 Toast.makeText(getApplicationContext(), "Point = "+strInputGlobal.substring(posicion+2,posicion+5), Toast.LENGTH_LONG).show();
							        }
					    });
			}	
			
			if(detectorString.contains("power =")){
				 runOnUiThread(new Runnable() {
					 int posicion =strInputGlobal.indexOf("=");
					        public void run() {
					        	 Toast.makeText(getApplicationContext(), "Tx Power = "+strInputGlobal.substring(posicion+2,posicion+5)+" dbm", Toast.LENGTH_LONG).show();
					        }
					    });
			}	
			if(detectorString.contains("Tx Frequency")){
				 runOnUiThread(new Runnable() {

				        int posicion =strInputGlobal.indexOf("=");
				    
				        public void run() {
				        	   Toast.makeText(getApplicationContext(), "Freq = "+strInputGlobal.substring(posicion+2,posicion+15), Toast.LENGTH_LONG).show();
							       }
				    });
				}	
			}
	
			else
			{
				telnet =true;
			
				
			if (detectorString.contains("iDirect login:")){
			FuncionEnviar("root");		
			}
			if(detectorString.contains("Password:")){
			Log.d("Linux", "Password:");
			
			if(boolPassword){
				FuncionEnviar("P@55w0rd!");
				Log.d("FuncionDetectarComando","FuncionEnviar(P@55w0rd!);");
				
				}
			
			else{
				FuncionEnviar("iDirect");
				Log.d("FuncionDetectarComando","FuncionEnviar(iDirect);");
			
			}
			}
			if(detectorString.contains("Login incorrect")){
				
				if (boolPassword){
					boolPassword=false;
				}else{
					boolPassword=true;
				}
				
			
			}
		/*	if(detectorString.contains(("Linux iDirect"))||CadenaPartida[2]==("#")){
				 runOnUiThread(new Runnable() {
         	        public void run() {
         	       	TB_Login.setChecked(true);
    				
         	        }
         	    });
				
			
			}*/
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
			progressDialog = ProgressDialog.show(ll_Inicio_Login.this, "Modulo Bluetooth...", "Conectando");// http://stackoverflow.com/a/11130220/1287554

			
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
			
			FuncionEnviar("\r");
			
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
						for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
						}
						final String strInput = new String(buffer, 0, i);
						strInputGlobal=strInput;
						FuncionDetectarComando(strInputGlobal,Habilitacion);
						
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

	public class VentanaDialogoNivel extends AsyncTask<Void, Void, Void> {
		
	@Override 
		protected void onPreExecute() {
		}

		@Override
		protected Void doInBackground(Void... devices) {
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			String[] NivelesAlmacenados = strInputGlobal.split("\r");
		
			try {
				float nivelFlotante= Float.parseFloat(NivelesAlmacenados[0])*100;
				int NivelBaliza=(int)nivelFlotante;
				//TextPrueba.setText("String: "+NivelesAlmacenados[0]+" float * 10:  "+nivelFlotante +" integer:  "+NivelBaliza);
				progressBar_Apuntamiento.setProgress(NivelBaliza);
				
				TextNivel.setText("Nivel= -"+NivelBaliza+" dbm");
				
			} catch (Exception e) {
				progressBar_Apuntamiento.setProgress(0);
				
				TextNivel.setText("Nivel= -  dbm");
			//	Toast.makeText(getApplicationContext(), "No hay medicion", Toast.LENGTH_SHORT).show();
				
			}
			
			strInputGlobal="";
		}

	}

	public void Hilo() {
		Log.d("Hilo", "th1 = new Thread(new Runnable()");
		th1 = new Thread(new Runnable() {
        	
		
            @Override
            public void run() {
            	 runOnUiThread(new Runnable() {
            	        public void run() {
            	    	progressDialog = new ProgressDialog(ll_Inicio_Login.this);
            	    		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            	    		progressDialog.setMessage("Esperando medicion");
            	    		progressDialog.setMax(10);
            	    		progressDialog.setProgress(0);
            	    		progressDialog.setCancelable(false);
            	    		progressDialog.show();	
            	        }
            	    });
            	
            	
            	try {
            		Log.d("Hilo", "th1 = Thread.sleep(11000)");
            		
            		Thread.sleep(12000);
            		progressDialog.cancel();	
				} catch (Exception e) {
					// TODO: handle exception
				}
            	
          
            	
            	while(Lectura_pointing){
            		Log.d("Hilo", "while");
           try {
            	Thread.sleep(1000);
				Log.d("Hilo", "DialogoNivel.execute()");
				DialogoNivel= new VentanaDialogoNivel();
				DialogoNivel.execute();
				
			} catch (InterruptedException e) {
				Lectura_pointing=false;
				e.printStackTrace();
			}
            	}
            	FuncionEnviar("tx iflDC on");
            	progressBar_Apuntamiento.setProgress(0);
            	
            }
          });
		}
	
	//////////////////////// cargar opt
	
	
	
	 // Listen for results.
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // See which child activity is calling us back.
    	if (requestCode == REQUEST_PATH){
    		if (resultCode == RESULT_OK) { 
    			curFileName = data.getStringExtra("GetFileName"); 
    			curFilePath = data.getStringExtra("GetPath"); 
    			EditPath.setText(curFilePath+"/"+curFileName);
    		}
    	 }
    }
	
    public static String LeerArchivo(String nombre)

	//El parametro nombre indica el nombre del archivo por ejemplo "prueba.txt" 

	{

	try{

	File f;
	FileReader lectorArchivo;

	//Creamos el objeto del archivo que vamos a leer
	f = new File(nombre);

	//Creamos el objeto FileReader que abrira el flujo(Stream) de datos para realizar la lectura
	lectorArchivo = new FileReader(f);

	//Creamos un lector en buffer para recopilar datos a travez del flujo "lectorArchivo" que hemos creado
	BufferedReader br = new BufferedReader(lectorArchivo);

	String l="";
	//Esta variable "l" la utilizamos para guardar mas adelante toda la lectura del archivo

	String aux="";/*variable auxiliar*/

	while(true)
	//este ciclo while se usa para repetir el proceso de lectura, ya que se lee solo 1 linea de texto a la vez
	{
		aux=br.readLine();
		//leemos una linea de texto y la guardamos en la variable auxiliar
		if(aux!=null)
		l=l+aux+"\n";
		/*si la variable aux tiene datos se va acumulando en la variable l,
		* en caso de ser nula quiere decir que ya nos hemos leido todo
		* el archivo de texto*/

		else
		break;
		}

		br.close();

		lectorArchivo.close();

		return l;

		}catch(IOException e){
		System.out.println("Error:"+e.getMessage());
		}
		return null;
		}
	
	
	}
	
	
	

