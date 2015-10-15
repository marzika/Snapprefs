package com.marz.snapprefs.FilterStoreUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.marz.snapprefs.R;

import java.io.File;
import java.util.ArrayList;

/**
 * @author mwho
 */
public class Tab1Fragment extends Fragment {
    public static boolean toReload = false;
    /**
     * (non-Javadoc)
     *
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    AsyncTaskLoadFiles myAsyncTaskLoadFiles;
    ImageAdapter myImageAdapter;

    public void runReload(View v, GridView gridview, ImageAdapter myImageAdapter, AsyncTaskLoadFiles myAsyncTaskLoadFiles) {
        //Cancel the previous running task, if exist.
        myAsyncTaskLoadFiles.cancel(true);

        //new another ImageAdapter, to prevent the adapter have
        //mixed files
        myImageAdapter = new ImageAdapter(v.getContext());
        gridview.setAdapter(myImageAdapter);
        myAsyncTaskLoadFiles = new AsyncTaskLoadFiles(myImageAdapter);
        myAsyncTaskLoadFiles.execute();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }

        final View v = (View) inflater.inflate(R.layout.myfilters_main, container, false);

        final GridView gridview = (GridView) v.findViewById(R.id.gridview);
        myImageAdapter = new ImageAdapter(v.getContext());
        gridview.setAdapter(myImageAdapter);

		/*
         * Move to asyncTaskLoadFiles String ExternalStorageDirectoryPath =
		 * Environment .getExternalStorageDirectory() .getAbsolutePath();
		 *
		 * String targetPath = ExternalStorageDirectoryPath + "/test/";
		 *
		 * Toast.makeText(getApplicationContext(), targetPath,
		 * Toast.LENGTH_LONG).show(); File targetDirector = new
		 * File(targetPath);
		 *
		 * File[] files = targetDirector.listFiles(); for (File file : files){
		 * myImageAdapter.add(file.getAbsolutePath()); }
		 */
        myAsyncTaskLoadFiles = new AsyncTaskLoadFiles(myImageAdapter);
        myAsyncTaskLoadFiles.execute();


        AdapterView.OnItemClickListener myOnItemClickListener = new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                Intent i = new Intent(getActivity(), FilterPreview.class);
                i.putExtra("imagePath", "" + (String) parent.getItemAtPosition(position));
                startActivity(i);

                //String prompt = "remove " + (String) parent.getItemAtPosition(position);
                //Toast.makeText(v.getContext(), prompt, Toast.LENGTH_SHORT)
                //      .show();

                //myImageAdapter.remove(position);
                //myImageAdapter.notifyDataSetChanged();

            }
        };

        gridview.setOnItemClickListener(myOnItemClickListener);

        Button buttonReload = (Button) v.findViewById(R.id.reload);
        buttonReload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                runReload(v, gridview, myImageAdapter, myAsyncTaskLoadFiles);

            }
        });


        return v;
    }

    public class AsyncTaskLoadFiles extends AsyncTask<Void, String, Void> {

        File targetDirector;
        ImageAdapter myTaskAdapter;

        public AsyncTaskLoadFiles(ImageAdapter adapter) {
            myTaskAdapter = adapter;
        }

        @Override
        protected void onPreExecute() {
            String ExternalStorageDirectoryPath = Environment.getExternalStorageDirectory().toString();

            String targetPath = ExternalStorageDirectoryPath + "/Snapprefs/Filters/";
            targetDirector = new File(targetPath);
            myTaskAdapter.clear();

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            File[] files = targetDirector.listFiles();
            if (files != null) {
                if (files.length > 0) {
                    for (File file : files) {
                        publishProgress(file.getAbsolutePath());
                        if (isCancelled()) break;
                    }
                }
            } else {
                targetDirector.mkdir();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            myTaskAdapter.add(values[0]);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void result) {
            myTaskAdapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }

    }

    public class ImageAdapter extends BaseAdapter {

        ArrayList<String> itemList = new ArrayList<String>();
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        void add(String path) {
            if (path.endsWith(".png"))
                itemList.add(path);
        }

        void clear() {
            itemList.clear();
        }

        void remove(int index) {
            itemList.remove(index);
        }

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) { // if it's not recycled, initialize some
                // attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(220, 391));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);

            } else {
                imageView = (ImageView) convertView;
            }

            Bitmap bm = decodeSampledBitmapFromUri(itemList.get(position), 220,
                    391);

            imageView.setImageBitmap(bm);
            return imageView;
        }

        public Bitmap decodeSampledBitmapFromUri(String path, int reqWidth,
                                                 int reqHeight) {

            Bitmap bm = null;
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth,
                    reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            bm = BitmapFactory.decodeFile(path, options);


            return bm;
        }

        public int calculateInSampleSize(

                BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                if (width > height) {
                    inSampleSize = Math.round((float) height
                            / (float) reqHeight);
                } else {
                    inSampleSize = Math.round((float) width / (float) reqWidth);
                }
            }

            return inSampleSize;
        }

    }
}