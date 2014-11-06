package com.clabs.mindclick;

import java.io.IOException;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.neurosky.thinkgear.TGDevice;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

    
	
	protected static final String TAG = "MainActivity";
	private Camera mCamera;
	private SurfaceView mPreview;
	private TextView tv;
	private TextView valueView;
	private ImageView modeStatusView;
	
	private float alpha = (float) 0.2;
	
	TGDevice tgDevice;
	final boolean rawEnabled = false;
	BluetoothAdapter bluetoothAdapter;
	
	boolean takingPicture = false;
	boolean attentionStatus = false;
	
	// Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
	
	
	
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
        
        tv = (TextView) findViewById(R.id.statusView);
        valueView = (TextView) findViewById(R.id.valueView);
        modeStatusView = (ImageView) findViewById(R.id.modeStatusView);
        
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(this).setBaseListener(mBaseListener);
        
        
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
        	// Alert user that Bluetooth is not available
        	Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
        	finish();
        	return;
        }else {
        	/* create the TGDevice */
        	tgDevice = new TGDevice(bluetoothAdapter, handler);
        }
        
        mCamera = getCameraInstance();
	 	mPreview = (SurfaceView) findViewById(R.id.preview);
        mPreview.getHolder().addCallback(mSurfaceHolderCallback);
        
        doStuff(tv);
        
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
    
    @Override
    public void onDestroy() {
    	tgDevice.close();
        super.onDestroy();
    }
    
    
    public void doStuff(View view) {
    	if(tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED)
    		tgDevice.connect(rawEnabled);   
    	//tgDevice.ena
    }

    
    
    /**
     * Handles messages from TGDevice
     */
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	switch (msg.what) {
            case TGDevice.MSG_STATE_CHANGE:

                switch (msg.arg1) {
	                case TGDevice.STATE_IDLE:
	                    break;
	                case TGDevice.STATE_CONNECTING:		                	
	                	tv.setText("Connecting...\n");
	                	break;		                    
	                case TGDevice.STATE_CONNECTED:
	                	tv.setText("");
	                	Log.e(TAG, "Connected!");
	                	//tv.setText("Connected.\n");
	                	tgDevice.start();
	                	
	                    break;
	                case TGDevice.STATE_NOT_FOUND:
	                	tv.setText("Can't find device\n");
	                	break;
	                case TGDevice.STATE_NOT_PAIRED:
	                	tv.setText("Not Paired\n");
	                	break;
	                case TGDevice.STATE_DISCONNECTED:
	                	tv.setText("Disconnected mang\n");
                }

                break;
            case TGDevice.MSG_POOR_SIGNAL:
            		//signal = msg.arg1;
            		//tv.append("PoorSignal: " + msg.arg1 + "\n");
                break;
            case TGDevice.MSG_RAW_DATA:	  
            		//raw1 = msg.arg1;
            		//tv.append("Got raw: " + msg.arg1 + "\n");
            	break;
            case TGDevice.MSG_HEART_RATE:
        		//tv.append("Heart rate: " + msg.arg1 + "\n");
                break;
            case TGDevice.MSG_ATTENTION:
            		//att = msg.arg1;
            		//tv.append("Attention: " + msg.arg1 + "\n");
            		//Log.v("HelloA", "Attention: " + att + "\n");
            	
            	valueView.setText(Integer.toString(msg.arg1));
        		
            	modeStatusView.setAlpha(((float)msg.arg1 / 70));
            	if(msg.arg1 < 70 && attentionStatus) {
        			attentionStatus = false;
        		}
            	
            	if( msg.arg1 > 70 && !takingPicture && !attentionStatus) {
        			
        			takingPicture = true;
        			attentionStatus = true;
        			//alpha = (float) 1.0;
        			//modeStatusView.setAlpha(alpha);
        			Log.e("Attention Value: ", Integer.toString(msg.arg1));
        			takePicture();
        		}
            	
            	break;
            case TGDevice.MSG_MEDITATION:

            	break;
            case TGDevice.MSG_BLINK:
            		
            		/*valueView.setText(Integer.toString(msg.arg1));
            		
            		if( msg.arg1 > 70 && !takingPicture ) {
            			
            			takingPicture = true;
            			alpha = (float) 1.0;
            			modeStatusView.setAlpha(alpha);
            			Log.e("Blink Count: ", Integer.toString(msg.arg1));
            			takePicture();
            		}*/
            		//tv.append("Blink: " + msg.arg1 + "\n");
            		
            	break;
            case TGDevice.MSG_RAW_COUNT:
            		//tv.append("Raw Count: " + msg.arg1 + "\n");
            	break;
            case TGDevice.MSG_LOW_BATTERY:
            	Toast.makeText(getApplicationContext(), "Low battery!", Toast.LENGTH_SHORT).show();
            	break;
            case TGDevice.MSG_RAW_MULTI:
            	//TGRawMulti rawM = (TGRawMulti)msg.obj;
            	//tv.append("Raw1: " + rawM.ch1 + "\nRaw2: " + rawM.ch2);
            default:
            	break;
        }
        	
        	
        }
    };
    
    
    
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
	            
	            alpha = (float) 0.2;
    			modeStatusView.setAlpha(alpha);
    			takingPicture = false;
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
