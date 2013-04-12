package com.PsichiX.JustIDS.display;

import com.PsichiX.JustIDS.MainActivity;
import com.PsichiX.JustIDS.R;
import com.PsichiX.JustIDS.R.layout;
import com.PsichiX.JustIDS.R.menu;
import com.PsichiX.JustIDS.trash.AudioRecordTest;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainMenuActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Button btnFight = (Button) findViewById(R.id.button_fight);
        btnFight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gotoFight();
			}	
		});
        
        Button btnHowto = (Button) findViewById(R.id.button_howto);
        btnHowto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gotoHowto();
			}	
		});
        
        Button btnProfile = (Button) findViewById(R.id.button_profile);
        btnProfile.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gotoProfile();
			}	
		});
    }
    
    public void gotoFight()	{
    	WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
    	if (wifi.isWifiEnabled()){
    		startActivity(new Intent(this, MainActivity.class));
    	}
    	else	{
    		Toast.makeText(this, "TURN ON WIFI, THEN RESTART THE GAME!!!", Toast.LENGTH_LONG).show();
    	}
    }
   
    public void gotoSpectate()	{
    	//TODO: Jak to ma wygl¹dac ? 
    	//lista graczy z health i powiadomienia kto co rzucil?
    }
    
    public void gotoHowto()	{
    	//TODO: testowo tylko. 
    	startActivity(new Intent(this, AudioRecordTest.class));
    }
    
    public void gotoProfile()	{
    	startActivity(new Intent(this, ProfileActivity.class));
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }
    
	@Override
	public void onBackPressed() {
    	//TODO: bardzo brzydko, ale pewnie.
		this.finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}
