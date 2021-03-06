package com.andyrobo.base.motion;

import java.util.Arrays;
import java.util.LinkedList;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.SystemClock;

class AudioSerial {

	private static AudioTrack audiotrk = null;

	// set that can be edited externally
	public static int max_sampleRate = 48000;
	public static int min_sampleRate = 4000;
	public static int new_baudRate = 4800; // assumes N,8,1 right now
	public static int new_sampleRate = 48000; // min 4000 max 48000
	public static int new_characterdelay = 0; // in audio frames, so depends on
												// the sample rate. Useful to
												// work with some
												// microcontrollers.
	public static boolean new_levelflip = false;
	public static String prefix = "";
	public static String postfix = "";

	// set that is actually used: this is so they get upadted all in one go
	// (safer)
	private static int baudRate = 4800;
	private static int sampleRate = 48000;
	private static int characterdelay = 0;
	private static boolean levelflip = false;

	public static LinkedList<byte[]> playque = new LinkedList<byte[]>();
	public static boolean active = false;

	public static void UpdateParameters(boolean AutoSampleRate) {
		baudRate = new_baudRate; // we're not forcing standard baud rates here
									// specifically because we want to allow odd
									// ones
		if (AutoSampleRate == true) {
			new_sampleRate = new_baudRate;
			while (new_sampleRate <= (max_sampleRate)) {
				new_sampleRate *= 2;// += new_baudRate;
			}
			new_sampleRate /= 2;
		}

		if (new_sampleRate > max_sampleRate)
			new_sampleRate = max_sampleRate;
		if (new_sampleRate < min_sampleRate)
			new_sampleRate = min_sampleRate;

		sampleRate = new_sampleRate; // min 4000 max 48000
		if (new_characterdelay < 0)
			new_characterdelay = 0;
		characterdelay = new_characterdelay;
		levelflip = new_levelflip;
		minbufsize = AudioTrack.getMinBufferSize(sampleRate,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_8BIT);

		if (audiotrk == null) {
			audiotrk = getTrack();
		}
	}

	private static AudioTrack getTrack() {
		return new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_8BIT, minbufsize,
				AudioTrack.MODE_STATIC);
	}

	public static void output(String sendthis) {
		if (sendthis != null) {
			output((prefix + sendthis + postfix).getBytes());
		}
	}

	public static void output(byte[] sendthis) {
		if (sendthis != null) {
			byte[] data = SerialDAC(sendthis);
			playSound(data);
		}
	}

	private static byte jitter = (byte) (4);
	private static byte logichigh = (byte) (-128);
	private static byte logiclow = (byte) (16);

	private static int bytesinframe = 10 + characterdelay;
	private static int i = 0; // counter
	private static int j = 0; // counter
	private static int k = 0; // counter
	private static int m = 0; // counter
	private static int n = sampleRate / baudRate;
	private static byte l = jitter; // intentional jitter used to prevent the
									// DAC from flattening the waveform
									// prematurely

	public static byte[] SerialDAC(byte[] sendme) {
		if (levelflip) {
			logiclow = (byte) (-128);
			logichigh = (byte) (16);
			jitter = (byte) (4);
		} else {
			logichigh = (byte) (-128);
			logiclow = (byte) (16);
			jitter = (byte) (4);
		}

		bytesinframe = 10 + characterdelay;
		i = 0; // counter
		j = 0; // counter
		k = 0; // counter
		m = 0; // counter
		n = sampleRate / baudRate;
		boolean[] bits = new boolean[sendme.length * bytesinframe];
		byte[] waveform = new byte[(sendme.length * bytesinframe * sampleRate / baudRate)]; // 8
																							// bit,
																							// no
																							// parity,
																							// 1
																							// stop
		// Arrays.fill(waveform, (byte) 0);
		Arrays.fill(bits, true); // slight opti to decide what to do with stop
									// bits

		// generate bit array first: makes it easier to understand what's going
		// on
		for (i = 0; i < sendme.length; ++i) {
			m = i * bytesinframe;
			bits[m] = false;
			bits[++m] = ((sendme[i] & 1) == 1);// ?false:true;
			bits[++m] = ((sendme[i] & 2) == 2);// ?false:true;
			bits[++m] = ((sendme[i] & 4) == 4);// ?false:true;
			bits[++m] = ((sendme[i] & 8) == 8);// ?false:true;
			bits[++m] = ((sendme[i] & 16) == 16);// ?false:true;
			bits[++m] = ((sendme[i] & 32) == 32);// ?false:true;
			bits[++m] = ((sendme[i] & 64) == 64);// ?false:true;
			bits[++m] = ((sendme[i] & 128) == 128);// ?false:true;
			// cheaper to prefill to true
			// now we need a stop bit, BUT we want to be able to add more
			// (character delay) to play-nice with some microcontrollers such as
			// the Picaxe or BS1 that need it in order to do decimal conversion
			// natively.
			// for(k=0;k<bytesinframe-9;k++)
			// bits[++m]=true;
		}

		// now generate the actual waveform using l to wiggle the DAC and
		// prevent it from zeroing out
		for (i = 0; i < bits.length; i++) {
			for (k = 0; k < n; k++) {
				waveform[j++] = (bits[i]) ? ((byte) (logichigh + l))
						: ((byte) (logiclow - l));
				l = (l == (byte) 0) ? jitter : (byte) 0;
			}
		}

		bits = null;
		return waveform;
	}

	// essentially a constructor, but i prefer to do a manual call.
	public static void activate() {
		UpdateParameters(true);
	}

	public static void deactivate() {
		if (audiotrk != null) {
			if (isAudioTrackInitialized()) {
				// audiotrk.stop(); this is probably causing the problem with
				// illegalstateexception
				audiotrk.pause();
				audiotrk.flush();
			}
			audiotrk.release();
		}
	}

	// public static void stop(PApplet parent) {
	public static void stop() {
		if (audiotrk != null) {
			if (isAudioTrackInitialized()) {
				audiotrk.stop();
			}
		}
	}

	private static int minbufsize;
	private static int length;

	private static void playSound(byte[] theSound) {
		if (audiotrk != null && isAudioTrackInitialized()) {
			if (theSound != null) {
				while (audiotrk.getPlaybackHeadPosition() < (theSound.length))
					SystemClock.sleep(20); // let existing sample finish first:
											// this can probably be set to a
											// smarter number using the
											// information above
			}
			if (isAudioTrackInitialized()) {
				// anything else will fail in obtaining the audiobuffer
				audiotrk.pause();
				audiotrk.reloadStaticData();
				audiotrk.flush();
			}
		}
		length = theSound.length;
		if (minbufsize < length)
			minbufsize = length;

		audiotrk.setStereoVolume(1, 1);
		// System.out.println(length);
		audiotrk.write(theSound, 0, length);

		if (isAudioTrackInitialized()) {
			audiotrk.play();
		}
	}

	private static boolean isAudioTrackInitialized() {
		return (audiotrk.getState() == AudioTrack.STATE_INITIALIZED);
	}
}
