package com.andyrobo.spybot;

import android.view.Menu;
import android.view.SurfaceView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.andyrobo.base.AndyActivity;
import com.andyrobo.base.functions.AndySpyCam;
import com.andyrobo.base.utils.AndyUtils;

public class AndySpyBot extends AndyActivity {

	private EditText ipAddress;
	private final AndySpyCam cam = AndySpyCam.SPYCAM;

	protected void setContentView() {	
		setContentView(R.layout.activity_andy_spy_bot);

		ipAddress = (EditText) findViewById(R.id.etSendIP);
		SurfaceView sView = (SurfaceView) findViewById(R.id.surfaceView);

		ipAddress.setText(AndyUtils.getWifiAddress(this));
		cam.init(sView);

		ToggleButton spyToggle = (ToggleButton) findViewById(R.id.tbtnSpyMode);
		spyToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				handleSpyToggle(isChecked);
			}
		});
	}
	
	protected void handleSpyToggle(boolean isChecked) {
		if (isChecked) {
			if (cam.setReceiverIP(getIP())) {
				cam.startSpyMode();
			} else {
				System.out.println("Cannot set recevier IP to " + getIP());
			}
		} else {
			cam.stopSpyMode();
		}
	}

	private String getIP() {
		return ipAddress.getText().toString();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_andy_spy_bot, menu);
		return super.onCreateOptionsMenu(menu);
	}
}
