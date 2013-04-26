package com.PsichiX.JustIDS.simulator;

import android.content.Context;
import android.os.Looper;
import com.PsichiX.JustIDS.comm.UDPBroadCastManager;

public class Simulator {
    public static final int DELAY_BETWEEN_USERS_START = 500;
    private final Context context;
    private final UDPBroadCastManager dependentBroadCastManager;
    private final int numberOfObservers;
    private final SimulatedScenarioEnum scenario;
    private Looper myLopper;

    private SimulatedUser simulatedPlayers[];
    private SimulatedUser simulatedObservers[];

    public void startSimulator() {
        int delay = 0;
        for (SimulatedUser simulatedPlayer: simulatedPlayers) {
            simulatedPlayer.startSimulatedUser(delay);
            delay += DELAY_BETWEEN_USERS_START;
        }
        for (SimulatedUser simulatedObserver: simulatedObservers) {
            simulatedObserver.startSimulatedUser(delay);
            delay += DELAY_BETWEEN_USERS_START;
        }
    }

    public void stopSimulator() {
        for (SimulatedUser simulatedPlayer: simulatedPlayers) {
            simulatedPlayer.stopSimulatedUser();
        }
        for (SimulatedUser simulatedObserver: simulatedObservers) {
            simulatedObserver.stopSimulatedUser();
        }
    }

    public Simulator(Context context, UDPBroadCastManager dependentBroadCastManager, int numberOfObservers,
                     SimulatedScenarioEnum scenario) {
        this.context = context;
        this.dependentBroadCastManager = dependentBroadCastManager;
        this.numberOfObservers = numberOfObservers;
        this.scenario = scenario;
        if (scenario != SimulatedScenarioEnum.OBSERVER_ONLY) {
            simulatedPlayers = new SimulatedUser[1];
            simulatedPlayers[0] = new SimulatedUser(context, "Simulated Player",
                    false, dependentBroadCastManager, scenario);
        } else {
            simulatedPlayers = new SimulatedUser[2];
            simulatedPlayers[0] = new SimulatedUser(context, "Simulated Player 0",
                    false, dependentBroadCastManager, scenario);
            simulatedPlayers[1] = new SimulatedUser(context, "Simulated Player 1",
                    false, dependentBroadCastManager, scenario);
        }
        simulatedObservers = new SimulatedUser[numberOfObservers];
        for (int i = 0; i < numberOfObservers; i++) {
            simulatedObservers[i] = new SimulatedUser(context, "Simulated Observer " + i,
                    true, dependentBroadCastManager, scenario);
        }
    }

}