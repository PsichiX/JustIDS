/*
 * The application needs to have the permission to write to external storage
 * if the output file is written to the external storage, and also the
 * permission to record audio. These permissions must be set in the
 * application's AndroidManifest.xml file, with something like:
 *
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 * <uses-permission android:name="android.permission.RECORD_AUDIO" />
 *
 */
package com.PsichiX.JustIDS;

import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Context;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;

import java.io.IOException;


public class AudioRecordTest extends Activity
{
	
	TextView manaLevel;
	TextView maxLevel;
	TextView voiceLevel;
	CountDownTimer cdt;
	int ITERATIONS_PER_SECOND = 30;
	int lastlevel =0;
	
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;

    private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;

    private PlayButton   mPlayButton = null;
    private MediaPlayer   mPlayer = null;

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
    
    	mPlayer.release();
        mPlayer = null;
        
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        
        try {
            mRecorder.prepare();
        } catch (IOException e) {
        	e.printStackTrace();
            Log.e(LOG_TAG, "prepare() failed");
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
						}
					}
					manaLevel.setText("mana: " + GameState.manaLevel);
					if(newlevel!=0) voiceLevel.setText("voice: " + newlevel);
					if(newlevel > lastlevel)	{
						lastlevel = newlevel;
						maxLevel.setText("max: " + lastlevel);
					}
				}
			}

			@Override
			public void onFinish() {

			}
		};
        cdt.start();
    }

    private void stopRecording() {
    	maxLevel.setText(mRecorder.getAudioSourceMax());
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    class RecordButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    class PlayButton extends Button {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }

    public AudioRecordTest() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        mRecordButton = new RecordButton(this);
        
        manaLevel = new TextView(this);
        voiceLevel = new TextView(this);
        maxLevel = new TextView(this);
        
        ll.addView(mRecordButton,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        mPlayButton = new PlayButton(this);
        ll.addView(mPlayButton,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        ll.addView(manaLevel);
        ll.addView(voiceLevel);
        ll.addView(maxLevel);
        manaLevel.setText("mana:");
        voiceLevel.setText("voice:");
        maxLevel.setText("max:");
        setContentView(ll);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
}