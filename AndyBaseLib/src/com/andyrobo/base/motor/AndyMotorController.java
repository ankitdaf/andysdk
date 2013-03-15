package com.andyrobo.base.motor;

import java.util.Arrays;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

public class AndyMotorController {

	private static final String TAG = "AndyMotorController";

	public static final AndyMotorController INSTANCE = new AndyMotorController();

	private static final long SEND_DELAY = 100;
	private static final int SKIP_MAX = (int) (500 / SEND_DELAY);
	private byte[] sendBytes = null; // new byte[] { 0x0A };
	private static int PADDING_BYTES;

	private boolean shouldRun;
	private boolean send;
	private int skips = 0;

	private int currentVolume;
	private Thread mct;

	private AndyMotorController() {
		// for singleton
		AudioSerialSingleTrack.activate();
	}

	public void startController() {
		if (mct != null && mct.isAlive()) {
			stopController();
		}

		mct = new Thread(new Runnable() {
			@Override
			public void run() {
				shouldRun = true;
				while (shouldRun) {
					if (send || skips >= SKIP_MAX) {
						// System.out.println("Sending..");
						if (sendBytes != null) {
							AudioSerialSingleTrack.output(sendBytes);
							//showBytes(sendBytes);
							skips = 0;
							send = false;
						}
					} else {
						// System.out.println("Skips: " + skips);
						skips = skips + 1;
					}
					try {
						Thread.sleep(SEND_DELAY);
					} catch (InterruptedException e) {
						break;
					}
				}
				shouldRun = false;
			}
		});

		mct.start();
	}

	protected void showBytes(byte[] b) {
		String s = "";
		for (int i = 0; i < b.length; i++) {
			s = s + (b[i] & 0xff) + " ";
		}
		System.out.println(s);
	}

	public void stopController() {
		if (mct != null) {
			shouldRun = false;
			mct.interrupt();
			mct = null;
		}
	}

	public void andyStopAfter(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		andyStop();
	}

	public void andyStop() {
		andyMove(0, 0);
	}

	public void andyMove(int leftSpeed, int rightSpeed) {
		byte b = 0;
		if (leftSpeed > 0) {
			b = (byte) (b | 1);
		}
		if (rightSpeed > 0) {
			b = (byte) (b | 2);
		}

		move(b, leftSpeed, rightSpeed);
	}

	public void andyMove(int leftSpeed, int rightSpeed, int leftDirection,
			int rightDirection) {
		
		byte b = 0;
		if (leftDirection > 0) {
			b = (byte) (b | 1);
		}
		if (rightDirection > 0) {
			b = (byte) (b | 2);
		}
		
		move(b, leftSpeed, rightSpeed);
	}
	
	private void move(byte b, int leftSpeed, int rightSpeed) {
		byte[] sendBytes = new byte[PADDING_BYTES + 5];

		int ls = Math.abs(leftSpeed);
		int rs = Math.abs(rightSpeed);

		sendBytes[PADDING_BYTES] = (byte) 107;
		sendBytes[PADDING_BYTES + 1] = b;
		sendBytes[PADDING_BYTES + 2] = (byte)getByte(ls);
		sendBytes[PADDING_BYTES + 3] = (byte)getByte(rs);
		sendBytes[PADDING_BYTES + 4] = (byte) 108;

		//showBytes(sendBytes);

		if (isNewStream(sendBytes)) {
			this.send = true;
			this.sendBytes = sendBytes.clone();
		}
		else {
			this.send = false;
			this.sendBytes = null;
		}
	}
	
	public int[] getWheelSpeeds(int maxSpeed, double radians) {
		int velX = (int) (maxSpeed * Math.sin(radians));
		int velY = (int) (maxSpeed * Math.cos(radians));

		// Log.i(TAG, "Vxy: " + velX + ", " + velY);
		int vL = velY + velX;
		int vR = velY - velX;

		return scaleUpper(vL, vR);
	}

	private final int sign(int x) {
		return (x >= 0) ? 1 : -1;
	}

	final int MAX_SPEED = 255;

	private int[] scaleUpper(int ls, int rs) {
		int sL = sign(ls);
		int sR = sign(rs);

		ls = Math.abs(ls);
		rs = Math.abs(rs);

		int max = Math.max(ls, rs);

		if (max > MAX_SPEED) {
			double k = MAX_SPEED * 1.0 / max;
			ls = (int) (ls * k);
			rs = (int) (rs * k);
		}
		return new int[] { sL * ls, sR * rs };
	}

	private byte getByte(int i) {
		return (byte)i;
	}

	public void andyMove(int linearVelocity, float angularVelocity) {
		if (isNewStream(sendBytes)) {
			this.send = true;
			this.sendBytes = sendBytes.clone();
		}
	}

	public void setSendBytes(byte[] sendBytes) {
		if (isNewStream(sendBytes)) {
			this.send = true;
			this.sendBytes = sendBytes.clone();
		}
	}

	private boolean isNewStream(byte[] newValues) {
		if (Arrays.equals(newValues, sendBytes)) {
			return false;
		}
		return true;
	}

	public void terminate() {
		stopController();
		AudioSerialSingleTrack.deactivate();
	}

	public void setAudioParams(int baudRate, int characterSpacing,
			boolean flip, int padding) {

		AudioSerialSingleTrack.new_baudRate = baudRate;
		AudioSerialSingleTrack.new_characterdelay = characterSpacing;
		AudioSerialSingleTrack.new_levelflip = flip;
		AudioSerialSingleTrack.UpdateParameters(true);
		PADDING_BYTES = padding;

		Log.i(TAG, "New characterdelay: " + characterSpacing);
		Log.i(TAG, "New levelflip: " + flip);
		Log.i(TAG, "New padding: " + padding);
	}

	public void setVolume(boolean shouldSetMax, Context context) {
		if (shouldSetMax) {
			setMaxVolume(context);
		} else {
			setPreviousVolume(context);
		}
	}

	public void setMaxVolume(Context context) {
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
				AudioManager.FLAG_SHOW_UI);
	}

	public void setPreviousVolume(Context context) {
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume,
				AudioManager.FLAG_SHOW_UI);
	}

}
