package com.PsichiX.JustIDS.game;

import com.PsichiX.JustIDS.display.PrintCurrentState;
import com.PsichiX.JustIDS.message.PlayerInformation.PlayerBroadcastInfo;

import java.util.logging.Logger;

final class ReadingThread extends Thread {
	private final GameManager gameManager;
	private GameStateMachine gameStateMachine;
	private String name;

    Logger logger = Logger.getLogger(ReadingThread.class.getName());

    /**
	 * @param gameManager
	 */
	ReadingThread(GameManager gameManager, GameStateMachine gameStateMachine) {
		this.gameManager = gameManager;
		this.gameStateMachine = gameStateMachine;
		this.name = gameManager.getName();
	}

	@Override
	public void run() {
		this.gameManager.logger.info(name + ":Starting listening for messages");
		while (!this.gameManager.paused) {
			PlayerBroadcastInfo pbi = this.gameManager.bcm.receiveBroadCast();
			if (pbi == null || gameStateMachine.isStateFinished()) {
				continue;
			}
            boolean somethingChanged = false;
			if (gameManager.shouldICareAboutThisBroadcast(pbi)) {
				somethingChanged = somethingChanged || gameManager.receiveMessage(pbi);
			}
			if (gameManager.didIWin()) {
                somethingChanged = true;
				gameStateMachine.processIWon();
			} else if (gameManager.didILoose()) {
                somethingChanged = true;
				gameStateMachine.processILost();
                gameManager.sendMyState();
			} else if (gameManager.isGameFinished()) {
                somethingChanged = true;
				gameStateMachine.processGameFinishedObserver();
			}
            if (somethingChanged) {
                logger.info("Sending state change notification :" +
                        PrintCurrentState.getCurrentStateAsString(gameManager.getMyPlayer(), gameManager.getPlayers()));
    			gameStateMachine.runSomethingChangedListener();
            }
		}
	}

}