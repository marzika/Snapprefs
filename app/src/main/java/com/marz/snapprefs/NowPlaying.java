package com.marz.snapprefs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by stirante
 */
public class NowPlaying {
    private final static int WIDTH = 886;
    private final static int HEIGHT = 1575;

    private static volatile String artist = "";
    private static volatile String album = "";
    private static volatile String title = "";
    private static volatile Bitmap albumArt;
    private static volatile boolean playing = false;
    private static volatile boolean isBitmapReady = false;
    private static View nowPlaying;
    private static Bitmap bitmap;
    private static GetSpotifyTrackTask lastTask;
    private static int layoutNumber = 0;
    private static boolean landscape = false;
    private static boolean careAboutOtherPlayersThanSpotify = true;//SUPERIOR PLAYER

    /**
     * This is EXTREMELY slow, but works
     */
    private static void rotate(Bitmap source, Bitmap target) {
        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                target.setPixel(source.getHeight() - y - 1, x, source.getPixel(x, y));
            }
        }
    }

    public static void changeLayout() {
        layoutNumber++;
        if (layoutNumber > 1) {
            layoutNumber = 0;
//            landscape = !landscape;
        }
        int id;
        switch (layoutNumber) {
            case 0:
                id = R.layout.now_playing;
                break;
            case 1:
                id = R.layout.now_playing_2;
                break;
            default:
                id = R.layout.now_playing;
                break;
        }
        Context myContext;
        try {
            myContext = HookMethods.context.createPackageContext("com.marz.snapprefs", Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }
        nowPlaying = LayoutInflater.from(myContext).inflate(id, null);
        generateBitmap();
    }

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
        iF.addAction("com.spotify.music.playbackstatechanged");
        //Poweramp
        iF.addAction("com.maxmpz.audioplayer.TRACK_CHANGED");
        HookMethods.context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase("com.spotify.music.playbackstatechanged") && intent.hasExtra("playing")) {
                    careAboutOtherPlayersThanSpotify = !intent.getBooleanExtra("playing", false);
                } else if (!careAboutOtherPlayersThanSpotify) {
                    return;
                }
                String artist = intent.getStringExtra("artist");
                if (artist == null) artist = "";
                String album = intent.getStringExtra("album");
                if (album == null) album = "";
                String title = intent.getStringExtra("track");
                if (title == null) {
                    title = intent.getStringExtra("title");
                }
                if (title == null) title = "";
                if (intent.hasExtra("playing")) {
                    playing = intent.getBooleanExtra("playing", title.isEmpty());
                } else if (title.isEmpty()) {
                    playing = false;
                }
                if (!playing) return;
                if (intent.getAction().equalsIgnoreCase("com.spotify.music.playbackstatechanged") && !intent.getStringExtra("id").contains("spotify:local")) {
                    String id = intent.getStringExtra("id").split(":")[2];
                    if (lastTask == null || !id.equals(lastTask.id)) {
                        if (lastTask != null)
                            lastTask.cancel(true);
                        lastTask = new GetSpotifyTrackTask(id);
                    }
                }
                if (!NowPlaying.artist.equalsIgnoreCase(artist) || !NowPlaying.album.equalsIgnoreCase(album) || !NowPlaying.title.equalsIgnoreCase(title)) {
                    isBitmapReady = false;
                    if (!intent.getAction().equalsIgnoreCase("com.spotify.music.playbackstatechanged")) {
                        if (lastTask != null) lastTask.cancel(true);
                        lastTask = new GetSpotifyTrackTask(artist, album, title);
                    }
                }
                NowPlaying.artist = artist;
                NowPlaying.album = album;
                NowPlaying.title = title;
            }
        }, iF);
    }

    private static void generateBitmap() {
        if (bitmap == null)
            bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        if (landscape) {
            Bitmap bmp = Bitmap.createBitmap(HEIGHT, WIDTH, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bmp);
            if (albumArt != null)
                ((ImageView) nowPlaying.findViewById(R.id.albumImage)).setImageBitmap(albumArt);
            else
                ((ImageView) nowPlaying.findViewById(R.id.albumImage)).setImageResource(R.drawable.no_cover);
            ((TextView) nowPlaying.findViewById(R.id.album)).setText(album);
            ((TextView) nowPlaying.findViewById(R.id.title)).setText(title);
            ((TextView) nowPlaying.findViewById(R.id.artist)).setText(artist);
            nowPlaying.setLayoutParams(new RelativeLayout.LayoutParams(HEIGHT, WIDTH));
            nowPlaying.measure(View.MeasureSpec.makeMeasureSpec(nowPlaying.getLayoutParams().width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(nowPlaying.getLayoutParams().height, View.MeasureSpec.EXACTLY));
            nowPlaying.layout(0, 0, HEIGHT, WIDTH);
            nowPlaying.draw(c);
            rotate(bmp, bitmap);
        } else {
            Canvas c = new Canvas(bitmap);
            c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            if (albumArt != null)
                ((ImageView) nowPlaying.findViewById(R.id.albumImage)).setImageBitmap(albumArt);
            else
                ((ImageView) nowPlaying.findViewById(R.id.albumImage)).setImageResource(R.drawable.no_cover);
            ((TextView) nowPlaying.findViewById(R.id.album)).setText(album);
            ((TextView) nowPlaying.findViewById(R.id.title)).setText(title);
            ((TextView) nowPlaying.findViewById(R.id.artist)).setText(artist);
            nowPlaying.setLayoutParams(new RelativeLayout.LayoutParams(WIDTH, HEIGHT));
            nowPlaying.measure(View.MeasureSpec.makeMeasureSpec(nowPlaying.getLayoutParams().width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(nowPlaying.getLayoutParams().height, View.MeasureSpec.EXACTLY));
            nowPlaying.layout(0, 0, WIDTH, HEIGHT);
            nowPlaying.draw(c);
        }
        isBitmapReady = true;
    }

    public static boolean isPlaying() {
        return playing;
    }

    public static Bitmap getBitmap() {
        if (!isBitmapReady)
            generateBitmap();
        return bitmap;
    }

    static class GetSpotifyTrackTask extends AsyncTask<Void, Void, Bitmap> {

        public static final String SPOTIFY_TRACKS = "https://api.spotify.com/v1/tracks/";
        public static final String SPOTIFY_SEARCH = "https://api.spotify.com/v1/search?q=artist:%s+album:%s+track:%s&type=track";
        private String artist;
        private String album;
        private String track;
        private boolean search = false;
        private String id = "";

        public GetSpotifyTrackTask(String id) {
            this.id = id;
            execute();
        }

        public GetSpotifyTrackTask(String artist, String album, String track) {
            this.artist = artist;
            this.album = album;
            this.track = track;
            search = true;
            execute();
        }

        @Override
        protected void onPostExecute(Bitmap bmp) {
            Bitmap oldOne = albumArt;
            albumArt = bmp;
            generateBitmap();
            if (oldOne != null) oldOne.recycle();
            Logger.log("Album art loaded!");
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            if (isCancelled()) return null;
            String jsonString;
            try {
                String str;
                if (!search) str = SPOTIFY_TRACKS + id;
                else
                    str = String.format(SPOTIFY_SEARCH, artist.replaceAll(" ", "+"), album.replaceAll(" ", "+"), track.replaceAll(" ", "+"));
                URL website = new URL(str);
                HttpURLConnection connection = (HttpURLConnection) website.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    response.append(inputLine);
                in.close();
                connection.disconnect();
                jsonString = response.toString();
            } catch (Throwable t) {
                t.printStackTrace();
                Logger.log("Failed to retrieve track info from Spotify!");
                Logger.log(t);
                return null;
            }
            if (isCancelled()) return null;
            String urlString;
            try {
                JSONObject json = new JSONObject(jsonString);
                if (!search)
                    urlString = json.getJSONObject("album").getJSONArray("images").getJSONObject(1).getString("url");
                else {
                    if (json.getJSONObject("tracks").getInt("total") == 1) {
                        urlString = json.getJSONObject("tracks").getJSONArray("items").getJSONObject(0).getJSONObject("album").getJSONArray("images").getJSONObject(1).getString("url");
                    } else return null;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Logger.log("Failed to parse response from Spotify!");
                Logger.log(e);
                return null;
            }
            if (isCancelled()) return null;
            InputStream input = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Logger.log("Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
                    return null;
                }
                input = connection.getInputStream();
                Bitmap bmp = BitmapFactory.decodeStream(input);
                if (isCancelled()) return null;
                return bmp;
            } catch (Exception e) {
                e.printStackTrace();
                Logger.log("Failed to retrieve album art from Spotify!");
                Logger.log(e);
                return null;
            } finally {
                try {
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }
                if (connection != null)
                    connection.disconnect();
            }
        }
    }

}
