package com.riis.androidarduino.can;

import com.riis.androidarduino.can.GuageViewNeedle.NeedleSize;

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
	private static final int BACKGROUND = R.drawable.guages;
	
	private static final int SPEED_CENTER_X = 657;
	private static final int SPEED_CENTER_Y = 316;
	private static final int TACH_CENTER_X = 228;
	private static final int TACH_CENTER_Y = 350;
	
	private static final int MAX_SPEED = 240;
	private static final int MAX_TACH = 8000;
	private static final int MIN_SPEED_ANGLE = 46;
	private static final int MAX_SPEED_ANGLE = 313;
	private static final int MIN_TACH_ANGLE = 29;
	private static final int MAX_TACH_ANGLE = 210;

	private Context context;
	
	private Point speedNeedlePos;
	private Point tachNeedlePos;
	private float currentScaleFactor;
	
	private GuageViewNeedle speedNeedle;
	private GuageViewNeedle tachNeedle;
	private float currentSpeed;
	private float currentTach;
	
	private boolean smoothValues;
	
	public GuageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.context = context;
		
		currentScaleFactor = 1.0f;
		speedNeedle = new GuageViewNeedle(context, NeedleSize.LARGE, currentScaleFactor, MIN_SPEED_ANGLE, MAX_SPEED_ANGLE);
		tachNeedle = new GuageViewNeedle(context, NeedleSize.SMALL, currentScaleFactor, MIN_TACH_ANGLE, MAX_TACH_ANGLE);
		
		speedNeedlePos = new Point(SPEED_CENTER_X, SPEED_CENTER_Y);
		tachNeedlePos = new Point(TACH_CENTER_X, TACH_CENTER_Y);
		
		smoothValues = true;
		
		this.setWillNotDraw(false);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {	
		//Draw the dials
		Bitmap dialBg = BitmapFactory.decodeResource(context.getResources(), BACKGROUND);		
		Rect srcRect = new Rect(0, 0, dialBg.getWidth(), dialBg.getHeight());
		Rect dstRect = new Rect(0, 0, getWidth(), (getWidth() * dialBg.getHeight()) /  dialBg.getWidth());
		canvas.drawBitmap(dialBg, srcRect, dstRect, new Paint());
		
		float newScaleFactor = ((float)getWidth() / (float)dialBg.getWidth());
		checkNewScaleFactor(newScaleFactor);	
		
		speedNeedle.setAngleForValueBetweenZeroAnd(currentSpeed, MAX_SPEED);
		tachNeedle.setAngleForValueBetweenZeroAnd(currentTach, MAX_TACH);

		speedNeedle.draw(canvas, speedNeedlePos);
		tachNeedle.draw(canvas, tachNeedlePos);
		
		invalidate();
	}
	
	private void checkNewScaleFactor(float newScaleFactor) {
		if(newScaleFactor != currentScaleFactor) {
			currentScaleFactor = newScaleFactor;
			
			speedNeedle.setScaleFactor(currentScaleFactor);
			tachNeedle.setScaleFactor(currentScaleFactor);
			
			speedNeedlePos.x = (int) (SPEED_CENTER_X * currentScaleFactor);
			speedNeedlePos.y = (int) (SPEED_CENTER_Y * currentScaleFactor);
			
			tachNeedlePos.x = (int) (TACH_CENTER_X * currentScaleFactor);
			tachNeedlePos.y = (int) (TACH_CENTER_Y * currentScaleFactor);
		}
	}
	
	public void setSpeed(float speed) {
		if(speed > MAX_SPEED) {
			currentSpeed = MAX_SPEED;
		} else if(speed < 0) {
			currentSpeed = 0;
		} else {
			currentSpeed = speed;
		}
		
		invalidate();
	}
	
	public void setTach(float rpm) {
		if(rpm > MAX_TACH) {
			currentTach = MAX_TACH;
		} else if(rpm < 0) {
			currentTach = 0;
		} else {
			currentTach = rpm;
		}
		
		invalidate();
	}
	
	public float getValue() {
		return this.currentSpeed;
	}
}
