package com.marz.snapprefs;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.marz.snapprefs.FilterStoreUtils.VisualPreview;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Marcell on 2015.12.05..
 */
public class VisualActivity extends Activity {
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    public static final int DIALOG_UNZIP_PROGRESS = 1;
    private ProgressDialog mProgressDialog;
    private final String destination = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Snapprefs/VisualFilters";
    private final String filePath = destination + "/visualfilters.zip";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual);
        Button download = (Button) findViewById(R.id.download);
        Button preview = (Button) findViewById(R.id.preview);
        Button manage = (Button) findViewById(R.id.manage);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDownload();
            }
        });
        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), VisualPreview.class);
                startActivity(intent);
            }
        });
        manage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace(R.id.framelayout2, new VisualSettings()).commit();
                PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.visual_preferences, false);
            }
        });


        File f = new File(destination);
        if (!f.exists()) {
            f.mkdir();
        }
    }
    private void startDownload() {
        String url = "http://snapprefs.com/visualfilters.zip";
        new DownloadFileAsync().execute(url);
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Downloading file..");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
            case DIALOG_UNZIP_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Unzipping file..");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    class DownloadFileAsync extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_DOWNLOAD_PROGRESS);
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
            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
            mProgressDialog.setProgress(0);
            new UnzipFileAsync().execute();
        }
    }
    class UnzipFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_UNZIP_PROGRESS);
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
            dismissDialog(DIALOG_UNZIP_PROGRESS);
        }
    }
    public void handleDirectory(String dir) {
        File f = new File(this.destination + dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }
}
