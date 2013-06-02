package com.andyrobo.hello.modes;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.TextView;
import com.andyrobo.base.graphics.AndyFace;
import com.andyrobo.base.utils.AndyUtils;
import com.andyrobo.hello.R;
import com.andyrobo.streaming.Streamer;

public class StreamingSpyBotMode extends AbstractMode{

	private SurfaceView camView;
	private TextView spyStatus;
	private TextView tvStatus;
	static final String TAG = "ANDY StreamingSpyBot";
	
	public Streamer streamer = null;
    private Context ctx=null;

	private final Handler h = new Handler();
	
	public StreamingSpyBotMode(Activity a) {
		super(a);
		ctx=a.getApplicationContext();
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
		return "Andy Streaming SpyBot";
	}

	@Override
	public String getDescription() {
		return "Andy Streaming with his SpyCam!";
	}

	@Override
	public int getImageResourceID() {
		return R.drawable.spybot;
	}

	@Override
	public void start() {
		streamer = new Streamer(camView,ctx); // Doing it here because start() is called after initView()
		setStatus("Streaming SpyCam Activated");
		tvStatus.setText("Now streaming at\nhttp://"+streamer.getLocalIpAddress()+":8080");
		if(streamer.isBroadcasting()) updateLED(true);
	}

	
	@Override
	public void initView(ViewGroup rootView) {
		init(rootView);
		rootView.removeView(AndyFace.getFaceLayer());
		AndyUtils.getLayoutInflater(activity).inflate(R.layout.spy_overlay,
				rootView);
		camView = (SurfaceView) activity.findViewById(R.id.camView);
		spyStatus = (TextView) activity.findViewById(R.id.spyStatus);
		tvStatus = (TextView) activity.findViewById(R.id.tvIPConnect);
		tvStatus.setBackgroundResource(R.drawable.back);
	}

	@Override
	public void stop() {
		updateLED(false);
		streamer.stop();
	}

}
