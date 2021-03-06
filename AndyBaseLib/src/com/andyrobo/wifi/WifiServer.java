package com.andyrobo.wifi;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;

/**
 * 
 * The Class WifiServer allows you to send/receive data over WiFi sockets
 * The implementation is generic, which means all data types and objects can be exchanged
 * 
 * The following methods can be implemented in a sketch : <br /><br />
 * 
 * void startListening()<br />
 * void stopServer()<br />
 * void onWifiDataReceived(WifiServer wf) - the WifiServer on which the data was received, call wf.getDataObject() to get the data object<br />
 * void sendData(Object obj) - the data object to be sent to connected client<br />
 * void getDataObject()<br />
 * 
 * @author ankitdaf
 *
 */
public class WifiServer {

	/** Network socket variables. */
    private String IP = "0.0.0.0";
    private int PORT = 9090 ;
    private ServerSocket serverSocket;
    private Thread serverThread = null;
    private Socket client;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private boolean close=false;

    /**  Sketch specific variables. **/
    private static Activity parent;
    private boolean shouldStop=false;
    private Object dataObject;
    private Method onWifiDataReceivedMethod;

    /**
     * 
     * Instantiates a new Wifi Server connection
     * 
     * @param _parent the calling sketch/Activity
     * @param port the port number for the connection
     */
	public WifiServer(final Activity _parent,int port) {
		PORT = port;
		parent= _parent;
		IP = getIP();
		findParentIntentions();
		serverThread = new Thread(new ServerThread());
	}
	
	
	/**
	 * 
	 * Threading class to handle socket connections
	 * 
	 * @author ankitdaf
	 *
	 */
    public class ServerThread implements Runnable {
        public void run() {
            try {
                if (IP != null) {
                    serverSocket = new ServerSocket();
                    serverSocket.setReuseAddress(true);
                    serverSocket.bind(new InetSocketAddress(IP,PORT));
                    while (!shouldStop) {
                        // listen for incoming clients
                    	System.out.println("Waiting at IP "+IP );
                        client = serverSocket.accept();
                        close=false;
                        System.out.println("Accepted from" + client.getInetAddress());
                        try {
                        		oos = new ObjectOutputStream(client.getOutputStream());
                        		oos.flush();
                        		ois = new ObjectInputStream(client.getInputStream());
                        		while(!close) {
                        			handleWirelessData();
                        		}
                        }
                        catch (EOFException e) {
                        	System.out.println("End of File encountered");
                        	e.printStackTrace();
                        	break;
                        }
                        catch (Exception e) {
                        	e.printStackTrace();
                        	break;
                        }
                        }
                } else {
                	System.out.println("No Wifi");
                }
            } catch (Exception e) {
            	System.out.println("Wifi error");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 
     * Start a Wifi Server listening for data
     * 
     */
	public void startListening() {
		serverThread.start();
	}
	
	/**
	 * 
	 * Stop the Wifi Server
	 * 
	 */
	public void stopServer() {
		try {
			shouldStop=true;
			serverSocket.close();
			System.out.println("Stopped server");
		}
		catch (IOException e) {
			System.out.println("Error closing socket");
			e.printStackTrace();
		}	
	}
	
	/**
	 * 
	 * Send data to the connected client
	 * 
	 * @param obj Data object to be sent
	 */
	public void sendData(final Object obj) {
		new Runnable() {
			
			public void run() {
				try {
					if(client.isConnected() && !client.isClosed()) {
					oos.writeObject(obj);
					}
					else {
						System.out.println("Client unavailable");
					}
					
				} catch (IOException e) {
					System.out.println("Could not send data");
					e.printStackTrace();
				}

			}
		}.run();
	}
	
	/**
	 * 
	 * Invoke the necessary methods to manipulate the received data
	 * 
	 * @param obj The data object received, typecast it correctly before use 
	 */
	private void handleWirelessData()
	{
		try {
			dataObject = ois.readObject();
			if(onWifiDataReceivedMethod != null)
			{
				onWifiDataReceivedMethod.invoke(parent, new Object[] {this});
			}
		} catch (OptionalDataException e1) {
			e1.printStackTrace();
			close=true;
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			close=true;
		} catch (IOException e1) {
			System.out.println("IO Error");
			e1.printStackTrace();
			close=true;
		} catch (Exception e1) {
			e1.printStackTrace();
			close=true;
		}
	}

	
	
	/**
	 * 
	 * Returns the PORT number of current connection
	 * 
	 * @return PORT port number of current connection
	 */
	public int getPort() {
		return PORT;
	}
	
	/**
	 * 
	 * Returns the dataObject associated with this connection
	 * 
	 * @return dataObject received dataObject
	 */
	public Object getDataObject() {
		return dataObject;
	}
	
	/**
	 * 
	 *  Find Parent Intentions
	 *  
	 */
	private void findParentIntentions(){
		try {
			onWifiDataReceivedMethod=parent.getClass().getMethod("onWifiDataReceived", new Class[] {WifiServer.class});
		}
		catch (NoSuchMethodException e) {
			System.out.println("onWifiDataReceived method not defined");
		}
	}
	
	private static String getIP() {
		String ip= "0.0.0.0";
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& ip == "0.0.0.0"
							&& inetAddress.getHostAddress().matches(
									"\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b")) {
						ip = inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		} catch (NullPointerException nx) {
			nx.printStackTrace();
		}
		return ip;
	}


	}
