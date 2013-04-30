package com.andyrobo.base.graphics;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.andyrobo.base.R;

public class AndyFace {

	public static final int RANDOM = -1;
	public static final int SMILE = 0;
	public static final int LAUGH = 1;
	public static final int ANGRY = 2;
	public static final int CONFUSED = 3;
	public static final int SCARED = 4;

	static final int[] faceDrawables = { R.drawable.smile, R.drawable.laugh,
			R.drawable.angry, R.drawable.confused, R.drawable.scared };

	private static ImageView img;
	private static Activity activity;

	private static final Handler h = new Handler();

	public static FrameLayout init(Activity a, boolean fullScreen) {
		activity = a;
		FrameLayout faceLayout = new FrameLayout(a);
		faceLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		faceLayout.setKeepScreenOn(true);

		if (fullScreen) {

			a.requestWindowFeature(Window.FEATURE_NO_TITLE);
			a.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		img = new ImageView(a.getApplicationContext());
		img.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		smile();

		faceLayout.addView(img, 0);
		return faceLayout;
	}

	public static ImageView getFaceLayer() {
		return img;
	}

	public static void smile() {
		showFaceDrawable(R.drawable.smile);
	}

	public static void angry() {
		showFaceDrawable(R.drawable.angry);
	}

	public static void confused() {
		showFaceDrawable(R.drawable.confused);
	}

	public static void laugh() {
		showFaceDrawable(R.drawable.laugh);
	}

	public static void scared() {
		showFaceDrawable(R.drawable.scared);
	}

	private static int oldF = 0;

	public static void random(boolean alwaysNew) {
		int n = faceDrawables.length - 1;
		int f = (int) (n * Math.random() + 1);
		
		f = getValidIndex(f);
		
		if ((f == oldF) && alwaysNew) {
			random(alwaysNew);
		} else {
			showFaceDrawable(faceDrawables[f]);
			oldF = f;
		}
	}
	
	private static int getValidIndex(int f) {
		int n = faceDrawables.length - 1;
		if (f > n) {
			f = n;
		}
		if (f < 0) {
			f = 0;
		}
		return f;
	}

	private static Drawable getFaceDrawable(int drawableID) {
		if (activity == null) {
			throw new NullPointerException("AndyFace not initialized");
		}
		//Log.i("AndyFace", "Face: " + drawableID);
		return activity.getResources().getDrawable(drawableID);
	}
	
	private static final void showFaceDrawable(final int drawableID) {
		h.post(new Runnable() {
			@Override
			public void run() {
				img.setBackgroundDrawable(getFaceDrawable(drawableID));
			}
		});
	}

	public static void showFace(final int faceID) {
		if (faceID == RANDOM) {
			random(true);
		} else {
			int f = getValidIndex(faceID);
			showFaceDrawable(faceDrawables[f]);
		}
	}
}
