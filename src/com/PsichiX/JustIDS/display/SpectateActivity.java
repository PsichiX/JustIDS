package com.PsichiX.JustIDS.display;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.PsichiX.JustIDS.R;
import com.PsichiX.JustIDS.R.layout;
import com.PsichiX.JustIDS.R.menu;
import com.PsichiX.JustIDS.game.GameStateMachine;
import com.PsichiX.JustIDS.message.PlayerInformation;
import com.PsichiX.JustIDS.message.PlayerInformation.Player;
import com.PsichiX.JustIDS.message.PlayerInformation.PlayerState;
import com.PsichiX.JustIDS.services.CameraService;
import com.PsichiX.JustIDS.views.CameraPreview;
import com.PsichiX.JustIDS.views.HorizontalBarHealth;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class SpectateActivity extends Activity {

	private HorizontalBarHealth playerBarLeft, playerBarRight;
	private ImageView attackLeft, attackRight;
	Player pLeft=null, pRight=null;

    private Camera mCamera;
    private CameraPreview mPreview;
	
	private static final String TAG = SpectateActivity.class.getName();

    private VibratorUtil vibratorUtil;

    private class SpectateHitSeenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            PlayerInformation.Player attacking = (PlayerInformation.Player) intent.getSerializableExtra("ATTACKING");
            double strength=20.0;
            if(attacking.getName().equals(pLeft.getName()))	{
            	animateAttack(attackLeft, strength);
            }
            else	{
            	animateAttack(attackRight, strength);
            }
            // TODO: do something when observing the attack
            //vibratorUtil.vibrate(200);
        }
    }

	private void animateAttack(final ImageView attack, double strength) {
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) attack.getLayoutParams();
        params.height = (int) (strength*5);
        attack.setLayoutParams(params);
        Animation anim = //new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF, 0.5f);
		 AnimationUtils.loadAnimation(this.getBaseContext(), R.anim.attackanim);
		anim.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				attack.setVisibility(View.VISIBLE);
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			@Override
			public void onAnimationEnd(Animation animation) {
				attack.setVisibility(View.INVISIBLE);
			}
		});
		attack.startAnimation(anim);
	}
    
    private class SpectateNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            GameStateMachine.GameStateNotificationEnum notification =
                    GameStateMachine.GameStateNotificationEnum.values()[intent.getIntExtra("NOTIFICATION_TYPE", -1)];
            //TODO: Jarek, po co mi myPlayer w spectator?
            PlayerInformation.Player myPlayer = (PlayerInformation.Player) intent.getSerializableExtra("MY_PLAYER");
            //TODO: to musi byc straszliwie niewydajne.
            PlayerInformation.Player allPlayers[] = (PlayerInformation.Player[]) intent.getSerializableExtra("ALL_PLAYERS");
            
            pLeft=null;
            pRight=null;
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
        attackLeft.setVisibility(View.INVISIBLE);
        attackRight.setVisibility(View.INVISIBLE);
        
        playerBarLeft = (HorizontalBarHealth) findViewById(R.id.playerBarLeft);
        playerBarRight = (HorizontalBarHealth) findViewById(R.id.playerBarRight);
        
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(new SpectateNotificationReceiver(),
                new IntentFilter("com.PsichiX.JustIDS.ScreamFightNotificationService"));

        localBroadcastManager.registerReceiver(
                new SpectateHitSeenReceiver(),
                new IntentFilter("com.PsichiX.JustIDS.ScreamFightAttackObservedService"));

        // Create an instance of Camera
        mCamera = CameraService.getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // get an image from the camera
                    mCamera.takePicture(null, null, mPicture);
                    //mCamera.startPreview();
                }
            }
        );
        
    }

    public void quitSpectate()	{
    	mCamera.release();
   }
    
    @Override
    public void onBackPressed() {
    	quitSpectate();
    	super.onBackPressed();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_spectate, menu);
        return true;
    }
    
    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile; 
            try {
            	pictureFile = CameraService.getOutputMediaFile(CameraService.MEDIA_TYPE_IMAGE);
            }
            catch(Exception e) 	{
                Log.e(TAG, "Error creating media file, check storage permissions: " + e.getMessage());
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };
}
