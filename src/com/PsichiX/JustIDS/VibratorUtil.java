package com.PsichiX.JustIDS;

import android.content.Context;
import android.os.Vibrator;

public class VibratorUtil {
	static Vibrator v;
	
	public static void vibrate(long[] pattern, int repeat)	{
		v.vibrate(pattern, repeat);
	}
}
