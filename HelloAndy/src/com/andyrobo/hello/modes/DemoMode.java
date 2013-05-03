package com.andyrobo.hello.modes;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import com.andyrobo.base.behaviours.BehaviourAngry;
import com.andyrobo.base.behaviours.BehaviourScared;
import com.andyrobo.base.behaviours.IAndyBehaviour;
import com.andyrobo.base.graphics.AndyFace;
import com.andyrobo.base.motion.AndyMotion;
import com.andyrobo.hello.R;

public class DemoMode extends AbstractMode implements OnTouchListener {

	private static final String TAG = "AndyPokeFace";
	private ViewGroup rootView;

	public DemoMode(Activity a) {
		super(a);
	}

	@Override
	public String getName() {
		return "Andy PokeFace";
	}

	@Override
	public String getDescription() {
		return "Poke Andy's face and watch him play!";
	}

	@Override
	public int getImageResourceID() {
		return R.drawable.poke;
	}

	// private final Handler h = new Handler();

	@Override
	public void initView(ViewGroup rootView) {
		AndyFace.smile();
		this.rootView = rootView;
		rootView.setOnTouchListener(this);
	}

	boolean runMotor = false;

	@Override
	public void start() {
		Log.i(TAG, "Starting PokeFace Mode");
	}

	@Override
	public void stop() {
		Log.i(TAG, "Andy Stop");
		AndyMotion.finish();
		this.rootView.setOnTouchListener(null);
	}

	void showHelp() {
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(activity);
		alt_bld.setMessage(R.string.demo_message).setCancelable(false)
				.setPositiveButton("OK", null);
		AlertDialog alert = alt_bld.create();
		// Title for AlertDialog
		alert.setTitle("Quick Hint!");
		// Icon for AlertDialog
		alert.setIcon(R.drawable.ic_launcher);
		alert.show();
	}

	float lastX = 0;
	float lastY = 0;

	@Override
	public boolean onTouch(View v, MotionEvent m) {

		if (m.getAction() == MotionEvent.ACTION_UP) {
			if (b == null || b.isCompleted()) {
				float x = m.getX();
				float y = m.getY();

				double d = getDistance(x, y);
				runBehaviour(d);
			}
		}

		return true;
	}

	private final double getDistance(float x, float y) {
		double d = Math.sqrt(Math.pow(x - lastX, 2) + Math.pow(y - lastY, 2));
		lastX = x;
		lastY = y;
		return d;
	}

	private IAndyBehaviour b;

	private void runBehaviour(double d) {
		Log.i("Distance", d + "");

		if (d < 120) {
			// Touching the same point -- angry
			b = new BehaviourAngry(10, 3000);
		} else {
			b = new BehaviourScared(1000);
		}

		b.start();
	}
}
