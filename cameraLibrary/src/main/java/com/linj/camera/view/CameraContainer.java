package com.linj.camera.view;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import com.linj.FileOperateUtil;
import com.linj.camera.view.CameraView.FlashMode;
import com.linj.cameralibrary.R;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;



public class CameraContainer extends RelativeLayout implements CameraOperation{

	public final static String TAG="CameraContainer";


	private CameraView mCameraView;


	private TempImageView mTempImageView;


	private FocusImageView mFocusImageView;


	private TextView mRecordingInfoTextView;


	private ImageView mWaterMarkImageView; 


	private String mSavePath;

	private DataHandler mDataHandler;


	private TakePictureListener mListener;


	private SeekBar mZoomSeekBar;

	private Handler mHandler;
	private long mRecordStartTime;
	private SimpleDateFormat mTimeFormat;
	public CameraContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
		mHandler=new Handler();
		mTimeFormat=new SimpleDateFormat("mm:ss",Locale.getDefault());
		setOnTouchListener(new TouchListener());
	}


	private void initView(Context context) {
		inflate(context, R.layout.cameracontainer, this);
		mCameraView=(CameraView) findViewById(R.id.cameraView);

		mTempImageView=(TempImageView) findViewById(R.id.tempImageView);

		mFocusImageView=(FocusImageView) findViewById(R.id.focusImageView);

		mRecordingInfoTextView=(TextView) findViewById(R.id.recordInfo);

		mWaterMarkImageView=(ImageView) findViewById(R.id.waterMark);

		mZoomSeekBar=(SeekBar) findViewById(R.id.zoomSeekBar);

		int maxZoom=mCameraView.getMaxZoom();
		if(maxZoom>0){
			mZoomSeekBar.setMax(maxZoom);
			mZoomSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		}
	}


	@Override
	public boolean startRecord(){
		mRecordStartTime=SystemClock.uptimeMillis();
		mRecordingInfoTextView.setVisibility(View.VISIBLE);
		mRecordingInfoTextView.setText("00:00");
		if(mCameraView.startRecord()){
			mHandler.postAtTime(recordRunnable, mRecordingInfoTextView, SystemClock.uptimeMillis()+1000);
			return true;
		}else {
			return false;
		}
	}

	Runnable recordRunnable=new Runnable() {	
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(mCameraView.isRecording()){
				long recordTime=SystemClock.uptimeMillis()-mRecordStartTime;
				mRecordingInfoTextView.setText(mTimeFormat.format(new Date(recordTime)));
				mHandler.postAtTime(this,mRecordingInfoTextView, SystemClock.uptimeMillis()+500);
			}else {
				mRecordingInfoTextView.setVisibility(View.GONE);
			}
		}
	};

	public Bitmap stopRecord(TakePictureListener listener){
		mListener=listener;
		return stopRecord();
	}
	
	@Override
	public Bitmap stopRecord(){
		mRecordingInfoTextView.setVisibility(View.GONE);
		Bitmap thumbnailBitmap=mCameraView.stopRecord();
		if(thumbnailBitmap!=null){
			mTempImageView.setListener(mListener);
			mTempImageView.isVideo(true);
			mTempImageView.setImageBitmap(thumbnailBitmap);
			mTempImageView.startAnimation(R.anim.tempview_show);
		}
		return thumbnailBitmap;
	}
	

	public void switchMode(int zoom){
		mZoomSeekBar.setProgress(zoom);
		mCameraView.setZoom(zoom);

		mCameraView.onFocus(new Point(getWidth()/2, getHeight()/2), autoFocusCallback);   

		mWaterMarkImageView.setVisibility(View.GONE);
	}



	public void setWaterMark(){
		if (mWaterMarkImageView.getVisibility()==View.VISIBLE) {
			mWaterMarkImageView.setVisibility(View.GONE);
		}else {
			mWaterMarkImageView.setVisibility(View.VISIBLE);
		}
	}


	@Override
	public void switchCamera(){
		mCameraView.switchCamera();
	}

	@Override
	public FlashMode getFlashMode() {
		return mCameraView.getFlashMode();
	}


	@Override
	public void setFlashMode(FlashMode flashMode) {
		mCameraView.setFlashMode(flashMode);
	}


	public void setRootPath(String rootPath){
		this.mSavePath=rootPath;

	}




	public void takePicture(){
		takePicture(pictureCallback,mListener);
	}


	public void takePicture(TakePictureListener listener){
		this.mListener=listener;
		takePicture(pictureCallback, mListener);
	}


	@Override
	public void takePicture(PictureCallback callback,
			TakePictureListener listener) {
		mCameraView.takePicture(callback,listener);
	}

	@Override
	public int getMaxZoom() {
		// TODO Auto-generated method stub
		return mCameraView.getMaxZoom();
	}

	@Override
	public void setZoom(int zoom) {
		// TODO Auto-generated method stub
		mCameraView.setZoom(zoom);
	}

	@Override
	public int getZoom() {
		// TODO Auto-generated method stub
		return mCameraView.getZoom();
	} 

	private final OnSeekBarChangeListener onSeekBarChangeListener=new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			mCameraView.setZoom(progress);
			mHandler.removeCallbacksAndMessages(mZoomSeekBar);

			mHandler.postAtTime(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mZoomSeekBar.setVisibility(View.GONE);
				}
			}, mZoomSeekBar,SystemClock.uptimeMillis()+2000);
		}



		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}



		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}
	};

	private final AutoFocusCallback autoFocusCallback=new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {

			if (success) {
				mFocusImageView.onFocusSuccess();
			}else {
				mFocusImageView.onFocusFailed();

			}
		}
	};

	private final PictureCallback pictureCallback=new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			if(mSavePath==null) throw new RuntimeException("mSavePath is null");
			if(mDataHandler==null) mDataHandler=new DataHandler();	
			mDataHandler.setMaxSize(200);
			Bitmap bm=mDataHandler.save(data);
			mTempImageView.setListener(mListener);
			mTempImageView.isVideo(false);
			mTempImageView.setImageBitmap(bm);
			mTempImageView.startAnimation(R.anim.tempview_show);
			camera.startPreview();
			if(mListener!=null) mListener.onTakePictureEnd(bm);
		}
	};

	private final class TouchListener implements OnTouchListener {


		private static final int MODE_INIT = 0;

		private static final int MODE_ZOOM = 1;
		private int mode = MODE_INIT;



		private float startDis;


		@Override
		public boolean onTouch(View v, MotionEvent event) {

			switch (event.getAction() & MotionEvent.ACTION_MASK) {

			case MotionEvent.ACTION_DOWN:
				mode = MODE_INIT;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:

				if(mZoomSeekBar==null) return true;

				mHandler.removeCallbacksAndMessages(mZoomSeekBar);
				mZoomSeekBar.setVisibility(View.VISIBLE);

				mode = MODE_ZOOM;

				startDis = distance(event);
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == MODE_ZOOM) {

					if(event.getPointerCount()<2) return true;
					float endDis = distance(event);
					int scale=(int) ((endDis-startDis)/10f);
					if(scale>=1||scale<=-1){
						int zoom=mCameraView.getZoom()+scale;
						if(zoom>mCameraView.getMaxZoom()) zoom=mCameraView.getMaxZoom();
						if(zoom<0) zoom=0;
						mCameraView.setZoom(zoom);
						mZoomSeekBar.setProgress(zoom);
						startDis=endDis;
					}
				}
				break;

			case MotionEvent.ACTION_UP:
				if(mode!=MODE_ZOOM){

					Point point=new Point((int)event.getX(), (int)event.getY());
					mCameraView.onFocus(point,autoFocusCallback);
					mFocusImageView.startFocus(point);
				}else {
					mHandler.postAtTime(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							mZoomSeekBar.setVisibility(View.GONE);
						}
					}, mZoomSeekBar,SystemClock.uptimeMillis()+2000);
				}
				break;
			}
			return true;
		}

		private float distance(MotionEvent event) {
			float dx = event.getX(1) - event.getX(0);
			float dy = event.getY(1) - event.getY(0);
			return (float) Math.sqrt(dx * dx + dy * dy);
		}

	}


	private final class DataHandler{
		private String mThumbnailFolder;
		private String mImageFolder;
		private int maxSize=200;

		public DataHandler(){
			mImageFolder=FileOperateUtil.getFolderPath(getContext(), FileOperateUtil.TYPE_IMAGE, mSavePath);
			mThumbnailFolder=FileOperateUtil.getFolderPath(getContext(),  FileOperateUtil.TYPE_THUMBNAIL, mSavePath);
			File folder=new File(mImageFolder);
			if(!folder.exists()){
				folder.mkdirs();
			}
			folder=new File(mThumbnailFolder);
			if(!folder.exists()){
				folder.mkdirs();
			}
		}


		public Bitmap save(byte[] data){
			if(data!=null){

				Bitmap bm=BitmapFactory.decodeByteArray(data, 0, data.length);

				bm=getBitmapWithWaterMark(bm);

				Bitmap thumbnail=ThumbnailUtils.extractThumbnail(bm, 213, 213);

				String imgName=FileOperateUtil.createFileNmae(".jpg");
				String imagePath=mImageFolder+File.separator+imgName;
				String thumbPath=mThumbnailFolder+File.separator+imgName;

				File file=new File(imagePath);  
				File thumFile=new File(thumbPath);
				try{

					FileOutputStream fos=new FileOutputStream(file);
					ByteArrayOutputStream bos=compress(bm);
					fos.write(bos.toByteArray());
					fos.flush();
					fos.close();

					BufferedOutputStream bufferos=new BufferedOutputStream(new FileOutputStream(thumFile));
					thumbnail.compress(Bitmap.CompressFormat.JPEG, 50, bufferos);
					bufferos.flush();
					bufferos.close();
					return bm; 
				}catch(Exception e){
					Log.e(TAG, e.toString());
					Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();

				}
			}else{
				Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
			}
			return null;
		}

		private Bitmap getBitmapWithWaterMark(Bitmap bm) {
			// TODO Auto-generated method stub
			if(!(mWaterMarkImageView.getVisibility()==View.VISIBLE)){
				return bm;
			}
			Drawable mark=mWaterMarkImageView.getDrawable();
			Bitmap wBitmap=drawableToBitmap(mark);
			int w = bm.getWidth();

			int h = bm.getHeight();

			int ww = wBitmap.getWidth();

			int wh = wBitmap.getHeight();
			Bitmap newb = Bitmap.createBitmap( w, h, Config.ARGB_8888 );
			Canvas canvas=new Canvas(newb);
			//draw src into

			canvas.drawBitmap( bm, 0, 0, null );
			canvas.drawBitmap( wBitmap, w - ww + 5, h - wh + 5, null );
			//save all clip

			canvas.save( Canvas.ALL_SAVE_FLAG );

			//store

			canvas.restore();
			bm.recycle();
			bm=null;
			wBitmap.recycle();
			wBitmap=null;
			return newb;

		}
		public  Bitmap drawableToBitmap(Drawable drawable) {       
			Bitmap bitmap = Bitmap.createBitmap(
					drawable.getIntrinsicWidth(),
					drawable.getIntrinsicHeight(),
					drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
							: Bitmap.Config.RGB_565);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			drawable.draw(canvas);
			return bitmap;
		}

		public ByteArrayOutputStream compress(Bitmap bitmap){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			int options = 99;
			while ( baos.toByteArray().length / 1024 > maxSize) {
				options -= 3;
				if (options<0) {
					break;
				}
				Log.i(TAG,baos.toByteArray().length / 1024+"");
				baos.reset();
				bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
			}
			return baos;
		}

		public void setMaxSize(int maxSize) {
			this.maxSize = maxSize;
		}
	}


	public static interface TakePictureListener{		

		public void onTakePictureEnd(Bitmap bm);


		public void onAnimtionEnd(Bitmap bm,boolean isVideo);
	}


	/**  
	 * dipתpx
	 *  @param dipValue
	 *  @return   
	 */
	private  int dip2px(float dipValue){ 
		final float scale = getResources().getDisplayMetrics().density; 
		return (int)(dipValue * scale + 0.5f); 
	}
}