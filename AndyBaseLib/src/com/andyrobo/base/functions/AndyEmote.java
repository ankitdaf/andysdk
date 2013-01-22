package com.andyrobo.base.functions;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.widget.RelativeLayout;

import com.andyrobo.base.R;

public class AndyEmote {
	
	public BitmapDrawable face;
	
	public AndyEmote() {
		face = null;
	}

	public boolean andyNormal(Context context, RelativeLayout rl) {
		face = (BitmapDrawable) context.getResources().getDrawable(
				R.drawable.smile);
		showFace(face, context, rl);
		return true;
	}

	public boolean andySmile(Context context, RelativeLayout rl) {
		face = (BitmapDrawable) context.getResources().getDrawable(
				R.drawable.laugh);
		showFace(face, context, rl);
		return true;
	}

	public boolean andyAngry(Context context, RelativeLayout rl) {
		face = (BitmapDrawable) context.getResources().getDrawable(
				R.drawable.angry);
		showFace(face, context, rl);
		return true;
	}

	public boolean andyConfused(Context context, RelativeLayout rl) {
		face = (BitmapDrawable) context.getResources().getDrawable(
				R.drawable.confused);
		showFace(face, context, rl);
		return true;
	}

	public boolean andyScared(Context context, RelativeLayout rl) {
		face = (BitmapDrawable) context.getResources().getDrawable(
				R.drawable.scared);
		showFace(face, context, rl);
		return true;
	}
	
	private final Handler faceHandler = new Handler();

	private void showFace(final BitmapDrawable andyFace, final Context context, final RelativeLayout rl) {
		
		faceHandler.post(new Runnable() {

			public void run() {
				rl.setBackgroundDrawable(andyFace);
			}
		});
		
	}
	
	

}
