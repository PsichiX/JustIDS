package com.PsichiX.JustIDS.game;

final class PingThread extends Thread {
	/**
	 * 
	 */
	private final GameManager gameManager;
	private final GameStateMachine gameStateMachine;
	private int finishGameBroadcastCount = 10;


	/**
	 * @param gameManager
	 */
	public PingThread(GameManager gameManager, GameStateMachine gameStateMachine) {
		this.gameManager = gameManager;
		this.gameStateMachine = gameStateMachine;
	}

	@Override
	public void run() {
		while (!gameManager.paused) {
			if (gameStateMachine.isStateFinished()) {
				if (--finishGameBroadcastCount > 0) {
					gameManager.sendMyState();
					gameManager.goToSleep(2 * GameManager.UNIT_OF_TIME_MILLIS); // broadcast finished state every 200 ms
				}
			} else {
                gameManager.sendMyState();
				gameManager.goToSleep(10 * GameManager.UNIT_OF_TIME_MILLIS); // ping every second
			}
		}
	}
}