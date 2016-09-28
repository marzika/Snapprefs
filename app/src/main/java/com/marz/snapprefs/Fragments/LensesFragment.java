package com.marz.snapprefs.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.marz.snapprefs.Common;
import com.marz.snapprefs.Databases.LensDatabaseHelper;
import com.marz.snapprefs.Logger;
import com.marz.snapprefs.MainActivity;
import com.marz.snapprefs.Preferences;
import com.marz.snapprefs.Preferences.Prefs;
import com.marz.snapprefs.R;
import com.marz.snapprefs.Util.LensData;
import com.marz.snapprefs.Util.LensIconLoader;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Andre on 16/09/2016.
 */
public class LensesFragment extends Fragment {
    private static final ArrayList<String> stringFilter = new ArrayList<String>() {
        {
            add("code_scheduled_lens_-_");
            add("len_");
            add("code_special_lens_-_");
        }
    };
    private static HashMap<String, LensContainerData> iconMap = new HashMap<>();

    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final int lensListSize = (int) MainActivity.lensDBHelper.getRowCount();
        int selectedLensSize = MainActivity.lensDBHelper.getActiveLensCount();

        View view = inflater.inflate(R.layout.lensloader_layout,
                container, false);

        Button lensLoaderButton = (Button) view.findViewById(R.id.btnLensSelector);
        TextView totalLensesTextView = (TextView) view.findViewById(R.id.textview_total_lens_count);
        final TextView loadedLensesTextView = (TextView) view.findViewById(R.id.textview_loaded_lens_count);
        Switch loadLensSwitch = (Switch) view.findViewById(R.id.lensloader_toggle);
        Switch collectLensSwitch = (Switch) view.findViewById(R.id.lenscollector_toggle);
        Switch autoEnableSwitch = (Switch) view.findViewById(R.id.autoenable_switch);

        loadLensSwitch.setChecked(Preferences.getBool(Prefs.LENSES_LOAD));
        collectLensSwitch.setChecked(Preferences.getBool(Prefs.LENSES_COLLECT));
        autoEnableSwitch.setChecked(Preferences.getBool(Prefs.LENSES_AUTO_ENABLE));

        loadLensSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.putBool(Prefs.LENSES_LOAD.key, isChecked);
            }
        });

        collectLensSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.putBool(Prefs.LENSES_COLLECT.key, isChecked);
            }
        });

        autoEnableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Preferences.putBool(Prefs.LENSES_AUTO_ENABLE.key, isChecked);
            }
        });

        totalLensesTextView.setText(String.format("%s", lensListSize));
        loadedLensesTextView.setText(String.format("%s", selectedLensSize));

        lensLoaderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lensListSize > 0)
                    DialogHelper.lensDialog(getContext(), loadedLensesTextView, inflater, container);
                else
                    Toast.makeText(v.getContext(), "You've not collected any lenses!", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    public static class DialogHelper {
        public static void lensDialog(final Context context, final TextView loadedLensesTextView, LayoutInflater inflater,
                                      ViewGroup container) {
            if (MainActivity.lensDBHelper == null)
                MainActivity.lensDBHelper = new LensDatabaseHelper(context);

            ArrayList<Object> lensList = MainActivity.lensDBHelper.getAllLenses();

            if (lensList == null) {
                Logger.log("Tried to create dialog with no lenses");
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Select Lenses");
            View view = inflater.inflate(R.layout.lenslist_layout, container, false);

            GridLayout gridLayout = (GridLayout) view.findViewById(R.id.lensloader_gridholder);

            for (Object lensObj : lensList) {
                final LensData lensData = (LensData) lensObj;

                // Set up Lens Container \\
                final LinearLayout inflatedLayout = (LinearLayout) inflater.inflate(R.layout.lensholder_layout, gridLayout, false);
                inflatedLayout.setBackgroundResource(lensData.mActive ? R.drawable.lens_bg_selected :
                        R.drawable.lens_bg_unselected);

                inflatedLayout.setTag(lensData.mCode);
                inflatedLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LinearLayout btn = (LinearLayout) v;
                        String mCode = (String) btn.getTag();
                        Logger.log("Clicked lens item: " + mCode);
                        try {
                            boolean activeState = MainActivity.lensDBHelper.toggleLensActiveState(mCode);
                            inflatedLayout.setBackgroundResource(activeState ? R.drawable.lens_bg_selected :
                                    R.drawable.lens_bg_unselected);
                            inflatedLayout.invalidate();
                        } catch (Exception e) {
                            Logger.log("No lens found with code: " + mCode + "\n" + e.getMessage());
                        }
                    }
                });

                ImageView iconImageView = (ImageView) inflatedLayout.findViewById(R.id.lensIconView);

                TextView iconName = (TextView) inflatedLayout.findViewById(R.id.lensTextView);
                String nameBuilder = lensData.mCode;

                for (String filter : stringFilter)
                    nameBuilder = nameBuilder.replace(filter, "");

                nameBuilder = nameBuilder.replaceAll("_", " ");

                iconName.setText(nameBuilder.trim());
                iconName.setMaxWidth(5);

                LensContainerData containerData = iconMap.get(lensData.mCode);
                if (containerData == null || containerData.bmp == null) {
                    containerData = new LensContainerData(inflatedLayout, iconImageView, iconName, lensData.mIconLink, null);
                    iconMap.put(lensData.mCode, containerData);
                    new LensIconLoader.AsyncLensIconDownloader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, containerData, context);
                } else {
                    iconImageView.setImageBitmap(containerData.bmp);
                    iconImageView.invalidate();

                    iconName.setMaxWidth(containerData.bmp.getWidth());
                    iconName.invalidate();
                    inflatedLayout.invalidate();
                }

                gridLayout.addView(inflatedLayout);
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
    }

    public static class LensContainerData {
        public LinearLayout inflatedLayout;
        public ImageView iconImageView;
        public TextView textView;
        public String url;
        public Bitmap bmp;

        public LensContainerData(LinearLayout inflatedLayout, ImageView iconImageView, TextView textView, String url, Bitmap bmp) {
            this.inflatedLayout = inflatedLayout;
            this.iconImageView = iconImageView;
            this.textView = textView;
            this.url = url;
            this.bmp = bmp;
        }
    }
}
