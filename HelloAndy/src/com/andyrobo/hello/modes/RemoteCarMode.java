package com.andyrobo.hello.modes;

import android.app.Activity;
import android.view.ViewGroup;

import com.andyrobo.hello.R;
import com.andyrobo.hello.core.HelloAndyServer;

public class RemoteCarMode extends AbstractMode {

	static final String TAG = "RemoteCarMode";
	private final HelloAndyServer server;

	public RemoteCarMode(Activity a) {
		super(a);
		this.server = new HelloAndyServer(a);
		this.server.showEmotion = true;
	}

	@Override
	public String getName() {
		return "Andy Remote Car";
	}

	@Override
	public String getDescription() {
		return "Remote Control Andy over Wifi!";
	}

	@Override
	public int getImageResourceID() {
		return R.drawable.remote_car;
	}

	@Override
	public void start() {
		server.startServer();
	}

	@Override
	public void initView(ViewGroup rootView) {
		server.init(rootView);
	}

	@Override
	public void stop() {
		server.stopServer();
	}
}
