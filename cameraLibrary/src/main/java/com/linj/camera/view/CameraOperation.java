package com.linj.camera.view;

import android.graphics.Bitmap;
import android.hardware.Camera.PictureCallback;

import com.linj.camera.view.CameraContainer.TakePictureListener;
import com.linj.camera.view.CameraView.FlashMode;


public interface CameraOperation {

	public boolean startRecord();


	public Bitmap stopRecord();

	public void switchCamera();

	public FlashMode getFlashMode();

	public void setFlashMode(FlashMode flashMode);

	public void takePicture(PictureCallback callback,TakePictureListener listener);

	public int getMaxZoom();

	public void setZoom(int zoom);

	public int getZoom();
}
