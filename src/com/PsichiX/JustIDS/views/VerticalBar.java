package com.PsichiX.JustIDS.views;

import com.PsichiX.JustIDS.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class VerticalBar extends View {

	protected Bitmap bcg, bar;
	private Rect destRect;
	private int resource;
	
	public VerticalBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		resource = R.drawable.fight_bar_hp;
		if(attrs.getAttributeValue("http://schemas.android.com/apk/gen/org.miscwidgets", "type").equals("mana"))	{
			resource = R.drawable.fight_bar_mana;
		}
	}

	protected void makeBitmaps() {
		destRect = new Rect(0, 0, getWidth()-0, getHeight()+0 );
		BitmapFactory.Options myOptions = new BitmapFactory.Options();
		myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important
		bcg = BitmapFactory.decodeResource(getResources(), R.drawable.fight_bar_bcg, myOptions);
		bcg = Bitmap.createScaledBitmap(bcg, getWidth(), getHeight(),  true);
		bar = BitmapFactory.decodeResource(getResources(), resource, myOptions);
		bar = Bitmap.createScaledBitmap(bar, getWidth()-0, getHeight()-0,  true);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		makeBitmaps();
	}

	protected double getFillPercentage() {
		if(isInEditMode()) return 0.5;
		return 0.5;
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		destRect.top = (int)((1-getFillPercentage())*(getHeight()));
		//canvas.rotate(-90);
		canvas.drawBitmap(bcg, 0, 0, null);
		canvas.drawBitmap(bar, null, destRect, null);
		
		// $log.d("VIEW", "creature health onDraw pos = " +toPosition +
		// " size = "+ size);
	}
}
