package com.andyrobo.comm;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.widget.TextView;

public class TCPThread extends MessagingThread {

	public TCPThread(TextView status, TextView message) {
		super(status, message);
	}

	public static final int SERVER_PORT = 9876;

	public void run() {
		try {
			ServerSocket socket = new ServerSocket(SERVER_PORT);
			Socket client = socket.accept();
			
			if(client.isConnected()) {
				postStatus("Client Connected. Communication initiated");
				doCommunication(client);				
			}
			
			
		} catch (Exception e) {
			postStatus("Error in comm");
		}
	}

	private void doCommunication(Socket client) throws IOException {
		InputStream is = client.getInputStream();
		byte[] data = new byte[8];
		
		while(!client.isClosed()) {
			is.read(data);
			postMessage(new Object().toString());
		}
		
	}

	@Override
	public void terminate() {
		
	}

}
