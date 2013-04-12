package com.PsichiX.JustIDS.display;

import java.lang.reflect.Method;
import java.util.LinkedList;

import com.PsichiX.JustIDS.MainActivity;
import com.PsichiX.JustIDS.PlayerInformation;
import com.PsichiX.JustIDS.R;
import com.PsichiX.JustIDS.R.layout;
import com.PsichiX.JustIDS.R.menu;
import com.PsichiX.JustIDS.SpectateActivity;
import com.PsichiX.JustIDS.logic.Player;
import com.PsichiX.JustIDS.services.WifiService;
import com.PsichiX.JustIDS.trash.AudioRecordTest;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MainScreenActivity extends Activity {
	private String tag = "MainScreenActivity";
	
	public LinkedList<Player> players = new LinkedList<Player>();
	public static String playerName;
	
	LayoutInflater inflater;
	WifiService wifi;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        
        //TESTOWE!!!
        Player pi = new Player();
        pi.name = ProfileActivity.generateName();
        players.add(pi);
        
        //sprawdzenie czy WiFI jest on.
        
        wifi = new WifiService(this);
    	if (!wifi.isWifiInWorkingState()){
    		startActivity(new Intent(this, WifiErrorActivity.class));
    		Log.d(tag , "WIFI DISABLED!");
    		this.finish();
    		return;
    	}
    	else	{
    		Log.d(tag , "WIFI ENABLED, carry on");
    	}
        
    	playerName = ProfileActivity.generateName();
        
    	setPlayersView();
    	//TODO: @Jarek odpalic siec, zaczac zbierac graczy i wysylac swoje imie i id, okreœlic czy jest juz walka w sieci. 
    	
    	setUpScreen();
    }

    /**
     * Listenery na guzikach, adaptery itp
     */
    private void setUpScreen()	{
    	//Pozostala konfiguracja ekranu
        TextView playerNameView = (TextView) findViewById(R.id.txt_yourname);
        playerNameView.setText(playerName);
        
        playerNameView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(tag , "clicked: PROFILE ");
				gotoProfile();
			}	
		});
        
        Button btnFight = (Button) findViewById(R.id.button_fight);
        btnFight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(tag , "clicked: FIGHT ");
				gotoFight();
			}	
		});
        
        Button btnHowto = (Button) findViewById(R.id.button_howto);
        btnHowto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(tag , "clicked: HOWTO ");
				gotoHowto();
			}	
		});
        
		ArrayAdapter<Player> playersAdapter = new ArrayAdapter<Player>(this,
				R.layout.listitem_player,
				players) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View row;
				row = inflater.inflate(R.layout.listitem_player, null);
				TextView name = (TextView) row.findViewById(R.id.listitem_player_name);
				name.setText(getItem(position).name);
				return row;
			}
		};
		ListView playersList = (ListView) findViewById(R.id.players_list);
		playersList.setAdapter(playersAdapter);
    }
    
    /**
     * Do wywo³ania przy odœwierzeniu listy graczy
     */
    public void setPlayersView()	{
        LinearLayout playersNoneLL = (LinearLayout) findViewById(R.id.players_none); 
        LinearLayout playersAreLL = (LinearLayout) findViewById(R.id.players_avalible); 
        if(players.size()>0)	{
            //JEŒLI GRACZE SA W SIECI, TO SUPER:
            playersNoneLL.setVisibility(View.GONE);
            playersAreLL.setVisibility(View.VISIBLE);
        }
        else	{
            //JESLI GRACZY NIE MA:
            playersNoneLL.setVisibility(View.VISIBLE);
            playersAreLL.setVisibility(View.GONE);
        }
    }
    
    public void gotoProfile()	{
    	startActivity(new Intent(this, ProfileActivity.class));
    }
    
    public void gotoFight()	{
    	startActivity(new Intent(this, MainActivity.class));
    }
    
    public void gotoHowto()	{
    	//TODO: testowo tylko. 
    	startActivity(new Intent(this, AudioRecordTest.class));
    }
    
    public void gotoSpectate()	{
    	startActivity(new Intent(this, SpectateActivity.class));
    }
    

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_screen, menu);
        return true;
    }
}
