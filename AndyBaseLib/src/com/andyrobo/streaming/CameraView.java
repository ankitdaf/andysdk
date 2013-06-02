package com.andyrobo.streaming;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.*;

public class CameraView implements SurfaceHolder.Callback{
    public static interface CameraReadyCallback { 
        public void onCameraReady(); 
    }  

    private Camera camera_ = null;
    private SurfaceHolder surfaceHolder_ = null;
    private SurfaceView	  surfaceView_;
    CameraReadyCallback cameraReadyCb_ = null;
 
    private List<Camera.Size> supportedSizes; 
    private Camera.Size procSize_;
    private boolean inProcessing_ = false;

    public CameraView(SurfaceView sv){
 
        surfaceHolder_ = sv.getHolder();
        surfaceHolder_.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder_.addCallback(this);

    }

    public List<Camera.Size> getSupportedPreviewSize() {
        return supportedSizes;
    }

    public int Width() {
        return procSize_.width;
    }

    public int Height() {
        return procSize_.height;
    }

    public void setCameraReadyCallback(CameraReadyCallback cb) {
        cameraReadyCb_ = cb;
    }

    public void StartPreview(){
        if ( camera_ == null)
            return;
        camera_.startPreview();
    }
    
    public void StopPreview(){
        if ( camera_ == null)
            return;
        camera_.stopPreview();
    }

    public void AutoFocus() {
        camera_.autoFocus(afcb);
    }

    public void Release() {
        if ( camera_ != null) {
            camera_.stopPreview();
            camera_.release();
            camera_ = null;
        }
    }
    
    public void setupCamera(int wid, int hei, PreviewCallback cb) {
        procSize_.width = wid;
        procSize_.height = hei;
        
        Camera.Parameters p = camera_.getParameters();        
        p.setPreviewSize(procSize_.width, procSize_.height);
        camera_.setParameters(p);
        try {
			camera_.setPreviewDisplay(surfaceHolder_);
		} catch (IOException e) {
			e.printStackTrace();
		}
        camera_.setPreviewCallback(cb);
    }

    private void setupCamera() {
    	boolean foundCamera=false;
    	try {
        camera_ = Camera.open();	// Get the first rear facing camera
        foundCamera=true;
    	}
    	catch (NullPointerException e) { // The device doesn't have a rear camera
    		Log.e("ANDY","Did not find rear camera");
    		}
    	catch (RuntimeException e1) {
        	Log.e("ANDY","Could not open rear camera");
        }
    		
    	if (!foundCamera){
    try {
		camera_ = Camera.open(0);
		foundCamera=true;
    }catch (NullPointerException e1) {
    	Log.e("ANDY","Did not find front camera");
    }	
    catch (RuntimeException e1) {
    	Log.e("ANDY","Could not open front camera");
    }   
    
    if(!foundCamera) {
    	//TODO : Show Toast
    			System.out.println("Could not open Camera");
    			return;
    }	
	}

    	
        procSize_ = camera_.new Size(0, 0);
        Camera.Parameters p = camera_.getParameters();        
       
        supportedSizes = p.getSupportedPreviewSizes();
        procSize_ = supportedSizes.get( supportedSizes.size()/2 );
        p.setPreviewSize(procSize_.width, procSize_.height);
        
        camera_.setParameters(p);
        //camera_.setDisplayOrientation(90);
        
        camera_.startPreview();	// This isn't going to display anything, since we haven't setPreviewDisplay
    }  
    
    private Camera.AutoFocusCallback afcb = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
        }
    };

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		setupCamera();        
        if ( cameraReadyCb_ != null)
            cameraReadyCb_.onCameraReady();
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Release();
		
	}

   
}
