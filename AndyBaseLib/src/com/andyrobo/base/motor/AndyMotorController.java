package com.andyrobo.base.motor;

import java.util.Arrays;

import android.content.Context;
import android.media.AudioManager;

public class AndyMotorController implements Runnable {
	public static final AndyMotorController INSTANCE = new AndyMotorController();
	private static final long SEND_DELAY = 100;
	private static final int SKIP_MAX = (int) (1000 / SEND_DELAY);
	private byte[] sendBytes = new byte[] { 0x0A };
	private static int PADDING_BYTES;

	private boolean shouldRun;
	private boolean send;
	private int skips = 0;

	private int currentVolume;

	@Override
	public void run() {
		AudioSerialSingleTrack.activate();
		shouldRun = true;
		while (shouldRun) {
			if (send || skips >= SKIP_MAX) {
				System.out.println("Sending..");
				AudioSerialSingleTrack.output(this.sendBytes);
				skips = 0;
				this.send = false;
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

	public void andyMove(int leftSpeed, int rightSpeed) {
		byte[] sendBytes = new byte[PADDING_BYTES + 5];
		byte b = 0;
		if (leftSpeed > 0) {
			b = (byte) (b | 1);
		}
		if (rightSpeed > 0) {
			b = (byte) (b | 2);
		}
		if (PADDING_BYTES > 0) {
			for (int i = 0; i < PADDING_BYTES; i++) {
				sendBytes[i] = (byte) 254;
			}
		}
		sendBytes[PADDING_BYTES] = (byte) 107;
		sendBytes[PADDING_BYTES + 1] = b;
		sendBytes[PADDING_BYTES + 2] = (byte) Math.abs(leftSpeed);
		sendBytes[PADDING_BYTES + 3] = (byte) Math.abs(rightSpeed);
		sendBytes[PADDING_BYTES + 4] = (byte) 108;

		if (isNewStream(sendBytes)) {
			this.send = true;
			this.sendBytes = sendBytes.clone();
		}
	}

	public void andyMove(int leftSpeed, int rightSpeed, int leftDirection,
			int rightDirection) {
		byte[] sendBytes = new byte[PADDING_BYTES + 5];
		byte b = 0;
		if (leftDirection > 0) {
			b = (byte) (b | 1);
		}
		if (rightDirection > 0) {
			b = (byte) (b | 2);
		}
		if (PADDING_BYTES > 0) {
			for (int i = 0; i < PADDING_BYTES; i++) {
				sendBytes[i] = (byte) 254;
			}
		}
		sendBytes[PADDING_BYTES] = (byte) 107;
		sendBytes[PADDING_BYTES + 1] = b;
		sendBytes[PADDING_BYTES + 2] = (byte) Math.abs(leftSpeed);
		sendBytes[PADDING_BYTES + 3] = (byte) Math.abs(rightSpeed);
		sendBytes[PADDING_BYTES + 4] = (byte) 108;

		if (isNewStream(sendBytes)) {
			this.send = true;
			this.sendBytes = sendBytes.clone();
		}
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
		// System.out.println("New Data");
		return true;
	}

	public void terminate() {
		deactivateAudio();
	}

	public void setAudioParams(int baudRate, int characterSpacing,
			boolean flip, int padding) {

		AudioSerialSingleTrack.new_baudRate = baudRate;
		AudioSerialSingleTrack.new_characterdelay = characterSpacing;
		AudioSerialSingleTrack.new_levelflip = flip;
		AudioSerialSingleTrack.UpdateParameters(true);
		PADDING_BYTES = padding;

		System.out.println("New characterdelay: " + characterSpacing);
		System.out.println("New levelflip: " + flip);
		System.out.println("New padding: " + padding);
	}

	private void deactivateAudio() {
		AudioSerialSingleTrack.deactivate();
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
