package com.PsichiX.JustIDS.trash;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.lang.reflect.Method;

import com.PsichiX.JustIDS.R;

/**
 * To ponoc obsluguje cale wifi, potrafi je nastawiac itd. 
 * Do wgladu. 
 * @author bartek
 *
 */
public class WifiAP extends Activity {

    // boolean mIsWifiEnabled = false;
    private static final int WIFI_AP_STATE_UNKNOWN = -1;
    private static final int WIFI_AP_STATE_DISABLING = 0;
    private static final int WIFI_AP_STATE_DISABLED = 1;
    private static final int WIFI_AP_STATE_ENABLING = 2;
    private static final int WIFI_AP_STATE_ENABLED = 3;
    private static final int WIFI_AP_STATE_FAILED = 4;

    private final String[] WIFI_STATE_TEXTSTATE = new String[] {
        "DISABLING","DISABLED","ENABLING","ENABLED","FAILED"
    };

    private WifiManager wifi;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        //setContentView(R.layout.wifi);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                |WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                |WindowManager.LayoutParams.FLAG_DIM_BEHIND
        );

        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatusDisplay();
    }

    public void toggleWifi(View v) {
        boolean wifiApIsOn = getWifiAPState()==WIFI_AP_STATE_ENABLED || getWifiAPState()==WIFI_AP_STATE_ENABLING;
        new SetWifiAPTask(!wifiApIsOn,false).execute();
    }

    public void close(View v) {
        boolean wifiApIsOn = getWifiAPState()==WIFI_AP_STATE_ENABLED || getWifiAPState()==WIFI_AP_STATE_ENABLING;
        if (wifiApIsOn) {
            new SetWifiAPTask(false,true).execute();
        } else {
            finish();
        }
    }


    /**
     * Endable/disable wifi
     * @param enabled
     * @return WifiAP state
     */
    private int setWifiApEnabled(boolean enabled) {
        Log.d("WifiAP", "*** setWifiApEnabled CALLED **** " + enabled);
        if (enabled && wifi.getConnectionInfo() !=null) {
            wifi.setWifiEnabled(false);
            try {Thread.sleep(1500);} catch (Exception e) {}
        }

        //int duration = Toast.LENGTH_LONG;
        //String toastText = "MobileAP status: ";
        int state = WIFI_AP_STATE_UNKNOWN;
        try {
            wifi.setWifiEnabled(false);
            Method method1 = wifi.getClass().getMethod("setWifiApEnabled",
                WifiConfiguration.class, boolean.class);
            method1.invoke(wifi, null, enabled); // true
            Method method2 = wifi.getClass().getMethod("getWifiApState");
            state = (Integer) method2.invoke(wifi);
        } catch (Exception e) {
           Log.e(WIFI_SERVICE, e.getMessage());
           // toastText += "ERROR " + e.getMessage();
        }

        if (!enabled) {
            int loopMax = 10;
            while (loopMax>0 && (getWifiAPState()==WIFI_AP_STATE_DISABLING
                    || getWifiAPState()==WIFI_AP_STATE_ENABLED
                    || getWifiAPState()==WIFI_AP_STATE_FAILED)) {
                try {Thread.sleep(500);loopMax--;} catch (Exception e) {}
            }
            wifi.setWifiEnabled(true);
        } else if (enabled) {
            int loopMax = 10;
            while (loopMax>0 && (getWifiAPState()==WIFI_AP_STATE_ENABLING
                    || getWifiAPState()==WIFI_AP_STATE_DISABLED
                    || getWifiAPState()==WIFI_AP_STATE_FAILED)) {
                try {Thread.sleep(500);loopMax--;} catch (Exception e) {}
            }
        }

        return state;
    }
    
    private int getWifiAPState() {
        int state = WIFI_AP_STATE_UNKNOWN;
        try {
            Method method2 = wifi.getClass().getMethod("getWifiApState");
            state = (Integer) method2.invoke(wifi);
        } catch (Exception e) {}
        Log.d("WifiAP", "getWifiAPState.state " + (state==-1?"UNKNOWN":WIFI_STATE_TEXTSTATE[state]));
        return state;
    }
    
    private void updateStatusDisplay() {
    	/*
        if (getWifiAPState()==WIFI_AP_STATE_ENABLED || getWifiAPState()==WIFI_AP_STATE_ENABLING) {
            ((Button)findViewById(R.id.btnWifiToggle)).setText("Turn off");
            findViewById(R.id.bg).setBackgroundResource(R.drawable.bg_wifi_on);
        } else {
            ((Button)findViewById(R.id.btnWifiToggle)).setText("Turn on");
            findViewById(R.id.bg).setBackgroundResource(R.drawable.bg_wifi_off);
        }
		*/
    }


    class SetWifiAPTask extends AsyncTask<Void, Void, Void> {

        boolean mMode;
        boolean mFinish;

        public SetWifiAPTask(boolean mode, boolean finish) {
            mMode = mode;
            mFinish = finish;
        }

        ProgressDialog d = new ProgressDialog(WifiAP.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            d.setTitle("Turning WiFi AP " + (mMode?"on":"off") + "...");
            d.setMessage("...please wait a moment.");
            d.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {d.dismiss();} catch (IllegalArgumentException e) {};
            updateStatusDisplay();
            if (mFinish) finish();
        }

        @Override
        protected Void doInBackground(Void... params) {
            setWifiApEnabled(mMode);
            return null;
        }
    }


}
