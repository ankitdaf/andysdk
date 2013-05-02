package com.andyrobo.base.motion;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

public class AndyMotion {

	public static final String TAG = "AndyMotion";
	public static final String PREFIX = "M";

	static final int MIN_SPEED = 60;
	static final int MAX_SPEED = 255;

	private static int PADDING_BYTES = 0;

	private static boolean initialized = false;
	private static int currentVolume;
	private static boolean stopAudio = false;

	private static Thread audioThread;
	private static byte[] motionBytes = null;

	public static final void init() {
		initialized = true;
		AudioSerial.activate();

		audioThread = new Thread() {
			@Override
			public void run() {
				stopAudio = false;
				while (true) {
					try {
						Thread.sleep(1000);
						sendAudio();
					} catch (InterruptedException e) {
						// Log.i(TAG, "audioThread interrupted");
						if (stopAudio) {
							break;
						}
						sendAudio();
					}
				}
				initialized = false;
			}
		};
		audioThread.start();
	}

	private static final void sendAudio() {
		if (motionBytes != null) {
			// Log.i(TAG, "Sending audio to motors");
			for (int i = 0; i < 3; i++) {
				AudioSerial.output(motionBytes);
			}
			if (isZeroSpeed(motionBytes)) {
				motionBytes = null;
			}
		}
	}

	private static boolean isZeroSpeed(byte[] data) {
		return (data[PADDING_BYTES + 2] == 0) && (data[PADDING_BYTES + 3] == 0);
	}

	public static final void finish() {
		setRawSpeeds(0, 0);
		initialized = false;
		stopAudio = true;
		audioThread.interrupt();

		// TODO: if we deactivate, we cannot activate again unless the app is
		// restarted!
		// AudioSerialSingleTrack.deactivate();
	}

	private static final byte getByte(int i) {
		return (byte) i;
	}

	private static final void setBytes(byte b, int leftSpeed, int rightSpeed) {
		if (!initialized) {
			init();
			Log.w(TAG,
					"AndyMotion was not initialized. No worries I have done it for you!");
		}

		byte[] sendBytes = new byte[PADDING_BYTES + 5];

		int ls = Math.abs(leftSpeed);
		int rs = Math.abs(rightSpeed);

		sendBytes[PADDING_BYTES] = (byte) 107;
		sendBytes[PADDING_BYTES + 1] = b;
		sendBytes[PADDING_BYTES + 2] = (byte) getByte(ls);
		sendBytes[PADDING_BYTES + 3] = (byte) getByte(rs);
		sendBytes[PADDING_BYTES + 4] = (byte) 108;

		motionBytes = sendBytes.clone();
		audioThread.interrupt();
	}

	/**
	 * Set Raw Values from -255 to +255
	 * 
	 * @param leftSpeed
	 * @param rightSpeed
	 */

	public static void setRawSpeeds(int leftSpeed, int rightSpeed) {
		byte b = 0;
		if (leftSpeed > 0) {
			b = (byte) (b | 1);
		}
		if (rightSpeed > 0) {
			b = (byte) (b | 2);
		}

		if (!block)
			setBytes(b, leftSpeed, rightSpeed);
	}

	/**
	 * Set Raw Values from -255 to +255 and stop afterwards
	 * 
	 * @param leftSpeed
	 * @param RightSpeed
	 * @param millis
	 *            time to wait after stopping
	 */

	public static void setRawSpeeds(int leftSpeed, int rightSpeed, long millis) {
		setRawSpeeds(leftSpeed, rightSpeed);
		if (millis > 0) {
			stop(millis);
		}
	}

	static boolean block = false;

	private static void stop(final long millis) {
		if (millis > 0) {
			block = true;
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
			}
			block = false;
			setRawSpeeds(0, 0);
		} else {
			setRawSpeeds(0, 0);
		}
	}

	public static final int MOVE_STOP = 0;
	public static final int MOVE_FORWARD = 1;
	public static final int MOVE_BACK = 2;
	public static final int MOVE_LEFT = 3;
	public static final int MOVE_RIGHT = 4;

	public static void move(int moveID, long millis) {
		switch (moveID) {
		case MOVE_STOP:
			setRawSpeeds(0, 0);
			break;
		case MOVE_FORWARD:
			setRawSpeeds(MAX_SPEED, MAX_SPEED, millis);
			break;
		case MOVE_BACK:
			setRawSpeeds(-MAX_SPEED, -MAX_SPEED, millis);
			break;
		case MOVE_LEFT:
			setRawSpeeds(MAX_SPEED, -MAX_SPEED, millis);
			break;
		case MOVE_RIGHT:
			setRawSpeeds(-MAX_SPEED, MAX_SPEED, millis);
			break;
		default:
			setRawSpeeds(0, 0);
			break;
		}
	}

	/*
	 * Additional Helper Functions
	 */

	public static final void setAudioParams(int baudRate, int characterSpacing,
			boolean flip, int padding) {

		AudioSerial.new_baudRate = baudRate;
		AudioSerial.new_characterdelay = characterSpacing;
		AudioSerial.new_levelflip = flip;
		AudioSerial.UpdateParameters(true);
		PADDING_BYTES = padding;

		Log.i(TAG, "New characterdelay: " + characterSpacing);
		Log.i(TAG, "New levelflip: " + flip);
		Log.i(TAG, "New padding: " + padding);
	}

	public static void setMaxVolume(Context context) {
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
				AudioManager.FLAG_SHOW_UI);
	}

	public static void setPreviousVolume(Context context) {
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume,
				AudioManager.FLAG_SHOW_UI);
	}

}
