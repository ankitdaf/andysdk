package com.andyrobo.helloandy.modes;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.andyrobo.base.graphics.AndyFace;
import com.andyrobo.base.net.AndyNet;
import com.andyrobo.base.net.AndyTCPServer;
import com.andyrobo.base.net.AndyWifiManager;
import com.andyrobo.base.net.helpers.IServerHandler;
import com.andyrobo.base.utils.AndyUtils;
import com.andyrobo.base.utils.RandomMessage;
import com.andyrobo.helloandy.AndyMaster;
import com.andyrobo.helloandy.R;

public class RemoteCarMode extends AbstractMode implements IServerHandler {

	private static final String TAG = "RemoteCarMode";
	private Handler h = new Handler();

	private TextView tvStatus;

	private AndyTCPServer server;
	private ServerSocket ss;

	public RemoteCarMode(Activity a) {
		super(a);
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
		server = new AndyTCPServer(9000, this);
		if (server != null) {
			server.startServer();
		}
	}

	private void startBroadcast() {
		new Thread() {
			public void run() {
				
				//start broadcasting my address
				InetAddress bAddr = AndyNet.getBroadcastIP(activity);
				Log.i(TAG, "Broadcast Address: " + bAddr.getHostAddress());
				if(bAddr != null) {
					AndyNet.startBroadcast(AndyMaster.getName(), 0, bAddr);
				}
				
			};
		}.start();		
	}
	
	private void stopBroadcast() {
		AndyNet.stopBroadcast();
	}

	@Override
	public void initView(ViewGroup rootView) {
		AndyFace.smile();
		AndyUtils.getLayoutInflater(activity).inflate(R.layout.connection_overlay,
				rootView);

		tvStatus = (TextView) activity.findViewById(R.id.tvIPConnect);
		tvStatus.setBackgroundResource(R.drawable.back);

		// ProgressBar pbConnect = (ProgressBar) activity
		// .findViewById(R.id.pbConnect);
		// pbConnect.setVisibility(View.INVISIBLE);
	}

	@Override
	public void stop() {
		if (server != null) {
			server.stopServer();
		}
		stopBroadcast();
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

	@Override
	public void handleStopServer() {
	}

	@Override
	public void handleStartServer(ServerSocket serverSocket) {
		this.ss = serverSocket;
		setStatus("Server Started\nWaiting for Client on\n"
				+ AndyWifiManager.getIP(activity) + " port "
				+ serverSocket.getLocalPort());
		startBroadcast();
	}

	@Override
	public void handleException(Exception e) {
		Log.e(TAG, "Server Exception: " + e.getMessage());
		RemoteCarMode.this.stop();
	}

	@Override
	public void handleData(String dataString) {
		setStatus("Client Says " + dataString);
	}

	@Override
	public void handleConnected(Socket clientSocket) {
		AndyFace.laugh();
		setStatus("Client connected from " + clientSocket.getInetAddress());
		stopBroadcast();
	}

	public void handleDisconnection() {
		AndyFace.smile();
		setStatus("Client Disconnected\nWaiting for new client on\n"
				+ AndyWifiManager.getIP(activity) + " port "
				+ ss.getLocalPort());
		startBroadcast();
	};

	public void handleConnectionTimeout() {
		AndyFace.scared();
		showToast(
				RandomMessage
						.getRandomMessage(RandomMessage.CONNECTION_MESSAGES),
				Toast.LENGTH_SHORT);
	};

}
