package com.andyrobo.base.comm;

import android.widget.TextView;

public class RobotControlThread extends UDPThread {

	public RobotControlThread(TextView status, TextView message, int port) {
		super(status, message, port);
	}

	@Override
	protected byte[] processCommand(byte[] data) {
		int x = this.ByteToInt(data[0]);
		postMessage("COMMAND: " + commandString[x] + " " + sentCount);

		/**
		 * Need to change this. The process should be dependent on the byte[]
		 * data and not be using the if - then - ladder
		 */

		data = new byte[] { data[0] };
		// if (Arrays.equals(data, CMD_FORWARD)) {
		// AudioSerial.INSTANCE.moveForward();
		// }
		// if (Arrays.equals(data, CMD_REVERSE)) {
		// AudioSerial.INSTANCE.moveReverse();
		// }
		// if (Arrays.equals(data, CMD_LEFT)) {
		// AudioSerial.INSTANCE.moveLeft();
		// }
		// if (Arrays.equals(data, CMD_RIGHT)) {
		// AudioSerial.INSTANCE.moveRight();
		// }
		// if (Arrays.equals(data, CMD_STOP)) {
		// AudioSerial.INSTANCE.stopRobot();
		// }
		
		return data;
	}
}