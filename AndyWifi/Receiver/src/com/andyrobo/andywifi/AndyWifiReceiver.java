package com.andyrobo.andywifi;

import android.app.AlertDialog;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import com.andyrobo.base.AndyActivity;
import com.andyrobo.base.graphics.AndyFace;
import com.andyrobo.base.motor.AndyMotorController;
import com.andyrobo.wifi.WifiServer;

public class AndyWifiReceiver extends AndyActivity {
	
	private WifiServer wf;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_hello_andy, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_help:
			showHelp();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void setContentView() {
		FrameLayout face = AndyFace.init(this, true);
		//face.addView(arrow, 1);
		showHelp();
		setContentView(face);
		wf = new WifiServer(this, 12012);
		wf.startListening();
	}

	@Override
	protected void onStart() {
		AndyMotorController.INSTANCE.startController();
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		AndyMotorController.INSTANCE.stopController();
	}

	private void showHelp() {
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage(R.string.help_message).setCancelable(false)
		.setPositiveButton("OK", null);
		AlertDialog alert = alt_bld.create();
		// Title for AlertDialog
		alert.setTitle("Quick Hint!");
		// Icon for AlertDialog
		alert.setIcon(R.drawable.ic_launcher);
		alert.show();
	}


	/**
	 * 
	 * An easy callback to handled received wifi data
	 * 
	 * @param wf the WifiServer object that received the data
	 */
	public void onWifiDataReceived(WifiServer wf) {
		AndyFace.random();
		int[] ws = (int[]) wf.getDataObject();
		AndyMotorController.INSTANCE.andyMove(ws[0],ws[1]);
		AndyMotorController.INSTANCE.andyStopAfter(1000);
		AndyFace.smile();
	}

}
