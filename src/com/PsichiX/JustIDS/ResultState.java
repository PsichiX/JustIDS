package com.PsichiX.JustIDS;

import android.util.Log;

import com.PsichiX.XenonCoreDroid.XeApplication.*;
import com.PsichiX.XenonCoreDroid.Framework.Graphics.*;
import com.PsichiX.XenonCoreDroid.XeUtils.*;
import com.PsichiX.XenonCoreDroid.XeSense;

public class ResultState extends State {
	private Camera2D _cam;
	private Scene _scn;
	private String _status;
	private Text _text;
	
	public ResultState(String status)
	{
		_status = status;
	}
	
	@Override
	public void onEnter()
	{
		_scn = (Scene)getApplication().getAssets().get(R.raw.scene, Scene.class);
		_cam = (Camera2D)_scn.getCamera();
		
		Material mat = (Material)getApplication().getAssets().get(R.raw.badaboom_material, Material.class);
		Font fnt = (Font)getApplication().getAssets().get(R.raw.badaboom_font, Font.class);
		
		_text = new Text();
		_text.build(_status, fnt, mat, Font.Alignment.CENTER, Font.Alignment.MIDDLE, 1.0f, 1.0f);
		_scn.attach(_text);
	}
	
	@Override
	public void onUpdate()
	{
		float dt = getApplication().getTimer().getDeltaTime() * 0.001f;
		//float dt = 1.0f / 30.0f;
		
		_scn.update(dt);
	}
	
	@Override
	public void onExit()
	{
		_scn.releaseAll();
	}
}
