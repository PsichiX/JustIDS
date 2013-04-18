package com.PsichiX.JustIDS;

import android.provider.Settings.Secure;

import com.PsichiX.JustIDS.comm.BroadCastManager;
import com.PsichiX.JustIDS.game.GameManager;
import com.PsichiX.JustIDS.game.GameStateMachine;
import com.PsichiX.JustIDS.services.RecorderService;
import com.PsichiX.XenonCoreDroid.XeActivity;
import com.PsichiX.XenonCoreDroid.XeApplication;
import com.PsichiX.XenonCoreDroid.XePhoton;
import com.PsichiX.XenonCoreDroid.XeSense;
import com.PsichiX.XenonCoreDroid.Framework.Graphics.Graphics;
import com.PsichiX.XenonCoreDroid.Framework.Utils.Utils;

public class MainActivity extends XeActivity {
	public static XeApplication app;
	RecorderService rs;
	private GameManager gm;
	private VibratorUtil vibratorUtil;
	private GameState gs;

	private class NotificationListener implements GameStateMachine.GameStateChangeListener {
		@Override
		public void notifyStateChange(
				GameStateMachine.GameStateNotificationEnum gameStateNotification) {
			switch (gameStateNotification) {
			case SOMETHING_CHANGED:
				GameState.healthLevel = gm.getLifePointsOfMine();
				break;
			case HIT:
				vibratorUtil.vibrate(300);
				break;
			case GAME_STARTED_OBSERVER:
				vibratorUtil.vibrate(200);
				break;
			case GAME_STARTED_PLAYER:
				vibratorUtil.vibrate(200);
				break;
			case GAME_FINISHED_OBSERVER:
				vibratorUtil.vibrate(200);
				break;
			case LIFE_DECREASED:
				vibratorUtil.vibrate(500);
				break;
			case GAME_FINISHED_PLAYER_LOST:
				vibratorUtil.vibrate(1000);
				gs.youLost();
				break;
			case GAME_FINISHED_PLAYER_WON:
				vibratorUtil.vibrate(1000);
				gs.youWon();
				break;
			}
		}

	}

	@Override
	public void onCreate(android.os.Bundle savedInstanceState) {
		// setup application before running it
		XeApplication.SETUP_SOUND_STREAMS = 1;
		XeApplication.SETUP_WINDOW_HAS_TITLE = false;
		XeApplication.SETUP_WINDOW_FULLSCREEN = true;
		XeApplication.SETUP_SCREEN_ORIENTATION = XeApplication.Orientation.PORTRAIT;
		XeApplication.SETUP_SENSORS_RATE = android.hardware.SensorManager.SENSOR_DELAY_GAME;
		// To ponizej zalatwia wylaczenie modulu Photon silnika, czyli nie bedzie renderowal nic.
		// TODO: trzeba zaaplikowac widok z layoutu, ale brak widoku z paskami
//		XeApplication.SETUP_MODULES_ENABLED = XeApplication.MODULE_ALL &~ XeApplication.MODULE_PHOTON;

		// create application
		super.onCreate(savedInstanceState);

		String name = "USER NAME"; // TODO: Get the name from the user
		String android_id = Secure.getString(this.getContentResolver(),
				Secure.ANDROID_ID);
		this.vibratorUtil = new VibratorUtil(this);
		gm = new GameManager(new BroadCastManager(this), android_id, name, new NotificationListener());

		this.gs = new GameState(gm);
		gm.resetGame();
		
		// obsluga mikrofonu
		rs = new RecorderService();
		rs.startRecording();

		// run state
		app = getApplicationCore();
		Utils.initModule(getApplicationCore().getAssets());
		Graphics.initModule(getApplicationCore().getAssets(),
				getApplicationCore().getPhoton());
		getApplicationCore().getTimer().setFixedStep(1000 / 30);
		getApplicationCore().getPhoton().getRenderer().getTimer()
				.setFixedStep(1000 / 30);
		getApplicationCore().getPhoton().setRenderMode(
				XePhoton.RenderMode.QUEUE, true);
		getApplicationCore().getPhoton().getRenderer()
				.setClearBackground(true, 0.0f, 0.0f, 0.0f, 1.0f);
		getApplicationCore().getSense().use(XeSense.Type.LINEAR_ACCELERATION);
		getApplicationCore().run(gs);

		// startActivity(new Intent(this, AudioRecordTest.class));
	}

	@Override
	protected void onPause() {
		gm.pause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		gm.resume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		gm.destroy();
		rs.stopRecording();
	}
}
