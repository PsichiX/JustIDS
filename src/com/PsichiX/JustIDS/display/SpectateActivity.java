package com.PsichiX.JustIDS.display;

import com.PsichiX.JustIDS.R;
import com.PsichiX.JustIDS.R.layout;
import com.PsichiX.JustIDS.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class SpectateActivity extends Activity {

<<<<<<< HEAD
=======

    private static final String TAG = SpectateActivity.class.getName();

    private VibratorUtil vibratorUtil;

    private class SpectateHitSeenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            PlayerInformation.Player attacking = (PlayerInformation.Player) intent.getSerializableExtra("ATTACKING");
            // TODO: do something when observing the attack
            vibratorUtil.vibrate(200);
        }
    }


    private class SpectateNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            GameStateMachine.GameStateNotificationEnum notification =
                    GameStateMachine.GameStateNotificationEnum.values()[intent.getIntExtra("NOTIFICATION_TYPE", -1)];
            PlayerInformation.Player myPlayer = (PlayerInformation.Player) intent.getSerializableExtra("MY_PLAYER");
            PlayerInformation.Player allPlayers[] = (PlayerInformation.Player[]) intent.getSerializableExtra("ALL_PLAYERS");

            switch (notification) {
                case SOMETHING_CHANGED:
                    Log.i(TAG, "Something changed: " + PrintCurrentState.getCurrentStateAsString(myPlayer, allPlayers));
                    // TODO: here you should display state of mine and others
                    // Note - this also can happen before game is started or after finished.
                    break;
                case GAME_STARTED_OBSERVER:
                    Log.i(TAG, "Game started: " + PrintCurrentState.getCurrentStateAsString(myPlayer, allPlayers));
                    // TODO: the game has started. We should indicate it somehow in the views
                    // Game started
                    vibratorUtil.vibrate(200);
                    break;
                case GAME_FINISHED_OBSERVER:
                    Log.i(TAG, "Game finished: " + PrintCurrentState.getCurrentStateAsString(myPlayer, allPlayers));
                    // TODO: the game has finished. We should indicate it somehow in the views
                    vibratorUtil.vibrate(1000);
                    break;
                case PLAYER_HIT_OBSERVER:
                    // TODO: observer sees HIT
                    // Game started
                    vibratorUtil.vibrate(200);
                    break;
                case HIT:
                case LIFE_DECREASED:
                case GAME_STARTED_PLAYER:
                case GAME_FINISHED_PLAYER_LOST:
                case GAME_FINISHED_PLAYER_WON:
                default:
                    // This should not have happened - just in case we finish the activity
                    Log.w(TAG, "Unexpected " + notification);
                    SpectateActivity.this.finish();
                    break;
            }
        }
    }


>>>>>>> 7f5a24726b08bcdebbb0691c81b1cdea3e4bccf2
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spectate);
<<<<<<< HEAD
=======

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(new SpectateNotificationReceiver(),
                new IntentFilter("com.PsichiX.JustIDS.ScreamFightNotificationService"));

        localBroadcastManager.registerReceiver(
                new SpectateHitSeenReceiver(),
                new IntentFilter("com.PsichiX.JustIDS.ScreamFightAttackObservedService"));

>>>>>>> 7f5a24726b08bcdebbb0691c81b1cdea3e4bccf2
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_spectate, menu);
        return true;
    }
}
