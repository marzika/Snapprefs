package com.marz.snapprefs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by stirante
 */
public class NowPlaying {
    private final static int WIDTH = 886;
    private final static int HEIGHT = 1575;

    private static volatile String artist = "";
    private static volatile String album = "";
    private static volatile String title = "";
    private static volatile String albumId = "";
    private static volatile boolean playing = false;
    private static volatile boolean isBitmapReady = false;
    private static View nowPlaying;
    private static Bitmap bitmap;

    public static void init() {
        Context myContext;
        try {
            myContext = HookMethods.context.createPackageContext("com.marz.snapprefs", Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }
        nowPlaying = LayoutInflater.from(myContext).inflate(R.layout.now_playing, null);
        IntentFilter iF = new IntentFilter();

        iF.addAction("com.andrew.apollo.metachanged");

        iF.addAction("com.android.music.queuechanged");
        iF.addAction("com.android.music.playstatechanged");
        iF.addAction("com.android.music.playbackcomplete");
        iF.addAction("com.android.music.metachanged");
        //HTC Music
        iF.addAction("com.htc.music.playstatechanged");
        iF.addAction("com.htc.music.playbackcomplete");
        iF.addAction("com.htc.music.metachanged");
        //MIUI Player
        iF.addAction("com.miui.player.playstatechanged");
        iF.addAction("com.miui.player.playbackcomplete");
        iF.addAction("com.miui.player.metachanged");
        //Real
        iF.addAction("com.real.IMP.playstatechanged");
        iF.addAction("com.real.IMP.playbackcomplete");
        iF.addAction("com.real.IMP.metachanged");
        //SEMC Music Player
        iF.addAction("com.sonyericsson.music.playbackcontrol.ACTION_TRACK_STARTED");
        iF.addAction("com.sonyericsson.music.playbackcontrol.ACTION_PAUSED");
        iF.addAction("com.sonyericsson.music.TRACK_COMPLETED");
        iF.addAction("com.sonyericsson.music.metachanged");
        iF.addAction("com.sonyericsson.music.playbackcomplete");
        iF.addAction("com.sonyericsson.music.playstatechanged");
        //rdio
        iF.addAction("com.rdio.android.metachanged");
        iF.addAction("com.rdio.android.playstatechanged");
        //Samsung Music Player
        iF.addAction("com.samsung.sec.android.MusicPlayer.playstatechanged");
        iF.addAction("com.samsung.sec.android.MusicPlayer.playbackcomplete");
        iF.addAction("com.samsung.sec.android.MusicPlayer.metachanged");
        iF.addAction("com.sec.android.app.music.playstatechanged");
        iF.addAction("com.sec.android.app.music.playbackcomplete");
        iF.addAction("com.sec.android.app.music.metachanged");
        //Winamp
        iF.addAction("com.nullsoft.winamp.playstatechanged");
        iF.addAction("com.nullsoft.winamp.metachanged");
        //Amazon
        iF.addAction("com.amazon.mp3.playstatechanged");
        iF.addAction("com.amazon.mp3.metachanged");
        //Rhapsody
        iF.addAction("com.rhapsody.playstatechanged");
        //PowerAmp
        iF.addAction("com.maxmpz.audioplayer.playstatechanged");
        //Last.fm
        iF.addAction("fm.last.android.metachanged");
        iF.addAction("fm.last.android.playbackpaused");
        iF.addAction("fm.last.android.playbackcomplete");
        //A simple last.fm scrobbler
        iF.addAction("com.adam.aslfms.notify.playstatechanged");
        //Scrobble Droid
        iF.addAction("net.jjc1138.android.scrobbler.action.MUSIC_STATUS");
        //Spotify
//        iF.addAction("com.spotify.music.metadatachanged");
        iF.addAction("com.spotify.music.playbackstatechanged");
//        iF.addAction("com.spotify.music.queuechanged");
        HookMethods.context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                XposedBridge.log(intent.getAction());
                for (String key : intent.getExtras().keySet()) {
                    XposedBridge.log(key + ": " + intent.getExtras().get(key));
                }
                XposedBridge.log("----");
                String artist = intent.getStringExtra("artist");
                if (artist == null) artist = "";
                String album = intent.getStringExtra("album");
                if (album == null) album = "";
                String title = intent.getStringExtra("track");
                if (title == null) title = "";
                if (intent.hasExtra("playing")) {
                    playing = intent.getBooleanExtra("playing", false);
                } else if (title.isEmpty()) {
                    playing = false;
                    return;
                }
                if (!NowPlaying.artist.equalsIgnoreCase(artist) || !NowPlaying.album.equalsIgnoreCase(album) || !NowPlaying.title.equalsIgnoreCase(title)) {
                    isBitmapReady = false;
                }
                NowPlaying.artist = artist;
                NowPlaying.album = album;
                NowPlaying.title = title;
                albumId = intent.getStringExtra("albumId");
                playing = intent.getBooleanExtra("playing", true);
            }
        }, iF);
    }

    private static void generateBitmap() {
        if (bitmap == null)
        bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        try {
//            Cursor cursor = HookMethods.context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
//                    new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ALBUM_ART},
//                    /*MediaStore.Audio.Albums.ALBUM + "=?"*/"1=1",
//                    new String[]{album},
//                    null);
//
//            if (cursor != null) {
//                while (cursor.moveToNext()) {
//                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
//                    String a = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM));
//                    XposedBridge.log("Path: " + path + ", album: " + a);
//                }
//                cursor.close();
//            }

            //TODO: Album art!
//        ((ImageView) nowPlaying.findViewById(R.id.albumImage)).setImageBitmap(albumArt);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        ((TextView) nowPlaying.findViewById(R.id.album)).setText(album);
        ((TextView) nowPlaying.findViewById(R.id.title)).setText(title);
        ((TextView) nowPlaying.findViewById(R.id.artist)).setText(artist);
        nowPlaying.setLayoutParams(new RelativeLayout.LayoutParams(WIDTH, HEIGHT));
        nowPlaying.measure(View.MeasureSpec.makeMeasureSpec(nowPlaying.getLayoutParams().width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(nowPlaying.getLayoutParams().height, View.MeasureSpec.EXACTLY));
        nowPlaying.layout(0, 0, WIDTH, HEIGHT);
        nowPlaying.draw(c);
    }

    public static boolean isPlaying() {
        return playing;
    }

    public static Bitmap getBitmap() {
        if (!isBitmapReady)
            generateBitmap();
        return bitmap;
    }
}
