package com.andyrobo.hello.modes;

import android.content.Intent;
import android.view.ViewGroup;

public interface IMode {

	void start();
	void initView(ViewGroup rootView);
	void stop();
	void handleActivityResult(int requestCode, int resultCode, Intent data);
	String getName();
	String getDescription();
	int getImageResourceID();

}
