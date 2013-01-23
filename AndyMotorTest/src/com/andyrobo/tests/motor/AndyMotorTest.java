package com.andyrobo.tests.motor;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.andyrobo.base.AndyActivity;
import com.andyrobo.base.motor.AndyMotorController;

public class AndyMotorTest extends AndyActivity {

	private EditText leftSpeed;
	private EditText rightSpeed;
	private ToggleButton btnController;
	private Button btnSetSpeed;

	@Override
	protected void setContentView() {
		setContentView(R.layout.motor_test);

		leftSpeed = (EditText) findViewById(R.id.etLeftSpeed);
		rightSpeed = (EditText) findViewById(R.id.etRightSpeed);
		
		btnSetSpeed  = (Button) findViewById(R.id.btnSetSpeed);
		btnController = (ToggleButton) findViewById(R.id.toggleRunController);
		
		btnController.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean b) {
				handleController(b);
			}
		});
		
		
		btnSetSpeed.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				setSpeeds();
			}
		});
		
		//set initial speeds
		leftSpeed.setText(255 + "");
		rightSpeed.setText(255 + "");
	}

	protected void setSpeeds() {
		int _leftSpeed = getSpeed(leftSpeed);
		int _rightSpeed = getSpeed(rightSpeed);
		
		AndyMotorController.INSTANCE.andyMove(_leftSpeed, _rightSpeed);
	}

	private int getSpeed(EditText text) {
		try {
			return Integer.parseInt(text.toString());
		} catch (Exception e) {
			return 0;
		}
	}

	protected void handleController(boolean startController) {
		if(startController) {
			AndyMotorController.INSTANCE.startController();
		} else {
			AndyMotorController.INSTANCE.stopController();
		}
	}
	
	@Override
	protected void onDestroy() {
		AndyMotorController.INSTANCE.terminate();
		super.onDestroy();
	}	

}
