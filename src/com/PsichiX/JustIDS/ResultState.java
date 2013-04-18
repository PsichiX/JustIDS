package com.PsichiX.JustIDS;

import com.PsichiX.XenonCoreDroid.XeApplication.State;
import com.PsichiX.XenonCoreDroid.Framework.Graphics.Camera2D;
import com.PsichiX.XenonCoreDroid.Framework.Graphics.Font;
import com.PsichiX.XenonCoreDroid.Framework.Graphics.Material;
import com.PsichiX.XenonCoreDroid.Framework.Graphics.Scene;
import com.PsichiX.XenonCoreDroid.Framework.Graphics.Text;

public class ResultState extends State {
	private Camera2D _cam;
	private Scene _scn;
	private String _status;
	private boolean _strobo = false;
	private Text _text;
	private int _stroboPhase = 0;
	
	public ResultState(String status, boolean strobo)
	{
		_status = status;
		_strobo = strobo;
	}
	
	@Override
	public void onEnter()
	{
		_scn = (Scene)getApplication().getAssets().get(R.raw.scene, Scene.class);
		_cam = (Camera2D)_scn.getCamera();
		_cam.setViewPosition(0.0f, 0.0f);
		_cam.setViewAngle(0.0f);
		
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
		
		if(_strobo)
		{
			getApplication().getPhoton().getRenderer().setClearBackground(true,
				_stroboPhase == 0 ? 1.0f : 0.0f,
				_stroboPhase == 1 ? 1.0f : 0.0f,
				_stroboPhase == 2 ? 1.0f : 0.0f,
				1.0f);
			_stroboPhase++;
			_stroboPhase %= 3;
		}
		
		_scn.update(dt);
	}
	
	@Override
	public void onExit()
	{
		_scn.releaseAll();
	}
}
