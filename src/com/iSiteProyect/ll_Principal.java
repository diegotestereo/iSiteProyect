package com.iSiteProyect;

import java.io.IOException;
import java.net.Socket;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;



public class ll_Principal extends Activity{
	
	Button btn_Cw,btn_Opt,btn_Otros,btn_Info;
	Socket mBTSocket;
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

}
