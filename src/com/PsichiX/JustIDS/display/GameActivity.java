package com.PsichiX.JustIDS.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
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

    private static final String TAG = GameActivity.class.getName();

    private class GameNotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            GameStateNotificationEnum notification =
                    GameStateNotificationEnum.values()[intent.getIntExtra("NOTIFICATION_TYPE", -1)];
            Player myPlayer = (Player) intent.getSerializableExtra("MY_PLAYER");
            Player allPlayers[] = (Player[]) intent.getSerializableExtra("ALL_PLAYERS");

            switch (notification) {
                case SOMETHING_CHANGED:
                    GameState.healthLevel = myPlayer.getLifePoints();
                    // TODO: here you should display state of mine and possibly others
                    // Note - this also can happen before game is started or after finished.
                    break;
                case GAME_STARTED_PLAYER:
                    // TODO: This is an indication that I am a player and the other player also joined.
                    // Here the battle begins. We should enable the player to fight here.
                    // Possibly even do some 1..2..3 count before the game start
                    vibratorUtil.vibrate(200);
                    break;
                case HIT:
                    // TODO: Here we are hit but not necessarily points are taken. We still have a chance to react
                    // which might decrease the damage for us. We should make some flashing that there is a hit.
                    vibratorUtil.vibrate(30);
                    break;
                case LIFE_DECREASED:
                    // TODO: This is an indication that after hit our life has actually been decreased.
                    // We should somehow indicate that life has been decreased.
                    vibratorUtil.vibrate(500);
                    GameState.healthLevel = myPlayer.getLifePoints();
                    break;
                case GAME_FINISHED_PLAYER_LOST:
                    // TODO: do more (?) when I lost the game
                    vibratorUtil.vibrate(1000);
                    gs.youLost();
                    break;
                case GAME_FINISHED_PLAYER_WON:
                    // TODO: do more (?) when  I won the game
                    vibratorUtil.vibrate(3000);
                    gs.youWon();
                    break;
                case GAME_FINISHED_OBSERVER:
                case GAME_STARTED_OBSERVER:
                case PLAYER_HIT_OBSERVER:
                default:
                    // This should not have happened - just in case we finish the activity
                    Log.w(TAG, "Unexpected " + notification);
                    GameActivity.this.finish();
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
