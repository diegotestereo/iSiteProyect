package com.iSiteProyect;


import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class ll_Apuntamiento extends Activity{
	
	private Button btn_Calcular,btn_Bluetooth,btn_GeoLocation;
	private TextView Azimuth,Elevacion,Polarizador,TextPrueba;
	private EditText ETxt_Lat,ETxt_Lon,ETxt_Sat;
	private Spinner Spin_Satelites,Spin_Offset;
	private String[] Satelites={"IS23","Arsat 1","AMC6"},OffsetAntena={"0°","15°","16°","17°","18°","19°","20°","21°","22°","23°","24°","25°"};
	ArrayAdapter<String> AdaptadorSatelites,AdaptadorOffset;
	double OffSet=0,Az=0,AzRad,El=0,ElRad,Skew=0,SkewRad=0,Sat,SatRad,Lat,LatRad,Lon,LonRad,Dif=0,DifRad, Const=0.1512,ConstRad=0;
	private LocationManager locManager;
	private LocationListener locListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ll_apuntamiento);
		LevantarXML();
		Botones();
		Spinners();
		
	}

	private void Spinners() {
		 AdaptadorSatelites = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Satelites);
		 AdaptadorOffset=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,OffsetAntena);
		 Spin_Satelites.setAdapter(AdaptadorSatelites);
		 Spin_Offset.setAdapter(AdaptadorOffset);
		 
		 Spin_Satelites.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				String var=null;
				switch (position) {
				case 0:
					var="-53.0";
					break;
				case 1:
					var="-71.8";
					break;
				case 2:
					var="-72.0";
					break;
				default:
					break;
				}
				
				ETxt_Sat.setText(var);
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
	
		 Spin_Offset.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					OffSet=0;
					break;
				case 1:
					OffSet=15.0;
					break;
				case 2:
					OffSet=16.0;
					break;
				case 3:
					OffSet=17.0;
					break;
				case 4:
					OffSet=18.0;
					break;
				case 5:
					OffSet=19.0;
					break;
				case 6:
					OffSet=20.0;
					break;
				case 7:
					OffSet=21.0;
					break;
				case 8:
					OffSet=22.0;
					break;
				case 9:
					OffSet=23.0;
					break;
				case 10:
					OffSet=24.0;
					break;
				case 11:
					OffSet=25.0;
					break;
				default:
					OffSet=0;
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	private void EnableGPSIfPossible()
	{   
	    final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
	     if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
	            buildAlertMessageNoGps();
	           
	        }
	}
	
	private void Botones() {
		
		btn_Bluetooth.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intento=new Intent(getApplicationContext(),Homescreen.class);
				startActivity(intento);
			}
		});
		
		
		btn_Calcular.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				calcular();
			}
		});
		
		btn_GeoLocation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EnableGPSIfPossible();
				comenzarLocalizacion();

  		}
		});
		
	}

	private void LevantarXML() {
		
		btn_Calcular=(Button) findViewById(R.id.btn_Calcular);
		btn_Bluetooth=(Button) findViewById(R.id.btn_Bluetooth);
		btn_GeoLocation=(Button) findViewById(R.id.btn_GeoLocation);
		
		Azimuth=(TextView) findViewById(R.id.Text_Azimuth);
		Elevacion=(TextView) findViewById(R.id.Text_Elevacion);
		Polarizador=(TextView) findViewById(R.id.Text_Polarizador);
		TextPrueba=(TextView) findViewById(R.id.textPrueba);
		
		ETxt_Lat=(EditText) findViewById(R.id.EditLat);
		ETxt_Lon=(EditText) findViewById(R.id.EditLong);
		ETxt_Sat=(EditText) findViewById(R.id.EditSat);
	
		Spin_Satelites=(Spinner)findViewById(R.id.SpinSatelites);
		Spin_Offset=(Spinner)findViewById(R.id.Spin_Offset);
		
	}

	public void calcular(){
		
		Lat=Float.parseFloat(ETxt_Lat.getText().toString());
		Lon=Float.parseFloat(ETxt_Lon.getText().toString());
		Sat=Float.parseFloat(ETxt_Sat.getText().toString());
		
		LatRad=Math.toRadians(Lat);
		LonRad=Math.toRadians(Lon);
		SatRad=Math.toRadians(Sat);

		Dif=Sat-Lon;
		DifRad=Math.toRadians(Dif);

		ElRad= Math.atan((Math.cos(DifRad)*Math.cos(LatRad) - Const )/Math.sqrt(1-Math.pow((Math.cos(DifRad)*Math.cos(LatRad)) ,2   )));
		El=Math.toDegrees(ElRad);
		El=redondear(El, 2)-OffSet;
		Elevacion.setText(""+El+"°");
		
		if (Lat>0){
			if(Dif<0){
				AzRad=Math.PI + Math.atan(Math.tan(DifRad)/Math.sin(-LatRad));}
			else{
				AzRad=-2*Math.PI + Math.atan(Math.tan(DifRad)/Math.sin(-LatRad));
			}
		}else{
			
			if(Dif>0){
				AzRad= Math.atan(Math.tan(DifRad)/Math.sin(-LatRad));}
			else{
				AzRad=2*Math.PI + Math.atan(Math.tan(DifRad)/Math.sin(-LatRad));
			}
		}
		
		if (Lon==Sat){
			AzRad=0;
		}
		
		
		Az=Math.toDegrees(AzRad);
		Az=redondear(Az,2);
		Azimuth.setText(""+Az+"°");
		
		SkewRad=Math.atan(Math.sin(DifRad)/Math.tan(LatRad));
		Skew=Math.toDegrees(SkewRad);
		Skew=redondear(Skew,1);
		Polarizador.setText(""+Skew+"°");
		
	}
	
	public static double redondear( double numero, int decimales ) {
    return Math.round(numero*Math.pow(10,decimales))/Math.pow(10,decimales);
  }

	private void comenzarLocalizacion()
    {
    	//Obtenemos una referencia al LocationManager
    	locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    	
    	//Obtenemos la última posición conocida
    	Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	
    	//Mostramos la última posición conocida
    	mostrarPosicion(loc);
    	
    	//Nos registramos para recibir actualizaciones de la posición
    	locListener = new LocationListener() {
	    	public void onLocationChanged(Location location) {
	    		mostrarPosicion(location);
	    	}
	    	
	    
	    	public void onStatusChanged(String provider, int status, Bundle extras){
	    		Log.i("", "Provider Status: " + status);
	    		
	    	}


			@Override
			public void onProviderEnabled(String provider) {
					
			}

			@Override
			public void onProviderDisabled(String provider) {
				
			}
    	};
    	
    	locManager.requestLocationUpdates(
    			LocationManager.GPS_PROVIDER, 30000, 0, locListener);
    }
	
	private void mostrarPosicion(Location loc) {
	    	if(loc != null)
	    	{
	    		ETxt_Lat.setText(String.valueOf(loc.getLatitude()));
	    		ETxt_Lon.setText(String.valueOf(loc.getLongitude()));
	    		Log.i("", String.valueOf(loc.getLatitude() + " - " + String.valueOf(loc.getLongitude())));
	    		calcular();
	    	}
	    	else
	    	{
	    		Toast.makeText(getApplicationContext(), "Sin localizacion", Toast.LENGTH_SHORT).show();
	    		ETxt_Lat.setText("-");
	    		ETxt_Lon.setText("-");
	    		
	    	}
	    }
	
	private  void buildAlertMessageNoGps() {
	        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setMessage("Su GPS parece estar deshabilitado, puede encenderlo?")
	               .setCancelable(false)
	               .setPositiveButton("Si", new DialogInterface.OnClickListener() {
	                   public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
	                     startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	                   }
	               })
	               .setNegativeButton("No", new DialogInterface.OnClickListener() {
	                   public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
	                        dialog.cancel();
	                   }
	               });
	        final AlertDialog alert = builder.create();
	        alert.show();
	    }
	
	
}
