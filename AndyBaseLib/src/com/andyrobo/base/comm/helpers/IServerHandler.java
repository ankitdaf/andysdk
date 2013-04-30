package com.andyrobo.base.comm.helpers;

import java.net.ServerSocket;
import java.net.Socket;

public interface IServerHandler {

	void handleException(Exception e);

	void handleStopServer();

	void handleStartServer(ServerSocket serverSocket);

	void handleConnected(Socket clientSocket);

	void handleDisconnection();

	void handleData(String dataString);

	void handleConnectionTimeout();

}
