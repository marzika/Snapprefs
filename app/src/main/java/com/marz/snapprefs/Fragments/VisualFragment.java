package com.marz.snapprefs.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.marz.snapprefs.R;
import com.marz.snapprefs.Util.DrawableManager;
import com.marz.snapprefs.Util.FilterPreview;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class VisualFragment extends Fragment {
    static Activity mActivity;
    static Context mContext;

    GridView gridView;
    public static FilterAdapter mAdapter;
    private static ArrayList<VisualFilter> filters = new ArrayList<>();

    private static SharedPreferences prefs;

    private ProgressDialog mProgressDialog;
    private final String destination = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Snapprefs/VisualFilters";
    private final String filePath = destination + "/visualfilters.zip";

    private static boolean mAmaro = false;
    private static boolean mF1997 = false;
    private static boolean mBrannan = false;
    private static boolean mEarlybird = true;
    private static boolean mHefe = false;
    private static boolean mHudson = false;
    private static boolean mInkwell = false;
    private static boolean mLomo = true;
    private static boolean mLordKelvin = false;
    private static boolean mNashville = false;
    private static boolean mRise = true;
    private static boolean mSierra = false;
    private static boolean mSutro = false;
    private static boolean mToaster = true;
    private static boolean mValencia = false;
    private static boolean mWalden = false;
    private static boolean mXproll = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.filter_layout,null);
        mActivity = getActivity();
        mContext = getActivity();
        File f = new File(destination);
        if (!f.exists()) {
            f.mkdir();
        }
        File vfilters = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapprefs/VisualFilters/xpro_map.png");
        if (!vfilters.exists()) {
            startDownload();
        }
        refreshPreferences();
        addFilters();
        gridView = (GridView) view.findViewById(R.id.filter_grid);
        gridView.setPadding(0,0,0,0);
        mAdapter = new FilterAdapter();
        gridView.setAdapter(mAdapter);
        gridView.setNumColumns(2);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), FilterPreview.class);
                VisualFilter rf = (VisualFilter) parent.getItemAtPosition(position);
                i.putExtra("imagePath", "" + rf.preview);
                i.putExtra("imageId", "" + rf.name);
                i.putExtra("visual", true);
                i.putExtra("enabled", rf.enabled);
                startActivity(i);
            }
        });

        return view;
    }

    public static void addFilters() {
        filters.clear();
        filters.add(new VisualFilter("AMARO", "AMARO", "http://snapprefs.com/amaro.jpg", mAmaro));
        filters.add(new VisualFilter("1997", "F1997", "http://snapprefs.com/1997.jpg", mF1997));
        filters.add(new VisualFilter("BRANNAN", "BRANNAN", "http://snapprefs.com/brannan.jpg", mBrannan));
        filters.add(new VisualFilter("EARLYBIRD", "EARLYBIRD", "http://snapprefs.com/earlybird.jpg", mEarlybird));
        filters.add(new VisualFilter("HEFE", "HEFE", "http://snapprefs.com/hefe.jpg", mHefe));
        filters.add(new VisualFilter("HUDSON", "HUDSON", "http://snapprefs.com/hudson.jpg", mHudson));
        filters.add(new VisualFilter("INKWELL", "INKWELL", "http://snapprefs.com/inkwell.jpg", mInkwell));
        filters.add(new VisualFilter("LOMO", "LOMO", "http://snapprefs.com/lomo.jpg", mLomo));
        filters.add(new VisualFilter("LORD_KELVIN", "LORD_KELVIN", "http://snapprefs.com/lord_kelvin.jpg", mLordKelvin));
        filters.add(new VisualFilter("NASHVILLE", "NASHVILLE", "http://snapprefs.com/nashville.jpg", mNashville));
        filters.add(new VisualFilter("RISE", "RISE", "http://snapprefs.com/rise.jpg", mRise));
        filters.add(new VisualFilter("SIERRA", "SIERRA", "http://snapprefs.com/sierra.jpg", mSierra));
        filters.add(new VisualFilter("SUTRO", "SUTRO", "http://snapprefs.com/sutro.jpg", mSutro));
        filters.add(new VisualFilter("TOASTER", "TOASTER", "http://snapprefs.com/toaster.jpg", mToaster));
        filters.add(new VisualFilter("RISE", "RISE", "http://snapprefs.com/rise.jpg", mRise));
        filters.add(new VisualFilter("VALENCIA", "VALENCIA", "http://snapprefs.com/valencia.jpg", mValencia));
        filters.add(new VisualFilter("WALDEN", "WALDEN", "http://snapprefs.com/walden.jpg", mWalden));
        filters.add(new VisualFilter("XPROLL", "XPROLL", "http://snapprefs.com/xproll.jpg", mXproll));
        if(mAdapter!= null) mAdapter.notifyDataSetChanged();
    }

    class FilterAdapter extends BaseAdapter {
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.visual_element, parent, false);
                vh = new ViewHolder();
                vh.image = (ImageView) convertView.findViewById(R.id.visual);
                vh.title = (TextView) convertView.findViewById(R.id.visual_title);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            vh.filter = filters.get(position);
            vh.image.setImageBitmap(null);
            vh.title.setText(vh.filter.name);
            DrawableManager.fetchDrawableOnThread(vh.filter.preview, vh.image);
            if (vh.filter.enabled)
                vh.title.setBackgroundColor(getResources().getColor(R.color.primary));
            else
                vh.title.setBackgroundColor(0xaa000000);
            return convertView;
        }

        @Override
        public int getCount() {
            return filters.size();
        }

        @Override
        public Object getItem(int position) {
            return filters.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

    class ViewHolder {
        VisualFilter filter;
        TextView title;
        ImageView image;
    }
    static class VisualFilter {
        String name;
        String pref;
        String preview;
        boolean enabled;
        public VisualFilter(String name, String pref, String preview, boolean enabled){
            this.name = name;
            this.pref = pref;
            this.preview = preview;
            this.enabled = enabled;
        }
    }
    public static void refreshPreferences() {
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mAmaro = prefs.getBoolean("AMARO", mAmaro);
        mF1997 = prefs.getBoolean("F1997", mF1997);
        mBrannan  = prefs.getBoolean("BRANNAN", mBrannan );
        mEarlybird   = prefs.getBoolean("EARLYBIRD", mEarlybird  );
        mHefe  = prefs.getBoolean("HEFE", mHefe);
        mHudson  = prefs.getBoolean("HUDSON", mHudson);
        mInkwell  = prefs.getBoolean("INKWELL", mInkwell);
        mLomo  = prefs.getBoolean("LOMO", mLomo);
        mLordKelvin  = prefs.getBoolean("LORD_KELVIN", mLordKelvin);
        mNashville  = prefs.getBoolean("NASHVILLE", mNashville);
        mRise  = prefs.getBoolean("RISE", mRise);
        mSierra  = prefs.getBoolean("SIERRA", mSierra);
        mSutro  = prefs.getBoolean("SUTRO", mSutro);
        mToaster  = prefs.getBoolean("TOASTER", mToaster);
        mValencia  = prefs.getBoolean("VALENCIA", mValencia);
        mWalden  = prefs.getBoolean("WALDEN", mWalden);
        mXproll  = prefs.getBoolean("XPROLL", mXproll);
    }
    private void startDownload() {
        String url = "http://snapprefs.com/visualfilters.zip";
        new DownloadFileAsync().execute(url);
    }

    class DownloadFileAsync extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage("Downloading file..");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... aurl) {
            int count = 0;
            try {

                URL url = new URL(aurl[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();

                int lenghtOfFile = conexion.getContentLength();
                Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);

                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(destination + "/visualfilters.zip");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(""+(int)((total*100)/lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {}
            return null;

        }
        protected void onProgressUpdate(String... progress) {
            Log.d("ANDRO_ASYNC", progress[0]);
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            mProgressDialog.dismiss();
            mProgressDialog.setProgress(0);
            new UnzipFileAsync().execute();
        }
    }
    class UnzipFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage("Unzipping file..");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... aurl) {
            int count = 0;
            try {
                FileInputStream inputStream = new FileInputStream(filePath);
                ZipInputStream zipStream = new ZipInputStream(inputStream);
                ZipEntry zEntry = null;
                while ((zEntry = zipStream.getNextEntry()) != null) {
                    Log.d("Unzip", "Unzipping " + zEntry.getName() + " at "
                            + destination);

                    if (zEntry.isDirectory()) {
                        handleDirectory(zEntry.getName());
                    } else {
                        FileOutputStream fout = new FileOutputStream(destination + "/" + zEntry.getName());
                        BufferedOutputStream bufout = new BufferedOutputStream(fout);
                        byte[] buffer = new byte[1024];
                        int read = 0;
                        while ((read = zipStream.read(buffer)) != -1) {
                            bufout.write(buffer, 0, read);
                        }
                        zipStream.closeEntry();
                        bufout.close();
                        fout.close();
                        count++;
                        Log.d("Unzip", "Unzipping filenumber: " + count);
                        publishProgress(String.valueOf(count));
                    }
                }
                zipStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }
        protected void onProgressUpdate(String... progress) {
            Log.d("UNZIP_PROGRESS", progress[0]);
            mProgressDialog.setProgress(Integer.parseInt(progress[0])*(100/43));
        }

        @Override
        protected void onPostExecute(String unused) {
            mProgressDialog.dismiss();
        }
    }
    public void handleDirectory(String dir) {
        File f = new File(this.destination + dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }
}
