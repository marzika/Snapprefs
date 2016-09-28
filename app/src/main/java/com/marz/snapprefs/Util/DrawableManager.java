package com.marz.snapprefs.Util;


import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DrawableManager {
    private static final Map<String, Drawable> drawableMap = new HashMap<>();
    private static Handler handler = new MyHandler();

    public static Drawable fetchDrawable(String urlString) {
        if (drawableMap.containsKey(urlString)) {
            return drawableMap.get(urlString);
        }
        try {
            InputStream is = fetch(urlString);
            Drawable drawable = Drawable.createFromStream(is, "src");
            if (drawable != null)
                drawableMap.put(urlString, drawable);
            return drawable;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Drawable getDrawable(String urlString) {
        return drawableMap.get(urlString);
    }

    public static void fetchDrawableOnThread(final String urlString, final ImageView imageView) {
        if (drawableMap.containsKey(urlString)) {
            imageView.setImageDrawable(drawableMap.get(urlString));
            return;
        }
        Thread thread = new Thread() {
            @Override
            public void run() {
                Drawable drawable = fetchDrawable(urlString);
                DrawableSetter drawableSetter = new DrawableSetter();
                drawableSetter.drawable = drawable;
                drawableSetter.imageView = imageView;
                Message message = handler.obtainMessage(1, drawableSetter);
                handler.sendMessage(message);
            }
        };
        thread.start();
    }

    public static void fetchDrawableOnThread(final String urlString, final FetchCallback callback) {
        if (drawableMap.containsKey(urlString)) {
            callback.onResult(drawableMap.get(urlString));
            return;
        }
        Thread thread = new Thread() {
            @Override
            public void run() {
                Drawable drawable = fetchDrawable(urlString);
                DrawableSetter drawableSetter = new DrawableSetter();
                drawableSetter.drawable = drawable;
                drawableSetter.callback = callback;
                Message message = handler.obtainMessage(1, drawableSetter);
                handler.sendMessage(message);
            }
        };
        thread.start();
    }

    private static InputStream fetch(String urlString) throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet request = new HttpGet(urlString);
        HttpResponse response = httpClient.execute(request);
        return response.getEntity().getContent();
    }

    public interface FetchCallback {
        void onResult(Drawable drawable);
    }

    static class DrawableSetter {
        ImageView imageView;
        Drawable drawable;
        FetchCallback callback;
    }

    static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            DrawableSetter obj = (DrawableSetter) message.obj;
            if (obj.imageView != null) {
                obj.imageView.setImageDrawable(obj.drawable);
                obj.imageView.setVisibility(View.VISIBLE);
            } else if (obj.callback != null) {
                obj.callback.onResult(obj.drawable);
            }
        }
    }

}
