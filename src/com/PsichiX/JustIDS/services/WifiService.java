package com.PsichiX.JustIDS.services;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;

/**
 * Faktyczny Serwis zalatwiajacy kwestie wifi 
 * TODO: Jarek, da sie spiac to z Broadcast Manager? 
 * @author bartek
 */
public class WifiService {

    private static final int WIFI_AP_STATE_UNKNOWN = -1;
    private static final int WIFI_AP_STATE_DISABLING = 0;
    private static final int WIFI_AP_STATE_DISABLED = 1;
    private static final int WIFI_AP_STATE_ENABLING = 2;
    private static final int WIFI_AP_STATE_ENABLED = 3;
    private static final int WIFI_AP_STATE_FAILED = 4;
	
	WifiManager wifi;
	
	public WifiService(Activity context)	{
		wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	}
	
	public boolean isWifiInWorkingState()	{
		if(wifi.isWifiEnabled() || getWifiAPState()==WIFI_AP_STATE_ENABLED)
			return true;
		else return false;
	}
	
	private int getWifiAPState() {
    	
        int state = WIFI_AP_STATE_UNKNOWN;
        try {
            Method method2 = wifi.getClass().getMethod("getWifiApState");
            state = (Integer) method2.invoke(wifi);
        } catch (Exception e) {}
        //Log.d("WifiAP", "getWifiAPState.state " + (state==-1?"UNKNOWN":WIFI_STATE_TEXTSTATE[state]));
        return state;
    }
}
