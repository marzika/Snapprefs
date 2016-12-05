package com.marz.snapprefs.Util;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.marz.snapprefs.Logger;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

public class BitmapCache {
    private static final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    private static final int cacheSize = maxMemory / 8;

    private LruCache<String, Bitmap> memoryCache;

    public BitmapCache() {
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key,
                                        Bitmap oldValue, Bitmap newValue) {
                oldValue.recycle();
                Logger.log("Recycling");
            }
        };
    }


    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }

    public void clearCache() {
        memoryCache.evictAll();
    }

    public void recycleAll() {
        for( Bitmap obj : memoryCache.snapshot().values())
            obj.recycle();
    }
}
