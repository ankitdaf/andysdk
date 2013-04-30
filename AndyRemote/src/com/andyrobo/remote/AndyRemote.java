package com.andyrobo.remote;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.andy.remote.R;
import com.andyrobo.base.AndyActivity;
import com.andyrobo.base.net.AndyNet;
import com.andyrobo.base.utils.AndyUtils;

public class AndyRemote extends AndyActivity {

	static final String robotIPKey = "com.andy.remote.robotIP";
	private static final String TAG = "AndyRemote";
	private final Handler h = new Handler();
	private List<DatagramPacket> foundPackets;

	private final OnTouchListener jsListener = new OnTouchListener() {

		public boolean onTouch(View v, MotionEvent event) {
			Log.d(TAG, event + ", " + v);
			return true;
		}
	};
	private TextView message;
	private TextView searchingView;

	@Override
	protected View createContentView() {
		ViewGroup root = (ViewGroup) AndyUtils.getLayoutInflater(this).inflate(
				R.layout.activity_andy_remote, null);
		return root;
	}

	@Override
	protected void initViews() {
		searchingView = (TextView)findViewById(R.id.search);
		searchingView.setTextColor(Color.WHITE);
		searchingView.setBackgroundResource(R.drawable.bg);
		
		searchingView.setVisibility(View.INVISIBLE);
		
		message = (TextView) findViewById(R.id.messageView);

		Button buttonConnect = (Button) findViewById(R.id.btnConnect);
		buttonConnect.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				performConnect();
			}
		});

		hookListener(findViewById(R.id.jsForward));
		hookListener(findViewById(R.id.jsReverse));
		hookListener(findViewById(R.id.jsLeft));
		hookListener(findViewById(R.id.jsRight));

		setMessage("Andy Remote");
	}

	private void hookListener(View v) {
		v.setOnTouchListener(jsListener);
	}

	protected void performConnect() {
		searchingView.setVisibility(View.VISIBLE);
		
		Thread t = new Thread(connectionThread);
		t.start();
	}

	public void showAndyList() {
		h.post(new Runnable() {

			public void run() {
				searchingView.setVisibility(View.INVISIBLE);
				showAndyListDialog();
			}
		});
	}
	
	public void showNotFound() {
		h.post(new Runnable() {
			public void run() {
				searchingView.setVisibility(View.INVISIBLE);
				Toast.makeText(AndyRemote.this, "No Andy's found", Toast.LENGTH_LONG).show();
			}
		});
	}

	protected void showAndyListDialog() {
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
		mBuilder.setTitle("Andys in Range");

		ListView andyListView = new ListView(this);
		andyListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> v, View arg1, int position,
					long arg3) {
				// Log.i(TAG, "Click Pos: " + position);
				connectRobot(position);
			}
		});
		ArrayAdapter<String> andyListAdapter = new ArrayAdapter<String>(this,
				R.layout.list_style, andyList);
		andyListView.setAdapter(andyListAdapter);
		mBuilder.setView(andyListView);
		mBuilder.setCancelable(true);
		AlertDialog andyListDialog = mBuilder.create();
		andyListDialog.show();
	}

	protected void connectRobot(int position) {
		if (foundPackets == null || foundPackets.isEmpty())
			return;

		DatagramPacket p = foundPackets.get(position);
		p.getAddress();
	}

	private void setMessage(final String msg) {
		h.post(new Runnable() {

			public void run() {
				if (message != null) {
					message.setText(msg);
				}
			}
		});
	}

	private final List<String> andyList = new ArrayList<String>();
	private Runnable connectionThread = new Runnable() {

		private static final long TIME_OUT = 10000;

		public void run() {
			andyList.clear();
			InetAddress bAddr = AndyNet.getBroadcastIP(AndyRemote.this);
			if (bAddr != null) {
				foundPackets = AndyNet.receiveBroadcast(bAddr, TIME_OUT, false);
				if (foundPackets == null || foundPackets.isEmpty()) {
					showNotFound();
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
}
