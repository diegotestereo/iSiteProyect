package com.iSiteProyect.Servicios;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MiServicio extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.d("Mi servicio"," Entro");
		return null;
	}

}
