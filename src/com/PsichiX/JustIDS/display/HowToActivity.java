package com.PsichiX.JustIDS.display;

import com.PsichiX.JustIDS.R;
import com.PsichiX.JustIDS.R.layout;
import com.PsichiX.JustIDS.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class HowToActivity extends Activity {

	/*
	 * 
	 * tresc How To:
	 * 
	 * 1) Connect to same WiFi hotspot with your opponent!
	 * You can start hotspot on your phone too! 
	 * 2) Click Fight Button
	 * when you see the opponent on the list
	 * 3) Scream to load energy. Wave to attack the opponent
	 * The more energy you have, and the stronger you wave, the harder you hit!
	 * 4) Watch, cheer and make photos!
	 * If there is a fight in the network, you automatically go to "spectate" mode.
	 * 5) Share the results
	 * and don't forget to <rate the game>
	 */
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_howto);
    }

}
