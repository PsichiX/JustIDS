package com.PsichiX.JustIDS.simulator;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import com.PsichiX.JustIDS.comm.BroadcastManagerInterface;
import com.PsichiX.JustIDS.comm.UDPBroadCastManager;
import com.PsichiX.JustIDS.game.GameManager;
import com.PsichiX.JustIDS.game.GameStateMachine;
import com.PsichiX.JustIDS.message.PlayerInformation;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SimulatedUser {
    private static final long JOIN_GAME_DELAY = 1000;
    private static final long FIGHT_BACK_DELAY = 2000;
    private static final long QUICK_COUNTER_ATTACK_DELAY = 300;
    public static final int STRONG_ATTACK = 30;
    public static final int WEAK_ATTACK = 10;
    private static final long RESET_GAME_DELAY_AFTER_END = 3000;
    private static final long CONTINUOUS_ATTACK_DELAY = 2000;
    private final String TAG;

    private final Context context;
    private final String name;
    private final GameManager gm;
    private final boolean amIObserver;
    private Handler handler;
    private final SimulatedScenarioEnum scenario;
    private Looper myLopper;

    private class SimulatedBroadcastManager implements BroadcastManagerInterface, UDPBroadCastManager.ChainedReceiver {

        private final UDPBroadCastManager dependentManager;
        private final BlockingQueue<PlayerInformation.PlayerBroadcastInfo> receivedQueue =
                new LinkedBlockingQueue<PlayerInformation.PlayerBroadcastInfo>();


        private SimulatedBroadcastManager(UDPBroadCastManager dependentManager) {
            this.dependentManager = dependentManager;
            dependentManager.addChainedReceiver(this);
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
        private final double strength;

        private Attack(double strength) {
            this.strength = strength;
        }

        @Override
        public void run() {
            gm.attackWithStrength(strength);
        }
    }

    private class JoinGame implements Runnable {
        @Override
        public void run() {
            gm.joinGame();
        }
    }

    private class ResetGame implements Runnable {
        @Override
        public void run() {
            gm.resetGame();
        }
    }

    private Runnable repeatedAttack = new Runnable() {
        @Override
        public void run() {
            gm.attackWithStrength(30);
            handler.postDelayed(this, CONTINUOUS_ATTACK_DELAY);
        }
    };

    private class SimulationNotificationListener implements GameStateMachine.GameStateChangeListener {
        @Override
        public void notifyStateChange(GameStateMachine.GameStateNotificationEnum gameStateNotification) {
            Log.i(TAG, gameStateNotification.toString());
            Log.i(TAG, Arrays.toString(gm.getPlayers()));
            switch (gameStateNotification) {
                case HIT:
                    Log.i(TAG, "I have been hit");
                    handleHit();
                    break;
                case LIFE_DECREASED:
                    Log.i(TAG, "My life points are decreased. I have " + gm.getMyPlayer().getLifePoints() + " left.");
                    break;
                case GAME_FINISHED_OBSERVER:
                    Log.i(TAG, "Game is finished");
                    handleEndGame();
                    break;
                case GAME_FINISHED_PLAYER_LOST:
                    Log.i(TAG, "Game is finished. I lost.");
                    handleEndGame();
                    break;
                case GAME_FINISHED_PLAYER_WON:
                    Log.i(TAG, "Game is finished. I won.");
                    handleEndGame();
                    break;
                case SOMETHING_CHANGED:
                    Log.i(TAG, "Something has changed.");
            }
        }

        private void handleEndGame() {
            handler.removeCallbacks(repeatedAttack);
            handler.postDelayed(new ResetGame(), RESET_GAME_DELAY_AFTER_END);
        }

        private void handleHit() {
            switch (scenario) {
                case FIGHT_BACK_STRONGER:
                    handler.postDelayed(new Attack(STRONG_ATTACK), FIGHT_BACK_DELAY);
                    break;
                case FIGHT_BACK_WEAKER:
                    handler.postDelayed(new Attack(WEAK_ATTACK), FIGHT_BACK_DELAY);
                    break;
                case COOUNTER_ATTACK_QUICKLY:
                    handler.postDelayed(new Attack(STRONG_ATTACK), QUICK_COUNTER_ATTACK_DELAY);
                    break;
                default:
                    // do nothing
                    break;
            }
        }
    }

    private class SimulatedUserMainLooper extends Thread {
        private final long startUpDelay;

        public SimulatedUserMainLooper(long startupDelay) {
            this.startUpDelay = startupDelay;
        }

        @Override
        public void run() {
            Looper.prepare();
            myLopper = Looper.myLooper();
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SimulatedUser.this.gm.startGameManager();
                    if (!amIObserver) {
                        handler.postDelayed(new JoinGame(), JOIN_GAME_DELAY); //join game after 4s.
                        if (scenario == SimulatedScenarioEnum.ATTACK_CONTINUOUSLY ||
                                scenario == SimulatedScenarioEnum.OBSERVER_ONLY) {
                            handler.postDelayed(repeatedAttack, CONTINUOUS_ATTACK_DELAY);
                        }
                    }
                }
            }, startUpDelay);
            Looper.loop();
        }

    }

    public void startSimulatedUser(long delay) {
        new SimulatedUserMainLooper(delay).start();
    }

    public void stopSimulatedUser() {
        myLopper.quit();
        this.gm.stopGameManager();
    }


    public SimulatedUser(Context context, String name, boolean amIObserver,
                         UDPBroadCastManager dependentBroadcastManager, SimulatedScenarioEnum scenario) {
        this.context = context;
        this.name = name;
        this.amIObserver = amIObserver;
        this.TAG = SimulatedUser.class.getName() + " " + name;
        this.scenario = scenario;
        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        this.gm = new GameManager(new SimulatedBroadcastManager(dependentBroadcastManager),
                android_id + "-" + name, name, new SimulationNotificationListener());
    }
}
