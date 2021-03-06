package com.andyrobo.hello.modes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.andyrobo.hello.R;

public abstract class AbstractMode implements IMode {

	protected Activity activity;

	public AbstractMode(Activity a) {
		this.activity = a;
	}
	
	public Context getContext() {
		return activity.getApplicationContext();
	}
	
	@Override
	public void handleActivityResult(int requestCode, int resultCode,
			Intent data) {
		//Do Nothing by default
	}
	
	@Override
	public String getDescription() {
		return "This is an Andy Mode. Description is not given!";
	}
	
	@Override
	public int getImageResourceID() {
		return R.drawable.ic_launcher;
	}
	
}
