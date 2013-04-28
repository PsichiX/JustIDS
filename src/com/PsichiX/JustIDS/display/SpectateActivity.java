package com.PsichiX.JustIDS.display;

import com.PsichiX.JustIDS.R;
import com.PsichiX.JustIDS.R.layout;
import com.PsichiX.JustIDS.R.menu;
import com.PsichiX.JustIDS.game.GameStateMachine;
import com.PsichiX.JustIDS.message.PlayerInformation;
import com.PsichiX.JustIDS.message.PlayerInformation.Player;
import com.PsichiX.JustIDS.message.PlayerInformation.PlayerState;
import com.PsichiX.JustIDS.views.HorizontalBarHealth;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.ImageView;

public class SpectateActivity extends Activity {

	private HorizontalBarHealth playerBarLeft, playerBarRight;
	private ImageView attackLeft, attackRight;
    private static final String TAG = SpectateActivity.class.getName();

    private VibratorUtil vibratorUtil;

    private class SpectateHitSeenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            PlayerInformation.Player attacking = (PlayerInformation.Player) intent.getSerializableExtra("ATTACKING");
            // TODO: do something when observing the attack
            //vibratorUtil.vibrate(200);
        }
    }

    private class SpectateNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            GameStateMachine.GameStateNotificationEnum notification =
                    GameStateMachine.GameStateNotificationEnum.values()[intent.getIntExtra("NOTIFICATION_TYPE", -1)];
            //TODO: Jarek, po co mi myPlayer w spectator?
            PlayerInformation.Player myPlayer = (PlayerInformation.Player) intent.getSerializableExtra("MY_PLAYER");
            PlayerInformation.Player allPlayers[] = (PlayerInformation.Player[]) intent.getSerializableExtra("ALL_PLAYERS");
            
            Player pLeft=null, pRight=null;
            for(Player p : allPlayers)	{
            	if(p.getState()==PlayerState.PLAYING ) {
            		if(pLeft == null)	{
                		pLeft = p;
                		playerBarLeft.setForPlayer(p, false);
            		}
            		else	{
            			pRight=p;
            			playerBarRight.setForPlayer(p, true);
            		}
            	}
            }
            
            switch (notification) {
                case SOMETHING_CHANGED:
                    Log.i(TAG, "Something changed: " + PrintCurrentState.getCurrentStateAsString(myPlayer, allPlayers));
                    // TODO: here you should display state of mine and others
                    // Note - this also can happen before game is started or after finished.
                    refreshView();
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

    public void refreshView()	{
    	ViewGroup vg = (ViewGroup) findViewById (R.id.specteteView);
    	vg.invalidate();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.vibratorUtil = new VibratorUtil(this);
        setContentView(R.layout.activity_spectate);
        
        attackLeft = (ImageView) findViewById(R.id.attackLeft);
        attackRight = (ImageView) findViewById(R.id.attackRight);
        
        playerBarLeft = (HorizontalBarHealth) findViewById(R.id.playerBarLeft);
        playerBarRight = (HorizontalBarHealth) findViewById(R.id.playerBarRight);
        
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(new SpectateNotificationReceiver(),
                new IntentFilter("com.PsichiX.JustIDS.ScreamFightNotificationService"));

        localBroadcastManager.registerReceiver(
                new SpectateHitSeenReceiver(),
                new IntentFilter("com.PsichiX.JustIDS.ScreamFightAttackObservedService"));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_spectate, menu);
        return true;
    }
}
