package com.andyrobo.base.net;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class AndyWifiManager {

	private static WifiManager wManager;
	private static boolean rollBackState = false;
	private static boolean rollBackSet = false;

	public static void activateWifi(boolean force, Context c) {
		if (isActive(c)) {
			// is already active
			setRollBackState(true);
		} else {
			// inactive
			setRollBackState(false);
			if (!force) {
				// need to ask user
				showRequestDialog(c);
			} else {
				getWifiManager(c).setWifiEnabled(true);
			}
		}
		System.out.println("Activated Wifi. Default state is " + rollBackState);
	}
	
	// Setting the rollback state with an "already set" flag so that
	// repeating the activate function will not affect the original rollback state

	private static void setRollBackState(boolean b) {
		if(!rollBackSet) {
			rollBackState = b;
		}
		rollBackSet = true;
	}

	private static void showRequestDialog(final Context c) {
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					getWifiManager(c).setWifiEnabled(true);
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(c);
		builder.setMessage("Wifi is Off. Turn it On?")
				.setPositiveButton("OK", listener)
				.setNegativeButton("NO", listener).show();
	}

	public static boolean isActive(Context c) {
		return getWifiManager(c).isWifiEnabled();
	}

	private final static WifiManager getWifiManager(Context c) {
		if (wManager == null) {
			assert c != null;
			wManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
		}
		return wManager;
	}

	public static void rollBackWifiState(Context c) {
		if (!rollBackState) {
			getWifiManager(c).setWifiEnabled(false);
		}
	}

	public static void deactivateWifi(Context c) {
		getWifiManager(c).setWifiEnabled(false);
	}
	
	public static String getIP(Context c) {
		WifiInfo wInfo = getWifiManager(c).getConnectionInfo();
			String ipAddress = android.text.format.Formatter
					.formatIpAddress(wInfo.getIpAddress());

		return ipAddress;
	}

	public static DhcpInfo getDhcpInfo(Context c) {
		return getWifiManager(c).getDhcpInfo();
	}
	
}
