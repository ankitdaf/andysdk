package com.andyrobo.base.functions;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.andyrobo.base.utils.AndyUtils;

public class AndySpyCam implements PreviewCallback, Callback, PictureCallback {

	private static final String TAG = "AndySpyCam";
	
	private Camera mCamera;
	private boolean mPreviewRunning;
	private boolean broadCast;
	private InetAddress ipAddress;

	
	public static final AndySpyCam SPYCAM = new AndySpyCam();
	
	private AndySpyCam() {
		//create for singleton SPYCAM instance
	}
	
	public void init(SurfaceView sView) {
		assert sView != null;
		
		SurfaceHolder holder = sView.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	public boolean setReceiverIP(String ipAddress) {
		try {
			this.ipAddress = InetAddress.getByName(ipAddress);
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void startSpyMode() {
		Log.i(TAG, "Starting Broadcast");
		this.broadCast = true;
	}
	
	public void stopSpyMode() {
		Log.i(TAG, "Ending Broadcast");
		this.broadCast = false;
	}

	public void onPictureTaken(byte[] data, Camera camera) {
		Log.d(TAG, "Picture Capture");
	}

	public void onPreviewFrame(byte[] data, Camera cam) {
		if (broadCast) {
			new ImageBroadCaster().execute(this.mCamera, data);
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Log.d(TAG, "surface changed");
		if (mPreviewRunning) {
			mCamera.stopPreview();
		}

		Camera.Parameters p = mCamera.getParameters();
		List<Size> sizes = p.getSupportedPreviewSizes();
		Size optimalSize = AndyUtils.getOptimalPreviewSize(sizes, w, h);
		p.setPreviewSize(optimalSize.width, optimalSize.height);
		mCamera.setParameters(p);

		//mCamera.setDisplayOrientation(AndyUtils.determineDisplayOrientation(display))
		mCamera.startPreview();
		mPreviewRunning = true;
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surface created");
		mCamera = Camera.open(0); // open() wont for devices with no back camera , eg Nexus 7, use open(0)

		Camera.Parameters p = mCamera.getParameters();
		p.setPreviewFrameRate(30);
		p.setPreviewSize(1100,700);
		//p.setPreviewFpsRange(25, 30);
		//p.setSceneMode(Camera.Parameters.SCENE_MODE_SPORTS); // Not supported in all devices eg. Nexus 7
		//p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		//p.setColorEffect(Camera.Parameters.EFFECT_AQUA);
		mCamera.setParameters(p);

		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.setPreviewCallback(this);
		} catch (Exception exception) {
			mCamera.release();
			mCamera = null;
		}
	}

	public void surfaceDestroyed(SurfaceHolder arg0) {
		Log.d(TAG, "surface destroyed");
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();
		}
		mPreviewRunning = false;
	}
	
	private class ImageBroadCaster extends AsyncTask {
		private static final int PREVIEW_PORT = 9020;

		public final static int HEADER_SIZE = 5;
		public final static int DATAGRAM_MAX_SIZE = 1450 - HEADER_SIZE;
		int frame_nb = 0;

		Bitmap mBitmap;
		int[] mRGBData;
		int width_ima, height_ima;

		private DatagramSocket socket;

		public ImageBroadCaster() {
			try {
				this.socket = new DatagramSocket();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private final void sendUDP() {
			if (mBitmap != null) {
				int size_p = 0, i;
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

				// change compression rate to change packet size
				mBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteStream);

				byte data[] = byteStream.toByteArray();

				int nb_packets = (int) Math.ceil(data.length
						/ (float) DATAGRAM_MAX_SIZE);
				int size = DATAGRAM_MAX_SIZE;

				/* Loop through slices */
				for (i = 0; i < nb_packets; i++) {
					if (i > 0 && i == nb_packets - 1)
						size = data.length - i * DATAGRAM_MAX_SIZE;

					/* Set additional header */
					byte[] data2 = new byte[HEADER_SIZE + size];
					data2[0] = (byte) frame_nb;
					data2[1] = (byte) nb_packets;
					data2[2] = (byte) i;
					data2[3] = (byte) (size >> 8);
					data2[4] = (byte) size;

					/* Copy current slice to byte array */
					System.arraycopy(data, i * DATAGRAM_MAX_SIZE, data2,
							HEADER_SIZE, size);

					try {
						size_p = data2.length;
						DatagramPacket packet = new DatagramPacket(data2, size_p,
								ipAddress, PREVIEW_PORT);
						socket.send(packet);

					} catch (Exception e) {
						//Log.e(TAG, e.getMessage());
						e.printStackTrace();
					}
				}
				frame_nb++;

				if (frame_nb == 128)
					frame_nb = 0;
			}
		}

		/*
		 * function converting image to RGB format taken from project:
		 * ViewfinderEE368
		 * http://www.stanford.edu/class/ee368/Android/ViewfinderEE368/
		 * 
		 * Copyright (C) 2007 The Android Open Source Project
		 * 
		 * Licensed under the Apache License, Version 2.0 (the "License"); you may
		 * not use this file except in compliance with the License. You may obtain a
		 * copy of the License at
		 * 
		 * http://www.apache.org/licenses/LICENSE-2.0
		 * 
		 * Unless required by applicable law or agreed to in writing, software
		 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
		 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
		 * License for the specific language governing permissions and limitations
		 * under the License.
		 */
		private final void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width,
				int height) {
			final int frameSize = width * height;

			for (int j = 0, yp = 0; j < height; j++) {
				int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
				for (int i = 0; i < width; i++, yp++) {
					int y = (0xff & ((int) yuv420sp[yp])) - 16;
					if (y < 0)
						y = 0;
					if ((i & 1) == 0) {
						v = (0xff & yuv420sp[uvp++]) - 128;
						u = (0xff & yuv420sp[uvp++]) - 128;
					}

					int y1192 = 1192 * y;
					int r = (y1192 + 1634 * v);
					int g = (y1192 - 833 * v - 400 * u);
					int b = (y1192 + 2066 * u);

					if (r < 0)
						r = 0;
					else if (r > 262143)
						r = 262143;
					if (g < 0)
						g = 0;
					else if (g > 262143)
						g = 262143;
					if (b < 0)
						b = 0;
					else if (b > 262143)
						b = 262143;

					rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
							| ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
				}
			}
		}

		public void sendImage(Camera cam, byte[] camData) {
			if (mBitmap == null) // create Bitmap image first time
			{
				Camera.Parameters params = cam.getParameters();
				width_ima = params.getPreviewSize().width;
				height_ima = params.getPreviewSize().height;
				mBitmap = Bitmap.createBitmap(width_ima, height_ima,
						Bitmap.Config.RGB_565);
				mRGBData = new int[width_ima * height_ima];
			}

			decodeYUV420SP(mRGBData, camData, width_ima, height_ima);
			mBitmap.setPixels(mRGBData, 0, width_ima, 0, 0, width_ima, height_ima);

		}

		@Override
		protected Object doInBackground(Object... arg0) {
			sendImage((Camera) arg0[0], (byte[]) arg0[1]);
			sendUDP();
			return null;
		}
	}
}
