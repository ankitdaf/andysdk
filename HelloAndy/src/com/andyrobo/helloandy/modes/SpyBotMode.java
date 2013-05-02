package com.andyrobo.helloandy.modes;

import java.net.DatagramPacket;
import java.net.InetAddress;

import android.app.Activity;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andyrobo.base.functions.AndySpyCam;
import com.andyrobo.base.graphics.AndyFace;
import com.andyrobo.base.utils.AndyUtils;
import com.andyrobo.helloandy.R;
import com.andyrobo.helloandy.core.HelloAndyServer;

public class SpyBotMode extends AbstractMode {

	private SurfaceView camView;
	private TextView spyStatus;
	
	static final String TAG = "Andy SpyBot";
	private final HelloAndyServer server;

	private final AndySpyCam spyCam = AndySpyCam.SPYCAM;
	private final Handler h = new Handler();

	public SpyBotMode(Activity a) {
		super(a);
		this.server = new HelloAndyServer(a) {
			@Override
			protected void onNewConnection(DatagramPacket rPacket) {
				InetAddress ip = rPacket.getAddress();
				if (ip != null) {
					spyCam.setReceiverIP(ip);
					spyCam.startSpyMode();
					SpyBotMode.this.setStatus("Sending images to " + ip + "[" + AndySpyCam.PREVIEW_PORT + "]");
				} else {
					SpyBotMode.this.setStatus("Error setting target IP");
				}
			}
			
			@Override
			protected void onClientIdle() {
				super.onClientIdle();
				spyCam.stopSpyMode();
				SpyBotMode.this.setStatus("Client away. Waiting for next");
			}
			
		};
		this.server.showEmotion = false;
	}

	protected void setStatus(final String status) {
		h.post(new Runnable() {
			
			@Override
			public void run() {
				spyStatus.setText(status);
			}
		});
	}

	@Override
	public String getName() {
		return "Andy SpyBot";
	}

	@Override
	public String getDescription() {
		return "Andy with his SpyCam!";
	}

	@Override
	public int getImageResourceID() {
		return R.drawable.spybot;
	}

	@Override
	public void start() {
		server.startServer();
		spyCam.init(camView);
		setStatus("SpyCam Activated");
	}

	@Override
	public void initView(ViewGroup rootView) {
		server.init(rootView);

		rootView.removeView(AndyFace.getFaceLayer());
		AndyUtils.getLayoutInflater(activity).inflate(R.layout.spy_overlay,
				rootView);
		camView = (SurfaceView) activity.findViewById(R.id.camView);
		spyStatus = (TextView) activity.findViewById(R.id.spyStatus);
	}

	@Override
	public void stop() {
		server.stopServer();
	}

}
