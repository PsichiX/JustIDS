package com.PsichiX.JustIDS.game;

import com.PsichiX.JustIDS.message.PlayerInformation.PlayerBroadcastInfo;

final class ReadingThread extends Thread {
	private final GameManager gameManager;
	private GameStateMachine gameStateMachine;
	private String name;

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
			PlayerBroadcastInfo pbi = this.gameManager.bcm
					.receiveBroadCast();
			if (pbi == null || gameStateMachine.isStateFinished()) {
				continue;
			}
			if (gameManager.shouldICareAboutThisBroadcast(pbi)) {
				gameManager.logger.info(gameManager.getName() + ":Received message: " + pbi);
				gameManager.receiveMessage(pbi);
			}
			this.gameManager.logger.info(name + ":PLAYERS:" + this.gameManager.getPlayers().toString());
			if (gameManager.didIWin()) {
				gameStateMachine.processIWon();
			} else if (gameManager.didILoose()) {
				gameStateMachine.processILost();
				gameManager.sendMyState();
			} else if (gameManager.isGameFinished()) {
				gameStateMachine.processGameFinishedObserver();		
			}
			gameStateMachine.runSomethingChangedListener();
		}
	}

}