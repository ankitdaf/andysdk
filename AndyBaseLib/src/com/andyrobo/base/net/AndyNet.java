package com.andyrobo.base.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.DhcpInfo;
import android.util.Log;

public class AndyNet {

	private static final String TAG = "AndyNet";
	public static final int BROADCAST_PORT = 3030;
	public static final int TCP_PORT = 9000;
	private static boolean doBroadcast = false;

	public static final InetAddress getBroadcastIP(Context c) {
		DhcpInfo dhcp = AndyWifiManager.getDhcpInfo(c);
		if (dhcp != null) {
			int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
			byte[] quads = new byte[4];
			for (int k = 0; k < 4; k++) {
				quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
			}

			try {
				return InetAddress.getByAddress(quads);
			} catch (UnknownHostException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		return null;
	}

	public static final boolean startBroadcast(String friendlyName,
			int timeout, InetAddress address) {
		try {
			DatagramSocket socket = new DatagramSocket(BROADCAST_PORT);
			socket.setBroadcast(true);
			byte[] data = friendlyName.getBytes();

			DatagramPacket packet = new DatagramPacket(data, data.length,
					address, BROADCAST_PORT);

			if (timeout <= 0) {
				doBroadcast = true;
			}

			long wait = System.currentTimeMillis() + timeout;
			while ((System.currentTimeMillis() < wait) || doBroadcast) {
				socket.send(packet);
				Thread.sleep(1000);
				Log.d("AndyNet", "broadcasting");
			}
			socket.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static final void stopBroadcast() {
		doBroadcast = false;
	}

	public static final List<DatagramPacket> receiveBroadcast(
			InetAddress address, long timeout, boolean deepSearch) {
		try {
			DatagramSocket s = new DatagramSocket(BROADCAST_PORT, address);
			s.setSoTimeout(1000);

			long start = System.currentTimeMillis();
			long now = start;
			List<DatagramPacket> packetList = new ArrayList<DatagramPacket>();

			while ((now - start) < timeout) {
				now = System.currentTimeMillis();

				byte[] d = new byte[1024];
				DatagramPacket p = new DatagramPacket(d, d.length);
				try {
					s.receive(p);
					if (!contains(p, packetList)) {
						packetList.add(p);
						Log.i(TAG, "Adding packet " + p);
						if(!deepSearch) {
							break;
						}
					}
				} catch (SocketTimeoutException e) {
					//Do nothing
					Log.e(TAG, "Timeout " + e.getMessage());
				}
			}
			s.close();
			return packetList;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	public static final String getData(DatagramPacket p) {
		byte[] data = p.getData();
		int length = p.getLength();
		return new String(data, 0, length);
	}

	private static boolean contains(DatagramPacket p,
			List<DatagramPacket> packetList) {

		for (int i = 0; i < packetList.size(); i++) {
			DatagramPacket packet = packetList.get(i);

			boolean check1 = packet.getLength() == p.getLength();
			boolean check2 = packet.getAddress().equals(p.getAddress());

			if (check1 && check2) {
				return true;
			}
		}
		return false;
	}
}
