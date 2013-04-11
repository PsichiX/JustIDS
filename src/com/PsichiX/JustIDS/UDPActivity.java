package com.PsichiX.JustIDS;

import android.os.Bundle;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class UDPActivity extends Activity {

	GameStateManager gsm;
	Thread thread;
	Button button10;
	Button button20;
	EditText text;
	TextView textView;
	TextView textView2;
	
	int count = 0;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_udp);
		String name = "NAME: " + Secure.getString(this.getContentResolver(),Secure.ANDROID_ID);
		gsm = new GameStateManager(this, new BroadCastManager(), name, true);
		button10 = (Button) findViewById(R.id.button1);
		button10.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gsm.attackWithStrength(10.0);
			}
			
		});
		button20 = (Button) findViewById(R.id.button2);
		button20.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gsm.attackWithStrength(20.0);
			}
			
		});
		textView = (TextView) this.findViewById(R.id.textView1);
		textView2 = (TextView) this.findViewById(R.id.textView2);

		gsm.setSomethingChangedListener(new Runnable() { 
			public void run() {
				UDPActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						textView.setText("LP: " + gsm.getLifePointsOfMine());
					}					
				});
			};
		});
		
		gsm.setHitListener(new Runnable() { 
			public void run() {
				UDPActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						textView2.setText("Hit: " + ++count);
					}					
				});
			};
		});
		
	}

}
