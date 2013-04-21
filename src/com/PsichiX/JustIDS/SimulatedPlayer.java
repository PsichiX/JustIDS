package com.PsichiX.JustIDS;

import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import com.PsichiX.JustIDS.comm.BroadCastManager;
import com.PsichiX.JustIDS.comm.BroadcastManagerInterface;
import com.PsichiX.JustIDS.game.GameManager;
import com.PsichiX.JustIDS.game.GameStateMachine;
import com.PsichiX.JustIDS.message.PlayerInformation;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SimulatedPlayer {

    private final Context context;
    private final String name;
    private final GameManager gm;
    private final boolean observer;
    private final Handler handler;

    private class SimulatedBroadcastManager implements BroadcastManagerInterface, BroadCastManager.ChainedReceiver {

        private final BroadCastManager dependentManager;
        private final BlockingQueue<PlayerInformation.PlayerBroadcastInfo> receivedQueue =
                new LinkedBlockingQueue<PlayerInformation.PlayerBroadcastInfo>();


        private SimulatedBroadcastManager(BroadCastManager dependentManager) {
            this.dependentManager = dependentManager;
        }

        @Override
        public void destroy() {
            // do nothing
        }

        @Override
        public void sendBroadcast(PlayerInformation.PlayerBroadcastInfo info) {
            dependentManager.sendBroadcast(info); // pass through to the other manager
        }

        @Override
        public PlayerInformation.PlayerBroadcastInfo receiveBroadCast() {
            try {
                return receivedQueue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void forwardReceivedMessage(PlayerInformation.PlayerBroadcastInfo pbi) {
            try {
                receivedQueue.put(pbi);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class Attack implements Runnable {
        @Override
        public void run() {
            gm.attackWithStrength(30);
            handler.postDelayed(new Attack(), 5000); //attack every 5 seconds with 30 points
        }
    }

    private class JoinGame implements Runnable {
        @Override
        public void run() {
            gm.joinGame();
            handler.postDelayed(new Attack(), 5000); //attack every 5 seconds with 30 points
        }
    }

    public SimulatedPlayer(Context context, String name, boolean observer, BroadCastManager dependentBroadcastManager) {
        this.context = context;
        this.name = name;
        this.observer = observer;
        String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        this.handler = new Handler();
        this.gm = new GameManager(new SimulatedBroadcastManager(dependentBroadcastManager),
                android_id + "-" + name, new NotificationListener());
        this.gm.readyToPlay("Simulated " + name);
        if (!observer) {
            handler.postDelayed(new JoinGame(), 4000); //join game after 4s.
        }
    }


    private class NotificationListener implements GameStateMachine.GameStateChangeListener {
        @Override
        public void notifyStateChange(GameStateMachine.GameStateNotificationEnum gameStateNotification) {
            Log.i("NOTIFICATION RECEIVED: SIMULATED-" + name, gameStateNotification.toString());
            Log.i("PLAYERS: SIMULATED-" + name, Arrays.toString(gm.getPlayers()));
        }
    }
}
