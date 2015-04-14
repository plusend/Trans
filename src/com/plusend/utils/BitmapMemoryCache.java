package com.plusend.utils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class BitmapMemoryCache {
	private static BitmapMemoryCache instance;
	private static final int cacheSize = 10 * 1024;
	private LruCache<String, Bitmap> mMemoryCache;

	public static BitmapMemoryCache getInstance() {
		synchronized (BitmapMemoryCache.class) {
			if (instance == null) {
				instance = new BitmapMemoryCache();
			}
		}
		return instance;
	}

	public BitmapMemoryCache() {
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize);
		// {
		// @SuppressLint("NewApi")
		// protected int sizeOf(String key, Bitmap bitmap) {
		// // 重写此方法来衡量每张图片的大小，默认返回图片数量。
		// return bitmap.getByteCount() / 1024;
		// }
		// };
	}

	public void addBitmap(String key, Bitmap bitmap) {
		synchronized (mMemoryCache) {
			if (key == null || bitmap == null) {
				Debug.w("BitmapMemoryCache", "Null key and bitmap");
				Debug.w("BitmapMemoryCache", "key = " + key);
				Debug.w("BitmapMemoryCache", "bitmap = " + bitmap);
				return;
			}
			mMemoryCache.put(key, bitmap);
		}
	}

	public Bitmap getBitmap(String key) {
		synchronized (mMemoryCache) {
			return mMemoryCache.get(key);
		}
	}

}
