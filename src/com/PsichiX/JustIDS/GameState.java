package com.PsichiX.JustIDS;

import android.util.Log;

import com.PsichiX.XenonCoreDroid.XeApplication.*;
import com.PsichiX.XenonCoreDroid.Framework.Graphics.*;
import com.PsichiX.XenonCoreDroid.XeUtils.*;
import com.PsichiX.XenonCoreDroid.XeSense;

public class GameState extends State implements CommandQueue.Delegate
{
	private Camera2D _cam;
	private Scene _scn;
	private CommandQueue _cmds = new CommandQueue();
	private float _currentForce = 0.0f;
	
	@Override
	public void onEnter()
	{
		_cmds.setDelegate(this);
		
		_scn = (Scene)getApplication().getAssets().get(R.raw.scene, Scene.class);
		_cam = (Camera2D)_scn.getCamera();
	}
	
	@Override
	public void onExit()
	{
		_scn.releaseAll();
	}
	
	@Override
	public void onInput(Touches ev)
	{
	}
	
	@Override
	public void onSensor(XeSense.EventData ev)
	{
		if(ev.type == XeSense.Type.LINEAR_ACCELERATION)
		{
			_currentForce = MathHelper.vecLength(ev.values[0], ev.values[1], ev.values[2]);
			Log.d("ACCEL", Float.toString(_currentForce));
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
	}
	
	public void onCommand(Object sender, String cmd, Object data)
	{
	}
}
