package com.andyrobo.helloandy.modes;

import java.net.SocketTimeoutException;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;

import com.andyrobo.base.graphics.AndyFace;
import com.andyrobo.base.motion.AndyMotion;
import com.andyrobo.helloandy.R;
import com.andyrobo.helloandy.core.HelloAndyServer;

public class RemoteCarMode extends AbstractMode {

	static final String TAG = "RemoteCarMode";
	private final HelloAndyServer server;

	public RemoteCarMode(Activity a) {
		super(a);
		this.server = new HelloAndyServer(a) {

			int toCount = 0;

			@Override
			protected void handleData(String d) {
				process(d);
			}

			@Override
			protected void onTimeout(SocketTimeoutException e) {
				if (toCount > 10) {
					super.onTimeout(e);
					toCount = 0;
				}
				// TODO: Important. Need to stop the robot on timeout
				// handleAndyMove(AndyMotion.MOVE_STOP);
				toCount++;
			}
		};
		this.server.setTimeOut(600);
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

	private int getInt(String d) {
		return Integer.parseInt(d.substring(1, d.length()));
	}

	private void process(String d) {
		if (d.startsWith(AndyFace.PREFIX)) {
			AndyFace.showFace(getInt(d));
		}

		if (d.startsWith(AndyMotion.PREFIX)) {
			int m = getInt(d);
			handleAndyMove(m);
		}
	}

	private final void handleAndyMove(int m) {
		if (!checkCodeRepeat(m)) {
			Log.i("Moving Andy", "Code: " + m);
			AndyMotion.move(m, 0);
		}
	}

	int oldMoveCode = 0;

	private boolean checkCodeRepeat(int m) {
		if (m == oldMoveCode) {
			return true;
		} else {
			oldMoveCode = m;
		}
		return false;
	}
}
