package com.PsichiX.JustIDS;

import android.content.Context;
import android.os.Looper;
import com.PsichiX.JustIDS.comm.BroadCastManager;

public class Simulator {
    private final Context context;
    private final BroadCastManager dependentBroadCastManager;
    int SIMULATED_OBSERVER_NUMBER = 3;


    private class SimulatorCreationThread extends Thread {
        @Override
        public void run() {
            Looper.prepare();
            if (SIMULATE_SECOND_PLAYER) {
                simulatedPlayer = new SimulatedPlayer(context, "Player",
                        false, dependentBroadCastManager);
            }

            try {
                if (SIMULATE_OBSERVERS) {
                    Thread.sleep(1000);
                    simulatedOservers = new SimulatedPlayer[SIMULATED_OBSERVER_NUMBER];
                    for (int i = 0; i < SIMULATED_OBSERVER_NUMBER; i++) {
                        simulatedOservers[i] = new SimulatedPlayer(context, "Observer-" + i,
                                true, dependentBroadCastManager);
                        Thread.sleep(1000);
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Looper.loop();
        }
    }

    private boolean SIMULATE_SECOND_PLAYER = true;
    private boolean SIMULATE_OBSERVERS = true;

    SimulatedPlayer simulatedPlayer;
    SimulatedPlayer simulatedOservers[];

    public Simulator(Context context, BroadCastManager dependentBroadCastManager) {
        this.context = context;
        this.dependentBroadCastManager = dependentBroadCastManager;
        new SimulatorCreationThread().start();
    }
}