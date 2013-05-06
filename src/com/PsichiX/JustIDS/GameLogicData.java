package com.PsichiX.JustIDS;

import com.PsichiX.JustIDS.display.MainScreenActivity;
import com.PsichiX.JustIDS.display.ProfileActivity;

import android.app.Activity;
import android.content.SharedPreferences;


public class GameLogicData {
	public static final String PREFS_NAME="ScreamFightPrefs";
	
	private static Activity loader;
	
    private static String playerName;
    public static boolean firstTimeRun=true;

	public static void loadOptions(Activity ack)	{
		loader = ack;
		 SharedPreferences settings = ack.getSharedPreferences(PREFS_NAME, 0);
		 playerName = settings.getString("PLAYER_NAME", null);
		 if(playerName==null)	{
			 setPlayerName(ProfileActivity.generateName());
			 firstTimeRun=true;
		 }
		 else	{
			 firstTimeRun=false;
		 }
	}
	
	public static void setPlayerName(String newPlayerName) {
		playerName = newPlayerName;
        SharedPreferences settings = loader.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("PLAYER_NAME", playerName);
        editor.commit();
	}

	public static String getPlayerName() {
		return playerName;
	}
}
