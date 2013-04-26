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
	
	public static final byte RANDOM = 0x05;
	public static final byte SMILE = 0x0a;
	public static final byte LAUGH = 0x0b;
	public static final byte ANGRY = 0x0c;
	public static final byte CONFUSED = 0x0d;
	public static final byte SCARED = 0x0e;

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
		showFace(R.drawable.smile);
	}

	public static void angry() {
		showFace(R.drawable.angry);
	}

	public static void confused() {
		showFace(R.drawable.confused);
	}

	public static void laugh() {
		showFace(R.drawable.laugh);
	}

	public static void scared() {
		showFace(R.drawable.scared);
	}
	
	static final int[] faces = {
			R.drawable.smile,
			R.drawable.laugh,
			R.drawable.angry,
			R.drawable.confused,
			R.drawable.scared
	};
	
	public static void random() {
		int n = faces.length - 1;
		int f = (int)(n * Math.random() + 1);
		
		if(f > n) {
			f = n;
		}
		if(f < 0) {
			f = 0;
		}
		
		showFace(faces[f]);
	}

	public static Drawable getFace(int faceID) {
		if (activity == null) {
			throw new NullPointerException("AndyFace not initialized");
		}
		return activity.getResources().getDrawable(faceID);
	}

	private static void showFace(final int faceID) {
		h.post(new Runnable() {
			@Override
			public void run() {
				img.setBackgroundDrawable(getFace(faceID));
			}
		});
	}

	// public BitmapDrawable face;
	//
	// public static
	//
	// public boolean andyNormal(Context context, RelativeLayout rl) {
	// face = (BitmapDrawable) context.getResources().getDrawable(
	// R.drawable.smile);
	// showFace(face, context, rl);
	// return true;
	// }
	//
	// public boolean andySmile(Context context, RelativeLayout rl) {
	// face = (BitmapDrawable) context.getResources().getDrawable(
	// R.drawable.laugh);
	// showFace(face, context, rl);
	// return true;
	// }
	//
	// public boolean andyAngry(Context context, RelativeLayout rl) {
	// face = (BitmapDrawable) context.getResources().getDrawable(
	// R.drawable.angry);
	// showFace(face, context, rl);
	// return true;
	// }
	//
	// public boolean andyConfused(Context context, RelativeLayout rl) {
	// face = (BitmapDrawable) context.getResources().getDrawable(
	// R.drawable.confused);
	// showFace(face, context, rl);
	// return true;
	// }
	//
	// public boolean andyScared(Context context, RelativeLayout rl) {
	// face = (BitmapDrawable) context.getResources().getDrawable(
	// R.drawable.scared);
	// showFace(face, context, rl);
	// return true;
	// }
	//
	// private final Handler faceHandler = new Handler();
	//
	// private void showFace(final BitmapDrawable andyFace, final Context
	// context, final RelativeLayout rl) {
	//
	// faceHandler.post(new Runnable() {
	//
	// public void run() {
	// rl.setBackgroundDrawable(andyFace);
	// }
	// });
	//
	// }

}
