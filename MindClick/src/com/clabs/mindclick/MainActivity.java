package com.clabs.mindclick;

import java.io.IOException;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;


public class MainActivity extends Activity {

    
	
	protected static final String TAG = "MainActivity";
	private Camera mCamera;
	private SurfaceView mPreview;
	
	
	/** Listener that displays the options menu when the touchpad is tapped. */


    private final GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener() {
        @Override
        public boolean onGesture(Gesture gesture) {
            if (gesture == Gesture.TAP) {
                mAudioManager.playSoundEffect(Sounds.TAP);
                openOptionsMenu();
                return true;
            } else {
                return false;
            }
        }
    };
    /** Audio manager used to play system sound effects. */
    private AudioManager mAudioManager;

	
	/** Gesture detector used to present the options menu. */
    private GestureDetector mGestureDetector;
    
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(this).setBaseListener(mBaseListener);
        
        mCamera = getCameraInstance();
	 	mPreview = (SurfaceView) findViewById(R.id.preview);
        mPreview.getHolder().addCallback(mSurfaceHolderCallback);
        
    }


    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event);
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_click) {
        	takePicture();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    
	 private Camera getCameraInstance() {
	        Camera camera = null;
	        try {
	            camera = Camera.open();
	            Camera.Parameters params = camera.getParameters();
	            params.setPreviewFpsRange(30000, 30000);
	            camera.setParameters(params);
	        } catch (Exception e) {
	            // cannot get camera or does not exist
	        }
	        return camera;
	    }
	 
	 private final PictureCallback mPictureCallback = new PictureCallback() {

	        @Override
	        public void onPictureTaken(byte[] data, Camera camera) {
	            Log.d(TAG, "Picture taken!");
	            
	            Log.d(TAG, "Stopping preview.");
	            mCamera.stopPreview();
	            
	            try {
					Thread.sleep(1000); // Later remove the thread and add async task to store the picture
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            
	            Toast.makeText(getApplicationContext(), "Captured!", Toast.LENGTH_SHORT).show();
	            
	            
	            Log.d(TAG, "Starting preview again");
	            mCamera.startPreview(); // Start the preview after 3 sec pause.
	            
	            
	        }
	    };

	 
	 private final SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {

	        @Override
	        public void surfaceCreated(SurfaceHolder holder) {
	            try {
	                mCamera.setPreviewDisplay(holder);
	                mCamera.startPreview();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }

	        @Override
	        public void surfaceDestroyed(SurfaceHolder holder) {
	            // Nothing to do here.
	        	
	        	if (mCamera != null) {
	        		mCamera.release();
	        	}
	        	
	        }

	        @Override
	        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	            // Nothing to do here.
	        }
	    };

	 
	 private void takePicture() {

		 Log.d("Camera","Taking Picture");
		 mCamera.takePicture(null, null, mPictureCallback);

		}

}
