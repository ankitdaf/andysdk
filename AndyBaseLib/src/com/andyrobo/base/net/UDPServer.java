package com.andyrobo.base.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Locale;

public class UDPServer {

	static final String TAG = "Andy UDPServer";

	private int PORT = AndyNet.TCP_PORT;
	private int dataSize = 8;
	private int timeOut = 7000;
	private int toCount = 0;
	public int ROUNDS_TIMEOUT = 3;

	boolean shouldRun;

	private InetAddress lockedClientAddress = null;
	private boolean echo = false;

	protected DatagramSocket serverSocket;

	public UDPServer(int PORT, int dataSize) {
		this.PORT = PORT;
		this.dataSize = dataSize;
		this.shouldRun = false;
	}

	public final void setEcho(boolean echoMode) {
		this.echo = echoMode;
	}

	public final void setTimeOut(int t) {
		this.timeOut = t;
	}

	public void startServer() {
		this.shouldRun = true;
		new Thread(serverThread).start();
	}

	public void stopServer() {
		this.shouldRun = false;
		serverSocket.close();
		onServerStop();
	}

	protected void onServerStart(int port) {
	}

	protected void onServerStop() {
	}

	protected void onReceiveData(DatagramPacket rPacket, boolean newClient) {
	}

	protected byte[] getSendData(byte[] rcvData) {
		// default echo mode
		String sentence = new String(rcvData);
		return sentence.toUpperCase(Locale.ENGLISH).getBytes();
	}

	protected void onTimeout(SocketTimeoutException e) {
	}

	protected void onClientIdle() {
	}

	private final void lockClient(InetAddress inetAddress) {
		// lock the client
		this.lockedClientAddress = inetAddress;

		// start looking for timeout
	}

	private Runnable serverThread = new Runnable() {

		@Override
		public void run() {
			try {
				serverSocket = new DatagramSocket(PORT);
				serverSocket.setSoTimeout(timeOut);

				byte[] receiveData = new byte[dataSize];

				onServerStart(PORT);

				while (shouldRun) {
					DatagramPacket receivePacket = new DatagramPacket(
							receiveData, receiveData.length);

					try {

						serverSocket.receive(receivePacket);

					} catch (SocketTimeoutException e) {
						onTimeout(e);

						toCount++;
						if (toCount > ROUNDS_TIMEOUT
								&& lockedClientAddress != null) {
							// open to anyone to connect
							lockedClientAddress = null;
							onClientIdle();
							toCount = 0;
						}

						continue;
					}

					toCount = 0;

					InetAddress senderAddress = receivePacket.getAddress();

					if (lockedClientAddress != null) {
						if (senderAddress.equals(lockedClientAddress)) {
							onReceiveData(receivePacket, false);
						}
					} else {
						onReceiveData(receivePacket, true);
						lockClient(senderAddress);
					}

					if (echo) {
						byte[] sendData = new byte[dataSize];

						int senderPort = receivePacket.getPort();
						sendData = getSendData(receivePacket.getData());

						DatagramPacket sendPacket = new DatagramPacket(
								sendData, sendData.length, senderAddress,
								senderPort);
						serverSocket.send(sendPacket);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
}
