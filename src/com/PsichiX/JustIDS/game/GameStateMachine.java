package com.PsichiX.JustIDS.game;

import java.util.Collection;
import java.util.logging.Logger;

import com.PsichiX.JustIDS.message.PlayerInformation.Player;
import com.PsichiX.JustIDS.message.PlayerInformation.PlayerState;

public class GameStateMachine {

	Logger logger = Logger.getLogger(GameStateMachine.class.getName());

	private GameStateChangeListener notificationStateListener;

	public enum GameStateNotificationEnum {
		SOMETHING_CHANGED, GAME_STARTED_OBSERVER, GAME_FINISHED_OBSERVER,
        GAME_STARTED_PLAYER, GAME_FINISHED_PLAYER_LOST, GAME_FINISHED_PLAYER_WON, HIT, LIFE_DECREASED
	}

	public static interface GameStateChangeListener {
		public void notifyStateChange(
				GameStateNotificationEnum gameStateNotification);
	}

	private String androidId;

	public GameStateMachine(String androidId, GameStateChangeListener listener) {
		this.androidId = androidId;
		this.notificationStateListener = listener;
	}

	private void sendNotification(GameStateNotificationEnum notification) {
		if (notificationStateListener != null) {
			notificationStateListener.notifyStateChange(notification);
		}
	}

	private PlayerState myState = PlayerState.WAITING;

	static boolean isInGame(PlayerState playerState) {
		return (playerState == PlayerState.IN_GAME
				|| playerState == PlayerState.PLAYING
				|| playerState == PlayerState.LOST || playerState == PlayerState.WON);
	}

	synchronized public void resetStateMachine() {
		sendNotification(GameStateNotificationEnum.SOMETHING_CHANGED);
		myState = PlayerState.WAITING;
	}

	synchronized boolean amIInGame() {
		return isInGame(myState);
	}

	synchronized boolean isStateFinished() {
		return (myState == PlayerState.WON || myState == PlayerState.LOST || myState == PlayerState.GAME_FINISHED);
	}

	public boolean isMyself(Player player) {
		return player.getId().equals(androidId);
	}

	public void startGameIfAllReady(Collection<Player> players) {
		int otherPlayersCount =0;
		for (Player player : players) {
			if (isInGame(player.getState()) && !isMyself(player)) {
				otherPlayersCount++;
				if (amIInGame() && myState != PlayerState.PLAYING) {
					processGameStartedPlayer();
				}
			}
		}
		if (otherPlayersCount >=2 && myState != PlayerState.OBSERVER){
			processGameStartedObserver();			
		}

	}

	private synchronized void processGameStartedObserver() {
		myState = PlayerState.OBSERVER;
		sendNotification(GameStateNotificationEnum.GAME_STARTED_OBSERVER);
	}

	private synchronized void processGameStartedPlayer() {
		myState = PlayerState.PLAYING;
		sendNotification(GameStateNotificationEnum.GAME_STARTED_PLAYER);
	}

	synchronized void processGameFinishedObserver() {
		myState = PlayerState.GAME_FINISHED;
		sendNotification(GameStateNotificationEnum.GAME_FINISHED_OBSERVER);
	}

	synchronized void processILost() {
		myState = PlayerState.LOST;
		sendNotification(GameStateNotificationEnum.GAME_FINISHED_PLAYER_LOST);
	}

	synchronized void processIWon() {
		myState = PlayerState.WON;
		sendNotification(GameStateNotificationEnum.GAME_FINISHED_PLAYER_WON);
	}

	void runSomethingChangedListener() {
		sendNotification(GameStateNotificationEnum.SOMETHING_CHANGED);
	}

	public void hit() {
		sendNotification(GameStateNotificationEnum.HIT);
	}

	public void successfulAttack() {
		sendNotification(GameStateNotificationEnum.LIFE_DECREASED);
	}

	public PlayerState getMyState() {
		return myState;
	}

	public boolean joinGame() {
		if (myState != PlayerState.WAITING) {
			return false;
		}
		myState = PlayerState.IN_GAME;
		return true;
	}

	public GameStateChangeListener getNotificationStateListener() {
		return notificationStateListener;
	}
}
