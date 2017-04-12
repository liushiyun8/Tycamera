package com.liuyun.mycamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.linj.album.view.FilterImageView;

import java.io.File;
import java.util.List;

/**
 * Created by 刘仕云 on 2017/4/12.
 */
public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.MyHolder> {
    Context context;
    List<File> list;
    public MyRecyclerAdapter(Context context,List<File> list){
        this.context=context;
        this.list=list;
    }
    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.myitem_view, null);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        Bitmap thumbBitmap= BitmapFactory.decodeFile(list.get(position).getAbsolutePath());
        if(thumbBitmap!=null){
            holder.mThumbView.setImageBitmap(thumbBitmap);
            if(list.get(position).getAbsolutePath().contains("video")){
                holder.mVideoIconView.setVisibility(View.VISIBLE);
            }else {
                holder.mVideoIconView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        private final FilterImageView mThumbView;
        private final ImageView mVideoIconView;

        public MyHolder(View itemView) {
            super(itemView);
            mThumbView = (FilterImageView) itemView.findViewById(R.id.btn_thumbnail);
            mVideoIconView = (ImageView) itemView.findViewById(R.id.videoicon);
        }
    }
}
