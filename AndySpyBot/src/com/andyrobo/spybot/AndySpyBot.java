package com.andyrobo.spybot;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class AndySpyBot extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_andy_spy_bot);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_andy_spy_bot, menu);
		return true;
	}

}
