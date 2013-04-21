package com.PsichiX.JustIDS.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import com.PsichiX.JustIDS.states.GameState;
import com.PsichiX.JustIDS.game.GameStateMachine.GameStateNotificationEnum;
import com.PsichiX.JustIDS.message.PlayerInformation.Player;
import com.PsichiX.JustIDS.services.RecorderService;
import com.PsichiX.XenonCoreDroid.Framework.Graphics.Graphics;
import com.PsichiX.XenonCoreDroid.Framework.Utils.Utils;
import com.PsichiX.XenonCoreDroid.XeActivity;
import com.PsichiX.XenonCoreDroid.XeApplication;
import com.PsichiX.XenonCoreDroid.XePhoton;
import com.PsichiX.XenonCoreDroid.XeSense;

public class GameActivity extends XeActivity {
    public static XeApplication app;
	RecorderService rs;
	private VibratorUtil vibratorUtil;
    private GameState gs;


    private static class VibratorUtil {
       Vibrator v;

        public VibratorUtil(Context c) {
            v = (Vibrator) c.getSystemService(VIBRATOR_SERVICE);
        }

        public void vibrate(int repeat)	{
            v.vibrate(repeat);
        }
    }

    private class GameNotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int oridinal = intent.getIntExtra("NOTIFICATION_TYPE", -1);
            GameStateNotificationEnum notification = GameStateNotificationEnum.values()[oridinal];
            Player myPlayerId = (Player) intent.getSerializableExtra("MY_PLAYER");
            Player allPlayers[] = (Player[]) intent.getSerializableExtra("ALL_PLAYERS");

            switch (notification) {
                case SOMETHING_CHANGED:
                    GameState.healthLevel = myPlayerId.getLifePoints();
                    // TODO: here you should display state of mine and others
                    // Note - this also can happen before game is started or after finished.
                    break;
                case GAME_STARTED_OBSERVER:
                    // TODO: Here we should automatically switch the view to observer when two other players joined the game.
                    // I am now observer and till the end of the game I won't be able to play.
                    break;
                case GAME_STARTED_PLAYER:
                    // TODO: This is an indication that I am a player and the other player also joined.
                    // Here the battle begins. We should enable the player to fight here.
                    vibratorUtil.vibrate(200);
                    break;
                case GAME_FINISHED_OBSERVER:
                    // TODO: This is an indication that game finished for the observer. There should be LOST/WON
                    // in the player list. We should display the result and let the user reset game here
                    vibratorUtil.vibrate(200);
                    break;
                case HIT:
                    // TODO: Here we are hit but not necessarily points are taken. We still have a chance to react
                    // which might decrease the damage for us. We should make some flashing that there is a hit.
                    // Note: this is only notified to players, not to observers. Observers just see (SOMETHING CHANGED)
                    vibratorUtil.vibrate(20);
                    break;
                case LIFE_DECREASED:
                    // TODO: This is an indication that after hit our life has actually been decreased.
                    // You can read the current life points by runnning gm.getMyPlayerId().
                    // We should somehow indicate that life has been decreased.
                    // Note: this is only notified to players, not to observers. Observers just see (SOMETHING CHANGED)
                    vibratorUtil.vibrate(500);
                    break;
                case GAME_FINISHED_PLAYER_LOST:
                    // TODO: This is an indication that I lost the game
                    // Note: this is only notified to players, not to observers. Observers just see (SOMETHING CHANGED)
                    vibratorUtil.vibrate(1000);
                    gs.youLost();
                    break;
                case GAME_FINISHED_PLAYER_WON:
                    // TODO: This is an indication that I won	 the game
                    // Note: this is only notified to players, not to observers. Observers just see (SOMETHING CHANGED)
                    vibratorUtil.vibrate(1000);
                    gs.youWon();
                    break;
            }
        }
    }

	@Override
	public void onCreate(android.os.Bundle savedInstanceState) {

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(
                new GameNotificationReceiver(),
                new IntentFilter("com.PsichiX.JustIDS.ScreamFightNotificationService"));

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
		this.vibratorUtil = new VibratorUtil(this);
		this.gs = new GameState(this);

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
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		rs.stopRecording();
	}

}
