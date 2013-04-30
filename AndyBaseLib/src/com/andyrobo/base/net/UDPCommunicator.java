package com.andyrobo.base.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

import android.util.Log;

public class UDPCommunicator {

	protected static final String TAG = "UDPCommunicator";

	private DatagramSocket serverSocket = null;
	private final int SERVER_PORT;
	private byte[] prevData = new byte[] { (byte) 0xff };
	protected int sentCount = 0;
	private int COMMAND_REPEAT_LIMIT = 2;

	private ICommandProcessor processor;

	public UDPCommunicator(int datagramPort, ICommandProcessor p) {
		this.SERVER_PORT = datagramPort;
		this.processor = p;
	}

	public void start() {
		Thread t = new Thread(commThread);
		t.start();
	}

	public void stop() {
		runThread = false;
		if (serverSocket != null) {
			serverSocket.close();
		}
	}

	private boolean runThread = false;

	private final Runnable commThread = new Runnable() {
		public void run() {
			try {
				runThread = true;
				processor.init();
				serverSocket = new DatagramSocket(SERVER_PORT);
				startReceptionLoop();
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, e.getMessage());
			}
		};
	};

	private void startReceptionLoop() {
		byte[] receiveData = new byte[8];
		DatagramPacket packet = new DatagramPacket(receiveData,
				receiveData.length);
		runThread = true;
		while (runThread) {
			try {
				serverSocket.receive(packet);
				byte[] data = packet.getData();
				if (data != null) {
					checkAndProcessReceivedData(data);
				}
				// serverSocket.send(packet);
				Thread.sleep(10);
			} catch (IOException ex) {
				Log.e(TAG, ex.getMessage());
			} catch (InterruptedException ie) {
				runThread = false;
			}

		}
		serverSocket.close();
	}

	private void checkAndProcessReceivedData(final byte[] data) {
		Thread t = new Thread() {
			public void run() {
				try {
					checkRepeat(data);
				} catch (Exception e) {
					Log.e("Parse Error", "parsing error");
				}
			};
		};
		t.start();
	}

	private void checkRepeat(byte[] data) {
		// System.out.println("============");
		// System.out.println("Previous Data: " + prevData + ", " +
		// prevData.length + ", " + prevData[0]);
		// System.out.println("Received Data: " + data + ", " + data.length +
		// ", " + data[0]);
		// System.out.println();

		if (Arrays.equals(data, prevData)) {
			if (sentCount < COMMAND_REPEAT_LIMIT) {
				processData(data);
				sentCount++;
			} else {
			}
		} else {
			sentCount = 0;
			processData(data);
		}

		prevData = data.clone();
	}

	private void processData(byte[] data) {
		// System.out.println("Lengths: " + data.length + ", " + prevData.length
		// + " | " + sentCount);
		processor.processCommand(data);
	}
}