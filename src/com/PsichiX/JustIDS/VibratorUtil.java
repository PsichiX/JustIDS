package com.PsichiX.JustIDS;

import android.content.Context;
import android.os.Vibrator;


public class VibratorUtil {
	
	Vibrator v;
	
	public VibratorUtil(Context c) {
		v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	public void vibrate(int repeat)	{
		v.vibrate(repeat);
	}
}
