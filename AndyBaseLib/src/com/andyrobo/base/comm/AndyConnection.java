package com.andyrobo.base.comm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class AndyConnection {
	private static String myIP = null;
	private static String deviceName = null;
	private static int DISCOVERY_PORT;
	public static boolean isDiscoveryPortInUse;
	public static boolean isAttemptPortInUse;
	private static int ATTEMPT_PORT;
	private static int TIMEOUT_MS;
	private static int TCP_SERVER_PORT;
	private static WifiManager wManager;
	private static InetAddress broadcastInet;
	private boolean abort;
	private static boolean WIFI_STATE;
	private String connectionPassword;

	public int getPort(String portName) {
		if (portName.equalsIgnoreCase("DISCOVERY_PORT")) {
			return DISCOVERY_PORT;
		}
		if (portName.equalsIgnoreCase("ATTEMPT_PORT")) {
			return ATTEMPT_PORT;
		}
		return 0;
	}

	public void setConnectionPassword(String password){
		connectionPassword = password;
	}
	
	public void setTimeout(int milliseconds) {
		TIMEOUT_MS = milliseconds;
	}

	public void setDeviceName(String devicename) {
		deviceName = devicename;
	}

	public String getBroadcastAddressString() {
		return broadcastInet.getHostAddress();
	}

	public InetAddress getbroadcastAddressInet() {
		return broadcastInet;
	}

	public boolean setNetworkAttributes(Context context) {
		if (wManager == null)
			try {
				wManager = (WifiManager) context
						.getSystemService(Context.WIFI_SERVICE);

			} catch (Exception exception) {
				exception.printStackTrace();
				return false;
			}
		WifiInfo myWifiInfo = wManager.getConnectionInfo();
		String ipAddress = android.text.format.Formatter
				.formatIpAddress(myWifiInfo.getIpAddress());
		myIP = ipAddress;
		System.out.println("My IP is: " + myIP);
		deviceName = Build.MODEL;
		System.out.println("My Device Name is: " + deviceName);
		DhcpInfo dhcp = wManager.getDhcpInfo();
		if (dhcp == null) {
			System.out.println("DHCP null: Check wifi Status");
			return false;
		}
		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);

		try {
			broadcastInet = InetAddress.getByAddress(quads);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.out
					.println("Exception occured during: Setting broadcast IP");
			return false;
		}
		System.out.println("broadcast ip set to: "
				+ broadcastInet.getHostAddress());
		setTimeout(10000);
		DISCOVERY_PORT = 3030;
		ATTEMPT_PORT = 3031;
		TCP_SERVER_PORT = 21111;
		this.abort = false;
		isAttemptPortInUse=false;
		isDiscoveryPortInUse=false;
		return true;
	}

	public void setMyDeviceName(String name) {
		deviceName = name;
	}

	public String getMyDeviceName() {
		return deviceName;
	}

	public void initWifi(Context context) {
		if (wManager == null)
			try {
				wManager = (WifiManager) context
						.getSystemService(Context.WIFI_SERVICE);

			} catch (Exception exception) {
				exception.printStackTrace();
			}
	}

	public String getMyIP() {
		if (myIP != null) {
			return myIP;
		} else {
			return null;
		}
	}

	public void setMyIP() {
		if (wManager != null) {
			WifiInfo myWifiInfo = wManager.getConnectionInfo();
			String ipAddress = android.text.format.Formatter
					.formatIpAddress(myWifiInfo.getIpAddress());
			myIP = ipAddress;
		}
	}

	public boolean announceMyPresence(int timeout) {
		isDiscoveryPortInUse= true;
		DatagramSocket socket = socketCreation(true, DISCOVERY_PORT, timeout);
		byte[] data = getMyDeviceName().getBytes();
		DatagramPacket packet = getDatagramPacket(data,
				getbroadcastAddressInet(), DISCOVERY_PORT);
		long wait = System.currentTimeMillis() + timeout;
		while (System.currentTimeMillis() < wait) {
			try {
				sendUDPData(socket, packet);
				Thread.sleep(100);
			} catch (Exception IOEXception) {
				IOEXception.printStackTrace();
				return false;
			}
		}
		socket.close();
		isDiscoveryPortInUse= false;
		return true;
	}

//	public boolean getConnectionRequest(boolean isPasswordRequired){
//		isDiscoveryPortInUse= true;
//		DatagramSocket socket = socketCreation(true, DISCOVERY_PORT, 0);
//		byte[] data = getMyDeviceName().getBytes();
//		DatagramPacket packet = getDatagramPacket(data,
//				getbroadcastAddressInet(), DISCOVERY_PORT);
//	
//		while (true) {
//			try {
//				sendUDPData(socket, packet);
//				Thread.sleep(100);
//			} catch (Exception IOEXception) {
//				IOEXception.printStackTrace();
//				return false;
//			}
//		}
//		//socket.close();
//		isDiscoveryPortInUse= false;
//		return true;
//		
//	}
	
	public DatagramPacket getDatagramPacket(int buffersize) {
		byte[] buf = new byte[1024];
		return new DatagramPacket(buf, buf.length);
	}

	public DatagramPacket getDatagramPacket(byte[] data, InetAddress inet,
			int port) {
		return new DatagramPacket(data, data.length, inet, port);
	}

	public DatagramSocket socketCreation(boolean isBroadcast, int port,
			int timeout) {
		try {
			DatagramSocket socket = new DatagramSocket(port);
			socket.setBroadcast(isBroadcast);
			socket.setSoTimeout(timeout);
			return socket;
		} catch (SocketException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean sendUDPData(byte[] data, int port, int timeOut,
			InetAddress inet, boolean isBroadcast) {
		DatagramSocket socket = socketCreation(isBroadcast, port, timeOut);
		try {
			DatagramPacket packet = new DatagramPacket(data, data.length, inet,
					port);
			socket.send(packet);
			System.out.println("Data sent: " + packet.toString());
		} catch (SocketException e) {
			e.printStackTrace();
			return false;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			socket.close();
		}
		return true;
	}

	public boolean sendUDPData(DatagramSocket socket, DatagramPacket packet) {
		try {
			socket.send(packet);
			System.out.println("Data sent: " + packet.toString());
		} catch (SocketException e) {
			e.printStackTrace();
			return false;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public DatagramPacket recieveUDPPacket(DatagramSocket socket,
			DatagramPacket recievePacket) {
		while (true) {
			try {
				System.out.println("waiting for data");
				socket.receive(recievePacket);
				System.out.println(recievePacket.getData().toString());
				return recievePacket;
			} catch (SocketException e) {
				e.printStackTrace();
				return null;
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
				System.out.println("Time out");
				// socket.close();
			}
		}
	}

	public byte[] recieveUDPByte(DatagramSocket socket,
			DatagramPacket recievePacket) {
		while (true) {
			try {
				socket.receive(recievePacket);
				return recievePacket.getData();
			} catch (SocketException e) {
				e.printStackTrace();
				return null;
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
				System.out.println("Recieved data");
				// socket.close();
			}
		}
	}

	public boolean wifiStatus() {
		if (wManager != null) {
			WIFI_STATE = wManager.isWifiEnabled();
			return WIFI_STATE;
		}
		return false;
	}

	public void setPreviousWifiState() {
		wifiEnable(WIFI_STATE);
	}

	private boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

	public boolean hasActiveInternetConnection(Context context) {
		if (isNetworkAvailable(context)) {
			try {
				HttpURLConnection urlc = (HttpURLConnection) (new URL(
						"http://www.google.com").openConnection());
				urlc.setRequestProperty("User-Agent", "Test");
				urlc.setRequestProperty("Connection", "close");
				urlc.setConnectTimeout(1500);
				urlc.connect();
				return (urlc.getResponseCode() == 200);
			} catch (IOException e) {
				Toast.makeText(context, "Error checking internet connection",
						500);
			}
		} else {
			Toast.makeText(context, "No network Available", 500);
		}
		return false;
	}

	public void wifiEnable(Context context) {
		initWifi(context);
		if (wifiStatus()) {
			System.out.println("Wifi ON");
		} else {

			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						wifiEnable(true);
						System.out.println("Wifi ON");
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						System.out.println("NO permission");
						break;
					}
				}
			};
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage("Turn ON Wifi?")
					.setPositiveButton("Yes", dialogClickListener)
					.setNegativeButton("No", dialogClickListener).show();
		}
	}

	public void wifiEnable(boolean status) {
		wManager.setWifiEnabled(status);
	}

	public List<DatagramPacket> andysFound(int waitTime, int port) {
		byte[] buf = new byte[1024];

		List<DatagramPacket> datagrampacket = new ArrayList<DatagramPacket>();
		DatagramSocket socket = socketCreation(true, port, TIMEOUT_MS);
		DatagramPacket data = new DatagramPacket(buf, buf.length);

		long wait = System.currentTimeMillis() + waitTime;
		while (System.currentTimeMillis() < wait) {
			DatagramPacket temp = recieveUDPPacket(socket, data);
			if (temp != null) {
				datagrampacket.add(temp);
				socket.close();
				break;
			}
		}
		return datagrampacket;
	}

	public byte[] recieveUDPByte(DatagramSocket socket,
			DatagramPacket recievePacket, InetAddress filterIP) {
		while (true) {
			try {
				socket.receive(recievePacket);
				if (recievePacket.getAddress().equals(filterIP)) {
					return recievePacket.getData();
				}
			} catch (SocketException e) {
				e.printStackTrace();
				return null;
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public DatagramPacket recieveUDPPacket(DatagramSocket socket,
			DatagramPacket recievePacket, InetAddress filterIP) {
		while (true) {
			try {
				socket.receive(recievePacket);
				if (recievePacket.getAddress().equals(filterIP)) {
					return recievePacket;
				}
			} catch (SocketException e) {
				e.printStackTrace();
				return null;
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public List<DatagramPacket> recieveUDPPackets(int duration,
			InetAddress filterIP) {
		byte[] buf = new byte[1024];
		List<DatagramPacket> datagrampacket = new ArrayList<DatagramPacket>();
		DatagramSocket socket = socketCreation(true, 3030, TIMEOUT_MS);
		DatagramPacket data = new DatagramPacket(buf, buf.length); // getDatagramPacket(1024);

		long wait = System.currentTimeMillis() + duration;
		while (System.currentTimeMillis() < wait) {
			DatagramPacket temp = recieveUDPPacket(socket, data, filterIP);
			if (temp != null) {
				datagrampacket.add(temp);
			}
		}
		socket.close();
		return datagrampacket;
	}

	public void sendTCPData(String outMsg) {
		Socket s;
		try {
			s = new Socket("192.168.1.17", TCP_SERVER_PORT);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					s.getOutputStream()));

			System.out.println("Trying to send...");

			while (!this.abort) {
				System.out.println("sent msg: " + outMsg);
				out.write(outMsg);
				out.flush();
				Log.i("TcpClient", "sent: " + outMsg);
			}
			System.out.println("Aborted");
			this.abort = false;
			s.close();
		} catch (UnknownHostException e) {
			System.out.println("Unknown host");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void recieveTCPData() {
		ServerSocket ss = null;
		String data;
		try {
			ss = new ServerSocket(TCP_SERVER_PORT);
			ss.setSoTimeout(30000);

			System.out.println("Waiting for connection..");
			Socket s = ss.accept();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					s.getInputStream()));

			while (!this.abort && (data = in.readLine()) != null) {
				Log.i("TcpServer", "received: " + data);
				System.out.println("received: " + data);
			}

			System.out.println("Aborted");
			this.abort = false;
			s.close();
		} catch (InterruptedIOException e) {
			// if timeout occurs
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void setAbort(boolean flag) {
		System.out.println("Abort pressed");
		this.abort = flag;
	}

	public boolean attemptToConnectAndy(InetAddress inet, int waittime,
			String data) {
		isAttemptPortInUse= true;
		DatagramSocket socket = socketCreation(false, ATTEMPT_PORT,
				waittime/10);
		DatagramPacket sendpacket = getDatagramPacket(data.getBytes(), inet,
				ATTEMPT_PORT);
		DatagramPacket recievepacket = getDatagramPacket(1024);
		long time = System.currentTimeMillis() + waittime;
		while (System.currentTimeMillis() < time) {
			sendUDPData(socket, sendpacket);
			if (recieveUDPPacket(socket, recievepacket, inet) != null) {
				System.out.println("Got ACK");
				socket.close();
				return true;
			} else {
				System.out.println("No ACK..time out");
			}
		}
		socket.close();
		isAttemptPortInUse= false;
		return false;
	}

}
