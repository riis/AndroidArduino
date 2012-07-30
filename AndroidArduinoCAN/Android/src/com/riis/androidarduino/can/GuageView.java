package com.riis.androidarduino.can;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class GuageView extends View {
	private static final int DEFAULT_MAX = 240;
	private static final int DEFAULT_MIN = 0;
	private static final int DEFAULT_BG = R.drawable.speedometerbg;
	private static final int DEFAULT_NEEDLE = R.drawable.speedometerneedle;
	private static final int MIN_ANGLE = 62;
	private static final int MAX_ANGLE = 300;

	private Context context;
	
	private int maxValue;
	private int minValue;
	
	private int dialBackground = DEFAULT_BG;
	private int dialNeedle = DEFAULT_NEEDLE;
	
	private Point dialNeedlePos;
	
	private float currentValue;
	
	public GuageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		parseAttributes(context, attrs);
		this.context = context;
		
		dialNeedlePos = new Point();
		
		currentValue = 0;
	}
	
	private void parseAttributes(Context context, AttributeSet attrs) {
		TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.GuageView);
		    
		maxValue = attrArray.getInt(R.styleable.GuageView_maxValue, DEFAULT_MAX);
		minValue = attrArray.getInt(R.styleable.GuageView_minValue, DEFAULT_MIN);
		        	
		attrArray.recycle();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {		
		Bitmap dialBg = BitmapFactory.decodeResource(context.getResources(), dialBackground);
		Bitmap dialNdl = BitmapFactory.decodeResource(context.getResources(), dialNeedle);
		
		Rect srcRect = new Rect(0, 0, dialBg.getWidth(), dialBg.getHeight());
		Rect dstRect = new Rect(0, 0, getWidth(), getWidth());
		canvas.drawBitmap(dialBg, srcRect, dstRect, new Paint());

		dialNeedlePos.x = dstRect.centerX() - (dialNdl.getWidth() / 2);
		dialNeedlePos.y = dstRect.centerY();
		
		float rotateAngle = ((currentValue / maxValue) * MAX_ANGLE)	+ MIN_ANGLE;
		float scaleFactor = ((float)dstRect.centerX() / (float)dialBg.getWidth()) * 1.5f;
		
		Matrix rotator = new Matrix();
		rotator.postScale(1, scaleFactor);
		rotator.postRotate(rotateAngle);
		rotator.postTranslate(dialNeedlePos.x, dialNeedlePos.y);
		
		canvas.drawBitmap(dialNdl, rotator, new Paint());
	}
	
	public void setValue(float value) {
		if(value > maxValue) {
			currentValue = maxValue;
		} else if(value < minValue) {
			currentValue = minValue;
		} else {
			this.currentValue = value;
		}
		
		invalidate();
	}
	
	public float getValue() {
		return this.currentValue;
	}
}
