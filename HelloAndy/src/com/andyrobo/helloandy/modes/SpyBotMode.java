package com.andyrobo.helloandy.modes;

import android.app.Activity;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.andyrobo.base.utils.AndyUtils;
import com.andyrobo.helloandy.R;

public class SpyBotMode extends RemoteCarMode {
	
	private SurfaceView camView;

	public SpyBotMode(Activity a) {
		super(a);
	}
	
	@Override
	public String getName() {
		return "Andy SpyBot";
	}
	
	@Override
	public String getDescription() {
		return "Andy Remote with his Spycam!";
	}
	
	@Override
	public int getImageResourceID() {
		return R.drawable.spybot;
	}
	
	@Override
	public void initView(ViewGroup rootView) {
		super.initView(rootView);
		
		AndyUtils.getLayoutInflater(activity).inflate(R.layout.spy_overlay, rootView);
		camView = (SurfaceView) activity.findViewById(R.id.camView);
	}
	
	@Override
	public void start() {
		super.start();
		
		//AndySpyCam.SPYCAM.init(camView);
	}

}
