package com.marz.snapprefs.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import com.marz.snapprefs.R;
import com.marz.snapprefs.Util.DownloadFilterAsync;

public class GooglePlusFragment extends Fragment {
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
        View v = inflater.inflate(R.layout.gplus_layout, container, false);
        browser = (WebView) v.findViewById(R.id.webview);
        browser.setWebViewClient(new MyBrowser());
        browser.getSettings().setLoadsImagesAutomatically(true);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        browser.getSettings().setAppCachePath(Environment.getDataDirectory() + "/data/com.marz.snapprefs/cache/");
        browser.getSettings().setAppCacheEnabled(true);
        browser.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
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
                                            DownloadFilterAsync job = new DownloadFilterAsync();
                                            job.execute(imageUrl, userInput.getText().toString() + ".png");
                                            context1 = context;
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

class MyBrowser extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }

}