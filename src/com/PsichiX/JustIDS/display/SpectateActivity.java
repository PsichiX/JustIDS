package com.PsichiX.JustIDS.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import com.PsichiX.JustIDS.R;
import com.PsichiX.JustIDS.game.GameStateMachine;
import com.PsichiX.JustIDS.message.PlayerInformation;
import com.PsichiX.JustIDS.states.GameState;

public class SpectateActivity extends Activity {

    private static final String TAG = SpectateActivity.class.getName();

    private VibratorUtil vibratorUtil;

    private class SpectateNotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            GameStateMachine.GameStateNotificationEnum notification =
                    GameStateMachine.GameStateNotificationEnum.values()[intent.getIntExtra("NOTIFICATION_TYPE", -1)];
            PlayerInformation.Player myPlayer = (PlayerInformation.Player) intent.getSerializableExtra("MY_PLAYER");
            PlayerInformation.Player allPlayers[] = (PlayerInformation.Player[]) intent.getSerializableExtra("ALL_PLAYERS");

            switch (notification) {
                case SOMETHING_CHANGED:
                    // TODO: here you should display state of mine and others
                    // Note - this also can happen before game is started or after finished.
                    break;
                case GAME_STARTED_OBSERVER:
                    // TODO: the game has started. We should indicate it somehow in the views
                    // Game started
                    vibratorUtil.vibrate(200);
                    break;
                case GAME_FINISHED_OBSERVER:
                    // TODO: the game has finished. We should indicate it somehow in the views
                    vibratorUtil.vibrate(1000);
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.vibratorUtil = new VibratorUtil(this);

        setContentView(R.layout.activity_spectate);
    }

}
