package com.PsichiX.JustIDS;

import com.PsichiX.XenonCoreDroid.XeApplication.*;
import com.PsichiX.XenonCoreDroid.Framework.Graphics.*;
import com.PsichiX.XenonCoreDroid.Framework.Actors.*;
import com.PsichiX.XenonCoreDroid.XeUtils.*;
import com.PsichiX.XenonCoreDroid.XeSense;

public class GameState extends State implements CommandQueue.Delegate
{
	private Camera2D _cam;
	private Scene _scn;
	private ActorsManager _actors = new ActorsManager(this);
	private CommandQueue _cmds = new CommandQueue();
	
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
		_actors.detachAll();
	}
	
	@Override
	public void onInput(Touches ev)
	{
		_actors.onInput(ev);
	}
	
	@Override
	public void onSensor(XeSense.EventData ev)
	{
		_actors.onSensor(ev);
	}

	@Override
	public void onUpdate()
	{
		getApplication().getSense().setCoordsOrientation(-1);
		
		float dt = getApplication().getTimer().getDeltaTime() * 0.001f;
		//float dt = 1.0f / 30.0f;
		
		_cmds.run();
		_actors.onUpdate(dt);
		_scn.update(dt);
	}
	
	public void onCommand(Object sender, String cmd, Object data)
	{
	}
}
