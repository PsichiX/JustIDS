package com.PsichiX.JustIDS;

import com.PsichiX.XenonCoreDroid.XeActivity;
import com.PsichiX.XenonCoreDroid.XeApplication;
import com.PsichiX.XenonCoreDroid.XePhoton;
import com.PsichiX.XenonCoreDroid.XeSense;
import com.PsichiX.XenonCoreDroid.Framework.Graphics.Graphics;
import com.PsichiX.XenonCoreDroid.Framework.Utils.Utils;

public class MainActivity extends XeActivity
{
	public static XeApplication app;
	RecorderService rs;
	private GameStateManager gsm;
	
	@Override
	public void onCreate(android.os.Bundle savedInstanceState) 
	{
		// setup application before running it
		XeApplication.SETUP_SOUND_STREAMS = 1;
		XeApplication.SETUP_WINDOW_HAS_TITLE = false;
		XeApplication.SETUP_WINDOW_FULLSCREEN = true;
		XeApplication.SETUP_SCREEN_ORIENTATION = XeApplication.Orientation.PORTRAIT;
		XeApplication.SETUP_SENSORS_RATE = android.hardware.SensorManager.SENSOR_DELAY_GAME;
		
		// create application
		super.onCreate(savedInstanceState);
		
		final GameState gs = new GameState();
		
		gsm = new GameStateManager(this, new BroadCastManager());
		gsm.setSomethingChangedListener(new Runnable() {
			@Override
			public void run() {	
				gs.healthLevel = gsm.getLifePointsOfMine();
			}
		});

		gsm.setHitListener(new Runnable() {
			@Override
			public void run() {	
				// TO DO: do something when hit
			}
		});

		gsm.resetGame();
		
		//obsluga mikrofonu
		rs = new RecorderService();
		rs.startRecording();
		
		// run state
		app = getApplicationCore();
		Utils.initModule(getApplicationCore().getAssets());
		Graphics.initModule(getApplicationCore().getAssets(), getApplicationCore().getPhoton());
		getApplicationCore().getTimer().setFixedStep(1000 / 30);
		getApplicationCore().getPhoton().getRenderer().getTimer().setFixedStep(1000 / 30);
		getApplicationCore().getPhoton().setRenderMode(XePhoton.RenderMode.QUEUE, true);
		getApplicationCore().getPhoton().getRenderer().setClearBackground(true, 1.0f, 1.0f, 1.0f, 1.0f);
		getApplicationCore().getSense().use(XeSense.Type.LINEAR_ACCELERATION);
		getApplicationCore().run(gs);
		
		//startActivity(new Intent(this, AudioRecordTest.class));
	}
}
