package com.PsichiX.JustIDS;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import com.PsichiX.JustIDS.comm.UDPBroadCastManager;
import com.PsichiX.JustIDS.game.GameManager;
import com.PsichiX.JustIDS.game.GameStateMachine;

public class ScreamFightApplication extends Application {


    private class ReadyToPlayReceiver extends BroadcastReceiver {

        @Override
        public synchronized void onReceive(Context context, Intent intent) {
            String name = intent.getStringExtra("NAME");
            String android_id = Settings.Secure.getString(ScreamFightApplication.this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            if (ScreamFightApplication.this.gm != null) {
                ScreamFightApplication.this.gm.pause();
                ScreamFightApplication.this.gm.destroy();
            }
            ScreamFightApplication.this.ubm = new UDPBroadCastManager(ScreamFightApplication.this);
            ScreamFightApplication.this.gm = new GameManager(ubm, android_id, name, new NotificationListener());
        }
    }

    private class JoinGameReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            gm.joinGame();
        }
    }

    private class ResetGameReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            gm.resetGame();
        }
    }

    private class AttackWithStrengthReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            double strength = intent.getDoubleExtra("STRENGTH", 0.0d);
            gm.attackWithStrength(strength);
        }
    }


    private class NotificationListener implements GameStateMachine.GameStateChangeListener {
        @Override
        public void notifyStateChange(GameStateMachine.GameStateNotificationEnum gameStateNotification) {
            Intent intent = new Intent("com.PsichiX.JustIDS.ScreamFightNotificationService");
            intent.putExtra("NOTIFICATION_TYPE", gameStateNotification.ordinal()); //passing enum as integer. Fast.
            intent.putExtra("MY_PLAYER", gm.getMyPlayerId());
            intent.putExtra("ALL_PLAYERS", gm.getPlayers());
            LocalBroadcastManager.getInstance(ScreamFightApplication.this).sendBroadcast(intent);
        }
    }

    private GameManager gm;
    private UDPBroadCastManager ubm;

    @Override
    public void onCreate() {
        super.onCreate();

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(new ReadyToPlayReceiver(),
                new IntentFilter("com.PsichiX.JustIDS.readyToPlay"));
        localBroadcastManager.registerReceiver(new JoinGameReceiver(),
                new IntentFilter("com.PsichiX.JustIDS.joinGame"));
        localBroadcastManager.registerReceiver(new ResetGameReceiver(),
                new IntentFilter("com.PsichiX.JustIDS.resetGame"));
        localBroadcastManager.registerReceiver(new AttackWithStrengthReceiver(),
                new IntentFilter("com.PsichiX.JustIDS.attackWithStrength"));
    }

}
