package com.marz.snapprefs.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.marz.snapprefs.Logger;
import com.marz.snapprefs.R;
import com.marz.snapprefs.Util.DownloadedFilterPreview;
import com.marz.snapprefs.Util.DrawableManager;
import com.marz.snapprefs.Util.FilterPreview;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FilterFragment extends Fragment {

    private GridView gridView;
    private ProgressDialog progress;
    private ArrayList<RedditFilter> filters = new ArrayList<>();
    private boolean loading = false;
    private String after = null;
    public static File filtersDir = new File(Environment.getExternalStorageDirectory() + "/Snapprefs/Filters/");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.filter_layout, container, false);
        filtersDir.mkdirs();
        gridView = (GridView) v.findViewById(R.id.filter_grid);
        gridView.setAdapter(new FilterAdapter());
        gridView.setNumColumns(2);
        progress = ProgressDialog.show(getActivity(), "Loading", "Please wait", true);
        new LoadFilters().execute();
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount >= totalItemCount) {
                    if (!loading) {
                        new LoadFilters().execute();
                        progress.setMessage("Loading filters");
                        progress.show();
                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*if (filters.get(position).downloaded) return;
                System.out.println(filters.get(position).url);
                new SaveFilter(filters.get(position)).execute();
                ((ViewHolder) view.getTag()).title.setBackgroundColor(getResources().getColor(R.color.primary));
                */
                Intent i = new Intent(getActivity(), FilterPreview.class);
                RedditFilter rf = (RedditFilter) parent.getItemAtPosition(position);
                i.putExtra("imagePath", "" + rf.url);
                i.putExtra("imageId", "" + rf.id);
                i.putExtra("visual", false);
                startActivity(i);
            }
        });
        return v;
    }

    class FilterAdapter extends BaseAdapter {
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.filter_element, parent, false);
                vh = new ViewHolder();
                vh.image = (ImageView) convertView.findViewById(R.id.filter);
                vh.title = (TextView) convertView.findViewById(R.id.filter_title);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            vh.filter = filters.get(position);
            vh.image.setImageBitmap(null);
            String text = vh.filter.name + "\nAuthor: " + vh.filter.author + "\n Score: " + vh.filter.score;
            vh.title.setText(text);
            DrawableManager.fetchDrawableOnThread(vh.filter.preview, vh.image);
            if (vh.filter.downloaded)
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
        RedditFilter filter;
        TextView title;
        ImageView image;
    }

    class RedditFilter {
        public String url;
        String id;
        String name;
        String preview;
        String image;
        String author;
        int score;
        boolean downloaded;

        @Override
        public String toString() {
            return "RedditFilter{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", preview='" + preview + '\'' +
                    ", image='" + image + '\'' +
                    ", author='" + author + '\'' +
                    ", score=" + score +
                    ", downloaded=" + downloaded +
                    ", url='" + url + '\'' +
                    '}';
        }

        public String getUrl() {
            return this.url;
        }
    }

    class LoadFilters extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            loading = true;
        }

        @Override
        protected Void doInBackground(Void... params) {
            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
            String uri = "https://www.reddit.com/r/snapprefs/hot.json?limit=25";
            if (after != null && !after.isEmpty()) {
                uri += "&after=" + after;
            }
            HttpGet get = new HttpGet(uri);
            get.setHeader("Content-type", "application/json");

            InputStream inputStream = null;
            String result = null;
            try {
                HttpResponse response = httpclient.execute(get);
                HttpEntity entity = response.getEntity();
                inputStream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                result = sb.toString();
            } catch (Exception ignored) {
            } finally {
                try {
                    if (inputStream != null) inputStream.close();
                } catch (Exception ignored) {
                }
            }
            try {
                JSONObject jObject = new JSONObject(result);
                after = jObject.getJSONObject("data").getString("after");
                JSONArray jsonArray = jObject.getJSONObject("data").getJSONArray("children");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i).getJSONObject("data");
                    if (obj.getString("domain").equalsIgnoreCase("self.snapprefs") || !obj.has("preview") || obj.getString("title").toLowerCase().contains("[request]"))
                        continue;
                    RedditFilter f = new RedditFilter();
                    f.id = obj.getString("id");
                    f.score = obj.getInt("score");
                    f.name = obj.getString("title");
                    f.preview = obj.getString("thumbnail");
                    f.image = obj.getJSONObject("preview").getJSONArray("images").getJSONObject(0).getJSONObject("source").getString("url");
                    f.author = obj.getString("author");
                    f.downloaded = new File(filtersDir, f.id + ".png").exists();
                    f.url = obj.getString("url");
                    if(f.name.toLowerCase().contains("filter") && !f.name.toLowerCase().contains("filterpack") && !f.url.toLowerCase().contains("/a/") && !f.url.toLowerCase().contains("reddituploads")){
                        f.name = f.name.substring(f.name.indexOf("]")+1);
                        filters.add(f);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progress.dismiss();
            ((FilterAdapter) gridView.getAdapter()).notifyDataSetChanged();
            gridView.invalidateViews();
            loading = false;
        }
    }

    class SaveFilter extends AsyncTask<Void, Void, Boolean> {

        private RedditFilter redditFilter;

        public SaveFilter(RedditFilter redditFilter) {
            this.redditFilter = redditFilter;
        }

        @Override
        protected void onPreExecute() {
            progress.setMessage("Downloading filter");
            progress.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet request = new HttpGet(redditFilter.url);
                HttpResponse response = httpClient.execute(request);
                InputStream input = response.getEntity().getContent();
                File f = new File(filtersDir, redditFilter.id + ".png");
                FileOutputStream output = new FileOutputStream(f);
                try {
                    byte[] buffer = new byte[4096];
                    int read;

                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                    output.flush();
                } finally {
                    output.close();
                    input.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progress.dismiss();
            if (result)
                redditFilter.downloaded = true;
            else
                Toast.makeText(getActivity(), "Failed to download filter!", Toast.LENGTH_LONG).show();
            DownloadedFiltersFragment.buttonReload.performClick();
        }
    }
}

