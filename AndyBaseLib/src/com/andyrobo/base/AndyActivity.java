package com.andyrobo.base;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * This
 * 
 * @author abheek
 * 
 */
public abstract class AndyActivity extends Activity {

	protected static final String TAG = "AndyActivity";
	private boolean killOnDestroy = false;
	protected boolean showMenu;

	protected final void setFullScreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		// registerForContextMenu(view)
		setKillOnDestroy(false);

		setContentView(createContentView());
		initViews();
		initBackend();
	}

	protected abstract View createContentView();

	protected void initViews() {
	};

	protected void initBackend() {
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (showMenu) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.menu, menu);
			// TODO Auto-generated method stub
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, item.toString());
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (killOnDestroy) {
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	protected void setKillOnDestroy(boolean shouldKill) {
		this.killOnDestroy = shouldKill;
	}
}
