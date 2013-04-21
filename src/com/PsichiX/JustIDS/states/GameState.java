package com.PsichiX.JustIDS.states;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.PsichiX.JustIDS.R;
import com.PsichiX.XenonCoreDroid.XeApplication.*;
import com.PsichiX.XenonCoreDroid.Framework.Graphics.*;
import com.PsichiX.XenonCoreDroid.XeUtils.*;
import com.PsichiX.XenonCoreDroid.XeSense;

public class GameState extends State implements CommandQueue.Delegate
{

    private static final String TAG = GameState.class.getName();
	
	/** od 0 do 100*/
	public static double manaLevel;
	public static double healthLevel;
    private final Context context;

    private Camera2D _cam;
	private Scene _scn;
	private CommandQueue _cmds = new CommandQueue();
	private float _lastForce = 0.0f;
    private float _currentForce = 0.0f;
	private float _maxForce = 0.0f;
	private boolean _forceRecording = false;
	private Sprite Mana;
	private Sprite Health;

    public GameState(Context context) {
        this.context = context;
    }
	@Override
	public void onEnter()
	{
		_cmds.setDelegate(this);
		
		_scn = (Scene)getApplication().getAssets().get(R.raw.scene, Scene.class);
		_cam = (Camera2D)_scn.getCamera();
		_cam.setViewPosition(_cam.getViewWidth() * 0.5f, _cam.getViewHeight() * 0.5f);
		_cam.setViewAngle(180);
		
		Material mat;
		mat = (Material) getApplication().getAssets().get(R.raw.mana_material, Material.class);
		Mana = new Sprite(mat);
		Mana.setSize(_cam.getViewWidth() * 0.5f, _cam.getViewHeight());
		_scn.attach(Mana);
		
		mat = (Material) getApplication().getAssets().get(R.raw.health_material, Material.class);
		Health = new Sprite(mat);
		Health.setSize(_cam.getViewWidth() * 0.5f, _cam.getViewHeight());
		Health.setPosition( _cam.getViewWidth() * 0.5f, 0.0f);
		_scn.attach(Health);

        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("com.PsichiX.JustIDS.resetGame"));

		manaLevel = 0;
		
		getApplication().getAssets().get(R.raw.badaboom_material, Material.class);
		getApplication().getAssets().get(R.raw.badaboom_font, Font.class);
		
		getApplication().getPhoton().getRenderer().setClearBackground(true, 0.0f, 0.0f, 0.0f, 1.0f);
	}
	
	@Override
	public void onExit()
	{
		_scn.releaseAll();
	}
		
	@Override
	public void onSensor(XeSense.EventData ev)
	{
		if(ev.type == XeSense.Type.LINEAR_ACCELERATION)
		{
			_lastForce = _currentForce;
			_currentForce = MathHelper.vecLength(ev.values[0], ev.values[1], ev.values[2]);
			if(_forceRecording)
				_maxForce = Math.max(_currentForce, _maxForce);
			//Log.d("ACCEL", Float.toString(_maxForce));
			if(_currentForce > 20.0f && _lastForce <= 20.0f)
				_cmds.queueCommand(this, "StartAttack", null);
			else if(_lastForce > 20.0f && _currentForce <= 20.0f)
				_cmds.queueCommand(this, "StopAttack", null);
		}
	}

	@Override
	public void onUpdate()
	{
		getApplication().getSense().setCoordsOrientation(-1);
		
		float dt = getApplication().getTimer().getDeltaTime() * 0.001f;
		//float dt = 1.0f / 30.0f;
		
		_cmds.run();
		_scn.update(dt);  
		Mana.setSize(_cam.getViewWidth() * 0.5f, (float) (healthLevel * _cam.getViewHeight() * 0.01f));
		Health.setSize(_cam.getViewWidth() * 0.5f, (float) (manaLevel * _cam.getViewHeight() * 0.01f));
		
		// hack zabezieczenie
//		if(healthLevel <= 0.0f)
//			youLost();
	}
	
	public void onCommand(Object sender, String cmd, Object data)
	{
		if(cmd.equals("StartAttack"))
		{
			Log.v(TAG, "Start Attack");
			_maxForce = 0.0f;
			_forceRecording = true;
		}
		else if(cmd.equals("StopAttack"))
		{
			Log.v(TAG, "Stop Attack");
			double strength = calculateStrength();
            Intent intent = new Intent("com.PsichiX.JustIDS.attackWithStrength");
            intent.putExtra("STRENGTH", strength);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            manaLevel = 0.0f;
			_forceRecording = false;
			_maxForce = 0.0f;
		}
	}

	private double calculateStrength() {
		double strength = 30 * Math.max(Math.min(1,(_maxForce- 20.0)/10.0),0)*(manaLevel/100.0); 
		return strength;
	}
	
	public void youWon() {
		getApplication().pushState(new ResultState("You won!", true));
	}
	
	public void youLost() {
		getApplication().pushState(new ResultState("You lost!", true));
	}

}

