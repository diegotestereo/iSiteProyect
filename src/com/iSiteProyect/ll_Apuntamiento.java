package com.iSiteProyect;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class ll_Apuntamiento extends Activity{
	
	Button btn_Calcular;
	TextView Azimuth,Elevacion,Polarizador,TextPrueba;
	EditText ETxt_Lat,ETxt_Lon,ETxt_Sat;
	double Az=0,AzRad,El=0,ElRad,Skew=0,SkewRad=0,Sat,SatRad,Lat,LatRad,Lon,LonRad,Dif=0,DifRad, Const=0.1512,ConstRad=0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ll_apuntamiento);
		LevantarXML();
		
		TextPrueba.setText(""+Float.parseFloat(ETxt_Sat.getText().toString()));//Float.parseFloat(ETxt_Lat.getText().toString());
		Botones();
		
	}

	private void Botones() {
		btn_Calcular.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
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
				El=redondear(El, 2);
				Elevacion.setText(""+El+"°");
				
				if (Lat>0){
					AzRad=Math.PI + Math.atan(Math.tan(DifRad)/Math.sin(-LatRad));
				}else{
					AzRad=2*Math.PI + Math.atan(Math.tan(DifRad)/Math.sin(-LatRad));
				}
				
				
				Az=Math.toDegrees(AzRad);
				Az=redondear(Az,2);
				Azimuth.setText(""+Az+"°");
				
				SkewRad=Math.atan(Math.sin(DifRad)/Math.tan(LatRad));
				Skew=Math.toDegrees(SkewRad);
				Skew=redondear(Skew,1);
				Polarizador.setText(""+Skew+"°");
			}
		});
		
	}

	private void LevantarXML() {
		btn_Calcular=(Button) findViewById(R.id.btn_Calcular);
		Azimuth=(TextView) findViewById(R.id.Text_Azimuth);
		Elevacion=(TextView) findViewById(R.id.Text_Elevacion);
		Polarizador=(TextView) findViewById(R.id.Text_Polarizador);
		TextPrueba=(TextView) findViewById(R.id.textPrueba);
		ETxt_Lat=(EditText) findViewById(R.id.EditLat);
		ETxt_Lon=(EditText) findViewById(R.id.EditLong);
		ETxt_Sat=(EditText) findViewById(R.id.EditSat);
		
	}

public static double redondear( double numero, int decimales ) {
    return Math.round(numero*Math.pow(10,decimales))/Math.pow(10,decimales);
  }

}
