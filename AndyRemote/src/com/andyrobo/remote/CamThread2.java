/**
 * parts of the code are taken from Regents of the University of California
 * Thanks for the brilliant work!
 */

/*******************************************************************************************************

 Copyright (c) 2011 Regents of the University of California.
 All rights reserved.

 This software was developed at the University of California, Irvine.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in
 the documentation and/or other materials provided with the
 distribution.

 3. All advertising materials mentioning features or use of this
 software must display the following acknowledgment:
 "This product includes software developed at the University of
 California, Irvine by Nicolas Oros, Ph.D.
 (http://www.cogsci.uci.edu/~noros/)."

 4. The name of the University may not be used to endorse or promote
 products derived from this software without specific prior written
 permission.

 5. Redistributions of any form whatsoever must retain the following
 acknowledgment:
 "This product includes software developed at the University of
 California, Irvine by Nicolas Oros, Ph.D.
 (http://www.cogsci.uci.edu/~noros/)."

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 IN NO EVENT SHALL THE UNIVERSITY OR THE PROGRAM CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package com.andyrobo.remote;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

public class CamThread2 implements Runnable {
	private static final int PREVIEW_PORT = 9020;

	public static int HEADER_SIZE = 5;
	public static int DATAGRAM_MAX_SIZE = 1450;
	public static int DATA_MAX_SIZE = DATAGRAM_MAX_SIZE - HEADER_SIZE;

	private ImageView camView;
	private Handler handler;

	public CamThread2(ImageView spyCamView, Handler h) {
		this.camView = spyCamView;
		this.handler = h;
	}

	private int current_frame = -1;
	private int slicesStored = 0;

	public void run() {

		DatagramSocket socket = null;
		System.out.println("Initiating fetch loop");

		try {
			socket = new DatagramSocket(PREVIEW_PORT);
			byte[] buffer = new byte[DATAGRAM_MAX_SIZE];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

			current_frame = -1;
			slicesStored = 0;

			while (true) {

				socket.receive(packet);
				byte[] data = packet.getData();

				try {
					convertDataToImage(data);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void convertDataToImage(byte[] data) {
		byte[] imageData = new byte[4];

		int frame_nb = (int) data[0];
		int nb_packets = (int) data[1];
		int packet_nb = (int) data[2];
		int size_packet = (int) ((data[3] & 0xff) << 8 | (data[4] & 0xff));
		
		Log.d("CT:", "Frame nb: " + frame_nb);
		Log.d("CT:", "Current Frame " + current_frame);
		Log.d("CT:", "nb Packets: " + nb_packets);
		Log.d("CT:", "packet_nb: " + packet_nb);
		Log.d("CT:", "size_packet: " + size_packet);
		

		if ((packet_nb == 0) && (current_frame != frame_nb)) {
			current_frame = frame_nb;
			slicesStored = 0;
			imageData = new byte[nb_packets * DATA_MAX_SIZE];
		}

		if (frame_nb == current_frame) {
			System.arraycopy(data, HEADER_SIZE, imageData, packet_nb
					* DATA_MAX_SIZE, size_packet);
			slicesStored++;
		}

		Log.d("CT:", "slices: " + slicesStored);

		
		/* If image is complete display it */
		if (slicesStored == nb_packets) {
			Bitmap image = BitmapFactory.decodeByteArray(imageData, 0,
					imageData.length);

			if (image != null) {

				Matrix m = new Matrix();
				m.postRotate(90);

				Bitmap resizedBitmap = Bitmap.createBitmap(image, 0, 0,
						image.getWidth(), image.getHeight(), m, true);

				drawImage(resizedBitmap);
			} else {
				Log.e("CamImage", "null image");
			}
		} else {
			Log.e("Slicing error", "slices stored !=  nb_packets");
		}
	}
	
	// public void run() {
	// int current_frame = -1;
	// int slicesStored = 0;
	// byte[] imageData = null;
	// DatagramSocket socket = null;
	//
	// System.out.println("Initiating fetch loop");
	// try {
	// socket = new DatagramSocket(PREVIEW_PORT);
	// byte[] buffer = new byte[DATAGRAM_MAX_SIZE];
	// DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
	//
	// while (true) {
	// socket.receive(packet);
	// robotAddress = packet.getAddress();
	// robotPort = packet.getPort();
	//
	// byte[] data = packet.getData();
	// int frame_nb = (int) data[0];
	// int nb_packets = (int) data[1];
	// int packet_nb = (int) data[2];
	// int size_packet = (int) ((data[3] & 0xff) << 8 | (data[4] & 0xff));
	//
	// if ((packet_nb == 0) && (current_frame != frame_nb)) {
	// current_frame = frame_nb;
	// slicesStored = 0;
	// imageData = new byte[nb_packets * DATA_MAX_SIZE];
	// }
	//
	// if (frame_nb == current_frame) {
	// System.arraycopy(data, HEADER_SIZE, imageData, packet_nb
	// * DATA_MAX_SIZE, size_packet);
	// slicesStored++;
	// }
	//
	// /* If image is complete display it */
	// if (slicesStored == nb_packets) {
	// ByteArrayInputStream bis = new ByteArrayInputStream(
	// imageData);
	// BufferedImage bImage = ImageIO.read(bis);
	// drawImage(bImage);
	// } else {
	//
	// }
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// socket.close();
	// }
	// }

	private void drawImage(final Bitmap bitMap) {
		handler.post(new Runnable() {

			public void run() {
				camView.setImageBitmap(bitMap);
			}
		});
	}

	// public static BufferedImage rotateCw(BufferedImage img) {
	// int width = img.getWidth();
	// int height = img.getHeight();
	// BufferedImage newImage = new BufferedImage(height, width, img.getType());
	//
	// for (int i = 0; i < width; i++)
	// for (int j = 0; j < height; j++)
	// newImage.setRGB(height - 1 - j, i, img.getRGB(i, j));
	//
	// return newImage;
	// }
	//
	// private void drawImage(BufferedImage bImage) {
	// bImage = CamThread.rotateCw(bImage);
	//
	// if (imagePanel != null) {
	// int width = bImage.getWidth();
	// int height = bImage.getHeight();
	//
	// imagePanel.getGraphics().drawImage(bImage, 0, 0, width, height,
	// null);
	// imagePanel.setSize(width, height);
	// } else {
	// System.out.println("null panel");
	// }
	// }
}
