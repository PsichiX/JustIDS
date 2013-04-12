package com.PsichiX.JustIDS.views;

import com.PsichiX.JustIDS.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class HorizontalBarHealth extends HorizontalBar {

	public HorizontalBarHealth(Context context, AttributeSet attrs) {
		super(context, attrs);
		barResourceID = R.drawable.mw_fight_bar_hp;

	}

	@Override
	protected double getFillPercentage() {
		//if(isInEditMode()) 
			return 0.5;
		/*double pos = Game.getGame().playerEgo.getHealthPoints();
		double posMax = Game.getGame().playerEgo.maxHealthPoints;
		if (pos <= 0)
			pos = 1;
		return (pos / posMax);*/
	}

	@Override
	protected String getText() {
		//if(isInEditMode()) 
			return "HP: 50/100";
		/*return new String("HP: "+ Game.getGame().playerEgo.getHealthPoints() +"/" + 
				Game.getGame().playerEgo.maxHealthPoints);
				*/
	}

}
