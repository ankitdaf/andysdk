package com.andyrobo.base.utils;

import java.util.List;

import android.hardware.Camera.Size;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.Display;
import android.view.Surface;
import android.view.ViewGroup.LayoutParams;

import com.andyrobo.base.AndyActivity;

public class AndyUtils {
	
	public static final int ByteToInt(byte b) {
		return b & 0xff;
	}
	
	public static final String getWifiAddress(AndyActivity a) {
		WifiManager myWifiManager = (WifiManager) a.getSystemService(AndyActivity.WIFI_SERVICE);
		WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();

		String ipAddress = android.text.format.Formatter
				.formatIpAddress(myWifiInfo.getIpAddress());

		return ipAddress;
	}
	
	public static Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.05;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public static int determineDisplayOrientation(Display display) {
		int rotation = display.getRotation();
		int degrees = 0;

		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;

		case Surface.ROTATION_90:
			degrees = 90;
			break;

		case Surface.ROTATION_180:
			degrees = 180;
			break;

		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		return (-degrees + 360) % 360;
	}

	public static LayoutParams getLayoutFill() {
		return new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}
}
