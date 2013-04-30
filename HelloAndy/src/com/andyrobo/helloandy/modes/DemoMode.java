package com.andyrobo.helloandy.modes;

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
import com.andyrobo.helloandy.R;

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

	@Override
	public boolean onTouch(View v, MotionEvent m) {

		if (m.getAction() == MotionEvent.ACTION_UP) {
			if (b == null || b.isCompleted()) {
				runBehaviour();
			}
		} 

		return true;
	}

	private IAndyBehaviour b;

	private void runBehaviour() {
		int r = (int) (Math.random() * 10);
		//Log.i(TAG, "" + r);
		if (r > 5) {
			b = new BehaviourAngry(10, 3000);
		} else {
			b = new BehaviourScared(2000);
		}
		
		b.start();
	}
}
