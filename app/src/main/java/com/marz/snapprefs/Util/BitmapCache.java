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
    private static int cacheSize;
    private int currentMemUsage = 0;

    public BitmapCache(int memoryModifier) {
        super(maxMemory / memoryModifier);
        cacheSize = maxMemory / memoryModifier;
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

        if( oldValue != null ) {
            currentMemUsage -= sizeOf(null, oldValue);

            Logger.log("Bitmap removed from cache [MemUsage:" + currentMemUsage + "/" + cacheSize + "]");

            if (!oldValue.isRecycled()) {
                oldValue.recycle();
                Logger.log("Recycled removed bitmap");
            }
        }
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            this.put(key, bitmap);
            currentMemUsage+= sizeOf(null, bitmap);
            Logger.log("Added image to memory cache [MemUsage:" + currentMemUsage + "/" + cacheSize + "]");
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return this.get(key);
    }

    public void clearCache() {
        this.evictAll();
        Logger.log("Evicted " + this.evictionCount() + " bitmaps from cache", LogType.DEBUG);
        currentMemUsage = 0;
    }
}
