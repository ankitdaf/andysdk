package com.andyrobo.streaming;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.http.conn.util.InetAddressUtils;

import com.andyrobo.streaming.VideoFrame;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;


public class Streamer implements CameraView.CameraReadyCallback{
	
    private static final String TAG = "ANDY";
    private static final String MSG_ACCESS_LOCAL = "Open at local:";
    private static final String MSG_ACCESS_INTERNET = "Open at internet:  ";
    private static final String MSG_ACCESS_QUERY = "Detecting internet ... ";
    private static final String MSG_ACCESS_QUERY_ERROR = "Also you can setup your router to access from internet";
    private static final String MSG_ERROR = "Error: Please enable wifi";

    boolean inProcessing = false;
    final int maxVideoNumber = 3;
    VideoFrame[] videoFrames = new VideoFrame[maxVideoNumber];
    byte[] preFrame = new byte[1024*1024*8];
    
    WebServer webServer = null;
    private CameraView cameraView_;

    private AudioRecord audioCapture = null;
    private StreamingLoop audioLoop = null;
    private static Context ctx = null;
    private static SurfaceView sv=null;
    private static boolean isBroadcasting=false; 
    
	public Streamer(SurfaceView _sv, Context _ctx) {
		sv = _sv;
		ctx = _ctx;
        for(int i = 0; i < maxVideoNumber; i++) {
            videoFrames[i] = new VideoFrame(1024*1024*2);        
        }    

        System.loadLibrary("mp3encoder");
        System.loadLibrary("natpmp");
        
        initAudio();
        initCamera();

	}
	
	@Override
	public void onCameraReady() {
		if ( initWebServer() ) {
			isBroadcasting = true;
            int wid = cameraView_.Width();
            int hei = cameraView_.Height();
            cameraView_.StopPreview();
            cameraView_.setupCamera(wid, hei, previewCb_);
            cameraView_.StartPreview();
		}
		
	}

	
	public boolean isBroadcasting() {
		return isBroadcasting;
	}
    private boolean initWebServer() {
        String ipAddr = getIP();
    	//String ipAddr = getLocalIpAddress();
        if ( ipAddr != null ) {
            try{
                webServer = new WebServer(8080, ctx); 
                webServer.registerCGI("/cgi/query", doQuery);
                webServer.registerCGI("/cgi/setup", doSetup);
                webServer.registerCGI("/stream/live.jpg", doCapture);
                webServer.registerCGI("/stream/live.mp3", doBroadcast);
            }catch (IOException e){
                webServer = null;
            }
        }
        if ( webServer != null) {
            NatPMPClient natQuery = new NatPMPClient();
            natQuery.start();  
            return true;
        }
        return false;
          
    }

	
    private void initAudio() {
        int minBufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        int minTargetSize = 4410 * 2;      // 0.1 seconds buffer size
        if (minTargetSize < minBufferSize) {
            minTargetSize = minBufferSize;
        }
        if (audioCapture == null) {
            audioCapture = new AudioRecord(MediaRecorder.AudioSource.MIC,
                                        44100,
                                        AudioFormat.CHANNEL_IN_MONO,
                                        AudioFormat.ENCODING_PCM_16BIT,
                                        minTargetSize);
        }

        if ( audioLoop == null) {  
            Random rnd = new Random();
            String etag = Integer.toHexString( rnd.nextInt() );
            audioLoop = new StreamingLoop("com.andyrobo.streaming" + etag );
        }

    }
	  private void initCamera() {
	        cameraView_ = new CameraView(sv);
	        cameraView_.setCameraReadyCallback(this);
	        
	        
	    }
	    
	  public String getLocalIpAddress() {
	        try {
	            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	                NetworkInterface intf = en.nextElement();
	                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                    InetAddress inetAddress = enumIpAddr.nextElement();
	                    //if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress() ) {
	                    if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress()) ) {
	                        String ipAddr = inetAddress.getHostAddress();
	                        return ipAddr;
	                    }
	                }
	            }
	        } catch (SocketException ex) {
	            Log.d(TAG, ex.toString());
	        }
	        return null;
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
		
		private WebServer.CommonGatewayInterface doQuery = new WebServer.CommonGatewayInterface () {
	        @Override
	        public String run(Properties parms) {
	            String ret = "";
	            List<Camera.Size> supportSize =  cameraView_.getSupportedPreviewSize();                             
	            ret = ret + "" + cameraView_.Width() + "x" + cameraView_.Height() + "|";
	            for(int i = 0; i < supportSize.size() - 1; i++) {
	                ret = ret + "" + supportSize.get(i).width + "x" + supportSize.get(i).height + "|";
	            }
	            int i = supportSize.size() - 1;
	            ret = ret + "" + supportSize.get(i).width + "x" + supportSize.get(i).height ;
	            return ret;
	        }
	        
	        @Override 
	        public InputStream streaming(Properties parms) {
	            return null;
	        }    
	    };
	    
	    private PreviewCallback previewCb_ = new PreviewCallback() {
	        public void onPreviewFrame(byte[] frame, Camera c) {
	            if ( !inProcessing ) {
	                inProcessing = true;
	           
	                int picWidth = cameraView_.Width();
	                int picHeight = cameraView_.Height(); 
	                ByteBuffer bbuffer = ByteBuffer.wrap(frame); 
	                bbuffer.get(preFrame, 0, picWidth*picHeight + picWidth*picHeight/2);

	                inProcessing = false;
	            }
	        }
	    };

	    
	    private WebServer.CommonGatewayInterface doSetup = new WebServer.CommonGatewayInterface () {
	        @Override
	        public String run(Properties parms) {
	            int wid = Integer.parseInt(parms.getProperty("wid")); 
	            int hei = Integer.parseInt(parms.getProperty("hei"));
	            Log.d("ANDY", ">>>>>>>run in doSetup wid = " + wid + " hei=" + hei);
	            cameraView_.StopPreview();
	            cameraView_.setupCamera(wid, hei, previewCb_);
	            cameraView_.StartPreview();
	            return "OK";
	        }   
	 
	        @Override 
	        public InputStream streaming(Properties parms) {
	            return null;
	        }    
	    }; 

	    
	    private WebServer.CommonGatewayInterface doCapture = new WebServer.CommonGatewayInterface () {
	        @Override
	        public String run(Properties parms) {
	           return null;
	        }   
	        
	        @Override 
	        public InputStream streaming(Properties parms) {
	            VideoFrame targetFrame = null;
	            for(int i = 0; i < maxVideoNumber; i++) {
	                if ( videoFrames[i].acquire() ) {
	                    targetFrame = videoFrames[i];
	                    break;
	                }
	            }
	            // return 503 internal error
	            if ( targetFrame == null) {
	                Log.d("ANDY", "No free videoFrame found!");
	                return null;
	            }

	            // compress yuv to jpeg
	            int picWidth = cameraView_.Width();
	            int picHeight = cameraView_.Height(); 
	            YuvImage newImage = new YuvImage(preFrame, ImageFormat.NV21, picWidth, picHeight, null);
	            targetFrame.reset();
	            boolean ret;
	            inProcessing = true;
	            try{
	                ret = newImage.compressToJpeg( new Rect(0,0,picWidth,picHeight), 30, targetFrame);
	            } catch (Exception ex) {
	                ret = false;    
	            } 
	            inProcessing = false;

	            // compress success, return ok
	            if ( ret == true)  {
	                parms.setProperty("mime", "image/jpeg");
	                InputStream ins = targetFrame.getInputStream();
	                return ins;
	            }
	            // send 503 error
	            targetFrame.release();

	            return null;
	        }
	    }; 

	    private WebServer.CommonGatewayInterface doBroadcast = new WebServer.CommonGatewayInterface() {
	        @Override
	        public String run(Properties parms) {
	            return null;
	        }   
	        
	        
	        @Override 
	        public InputStream streaming(Properties parms) {
	            if ( audioLoop.isConnected() ) {     
	                return null;                    // tell client is is busy by 503
	            }    
	 
	            audioLoop.InitLoop(128, 8192);
	            InputStream is = null;
	            try{
	                is = audioLoop.getInputStream();
	            } catch(IOException e) {
	                audioLoop.ReleaseLoop();
	                return null;
	            }
	            
	            audioCapture.startRecording();
	            AudioEncoder audioEncoder = new AudioEncoder();
	            audioEncoder.start();  
	            
	            return is;
	        }

	    };
		
	    static private native int nativeOpenEncoder();
	    static private native void nativeCloseEncoder();
	    static private native int nativeEncodingPCM(byte[] pcmdata, int length, byte[] mp3Data);    
	    private class AudioEncoder extends Thread {
	        byte[] audioPackage = new byte[1024*16];
	        byte[] mp3Data = new byte[1024*8];
	        int packageSize = 4410 * 2;
	        @Override
	        public void run() {
	            nativeOpenEncoder(); 
	            
	            OutputStream os = null;
	            try {
	                os = audioLoop.getOutputStream();
	            } catch(IOException e) {
	                os = null;
	                audioLoop.ReleaseLoop();
	                nativeCloseEncoder();
	                return;
	            }
	            
	            while(true) {

	                int ret = audioCapture.read(audioPackage, 0, packageSize);
	                if ( ret == AudioRecord.ERROR_INVALID_OPERATION ||
	                        ret == AudioRecord.ERROR_BAD_VALUE) {
	                    break; 
	                }

	                //TODO: call jni encoding PCM to mp3
	                ret = nativeEncodingPCM(audioPackage, ret, mp3Data);          
	                
	                try {
	                    os.write(mp3Data, 0, ret);
	                } catch(IOException e) {
	                    break;    
	                }
	            }
	            
	            audioLoop.ReleaseLoop();
	            nativeCloseEncoder();
	        }
	    }
		
	    static private native String nativeQueryInternet();    
	    private class NatPMPClient extends Thread {
	        String queryResult;
	        Handler handleQueryResult = new Handler(ctx.getMainLooper());  
	        @Override
	        public void run(){
	            queryResult = nativeQueryInternet();
	            if ( queryResult.startsWith("error:") ) {
	            	Log.d(TAG, "Query returned " + queryResult);
	                handleQueryResult.post( new Runnable() {
	                    @Override
	                    public void run() {
	                        Log.d(TAG,MSG_ACCESS_QUERY_ERROR);                        
	                    }
	                });
	            } else {
	                handleQueryResult.post( new Runnable() {
	                    @Override
	                    public void run() {
	                        Log.d(TAG, MSG_ACCESS_INTERNET + " " + queryResult );
	                    }
	                });
	            }
	        }    
	    }
		
		public void stop() {
			isBroadcasting = false;
	        inProcessing = true;
	        if ( webServer != null)
	            webServer.stop();
	        cameraView_.StopPreview();
	        cameraView_.Release();
			audioLoop.ReleaseLoop();
	        audioCapture.release();
		}
		
		
}
