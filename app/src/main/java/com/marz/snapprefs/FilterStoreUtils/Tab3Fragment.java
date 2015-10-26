package com.marz.snapprefs.FilterStoreUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import com.marz.snapprefs.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


/**
 * @author mwho
 */
public class Tab3Fragment extends Fragment {
    public static Context context1;
    /**
     * (non-Javadoc)
     *
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */

    WebView browser;

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
        View v = (View) inflater.inflate(R.layout.tab4_layout, container, false);
        browser = (WebView) v.findViewById(R.id.webview);
        browser.setWebViewClient(new MyBrowser());
        browser.getSettings().setLoadsImagesAutomatically(true);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        if (savedInstanceState == null) {
            browser.loadUrl("https://plus.google.com/communities/111884042638955665569");
        }
        browser.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    WebView webView = (WebView) v;

                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            if (webView.canGoBack()) {
                                webView.goBack();
                                return true;
                            }
                            break;
                    }
                }

                return false;
            }
        });
        this.registerForContextMenu(browser);

        return v;
    }


    public String saveImage(String strurl, String filen) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        String filepath = null;
        try {
            URL wallpaperURL = new URL(strurl);
            URLConnection connection = wallpaperURL.openConnection();
            InputStream inputStream = new BufferedInputStream(wallpaperURL.openStream(), 10240);
            File filterDir = new File(Environment.getExternalStorageDirectory().toString() + "/Snapprefs");
            File saveFile = new File(filterDir, filen);
            FileOutputStream outputStream = new FileOutputStream(saveFile);

            byte buffer[] = new byte[1024];
            int dataSize;
            int loadedSize = 0;
            while ((dataSize = inputStream.read(buffer)) != -1) {
                loadedSize += dataSize;
                //publishProgress(loadedSize);
                outputStream.write(buffer, 0, dataSize);
            }
            filepath = saveFile.getAbsolutePath();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
        return filepath;
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        // Confirm the view is a webview
        if (v instanceof WebView) {
            WebView.HitTestResult result = ((WebView) v).getHitTestResult();

            if (result != null) {
                int type = result.getType();

                // Confirm type is an image
                if (type == WebView.HitTestResult.IMAGE_TYPE || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                    final String imageUrl = result.getExtra();
                    //Toast.makeText(this, imageUrl, Toast.LENGTH_LONG).show();
                    final Context context = v.getContext();
                    LayoutInflater li = LayoutInflater.from(context);
                    View promptsView = li.inflate(R.layout.save_prompt_layout, null);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            context);

                    // set prompts.xml to alertdialog builder
                    alertDialogBuilder.setView(promptsView);

                    final EditText userInput = (EditText) promptsView
                            .findViewById(R.id.editTextfilename);

                    // set dialog message
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("Save",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            SendfeedbackJob job = new SendfeedbackJob();
                                            job.execute(imageUrl, userInput.getText().toString() + ".png");
                                            context1 = context;
                                            //String locSave = saveImage(imageUrl, userInput.getText().toString());
                                            /*
                                            if(locSave != null){
                                                Toast toast = Toast.makeText(context, locSave, Toast.LENGTH_LONG);
                                                toast.show();
                                            }else{
                                                Toast toast = Toast.makeText(context, "null " + locSave, Toast.LENGTH_LONG);
                                                toast.show();
                                            }
                                            */
                                            Toast toast = Toast.makeText(context, "Attempting Save...", Toast.LENGTH_LONG);
                                            toast.show();
                                            dialog.dismiss();
                                        }
                                    })
                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        browser.saveState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        browser.restoreState(savedInstanceState);
    }
}

class MyBrowser2 extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }

}