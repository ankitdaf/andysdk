package com.andyrobo.remote;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.andy.remote.R;
import com.andyrobo.base.AndyActivity;
import com.andyrobo.base.graphics.AndyFace;
import com.andyrobo.base.motion.AndyMotion;
import com.andyrobo.base.net.AndyNet;
import com.andyrobo.base.net.AndyWifiManager;
import com.andyrobo.base.utils.AndyUtils;

public class AndyRemote extends AndyActivity {

	static final String robotIPKey = "com.andy.remote.robotIP";
	private static final String TAG = "AndyRemote";
	private final Handler h = new Handler();

	private TextView searchingView;
	private CheckBox cbSearch;
	private Button buttonConnect;
	private View LED;
	private View faceButtons;

	private RemoteClient client;
	private ImageView camView;
	private CamThread camThread;

	@Override
	protected View createContentView() {
		ViewGroup root = (ViewGroup) AndyUtils.getLayoutInflater(this).inflate(
				R.layout.activity_andy_remote, null);
		return root;
	}
	
	private void toggleVisibility(View v) {
		if(v.getVisibility() == View.VISIBLE) {
			v.setVisibility(View.INVISIBLE);
		} else {
			v.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	protected void initViews() {
		
		camView = (ImageView) findViewById(R.id.spyCamView);
		camView.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				toggleVisibility(faceButtons);
			}
		});
		
		searchingView = (TextView) findViewById(R.id.search);
		searchingView.setTextColor(Color.WHITE);
		searchingView.setBackgroundResource(R.drawable.bg);
		searchingView.setVisibility(View.INVISIBLE);

		LED = findViewById(R.id.led);
		LED.setBackgroundColor(Color.RED);

		cbSearch = (CheckBox) findViewById(R.id.cbSearch);
		cbSearch.setChecked(false);
		cbSearch.setTextColor(Color.BLACK);

		buttonConnect = (Button) findViewById(R.id.btnConnect);
		buttonConnect.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if (!client.isConnected()) {
					performConnect(cbSearch.isChecked());
				} else {
					performDisconnect();
				}
			}
		});
		
		hookListener(findViewById(R.id.jsForward), AndyMotion.MOVE_FORWARD);
		hookListener(findViewById(R.id.jsReverse), AndyMotion.MOVE_BACK);
		hookListener(findViewById(R.id.jsLeft), AndyMotion.MOVE_LEFT);
		hookListener(findViewById(R.id.jsRight), AndyMotion.MOVE_RIGHT);

		faceButtons = findViewById(R.id.facebuttons);
		faceButtons.setVisibility(View.INVISIBLE);

		hookFaceButton(findViewById(R.id.iangry), AndyFace.ANGRY);
		hookFaceButton(findViewById(R.id.iconfused), AndyFace.CONFUSED);
		hookFaceButton(findViewById(R.id.ismile), AndyFace.SMILE);
		hookFaceButton(findViewById(R.id.ihappy), AndyFace.LAUGH);
		hookFaceButton(findViewById(R.id.iscared), AndyFace.SCARED);
	}

	private void hookFaceButton(View v, final int andyFace) {
		v.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View arg0, MotionEvent arg1) {
				client.sendData((AndyFace.PREFIX + andyFace).getBytes());
				return true;
			}
		});
	}

	private void hookListener(View v, final int moveCmd) {
		v.setOnTouchListener(new OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent m) {
				int cmd = moveCmd;
				if(m.getAction() == MotionEvent.ACTION_UP){
					cmd = AndyMotion.MOVE_STOP;
				} 
				client.sendData((AndyMotion.PREFIX + cmd).getBytes());
				return true;
			}
		});
	}

	@Override
	protected void initBackend() {
		AndyWifiManager.activateWifi(false, this);

		
		client = new RemoteClient(AndyNet.TCP_PORT, 8) {
			@Override
			public void onConnect(String ip) {
				setTargetIP(ip);
				camThread = new CamThread(camView, h);
				new Thread(camThread).start();
				handleConnectionSuccess();
			}
			
			@Override
			public void onDisconnect() {
				camThread.stop();
				handleDisconnectionSuccess();
			}
		};
	}

	protected void performConnect(boolean shouldSearch) {
		if (shouldSearch) {
			searchingView.setVisibility(View.VISIBLE);
			Thread t = new Thread(connectionThread);
			t.start();
		} else {
			// connect using a text input
			AndyNet.openConnectDialog(this, getTargetIP(), client);
		}
	}
	
	protected void performDisconnect() {
		client.disconnect();
	}
	
	private String getTargetIP() {
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		return prefs.getString(robotIPKey, AndyWifiManager.getIP(this));
	}
	
	private void setTargetIP(String newIP) {
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor e = prefs.edit();
		e.putString(robotIPKey, newIP);
		e.commit();
	}

	protected void handleDisconnectionSuccess() {
		LED.setBackgroundColor(Color.RED);
		faceButtons.setVisibility(View.INVISIBLE);
		buttonConnect.setText("Connect");
		cbSearch.setVisibility(View.VISIBLE);
		camView.setVisibility(View.INVISIBLE);
	}

	protected void handleConnectionSuccess() {
		LED.setBackgroundColor(Color.GREEN);
		faceButtons.setVisibility(View.VISIBLE);
		buttonConnect.setText("Disconnect");
		cbSearch.setVisibility(View.INVISIBLE);
		camView.setVisibility(View.VISIBLE);
		camView.setImageDrawable(null);
	}

	public void showAndyList() {
		h.post(new Runnable() {

			public void run() {
				searchingView.setVisibility(View.INVISIBLE);
				showAndyListDialog();
			}
		});
	}

	private AlertDialog andyListDialog;

	protected void showAndyListDialog() {
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
		mBuilder.setTitle("Andys in Range");

		ListView andyListView = new ListView(this);
		andyListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> v, View view, int position,
					long id) {

				if (connectRobot(position)) {
					if (andyListDialog != null) {
						andyListDialog.dismiss();
					}
				} else {
					showToast("Failed to connect. Try again.",
							Toast.LENGTH_SHORT);
				}
			}
		});

		ArrayAdapter<String> andyListAdapter = new ArrayAdapter<String>(this,
				R.layout.list_style, andyList);
		andyListView.setAdapter(andyListAdapter);
		mBuilder.setView(andyListView);
		mBuilder.setCancelable(true);

		andyListDialog = mBuilder.create();
		andyListDialog.show();
	}

	protected void showToast(final String message, final int duration) {
		h.post(new Runnable() {
			public void run() {
				searchingView.setVisibility(View.INVISIBLE);
				Toast.makeText(AndyRemote.this, message, duration).show();
			}
		});
	}

	private List<DatagramPacket> foundPackets;
	private final List<String> andyList = new ArrayList<String>();

	protected boolean connectRobot(int position) {
		if (foundPackets == null || foundPackets.isEmpty())
			return false;

		DatagramPacket p = foundPackets.get(position);
		try {
			client.connect(p.getAddress());
		} catch (SocketException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private Runnable connectionThread = new Runnable() {

		private static final long TIME_OUT = 10000;

		public void run() {
			andyList.clear();
			InetAddress bAddr = AndyNet.getBroadcastIP(AndyRemote.this);
			if (bAddr != null) {
				foundPackets = AndyNet.receiveBroadcast(bAddr, TIME_OUT, false);
				if (foundPackets == null || foundPackets.isEmpty()) {
					showToast("No Andys Found!", Toast.LENGTH_SHORT);
				} else {
					for (int i = 0; i < foundPackets.size(); i++) {
						Log.i(TAG, "found packet");
						DatagramPacket p = foundPackets.get(i);
						String s = AndyNet.getData(p) + " ["
								+ p.getAddress().getHostAddress() + "]";
						Log.i(TAG, "Adding: " + s);
						andyList.add(s);
					}
					showAndyList();
				}
			}
		}
	};

	private void exit() {
		AndyWifiManager.rollBackWifiState(this);
		setKillOnDestroy(true);
		finish();
	}
	
	@Override
	public void onBackPressed() {
		AlertDialog.Builder exitDialog = new AlertDialog.Builder(this);
		exitDialog.setTitle("Quit?");
		exitDialog.setMessage("Really want to quit?");

		exitDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {
				exit();
			}
		});
		exitDialog.setNegativeButton("Cancel", null);
		exitDialog.show();
	}
}
