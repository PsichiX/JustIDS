package com.PsichiX.JustIDS.simulator;

import android.content.Context;
import android.os.Looper;
import com.PsichiX.JustIDS.comm.UDPBroadCastManager;

public class Simulator {
    public static final int DELAY_BETWEEN_OBSERVER_START = 1000;
    private final Context context;
    private final UDPBroadCastManager dependentBroadCastManager;
    private final int numberOfObservers;
    private final SimulatedScenarioEnum scenario;

    private class SimulatorCreationThread extends Thread {
        @Override
        public void run() {
            Looper.prepare();
            simulatedPlayer = new SimulatedPlayer(context, "Simulated Player", false, dependentBroadCastManager, scenario);
            simulatedObservers = new SimulatedPlayer[numberOfObservers];
            try {
                for (int i = 0; i < numberOfObservers; i++) {
                    Thread.sleep(DELAY_BETWEEN_OBSERVER_START);
                    simulatedObservers[i] = new SimulatedPlayer(context, "Simulated Observer " + i,
                                true, dependentBroadCastManager, scenario);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Looper.loop();
        }
    }

    SimulatedPlayer simulatedPlayer;
    SimulatedPlayer simulatedObservers[];

    public Simulator(Context context, UDPBroadCastManager dependentBroadCastManager, int numberOfObservers,
                     SimulatedScenarioEnum scenario) {
        this.context = context;
        this.dependentBroadCastManager = dependentBroadCastManager;
        this.numberOfObservers = numberOfObservers;
        this.scenario = scenario;
        new SimulatorCreationThread().start();
    }
}