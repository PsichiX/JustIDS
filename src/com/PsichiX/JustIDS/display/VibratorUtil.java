package com.PsichiX.JustIDS.display;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;

public class VibratorUtil {
    Vibrator v;

    public VibratorUtil(Context c) {
        v = (Vibrator) c.getSystemService(Activity.VIBRATOR_SERVICE);
    }

    public void vibrate(int repeat)	{
        v.vibrate(repeat);
    }
}

