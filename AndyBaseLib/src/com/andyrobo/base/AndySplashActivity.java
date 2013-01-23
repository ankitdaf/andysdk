package com.andyrobo.base;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

public abstract class AndySplashActivity extends Activity {

	/**
	 * On creation of this activity, we load the default Andy 
	 * Splash Screen which is the Andy Logo. There is an xml file 
	 * transition.xml in the res/layout which changes the background of
	 * the splash screen
	 * 
	 * After the splash is displayed, the main activity is launched 
	 * based on intent. The class of the main activity is to be provided
	 * by the class extending this class via the getMainActivityClass function
	 */
	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.activity_splash);
		
		TextView tv1 = (TextView) findViewById(R.id.textView1);
		TextView tv2 = (TextView) findViewById(R.id.textView2);
		TextView tv3 = (TextView) findViewById(R.id.textView3);
		
		AlphaAnimation animation1 = new AlphaAnimation(0.0f, 1.0f);
		AlphaAnimation animation2 = new AlphaAnimation(0.0f, 1.0f);
		AlphaAnimation animation3 = new AlphaAnimation(0.0f, 1.0f);
		
		animation1.setDuration(1000);
		animation2.setDuration(1000);
		animation3.setDuration(1000);
		
		animation1.setStartOffset(1000);
		animation2.setStartOffset(1500);
		animation3.setStartOffset(2000);
		
		tv1.startAnimation(animation1);
		tv2.startAnimation(animation2);
		tv3.startAnimation(animation3);
		

		RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeLay);
		int transitionTime = 3000;

		TransitionDrawable transition = (TransitionDrawable) rl.getBackground();
		transition.startTransition(transitionTime);

		Handler handler = new Handler();

		// run a thread after 6 seconds to start the home screen
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {

				// make sure we close the splash screen so the user won't come
				// back when it presses back key

				finish();
				// start the home screen

				Intent intent = new Intent(AndySplashActivity.this,
						getMainActivityClass());
				AndySplashActivity.this.startActivity(intent);
			}

		}, 3500);
	}

	/**
	 * The implementing class must provide the class of the Main Activity
	 * @return the class of the main Activity (e.g. MyMainActivity.class)
	 */
	protected abstract Class<?> getMainActivityClass();
	
	@Override
	public final void onBackPressed() {
		//Do Nothing!
		// 		android.os.Process.killProcess(android.os.Process.myPid());
	}
}
