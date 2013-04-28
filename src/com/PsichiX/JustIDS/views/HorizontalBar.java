package com.PsichiX.JustIDS.views;

import com.PsichiX.JustIDS.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;


public abstract class HorizontalBar extends View {

	protected Bitmap bcg, bar;
	protected int barResourceID;
	protected Rect destRect;
	Paint paint; 
	
	public HorizontalBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
		paint.setTextAlign(Align.LEFT);
	}

	protected void makeBitmaps() {
		destRect = new Rect(7, 7, (int)(getFillPercentage()*getWidth()-7), getHeight()-7);
		BitmapFactory.Options myOptions = new BitmapFactory.Options();
		myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important
		bcg = BitmapFactory.decodeResource(getResources(), R.drawable.mw_fight_bar_bcg, myOptions);
		bcg = Bitmap.createScaledBitmap(bcg, getWidth(), getHeight(), true);
		bar = BitmapFactory.decodeResource(getResources(), barResourceID, myOptions);
		bar = Bitmap.createScaledBitmap(bar, getWidth()-14, getHeight()-14, true);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		paint.setTextSize((float)(getHeight()*6/10));
		makeBitmaps();
	}

	abstract protected double getFillPercentage(); 
	
	abstract protected String getText();

}
