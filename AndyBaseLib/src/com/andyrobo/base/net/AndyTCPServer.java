package com.andyrobo.base.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import android.util.Log;

import com.andyrobo.base.net.helpers.IServerHandler;

public class AndyTCPServer {

	private static final String TAG = "AndyTCPServer";
	private static final int TIMEOUT = 10000; // set a time out for 10s

	private ServerSocket serverSocket;
	private Socket clientSocket;
	private boolean shouldRun;
	private IServerHandler h;

	public AndyTCPServer(int port, IServerHandler handler) {
		this.h = handler;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			serverSocket.setSoTimeout(TIMEOUT);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		h.handleStartServer(serverSocket);
	}

	public void startServer() {
		this.shouldRun = true;
		new Thread(serverThread).start();
	}

	public void stopServer() {
		this.shouldRun = false;
		try {
			if (serverSocket != null)
				serverSocket.close();
			if (clientSocket != null)
				clientSocket.close();
			
			h.handleStopServer();
		} catch (IOException e) {
			e.printStackTrace();
			h.handleException(e);
		}
	}

	private final Runnable serverThread = new Runnable() {

		@Override
		public void run() {
			// connection loop
			while (shouldRun) {
				connect_receive();
			}
		}
	};

	protected void connect_receive() {
		try {
			clientSocket = serverSocket.accept();
			Log.i(TAG, "Client connected " + clientSocket.getInetAddress());
			h.handleConnected(clientSocket);

			// open up IO streams
			BufferedReader in = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream());

			// waits for data and reads it in until connection dies
			// readLine() blocks until the server receives a new line from
			// client
			String s;
			while ((s = in.readLine()) != null) {
				h.handleData(s);
				System.out.println("Client Says: " + s);
				out.println(s);
				out.flush();
			}

			// close IO streams, then socket
			Log.e(TAG, "Closing connection with client");
			h.handleDisconnection();
			out.close();
			in.close();
			clientSocket.close();
		} catch (IOException e) {
			if (e instanceof SocketTimeoutException) {
				h.handleConnectionTimeout();
				Log.i(TAG, "Timeout");
			} else {
				// h.handleException(e);
				Log.e(TAG, "Error connecting " + e.getMessage());
				// e.printStackTrace();
			}
		}
	}
}
