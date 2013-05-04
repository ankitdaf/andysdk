package com.andyrobo.helloandy.core;

import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

public class SwipeListener implements OnGestureListener {

	private static final float DELTA_THRESH = 100;

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		
		float x1 = e1.getX();
		float y1 = e1.getY();
		
		float x2 = e2.getX();
		float y2 = e2.getY();
		
		float delta_x = x2 - x1;
		float delta_y = y2 - y1;
		
		//System.out.println("P1: [" + x1 + ", " + y1 + "]");
		//System.out.println("P2: [" + x2 + ", " + y2 + "]");
		
		if(delta_x > DELTA_THRESH) {
			handleSwipeRight();
			return true;
		}
		
		if(delta_x < -DELTA_THRESH) {
			handleSwipeLeft();
			return true;
		}
		
		if(delta_y > DELTA_THRESH && y1 < 50) {
			handleSwipeDown();
			return true;
		}
		
		//TODO: better swipe detection
		
		return true;
	}

	protected void handleSwipeRight() {
	}
	
	protected void handleSwipeLeft() {
	}
	
	protected void handleSwipeUp() {
	}
	
	protected void handleSwipeDown() {
		
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

}
