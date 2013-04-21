package com.PsichiX.JustIDS;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.PsichiX.JustIDS.comm.UDPBroadCastManager;
import com.PsichiX.JustIDS.display.PrintCurrentState;
import com.PsichiX.JustIDS.game.GameManager;
import com.PsichiX.JustIDS.game.GameStateMachine;
import com.PsichiX.JustIDS.simulator.SimulatedScenarioEnum;
import com.PsichiX.JustIDS.simulator.Simulator;

public class ScreamFightApplication extends Application {

    private static final String TAG = ScreamFightApplication.class.getName();

    private Simulator simulator = null;

    private class ReadyToPlayReceiver extends BroadcastReceiver {

        @Override
        public synchronized void onReceive(Context context, Intent intent) {
            String name = intent.getStringExtra("NAME");
            String android_id = Settings.Secure.getString(ScreamFightApplication.this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            if (ScreamFightApplication.this.gm != null) {
                ScreamFightApplication.this.gm.pause();
                ScreamFightApplication.this.gm.stopGameManager();
            }
            ScreamFightApplication.this.ubm = new UDPBroadCastManager(ScreamFightApplication.this);
            ScreamFightApplication.this.gm = new GameManager(ubm, android_id, name, new NotificationListener());
            ScreamFightApplication.this.gm.startGameManager();
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

    private class StartSimulationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            SimulatedScenarioEnum scenario = SimulatedScenarioEnum.values()[intent.getIntExtra("SCENARIO", -1)];
            if (simulator != null) {
                simulator.stopSimulator();
            }
            gm.resetGame();
            // start simulation with 3 observers
            simulator = new Simulator(ScreamFightApplication.this, ubm, 3, scenario);
            simulator.startSimulator();
        }
    }

    private class StopSimulationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (simulator != null) {
                simulator.stopSimulator();
                gm.resetGame();
            }
        }
    }

    private class NotificationListener implements GameStateMachine.GameStateChangeListener {
        @Override
        public void notifyStateChange(GameStateMachine.GameStateNotificationEnum gameStateNotification) {
            Intent intent = new Intent("com.PsichiX.JustIDS.ScreamFightNotificationService");
                intent.putExtra("NOTIFICATION_TYPE", gameStateNotification.ordinal()); //passing enum as integer. Fast.
            intent.putExtra("MY_PLAYER", gm.getMyPlayer());
            intent.putExtra("ALL_PLAYERS", gm.getPlayers());
            Log.i(TAG, "Sending local broadcast notification (" + gm.getName() + "): " + gameStateNotification + ":" +
                    PrintCurrentState.getCurrentStateAsString(gm.getMyPlayer(), gm.getPlayers()));
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
        localBroadcastManager.registerReceiver(new StartSimulationReceiver(),
                new IntentFilter("com.PsichiX.JustIDS.startSimulation"));
        localBroadcastManager.registerReceiver(new StopSimulationReceiver(),
                new IntentFilter("com.PsichiX.JustIDS.stopSimulation"));
    }

}
