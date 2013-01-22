package com.andyrobo.base.functions;

import android.app.Activity;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andyrobo.base.R;

public class SplashActivity extends Activity {

	RelativeLayout rl;
	int transitionTime;
	TextView tv1, tv2, tv3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		tv1 = (TextView) findViewById(R.id.textView1);
		tv2 = (TextView) findViewById(R.id.textView2);
		tv3 = (TextView) findViewById(R.id.textView3);
		
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
		

		rl = (RelativeLayout) findViewById(R.id.relativeLay);
		transitionTime = 3000;

		TransitionDrawable transition = (TransitionDrawable) rl.getBackground();
		transition.startTransition(transitionTime);

//		Handler handler = new Handler();
//
//		// run a thread after 6 seconds to start the home screen
//		handler.postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//
//				// make sure we close the splash screen so the user won't come
//				// back when it presses back key
//
//				finish();
//				// start the home screen
//
//				Intent intent = new Intent(SplashActivity.this,
//						AndyActivity.class);
//				SplashActivity.this.startActivity(intent);
//			}
//
//		}, 3500);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.activity_splash, menu);
		return true;
	}

}
