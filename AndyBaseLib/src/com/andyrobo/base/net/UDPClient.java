package com.andyrobo.base.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

public class UDPClient {

	protected static final long LOOP_DELAY = 100;
	protected InetAddress address;
	protected int port;
	protected int dataSize;
	protected DatagramSocket clientSocket;

	private byte[] theData;
	private Timer timer;

	public UDPClient(int port, int dataSize) {
		this.port = port;
		this.dataSize = dataSize;
	}

	public void connect(InetAddress addr) throws SocketException {
		this.address = addr;
		this.clientSocket = new DatagramSocket();
		clientSocket.connect(addr, port);

		sendData(AndyNet.CONNECTION_REQUEST);

		if (clientSocket.isConnected()) {
			startSendLoop();
			onConnect(addr.getHostAddress());
		}
	}

	public void disconnect() {
		sendData(AndyNet.DISCONNECTION_REQUEST);
		clientSocket.disconnect();

		// TODO: on_ok
		clientSocket.close();
		stopSendLoop();
		onDisconnect();
	}

	public boolean isConnected() {
		if (clientSocket == null) {
			return false;
		}
		return clientSocket.isConnected();
	}

	public void sendData(byte[] data) {
		theData = data;
	}

	public void onConnect(String ip) {

	}

	public void onDisconnect() {

	}

	private final void doSendData() {
		if (theData != null) {
			DatagramPacket sendPacket = new DatagramPacket(theData,
					theData.length);
			try {
				if (clientSocket != null) {
					clientSocket.send(sendPacket);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private final void startSendLoop() {
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				doSendData();
			}
		}, 0, LOOP_DELAY);
	}
	
	private final void stopSendLoop() {
		if(timer != null) {
			timer.cancel();
		}
	}

}
