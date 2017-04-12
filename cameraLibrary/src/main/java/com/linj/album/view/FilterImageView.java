package com.linj.album.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.ImageView;


public class FilterImageView extends ImageView implements GestureDetector.OnGestureListener{
	public static final String TAG="FilterImageView";

	private GestureDetector mGestureDetector;
	public FilterImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mGestureDetector=new GestureDetector(context, this);
	
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if(event.getActionMasked()== MotionEvent.ACTION_CANCEL
				||event.getActionMasked()== MotionEvent.ACTION_UP){
			removeFilter();
		}
		return mGestureDetector.onTouchEvent(event);
	}


	private void setFilter() {

		Drawable drawable=getDrawable();

		if (drawable==null) {
			drawable=getBackground();
		}
		if(drawable!=null){

			drawable.setColorFilter(Color.GRAY,PorterDuff.Mode.MULTIPLY);;
		}
	}

	private void removeFilter() {

		Drawable drawable=getDrawable();

		if (drawable==null) {
			drawable=getBackground();
		}
		if(drawable!=null){

			drawable.clearColorFilter();
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		setFilter();

		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
	
		performClick();	
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		performLongClick();
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}
}
