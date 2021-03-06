package com.PsichiX.JustIDS.services;

import java.io.IOException;

import com.PsichiX.JustIDS.states.GameState;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;

public class RecorderService {
	
    private static final String TAG = RecorderService.class.getName();
    private static String mFileName = null;

    private MediaRecorder mRecorder = null;

    private MediaPlayer   mPlayer = null;
    
	CountDownTimer cdt;
	int ITERATIONS_PER_SECOND = 30;
	
    public void startRecording() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/ScreamFight/audiorecord.3gp";
    	
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        
        //AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        //am.setStreamVolume(AudioManager.STREAM_MUSIC, yourVolume, 0);
        
        try {
            mRecorder.prepare();
        } catch (IOException e) {
        	e.printStackTrace();
            Log.e(TAG, "prepare() failed", e);
        }

        mRecorder.start();
        
        
        cdt = new CountDownTimer(25 * 3600 * 24 * 30,
				1000 / ITERATIONS_PER_SECOND) {
			@Override
			public void onTick(long millisUntilFinished) {
				if(mRecorder != null)	{
					int newlevel = mRecorder.getMaxAmplitude();

					if (newlevel>20000) {
						synchronized(GameState.class)	{
							GameState.manaLevel+=newlevel/10000;
							if(GameState.manaLevel>100) GameState.manaLevel=100;	
						}
					}
				}
			}

			@Override
			public void onFinish() {

			}
		};
        cdt.start();
    }

	public void stopRecording() {
		mRecorder.reset();
		mRecorder.release();
		cdt.cancel();
	}
}
