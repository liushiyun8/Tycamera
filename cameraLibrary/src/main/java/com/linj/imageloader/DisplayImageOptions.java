package com.linj.imageloader;

import com.linj.imageloader.displayer.BitmapDisplayer;


public class DisplayImageOptions {
	
	public int imageResOnLoading;
	public int imageResOnFail;
	public boolean cacheInMemory;
	public boolean cacheOnDisk;
	public BitmapDisplayer displayer;
	public boolean fromNet;
	private DisplayImageOptions(Builder builder) {
		this.imageResOnLoading=builder.imageResOnLoading;
		this.imageResOnFail=builder.imageResOnFail;
		this.cacheInMemory=builder.cacheInMemory;
		this.cacheOnDisk=builder.cacheOnDisk;
		this.displayer=builder.displayer;
		this.fromNet=builder.fromNet;
	}

	public static class Builder{
		
		private int imageResOnLoading;
		private int imageResOnFail;
		private boolean cacheInMemory;
		private boolean cacheOnDisk;
		private BitmapDisplayer displayer;
		private boolean fromNet;

		public Builder showImageOnLoading(int imageRes) {
			this.imageResOnLoading = imageRes;
			return this;
		}

		public Builder showImageOnFail(int imageRes) {
			this.imageResOnFail = imageRes;
			return this;
		}

		public Builder cacheInMemory(boolean cacheInMemory) {
			this.cacheInMemory = cacheInMemory;
			return this;
		}

		public Builder cacheOnDisk(boolean cacheOnDisk) {
			this.cacheOnDisk = cacheOnDisk;
			return this;
		}

		public Builder displayer(BitmapDisplayer displayer) {
			if (displayer == null) throw new IllegalArgumentException("displayer can't be null");
			this.displayer = displayer;
			return this;
		}

		public Builder setFromNet(boolean fromNet) {
			this.fromNet = fromNet;
			return this;
		}

		public DisplayImageOptions build() {
			return new DisplayImageOptions(this);
		}

		
	}
}
