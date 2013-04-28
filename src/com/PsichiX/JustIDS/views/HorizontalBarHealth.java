package com.PsichiX.JustIDS.views;

import com.PsichiX.JustIDS.R;
import com.PsichiX.JustIDS.message.PlayerInformation.Player;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class HorizontalBarHealth extends HorizontalBar {
 
	Player playerDisplayed;
	boolean rightAlign;
	
	public HorizontalBarHealth(Context context, AttributeSet attrs) {
		super(context, attrs);
		barResourceID = R.drawable.mw_fight_bar_hp;
		rightAlign = true;
		paint.setColor(Color.GRAY);
	}
	
	public void setForPlayer(Player p, boolean right)	{
		playerDisplayed = p;
		rightAlign = right;
	}
	
	@Override
	protected double getFillPercentage() {
		if(isInEditMode()) 
			return 1;
		else
			if(playerDisplayed==null) return 0;
			return playerDisplayed.getLifePoints()/100;

	}

	@Override
	protected String getText() {
		if(isInEditMode()) 
			return "SonGoku";
		return playerDisplayed.getName();
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int txtPos;
		if(!isInEditMode() && playerDisplayed==null) return; 
		if(rightAlign)	{	
			destRect.right = getWidth()-7;
			destRect.left = (int)((1-getFillPercentage())*getWidth()+7);
			paint.setTextAlign(Align.RIGHT);
			txtPos = getWidth() - 14;
		}
		else	{
			destRect.right = (int)(getFillPercentage()*getWidth()-7);
			paint.setTextAlign(Align.LEFT);
			txtPos = 14;
		}
		canvas.drawBitmap(bcg, 0, 0, null);
		canvas.drawBitmap(bar, null, destRect, null);
		canvas.drawText(getText(), txtPos, (float)(getHeight()*7/10), paint);
		
	}
	
}
