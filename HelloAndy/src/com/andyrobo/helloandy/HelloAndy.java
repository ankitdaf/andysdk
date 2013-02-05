package com.andyrobo.helloandy;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Handler;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.andyrobo.base.AndyActivity;
import com.andyrobo.base.graphics.AndyFace;
import com.andyrobo.base.graphics.ArrowView;
import com.andyrobo.base.motor.AndyMotorController;

public class HelloAndy extends AndyActivity implements OnGestureListener {

	private GestureDetector gDetector;
	private ArrowView arrow;
	private PointF center;
	private int screenWidth;
	private int screenHeight;

	private final Handler h = new Handler();
	private boolean receiveTouch;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_hello_andy, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_help:
			showHelp();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void setContentView() {
		arrow = new ArrowView(this);
		arrow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

		FrameLayout face = AndyFace.init(this, true);
		//face.addView(arrow, 1);

		gDetector = new GestureDetector(this);
		showHelp();

		center = setOrigin();
		arrow.setOrigin(center);
		arrow.setColor(Color.BLUE);

		hideArrow();
		receiveTouch = true;

		setContentView(face);
	}

	private void hideArrow() {
		arrow.setVisibility(View.INVISIBLE);
	}

	private void showArrow() {
		arrow.setVisibility(View.VISIBLE);
	}

	private PointF setOrigin() {
		Display display = getWindowManager().getDefaultDisplay();
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();

		return new PointF(screenWidth / 2f, screenHeight / 2f);
	}

	@Override
	protected void onStart() {
		AndyMotorController.INSTANCE.startController();
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		AndyMotorController.INSTANCE.stopController();
	}

	private void showHelp() {
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage(R.string.help_message).setCancelable(false)
				.setPositiveButton("OK", null);
		AlertDialog alert = alt_bld.create();
		// Title for AlertDialog
		alert.setTitle("Quick Hint!");
		// Icon for AlertDialog
		alert.setIcon(R.drawable.ic_launcher);
		alert.show();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		AndyFace.random();

		if (receiveTouch) {
			return gDetector.onTouchEvent(event);
		} else {
			return false;
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {

		int velX = processFling(velocityX, screenWidth);
		int velY = processFling(velocityY, screenHeight);

		double a = Math.atan2(velX, velY);
		a = snapAngle(Math.toDegrees(a));

		final double angle = Math.toRadians(a);

		updateArrow(angle);
		showArrow();

		h.post(new Runnable() {

			@Override
			public void run() {
				receiveTouch = false;
				int[] ws = getWheelSpeeds(255, angle);

				// System.out.println(ws[0] + ", " + ws[1]);

				AndyMotorController.INSTANCE.andyMove(ws[0], ws[1]);
				AndyMotorController.INSTANCE.andyStopAfter(1000);
				hideArrow();
				receiveTouch = true;
				AndyFace.smile();
			}
		});
		return true;
	}

	private double snapAngle(double d) {
		if (d < -155) {
			return -180;
		}

		if (d < -115) {
			return -135;
		}

		if (d < -70) {
			return -90;
		}

		if (d < -20) {
			return -45;
		}

		if (d > 155) {
			return 180;
		}

		if (d > 115) {
			return 135;
		}

		if (d > 70) {
			return 90;
		}

		if (d > 20) {
			return 45;
		}

		return 0;
	}

	private void updateArrow(double radians) {
		int length = screenHeight / 3;

		PointF p = new PointF();
		float dx = (float) (length * Math.sin(radians));
		float dy = (float) (length * Math.cos(radians));

		p.x = center.x + dx;
		p.y = center.y + dy;

		arrow.setEnd(p);
		arrow.invalidate();
	}

	private int[] getWheelSpeeds(int maxSpeed, double radians) {
		int velX = (int) (maxSpeed * Math.sin(radians));
		int velY = (int) (maxSpeed * Math.cos(radians));

		// Log.i(TAG, "Vxy: " + velX + ", " + velY);
		int vL = velY + velX;
		int vR = velY - velX;

		return scaleUpper(vL, vR);
	}

	private final int sign(int x) {
		return (x >= 0) ? 1 : -1;
	}

	int MAX_SPEED = 255;

	private int[] scaleUpper(int ls, int rs) {
		int sL = sign(ls);
		int sR = sign(rs);

		ls = Math.abs(ls);
		rs = Math.abs(rs);

		int max = Math.max(ls, rs);

		if (max > MAX_SPEED) {
			double k = MAX_SPEED * 1.0 / max;
			ls = (int) (ls * k);
			rs = (int) (rs * k);
		}
		return new int[] { sL * ls, sR * rs };
	}

	private int processFling(float velocity, int max) {
		int v = (int) (velocity / max);
		v = (250 * v) / 6;
		return v;
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
