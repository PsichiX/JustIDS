package com.PsichiX.JustIDS;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import android.content.Context;
import android.provider.Settings.Secure;
import android.util.Log;

import com.PsichiX.JustIDS.PlayerInformation.PlayerBroadcastInfo;
import com.PsichiX.JustIDS.PlayerInformation.PlayerBroadcastInfo.BroadcastType;
import com.PsichiX.JustIDS.PlayerInformation.PlayerId;
import com.google.protobuf.InvalidProtocolBufferException;

public class GameStateManager {
	
	enum GameStateEnum {
		WAITING_FOR_PLAYERS,
		IN_GAME,
		FINISHED
	}

	GameStateEnum gameStateEnum;

	private void goToSleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			Log.e("ERR", e.getMessage());
		}
	}
	
	
	/**
	 * Send my state message periodically, so that others for sure know that I finished
	 */
	private final class FinishedThread extends Thread {
		@Override
		public void run() {
			while (!paused) { 
				if (gameStateEnum == GameStateEnum.FINISHED) {
					if (isLost()) {
						sendMyState(0.0);
					}
					goToSleep(150);
				} else {
					goToSleep(300);
				}
			}
		}

	}
	
	private final class ReadingThread extends Thread {
		@Override
		public void run() {
			Log.i("INFO", "Starting listening for messages");
			while (!paused) {
				final byte[] message = GameStateManager.this.bcm
						.receiveBroadCast(GameStateManager.this.context);
				if (message == null)  {
					continue;
				}
				try {
					Log.d("RCVD:", Arrays.toString(message));
					PlayerBroadcastInfo pbi = PlayerBroadcastInfo
							.parseFrom(message);
					if (gameStateEnum == GameStateEnum.FINISHED) {
						continue;
					}
					if (shouldICare(pbi)) {
						Log.i("MSG", "Received message: " + pbi);
						if (pbi.getType() == BroadcastType.STATE) {
							others.put(pbi.getPlayerId().getId(), pbi.getPlayerId());
						} else if (isAttackSuccessfull(pbi)) { 
							decreaseLifePointsOfMine(pbi.getAttackStrength());
							if (hitListener != null) {
								hitListener.run();
							}
							vibrateOnHit();
						}
					}
					Log.d("PLAYERS",getOthers().toString());
					if (isWon()) {
						gameStateEnum = GameStateEnum.FINISHED;
						vibrateOnWon();
						if (wonListener != null) {
							wonListener.run();
						}
					} else {
						if (GameStateManager.this.onSomethingChanged != null) {
							GameStateManager.this.onSomethingChanged.run();
						}
						if (isLost()) {
							gameStateEnum = GameStateEnum.FINISHED;
							iLostTheGame();
							vibrateOnLost();
							if (lostListener != null) {
								lostListener.run();
							}
						}
					}
				} catch (InvalidProtocolBufferException e) {
					Log.e("ERR", e.getMessage());
				}
			}
		}

		private boolean shouldICare(PlayerBroadcastInfo pbi) {
			if (hasFinished()) {
				Log.i("MSG", "Skipping message (I finished already): " + pbi);
				return false;
			}
			if (DEBUG_SELF_SENDING) {
				return true;
			}
			boolean mine = pbi.getPlayerId().getId().equals(android_id);
			if (mine) {
				Log.i("MSG", "Skipping message (It's message from self): " + pbi);
			}
			return !mine;
		}
	}

	private static final double MIN_LIFE_POINTS = 0.01;

	private boolean DEBUG_SELF_SENDING = false;

	private volatile double lifePointsOfMine;
	private HashMap<String, PlayerId> others = new HashMap<String, PlayerId>();

	public double getLifePointsOfMine() {
		return lifePointsOfMine;
	}

	public synchronized boolean isLost() {
		return gameStateEnum == GameStateEnum.IN_GAME && lifePointsOfMine <= MIN_LIFE_POINTS;
	}

	public synchronized boolean isWon() {
		if (gameStateEnum == GameStateEnum.IN_GAME) {
			if (others.keySet().size() == 0) {
				return false;
			}
			for (PlayerId value : others.values()) {
				if (value.getLifePoints() > MIN_LIFE_POINTS) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	BroadCastManager bcm;
	private Context context;
	private Thread readingThread;
	private Runnable onSomethingChanged;
	private Runnable hitListener;

	private VibratorUtil vibratorUtil;

	private Runnable wonListener;

	private Runnable lostListener;

	private boolean paused;
	
	private boolean active;

	private FinishedThread finishedThread;

	private String android_id;

	private String name;

	public GameStateManager(Context context, String name,boolean active) {
		this.bcm = new BroadCastManager();
		this.name = name;
		this.active = active;
		this.android_id = Secure.getString(context.getContentResolver(),Secure.ANDROID_ID);
		this.context = context;
		this.vibratorUtil = new VibratorUtil(context);
		resetGame();
		resume();
	}

	private PlayerId getPlayerId() {
		return PlayerId.newBuilder().
				setId(android_id).
				setName(name).
				setLifePoints(lifePointsOfMine).
				build();
	}
	private boolean isAttackSuccessfull(PlayerBroadcastInfo pbi) {
		return gameStateEnum == GameStateEnum.IN_GAME && pbi.getAttackStrength() > 0.01 && active;
	}

	private void vibrateOnHit() {
		vibratorUtil.vibrate(500);
	}

	private void vibrateOnWon() {
		vibratorUtil.vibrate(1000);
	}

	private void vibrateOnLost() {
		vibratorUtil.vibrate(1000);
	}

	private synchronized double decreaseInternally(double decreaseBy) {
		lifePointsOfMine -= decreaseBy;
		if (lifePointsOfMine < 0.01) {
			lifePointsOfMine = 0.0;
		}
		return lifePointsOfMine;
	}

	public void decreaseLifePointsOfMine(double decreaseBy) {
		double lp = decreaseInternally(decreaseBy);
		sendMyState(lp);
	}

	private void sendMyState(double lp) {
		if (!active) {
			return;
		}
		
		PlayerBroadcastInfo info = PlayerBroadcastInfo.newBuilder()
				.setPlayerId(getPlayerId())
				.setType(BroadcastType.STATE).build();
		byte[] message = info.toByteArray();
		this.bcm.sendBroadcast(context, message);
		Log.d("SENT:", Arrays.toString(message));
	}

	public boolean hasFinished() {
		return (isLost() || isWon());
	}

	public void attackWithStrength(double strength) {
		if (hasFinished()) {
			// do nothing
			return;
		}
		PlayerBroadcastInfo info = PlayerBroadcastInfo.newBuilder()
				.setPlayerId(getPlayerId()).setAttackStrength(strength)
				.setType(BroadcastType.ATTACK).build();
		byte[] message = info.toByteArray();
		this.bcm.sendBroadcast(context, message);
		Log.d("SENT:", Arrays.toString(message));
	}

	public void iLostTheGame() {
		sendMyState(0.0);
	}

	public void setSomethingChangedListener(Runnable runnable) {
		this.onSomethingChanged = runnable;
	}

	public void setHitListener(Runnable runnable) {
		this.hitListener = runnable;
	}

	public void setWonListener(Runnable runnable) {
		this.wonListener = runnable;
	}

	public void setLostListener(Runnable runnable) {
		this.lostListener = runnable;
	}

	public synchronized void resetGame() {
		this.lifePointsOfMine = 100.0;
		this.others.clear();
		if (this.onSomethingChanged != null) {
			this.onSomethingChanged.run();
		}
		gameStateEnum = GameStateEnum.WAITING_FOR_PLAYERS;
	}	
	
	public synchronized void startGame() {
		gameStateEnum = GameStateEnum.IN_GAME;
	}
	
	public void pause() {
		paused = true;
		this.readingThread = null;
		this.finishedThread = null;
	}
	
	public void resume() {
		paused = false;
		readingThread = new ReadingThread();
		readingThread.start();	
		finishedThread = new FinishedThread();
		finishedThread.start();	
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public Collection<PlayerId> getOthers() {
		return others.values();
	}

	public void destroy() {
		bcm.destroy();
	}

}
