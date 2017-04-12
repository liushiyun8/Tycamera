package com.linj.imageloader;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

import com.linj.imageloader.ImageSizeUtil.ImageSize;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;



public class ImageLoader {
	private static final String TAG = "ImageLoader";

	private static ImageLoader mInstance;

	private LinkedBlockingDeque<Runnable> mTaskQueue;


	private LruCache<String, Bitmap> mLruCache;

	private ExecutorService mThreadPool;
	private static final int DEAFULT_THREAD_COUNT = 1;

	private Type mType = Type.LIFO;

	private Thread mPoolThread;

	private Handler mUIHandler;


	private Semaphore mSemaphoreThreadPool;

	private Context mContext;

	public enum Type
	{
		FIFO, LIFO;
	}
	public static ImageLoader getInstance(Context context)
	{
		if (mInstance == null)
		{
			synchronized (ImageLoader.class)
			{
				if (mInstance == null)
				{
					mInstance = new ImageLoader(DEAFULT_THREAD_COUNT, Type.LIFO,context);
				}
			}
		}
		return mInstance;
	}
	private ImageLoader(int threadCount, Type type,Context context)
	{
		init(threadCount, type,context);
	}
	public static ImageLoader getInstance(int threadCount, Type type,Context context)
	{
		if (mInstance == null)
		{
			synchronized (ImageLoader.class)
			{
				if (mInstance == null)
				{

					mInstance = new ImageLoader(threadCount, type,context);
				}
			}
		}
		return mInstance;
	}


	private void init(int threadCount, Type type,Context context)
	{

		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheMemory = maxMemory / 8;

		mContext=context.getApplicationContext();
		mLruCache = new LruCache<String, Bitmap>(cacheMemory){
			@Override
			protected int sizeOf(String key, Bitmap value)
			{
				//				return value.getAllocationByteCount();
				return value.getRowBytes() * value.getHeight();
			}

		};


		mThreadPool = Executors.newFixedThreadPool(threadCount);
		mType = type;
		mSemaphoreThreadPool = new Semaphore(threadCount,true);
		mTaskQueue = new LinkedBlockingDeque<Runnable>();	
		initBackThread();
	}

	private void initBackThread()
	{

		mPoolThread = new Thread()
		{
			@Override
			public void run()
			{
				while(true){
					try {
						mSemaphoreThreadPool.acquire();
						Runnable runnable=getTask();
						mThreadPool.execute(runnable);	
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}


			};
		};

		mPoolThread.start();
	}


	public void loadImage( String path,  ImageView imageView, DisplayImageOptions options) 
	{
		options.displayer.display(options.imageResOnLoading, imageView);
		if (mUIHandler == null)
		{
			mUIHandler = new Handler(mContext.getMainLooper())
			{
				public void handleMessage(Message msg)
				{
					ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
					Bitmap bm = holder.bitmap;
					ImageView view = holder.imageView;
					DisplayImageOptions options=holder.options;
					if(bm!=null){			
						options.displayer.display(bm, view);
					}
					else {
						options.displayer.display(options.imageResOnFail, view);
					}
				};
			};
		}

		Bitmap bm = getBitmapFromLruCache(path);

		if (bm != null)
		{
			refreashBitmap(path, imageView, bm,options);
		} else{
			addTask(buildTask(path, imageView,options));
		}

	}


	private Runnable buildTask(final String path, final ImageView imageView,
			final DisplayImageOptions options)
	{
		return new Runnable()
		{
			@Override
			public void run()
			{
				Bitmap bm = null;
				if (options.fromNet)
				{

					File file = getDiskCacheDir(imageView.getContext(),
							md5(path));

					if (file.exists()){
						bm = loadImageFromLocal(file.getAbsolutePath(),
								imageView);
					} else{

						if (options.cacheOnDisk){
							boolean downloadState = DownloadImgUtils
									.downloadImgByUrl(path, file);
							if (downloadState){
								bm = loadImageFromLocal(file.getAbsolutePath(),
										imageView);
							}
						} else{
							bm = DownloadImgUtils.downloadImgByUrl(path,
									imageView);
						}
					}
				} else{
					bm = loadImageFromLocal(path, imageView);
				}

				if (options.cacheInMemory) {
					addBitmapToLruCache(path, bm);
				}

				refreashBitmap(path, imageView, bm,options);

				mSemaphoreThreadPool.release();
			}


		};
	}

	private Bitmap loadImageFromLocal(final String path,
			final ImageView imageView)
	{
		Bitmap bm;

		ImageSize imageSize = ImageSizeUtil.getImageViewSize(imageView);

		bm = decodeSampledBitmapFromPath(path, imageSize.width,
				imageSize.height);
		return bm;
	}



	public String md5(String str)
	{
		byte[] digest = null;
		try
		{
			MessageDigest md = MessageDigest.getInstance("md5");
			digest = md.digest(str.getBytes());
			return bytes2hex02(digest);

		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		return null;
	}


	public String bytes2hex02(byte[] bytes)
	{
		StringBuilder sb = new StringBuilder();
		String tmp = null;
		for (byte b : bytes)
		{

			tmp = Integer.toHexString(0xFF & b);
			if (tmp.length() == 1)
			{
				tmp = "0" + tmp;
			}
			sb.append(tmp);
		}

		return sb.toString();

	}

	private void refreashBitmap(String path, ImageView imageView,
			Bitmap bm,DisplayImageOptions options){
		Message message = Message.obtain();
		ImgBeanHolder holder = new ImgBeanHolder();
		holder.bitmap = bm;
		holder.path = path;
		holder.imageView = imageView;
		holder.options=options;
		message.obj = holder;
		mUIHandler.sendMessage(message);
	}


	protected void addBitmapToLruCache(String path, Bitmap bm)
	{
		if (getBitmapFromLruCache(path) == null){
			if (bm != null)
				mLruCache.put(path, bm);
		}
	}


	protected Bitmap decodeSampledBitmapFromPath(String path, int width,
			int height)
	{

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		options.inSampleSize = ImageSizeUtil.caculateInSampleSize(options,
				width, height);


		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		return bitmap;
	}




	public File getDiskCacheDir(Context context, String uniqueName)
	{
		String cachePath;
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState()))
		{
			cachePath = context.getExternalCacheDir().getPath();
		} else
		{
			cachePath = context.getCacheDir().getPath();
		}
		return new File(cachePath + File.separator + uniqueName);
	}


	private Bitmap getBitmapFromLruCache(String key)
	{
		return mLruCache.get(key);
	}



	private Runnable getTask() throws InterruptedException
	{
		if (mType == Type.FIFO)
		{
			return mTaskQueue.takeFirst();
		} else 
		{
			return mTaskQueue.takeLast();
		}
	}

	private  void addTask(Runnable runnable)
	{
		try {
			mTaskQueue.put(runnable);
		} catch (Exception e) {
			Log.i(TAG, e.toString());
		}

	}
	private class ImgBeanHolder
	{
		Bitmap bitmap;
		ImageView imageView;
		String path;
		DisplayImageOptions options;
	}
}
