package com.PsichiX.JustIDS;

import android.content.Context;
import android.provider.Settings.Secure;
import android.util.Log;

public class GameStateManager {
	
	private volatile double lifePointsOfMine = 100.0;
	private volatile double lifePointsOfOther = 100.0;
	
	public double getLifePointsOfMine() {
		return lifePointsOfMine;
	}
	
	public synchronized boolean isLost() {
		return lifePointsOfMine <= 0.01;
	}

	public synchronized boolean isWon() {
		return lifePointsOfOther <= 0.01;
	}

	BroadCastManager bcm;
	private String android_id;
	private Context context;
	private Thread thread;
	private Runnable onSomethingChanged;
	private Runnable hitListener;
	
	public GameStateManager(Context context, BroadCastManager bcm) {
		this.bcm = bcm;
		this.android_id = Secure.getString(context.getContentResolver(),Secure.ANDROID_ID); 
		this.context = context;
		
		this.thread = new Thread() {
			@Override
			public void run() {
				Log.i("INFO", "Starting listening for messages");
				while (true) {
					final byte[] message = GameStateManager.this.bcm.receiveBroadCast(GameStateManager.this.context);
					PlayerInfo pi = (PlayerInfo) Serializer.deserialize(message);
					if (!pi.id.equals(android_id)) {
						Log.i("MSG","Received message from: " + pi);
						lifePointsOfOther = pi.lifePoints;
						if (pi.attackStrength > 0.01) {
							decreaseLifePointsOfMine(pi.attackStrength);
							vibrateOnHit();
							hitListener.run();
						}
					} else {
						Log.i("MSG", "Skipping message from self: " + pi);
					}
					GameStateManager.this.onSomethingChanged.run();
				}
			}

		};
		thread.start();
	}

	private void vibrateOnHit() {
		// TODO vibrate on hit
		
	}
	
	private synchronized double decreaseInternally(double decreaseBy) {
		lifePointsOfMine -= decreaseBy;
		return lifePointsOfMine;
	}
	
	public void decreaseLifePointsOfMine(double decreaseBy) {
		double lp = decreaseInternally(decreaseBy);
		PlayerInfo pi = new PlayerInfo();
		pi.id = android_id; 
		pi.lifePoints = lp;
		pi.attackStrength = 0;
		this.bcm.sendBroadcast(context, Serializer.serialize(pi));
	}
	
	public void attackWithStrength(double strength) {
		double lp = decreaseInternally(0.0f);
		PlayerInfo pi = new PlayerInfo();
		pi.id = android_id; 
		pi.lifePoints = lp;
		pi.attackStrength = strength;
		this.bcm.sendBroadcast(context, Serializer.serialize(pi));
	}

	public void setSomethingChangedListener(Runnable runnable) {
		this.onSomethingChanged = runnable;
	}

	public void setHitListener(Runnable runnable) {
		this.hitListener = runnable;
	}

}
