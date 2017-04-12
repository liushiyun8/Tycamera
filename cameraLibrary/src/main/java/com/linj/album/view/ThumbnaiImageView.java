package com.linj.album.view;

import com.linj.cameralibrary.R;
import com.linj.imageloader.DisplayImageOptions;
import com.linj.imageloader.ImageLoader;
import com.linj.imageloader.displayer.RoundedBitmapDisplayer;

import android.R.bool;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;


public class ThumbnaiImageView extends FrameLayout  {
	public static  final String TAG="AlbumItemView";
	private final ViewHolder mViewHolder;
	private final ImageLoader mImageLoader;
	private final DisplayImageOptions mOptions;
	private String mPath;
	private int mPosition;

	public ThumbnaiImageView(Context context,ImageLoader imageLoader,DisplayImageOptions options) {
		super(context);
		inflate(context, R.layout.item_album_grid, this);
		FilterImageView imageView=(FilterImageView) findViewById(R.id.imgThumbnail);
		CheckBox checkBox=(CheckBox) findViewById(R.id.checkbox);
		ImageView icon=(ImageView)findViewById(R.id.videoicon);
		mViewHolder=new ViewHolder(imageView,checkBox,icon);
		this.mImageLoader=imageLoader;
		this.mOptions=options;
	}


	public void setTags(String path,int position,boolean editable,boolean checked){

		if (editable) {
			mViewHolder.checkBox.setVisibility(View.VISIBLE);
			mViewHolder.checkBox.setChecked(checked);
		}else {
			mViewHolder.checkBox.setVisibility(View.GONE);
		}

		if (mPath==null||!mPath.equals(path)) {
			mImageLoader.loadImage(path, mViewHolder.imageView, mOptions);
			mPath=path;

			mViewHolder.checkBox.setTag(path);
			setTag(path);
			if(mPath.contains("video")){
				mViewHolder.videoIconView.setVisibility(View.VISIBLE);
			}else {
				mViewHolder.videoIconView.setVisibility(View.GONE);
			}
			mPosition=position;
		}
	}

	public int getPosition(){
		return mPosition;
	}

	public void setOnCheckedChangeListener(OnCheckedChangeListener listener){
		mViewHolder.checkBox.setOnCheckedChangeListener(listener);
	}

	@Override
	public void setOnClickListener(OnClickListener l) {

		mViewHolder.imageView.setOnClickListener(l);
	}

	public class ViewHolder {
		public ViewHolder(ImageView imageView,CheckBox checkBox,ImageView icon){
			this.imageView=imageView;
			this.checkBox=checkBox;
			this.videoIconView=icon;
		}
		ImageView imageView;
		ImageView videoIconView;
		CheckBox checkBox;

	}
}
