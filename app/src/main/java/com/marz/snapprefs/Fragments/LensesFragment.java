package com.marz.snapprefs.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.marz.snapprefs.Common;
import com.marz.snapprefs.Logger;
import com.marz.snapprefs.MainActivity;
import com.marz.snapprefs.Preferences;
import com.marz.snapprefs.Preferences.Prefs;
import com.marz.snapprefs.R;
import com.marz.snapprefs.Util.LensData;
import com.marz.snapprefs.Util.LensDatabaseHelper;
import com.marz.snapprefs.Util.LensIconLoader;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Andre on 16/09/2016.
 */
public class LensesFragment extends Fragment {
    private static HashMap<String, LensButtonPair> iconMap = new HashMap<>();

    public static void lensDialog(final Context context, final TextView loadedLensesTextView, LayoutInflater inflater,
                                  ViewGroup container) {
        if (MainActivity.lensDBHelper == null)
            MainActivity.lensDBHelper = new LensDatabaseHelper(context);

        ArrayList<LensData> lensList = MainActivity.lensDBHelper.getAllLenses();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Lenses");
        View view = inflater.inflate(R.layout.lenslist_layout, container, false);

        GridLayout gridLayout = (GridLayout) view.findViewById(R.id.lensloader_gridholder);

        for (LensData lensData : lensList) {
            ImageButton button = new ImageButton(context);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageButton btn = (ImageButton) v;
                    String mCode = (String) btn.getTag();
                    Logger.log("Clicked lens item: " + mCode);
                    try {
                        boolean activeState = MainActivity.lensDBHelper.toggleLensActiveState(mCode);
                        btn.setBackgroundColor(Color.argb(activeState ? 150 : 0, 0, 200, 0));
                        btn.invalidate();
                    } catch (Exception e) {
                        Logger.log("No lens found with code: " + mCode + "\n" + e.getMessage());
                    }
                }
            });

            button.setMinimumHeight(50);
            button.setMinimumWidth(50);
            button.setBackgroundColor(Color.argb(lensData.mActive ? 150 : 0, 0, 200, 0));
            button.setTag(lensData.mCode);

            LensButtonPair buttonPair = iconMap.get(lensData.mCode);

            if (buttonPair == null || buttonPair.bmp == null) {
                buttonPair = new LensButtonPair(button, null, lensData.mIconLink, lensData.mCode);
                iconMap.put(lensData.mCode, buttonPair);
                new LensIconLoader.AsyncLensIconDownloader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, buttonPair, context);
            } else {
                button.setImageBitmap(buttonPair.bmp);
                button.invalidate();
            }

            gridLayout.addView(button);
        }


        builder.setView(view);
        builder.setPositiveButton(Common.dialog_done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedLensSize = MainActivity.lensDBHelper.getActiveLensCount();
                loadedLensesTextView.setText(String.format("%s", selectedLensSize));
            }
        });
        builder.setNegativeButton(Common.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedLensSize = MainActivity.lensDBHelper.getActiveLensCount();
                loadedLensesTextView.setText(String.format("%s", selectedLensSize));
            }
        });

        builder.show();
    }

    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        int lensListSize = (int) MainActivity.lensDBHelper.getRowCount();
        int selectedLensSize = MainActivity.lensDBHelper.getActiveLensCount();

        View view = inflater.inflate(R.layout.lensloader_layout,
                container, false);

        Button lensLoaderButton = (Button) view.findViewById(R.id.btnLensSelector);
        TextView totalLensesTextView = (TextView) view.findViewById(R.id.textview_total_lens_count);
        final TextView loadedLensesTextView = (TextView) view.findViewById(R.id.textview_loaded_lens_count);
        Switch loadLensSwitch = (Switch) view.findViewById(R.id.lensloader_toggle);
        Switch collectLensSwitch = (Switch) view.findViewById(R.id.lenscollector_toggle);

        loadLensSwitch.setChecked(Preferences.getBool(Prefs.LENSES_LOAD));
        collectLensSwitch.setChecked(Preferences.getBool(Prefs.LENSES_COLLECT));
        loadLensSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.putBool("pref_key_load_lenses", isChecked);
            }
        });

        collectLensSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.putBool("pref_key_collect_lenses", isChecked);
            }
        });


        totalLensesTextView.setText(String.format("%s", lensListSize));
        loadedLensesTextView.setText(String.format("%s", selectedLensSize));

        lensLoaderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lensDialog(getContext(), loadedLensesTextView, inflater, container);
            }
        });
        return view;
    }

    public static class LensButtonPair {
        public ImageButton button;
        public Bitmap bmp;
        public String url;
        public String mCode;

        public LensButtonPair(ImageButton button, Bitmap bmp, String url, String mCode) {
            this.button = button;
            this.bmp = bmp;
            this.url = url;
            this.mCode = mCode;
        }
    }
}
