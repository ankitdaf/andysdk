package com.andyrobo.base.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.PointF;
import android.view.View;

public class ArrowView extends View {

	private Paint paint = new Paint();
	private final PointF origin = new PointF();
	private final PointF end = new PointF();
	
	public ArrowView(Context context) {
		super(context);
		origin.set(0, 0);
		end.set(0, 0);
		
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeCap(Cap.ROUND);
		paint.setStrokeWidth(3f);
	}
	
	public void setThickness(float t) {
		paint.setStrokeWidth(t);
	}

	public void setColor(int color) {
		paint.setColor(color);
	}
	
	public void setOrigin(PointF p) {
		origin.set(p);
	}
	
	public void setEnd(PointF p) {
		end.set(p);
	}

	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawLine(origin.x, origin.y, end.x, end.y, paint);
		canvas.drawCircle(end.x, end.y, 5f, paint);
	}
}
