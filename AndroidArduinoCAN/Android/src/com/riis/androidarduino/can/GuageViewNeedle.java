package com.riis.androidarduino.can;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;

public class GuageViewNeedle {
	private static int GHOST_ALPHA = 128;
	
	private static int SMALL_NEEDLE_IMG = R.drawable.tach_needle;
	private static int LARGE_NEEDLE_IMG = R.drawable.speed_needle;
	
	private static int SMALL_NEEDLE_CENTER_X = 27;
	private static int SMALL_NEEDLE_CENTER_Y = 171;
	
	private static int LARGE_NEEDLE_CENTER_X = 39;
	private static int LARGE_NEEDLE_CENTER_Y = 250;
	
	public enum NeedleSize {LARGE, SMALL};
	private NeedleSize needleSize;
	
	private double scaleFactor;
	private double minAngle;
	private double maxAngle;
	
	private double angle;
	private Point needleCenter;
	private Matrix needleMatrix;
	
	private Bitmap needleBitmap;	
	private boolean isGhost;
	
	public GuageViewNeedle(Context context, NeedleSize needleSize, double currentScaleFactor, double minAngle, double maxAngle) {
		this.needleSize = needleSize;
		this.scaleFactor = currentScaleFactor;
		
		this.minAngle = minAngle;
		this.maxAngle = maxAngle;
		
		angle = 0.0f;
		needleCenter = new Point();
		needleMatrix = new Matrix();
		
		isGhost = false;
		
		setUpNeedle(context);
	}
	
	private void setUpNeedle(Context context) {
		setUpNeedleCenter();
		setUpNeedleBitmap(context);
	}
	
	private void setUpNeedleCenter() {
		if(needleSize == NeedleSize.LARGE) {
			setUpCenterForLargeNeedle();
		} else {
			setUpCenterForSmallNeedle();
		}
	}
	
	private void setUpCenterForLargeNeedle() {
		needleCenter.x = (int) (LARGE_NEEDLE_CENTER_X * scaleFactor);
		needleCenter.y = (int) (LARGE_NEEDLE_CENTER_Y * scaleFactor);
	}
	
	private void setUpCenterForSmallNeedle() {
		needleCenter.x = (int) (SMALL_NEEDLE_CENTER_X * scaleFactor);
		needleCenter.y = (int) (SMALL_NEEDLE_CENTER_Y * scaleFactor);
	}
	
	private void setUpNeedleBitmap(Context context) {
		if(needleSize == NeedleSize.LARGE) {
			setUpBitmapForLargeNeedle(context);
		} else {
			setUpBitmapForSmallNeedle(context);
		}
	}
	
	private void setUpBitmapForLargeNeedle(Context context) {
		needleBitmap = BitmapFactory.decodeResource(context.getResources(), LARGE_NEEDLE_IMG);
	}
	
	private void setUpBitmapForSmallNeedle(Context context) {
		needleBitmap = BitmapFactory.decodeResource(context.getResources(), SMALL_NEEDLE_IMG);
	}
	
	public void setAngleForValueBetweenZeroAnd(double value, double maxValue) {
		double newAngle = ((value / maxValue) * (maxAngle - minAngle)) + minAngle;
		setAngle(newAngle);
	}
	
	public void setAngle(double angle) {
		if(angle > maxAngle) {
			this.angle = maxAngle;
		} else if(angle < minAngle) {
			this.angle = minAngle;
		} else {
			this.angle = angle;
		}
	}
	
	public double getMinAngle() {
		return minAngle;
	}
	
	public double getMaxAngle() {
		return maxAngle;
	}
	
	public void setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;
		setUpNeedleCenter();
	}
	
	public void setIsGhost(boolean isGhost) {
		this.isGhost = isGhost;
	}
	
	public void draw(Canvas canvas, Point destination) {
		needleMatrix.reset();
		
		needleMatrix.postScale((float)scaleFactor, (float)scaleFactor);
		needleMatrix.postRotate((float)angle + 180.0f, needleCenter.x, needleCenter.y);
		needleMatrix.postTranslate(destination.x - needleCenter.x, destination.y - needleCenter.y);
		
		Paint paint = new Paint();
		if(isGhost) {
			paint.setAlpha(GHOST_ALPHA);
		}
		
		canvas.drawBitmap(needleBitmap, needleMatrix, paint);
	}
}
