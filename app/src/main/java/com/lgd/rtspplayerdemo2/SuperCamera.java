

package com.lgd.rtspplayerdemo2;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.wfty.cameracommon.AbstractSDTYCameraHandler;
import com.wfty.cameracommon.SDTYCameraHandlerMultiSurface;
import com.wfty.common.BaseActivity;
import com.wfty.dev.CameraCheck;
import com.wfty.dev.DevMonitor;
import com.wfty.dev.DevMonitor.OnDeviceConnectListener;
import com.wfty.dev.SDTYCamera;
import com.wfty.encoder.MediaMuxerWrapper;
import com.wfty.encoder.MediaVideoBufferEncoder;
import com.wfty.widget.CameraViewInterface;

import java.io.File;
import java.nio.ByteBuffer;


public final class SuperCamera extends BaseActivity implements CameraCheck.CameraCheckParent {
	private static final boolean DEBUG = true;    // TODO set false on release
	private static final String TAG = "MainActivity";

	private final Object mSync = new Object();
	/**
	 * for accessing USB
	 */
	private DevMonitor mDevMonitor;
	/**
	 * Handler to execute camera releated methods sequentially on private thread
	 */
	private SDTYCameraHandlerMultiSurface mCameraHandler;
	/**
	 * for camera preview display
	 */
	private CameraViewInterface mCameraViewL;
	private CameraViewInterface mCameraViewR;
	/**
	 * for open&start / stop&close camera preview
	 */
	private ToggleButton mCameraButton;
	/**
	 * button for start/stop recording
	 */
	private ImageButton mCaptureButton;
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.v(TAG, "onCreate:");
		setContentView(R.layout.super_camera);
		mCameraButton = (ToggleButton) findViewById(R.id.camera_button);
		mCameraButton.setOnCheckedChangeListener(mOnCheckedChangeListener);
		mCaptureButton = (ImageButton) findViewById(R.id.capture_button);
		mCaptureButton.setOnClickListener(mOnClickListener);
		mCaptureButton.setVisibility(View.INVISIBLE);

		mCameraViewL = (CameraViewInterface) findViewById(R.id.camera_view_L);
		mCameraViewL.setAspectRatio(SDTYCamera.DEFAULT_PREVIEW_WIDTH / (float) SDTYCamera.DEFAULT_PREVIEW_HEIGHT);
		mCameraViewL.setCallback(mCallback);
		((View) mCameraViewL).setOnLongClickListener(mOnLongClickListener);

		mCameraViewR = (CameraViewInterface) findViewById(R.id.camera_view_R);
		mCameraViewR.setAspectRatio(SDTYCamera.DEFAULT_PREVIEW_WIDTH / (float) SDTYCamera.DEFAULT_PREVIEW_HEIGHT);
		mCameraViewR.setCallback(mCallback);
		((View) mCameraViewR).setOnLongClickListener(mOnLongClickListener);

		synchronized (mSync) {
			mDevMonitor = new DevMonitor(this, mOnDeviceConnectListener);
			mCameraHandler = SDTYCameraHandlerMultiSurface.createHandler(this, mCameraViewL, 2,
					SDTYCamera.DEFAULT_PREVIEW_WIDTH, SDTYCamera.DEFAULT_PREVIEW_HEIGHT);
		}
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.



	}




	@Override
	protected void onStart() {
		super.onStart();
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		if (DEBUG) Log.v(TAG, "onStart:");
		synchronized (mSync) {
			mDevMonitor.register();
		}
		if (mCameraViewL != null) {
			mCameraViewL.onResume();
		}
		if (mCameraViewR != null) {
			mCameraViewR.onResume();
		}
	}

	@Override
	protected void onStop() {
		if (DEBUG) Log.v(TAG, "onStop:");
		synchronized (mSync) {
//			mCameraHandler.stopRecording();
//			mCameraHandler.stopPreview();
			mCameraHandler.close();    // #close include #stopRecording and #stopPreview
			mDevMonitor.unregister();
		}
		if (mCameraViewL != null) {
			mCameraViewL.onPause();
		}
		if (mCameraViewR != null) {
			mCameraViewR.onPause();
		}
		setCameraButton(false);
		super.onStop();
	}

	@Override
	public void onDestroy() {
		if (DEBUG) Log.v(TAG, "onDestroy:");
		synchronized (mSync) {
			if (mCameraHandler != null) {
				mCameraHandler.release();
				mCameraHandler = null;
			}
			if (mDevMonitor != null) {
				mDevMonitor.destroy();
				mDevMonitor = null;
			}
		}
		mCameraViewL = null;
		mCameraViewR = null;
		mCameraButton = null;
		mCaptureButton = null;
		super.onDestroy();
	}



	/**
	 * event handler when click camera / capture button
	 */
	private final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(final View view) {
			switch (view.getId()) {
				case R.id.capture_button:
					synchronized (mSync) {
						if ((mCameraHandler != null) && mCameraHandler.isOpened()) {
							if (checkPermissionWriteExternalStorage() && checkPermissionAudio()) {
								if (!mCameraHandler.isRecording()) {
									mCaptureButton.setColorFilter(0xffff0000);    // turn red
									mCameraHandler.startRecording();
									AbstractSDTYCameraHandler.MyHandler = new Handler(){
										public void handleMessage(Message msg) {
											switch (msg.what) {
												case 100:
													ByteBuffer buffer	=(ByteBuffer)msg.obj;
													final MediaVideoBufferEncoder videoEncoder;
													synchronized (mSync) {
														videoEncoder =mCameraHandler.getVideoEncoder();
													}
													if (videoEncoder != null) {
														videoEncoder.frameAvailableSoon();
														videoEncoder.encode(buffer);
													}

//													Log.e("hjs","obtian100"+buffer);
//													byte[] abytes = new byte[50];
//													buffer.get(abytes);
//													if (DEBUG) Log.v(TAG, "encode2:buffer=" + Util.bytesToHexString(abytes));

													break;
											}
										};
									};
								} else {
									mCaptureButton.setColorFilter(0);    // return to default color
									mCameraHandler.stopRecording();
								}
							}
						}
					}
					break;
			}
		}
	};

	private final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener
			= new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(final CompoundButton compoundButton, final boolean isChecked) {
			switch (compoundButton.getId()) {
				case R.id.camera_button:
					synchronized (mSync) {
						if (isChecked && (mCameraHandler != null) && !mCameraHandler.isOpened()) {
							//CameraDialog.showDialog(MainActivity.this);
							CameraCheck.cameraCheck(SuperCamera.this);
						} else {
							mCameraHandler.close();
							setCameraButton(false);
						}
					}
					break;
			}
		}
	};

	/**
	 * capture still image when you long click on preview image(not on buttons)
	 */
	private final OnLongClickListener mOnLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(final View view) {
			switch (view.getId()) {
				case R.id.camera_view_L:
				case R.id.camera_view_R:
					synchronized (mSync) {
						if ((mCameraHandler != null) && mCameraHandler.isOpened()) {
							if (checkPermissionWriteExternalStorage()) {
								String savefile = Environment.getExternalStorageDirectory().toString()+"/bkimage";
								final File outputFile = MediaMuxerWrapper.getCaptureFile(savefile, ".jpg");
								mCameraHandler.captureStill(outputFile.toString());

								runOnUiThread(new Runnable() {
									@Override
									public void run() {

										Toast.makeText(getApplicationContext(),"照片路径:"+outputFile.getAbsolutePath().toString(), Toast.LENGTH_SHORT).show();
									}
								});

							}
							return true;
						}
					}
			}
			return false;
		}
	};

	private void setCameraButton(final boolean isOn) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mCameraButton != null) {
					try {
						mCameraButton.setOnCheckedChangeListener(null);
						mCameraButton.setChecked(isOn);
					} finally {
						mCameraButton.setOnCheckedChangeListener(mOnCheckedChangeListener);
					}
				}
				if (!isOn && (mCaptureButton != null)) {
					mCaptureButton.setVisibility(View.INVISIBLE);
				}
			}
		}, 0);
	}

	private void startPreview() {
		synchronized (mSync) {
			if (mCameraHandler != null) {
				mCameraHandler.startPreview();
			}
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mCaptureButton.setVisibility(View.VISIBLE);
			}
		});
	}

	private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {

		public void onDevInit(){
			if (DEBUG) Log.v(TAG, "onDevInit:");
		}
		public void onDevClose(){

		}
		@Override
		public void onConnect( final DevMonitor.CameraControlBlock ctrlBlock, final boolean createNew) {
			if (DEBUG) Log.v(TAG, "onConnect:");
			synchronized (mSync) {
				if (mCameraHandler != null) {
					mCameraHandler.open(ctrlBlock);
					startPreview();
				}
			}
		}

		@Override
		public void onDisconnect(final DevMonitor.CameraControlBlock ctrlBlock) {
			if (DEBUG) Log.v(TAG, "onDisconnect:");
			synchronized (mSync) {
				if (mCameraHandler != null) {
					queueEvent(new Runnable() {
						@Override
						public void run() {
							synchronized (mSync) {
								if (mCameraHandler != null) {
									mCameraHandler.close();
								}
							}
						}
					}, 0);
				}
			}
			setCameraButton(false);
		}


		@Override
		public void onCancel() {
			setCameraButton(false);
		}
	};

	/**
	 * to access from CameraDialog
	 *
	 * @return
	 */
	@Override
	public DevMonitor getDevMonitor() {
		synchronized (mSync) {
			return mDevMonitor;
		}
	}

	@Override
	public void onDialogResult(boolean canceled) {
		if (canceled) {
			setCameraButton(false);
		}
	}

	private final CameraViewInterface.Callback
			mCallback = new CameraViewInterface.Callback() {
		@Override
		public void onSurfaceCreated(final CameraViewInterface view, final Surface surface) {
			mCameraHandler.addSurface(surface.hashCode(), surface, false);
		}

		@Override
		public void onSurfaceChanged(final CameraViewInterface view, final Surface surface, final int width, final int height) {

		}

		@Override
		public void onSurfaceDestroy(final CameraViewInterface view, final Surface surface) {
			synchronized (mSync) {
				if (mCameraHandler != null) {
					mCameraHandler.removeSurface(surface.hashCode());
				}
			}
		}
	};

}
