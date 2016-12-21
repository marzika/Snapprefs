package com.marz.snapprefs.Util;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Logger.LogType;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

public class BitmapCache extends LruCache<String, Bitmap> {
    private static final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    private static final int cacheSize = maxMemory / 8;

    public BitmapCache() {
        super(cacheSize);
    }

    @Override
    protected int sizeOf(String key, Bitmap bitmap) {
        // The cache size will be measured in kilobytes rather than
        // number of items.
        return bitmap.getByteCount() / 1024;
    }

    @Override
    protected void entryRemoved(boolean evicted, String key,
                                Bitmap oldValue, Bitmap newValue) {
        super.entryRemoved(evicted,key, oldValue, newValue);
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            this.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return this.get(key);
    }

    public void clearCache() {
        this.evictAll();
        Logger.log("Evicted " + this.evictionCount() + " bitmaps from cache", LogType.DEBUG);
    }

    public void recycleAll() {
        for( Bitmap obj : this.snapshot().values())
            obj.recycle();
    }
}
