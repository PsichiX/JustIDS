package com.PsichiX.JustIDS;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import com.PsichiX.JustIDS.comm.BroadCastManager;
import com.PsichiX.JustIDS.game.GameManager;
import com.PsichiX.JustIDS.game.GameStateMachine;

public class ScreamFightApplication extends Application {

    private class ReadyToPlayReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String name = intent.getStringExtra("NAME");
            gm.readyToPlay(name);
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
            intent.putExtra("MY_PLAYER", gm.myPlayerId());
            intent.putExtra("ALL_PLAYERS", gm.getPlayers());
            LocalBroadcastManager.getInstance(ScreamFightApplication.this).sendBroadcast(intent);
        }
    }

    private GameManager gm;

    @Override
    public void onCreate() {
        super.onCreate();
        String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        this.gm = new GameManager(new BroadCastManager(this),
                android_id, new NotificationListener());

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
