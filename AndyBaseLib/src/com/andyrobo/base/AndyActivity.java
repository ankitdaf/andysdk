package com.andyrobo.base;

import android.app.Activity;

public abstract class AndyActivity extends Activity {

	@Override
	protected void onDestroy() {
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}
