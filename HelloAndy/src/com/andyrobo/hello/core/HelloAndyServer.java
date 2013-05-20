package com.andyrobo.hello.core;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andyrobo.base.graphics.AndyFace;
import com.andyrobo.base.motion.AndyMotion;
import com.andyrobo.base.net.AndyNet;
import com.andyrobo.base.net.AndyWifiManager;
import com.andyrobo.base.net.UDPServer;
import com.andyrobo.base.utils.AndyUtils;
import com.andyrobo.base.utils.RandomMessage;
import com.andyrobo.hello.AndyMaster;
import com.andyrobo.hello.R;

public class HelloAndyServer extends UDPServer {

	private Activity activity;
	private boolean broadcasting;
	public boolean showEmotion = true;

	private ImageView redLED;
	private ImageView greenLED;
	private TextView tvStatus;

	private Handler h = new Handler();

	static final String TAG = "HelloAndyServer";

	public HelloAndyServer(Activity a) {
		super(AndyNet.TCP_PORT, 8);
		this.activity = a;
		this.setTimeOut(2000);
		this.ROUNDS_TIMEOUT = 2;
	}

	public void init(ViewGroup root) {
		AndyFace.smile();
		AndyUtils.getLayoutInflater(activity).inflate(
				R.layout.connection_overlay, root);

		tvStatus = (TextView) activity.findViewById(R.id.tvIPConnect);
		tvStatus.setBackgroundResource(R.drawable.back);

		LayoutInflater.from(activity).inflate(R.layout.net_layout, root);
		redLED = (ImageView) activity.findViewById(R.id.redled);
		greenLED = (ImageView) activity.findViewById(R.id.greenled);

		updateLED(false);
	}

	@Override
	protected void onServerStart(int port) {
		AndyFace.smile();
		setStatus("Server Active\nWaiting for new client on\n"
				+ AndyWifiManager.getIP(activity) + " port " + AndyNet.TCP_PORT);
		startBroadcast();
	}

	@Override
	protected void onServerStop() {
		stopBroadcast();
	}

	@Override
	protected void onReceiveData(DatagramPacket rPacket, boolean newClient) {
		if (newClient) {
			AndyFace.laugh();
			onNewConnection(rPacket);
		}

		stopBroadcast();

		String sentence = new String(rPacket.getData(), 0, rPacket.getLength());
		setStatus(sentence);

		handleData(sentence);
	}

	protected void onNewConnection(DatagramPacket rPacket) {

	}

	protected void handleData(String d) {
		process(d);
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

	int toCount = 0;

	@Override
	protected void onTimeout(SocketTimeoutException e) {
		if (showEmotion && toCount==5) {
			AndyFace.random(true);
			showToast(
					RandomMessage
							.getRandomMessage(RandomMessage.CONNECTION_MESSAGES),
					Toast.LENGTH_SHORT);
		}

		if (toCount > 10) {
			super.onTimeout(e);
			toCount = 0;
		}
		// TODO: Important. Need to stop the robot on timeout
		// handleAndyMove(AndyMotion.MOVE_STOP);
		toCount++;
	}

	@Override
	protected void onClientIdle() {
		onServerStart(0);
	}

	private void startBroadcast() {
		broadcasting = true;
		new Thread() {
			public void run() {

				// start broadcasting my address
				InetAddress bAddr = AndyNet.getBroadcastIP(activity);
				Log.i(TAG, "Broadcast Address: " + bAddr.getHostAddress());
				if (bAddr != null) {
					AndyNet.startBroadcast(AndyMaster.getName(), 0, bAddr);
				}

			};
		}.start();

		updateLED(broadcasting);
	}

	private void stopBroadcast() {
		if (broadcasting) {
			AndyNet.stopBroadcast();
			broadcasting = false;
		}

		updateLED(broadcasting);
	}

	private final void updateLED(final boolean broadcasting) {
		h.post(new Runnable() {

			@Override
			public void run() {
				if (!broadcasting) {
					redLED.setVisibility(View.VISIBLE);
					greenLED.setVisibility(View.INVISIBLE);
				} else {
					redLED.setVisibility(View.INVISIBLE);
					greenLED.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	public void setStatus(final String status) {
		if (h != null && tvStatus != null) {
			h.post(new Runnable() {

				@Override
				public void run() {
					tvStatus.setText(status);
				}
			});
		}
	}

	public void showToast(final String message, final int duration) {
		if (h != null) {
			h.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(activity, message, duration).show();

				}
			});
		}
	}
}
