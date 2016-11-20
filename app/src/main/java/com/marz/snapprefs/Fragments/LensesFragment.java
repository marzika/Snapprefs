package com.marz.snapprefs.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
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
import com.marz.snapprefs.Lens;
import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Preferences;
import com.marz.snapprefs.Preferences.Prefs;
import com.marz.snapprefs.R;
import com.marz.snapprefs.Util.LensData;
import com.marz.snapprefs.Util.LensIconLoader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Andre on 16/09/2016.
 */
public class LensesFragment extends Fragment {
    private static final List<String> stringFilter = Arrays.asList(
            "code_scheduled_lens_-_",
            "len_",
            "code_special_lens_-_"
    );
    private static HashMap<String, LensContainerData> iconMap = new HashMap<>();
    private static final DialogInterface.OnClickListener onSelectAllClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Lens.getLensDatabase().setActiveStateOfAllLenses(true);
            for (LensContainerData containerData : iconMap.values()) {
                containerData.inflatedLayout.setBackgroundResource(R.drawable.lens_bg_selected);
                containerData.inflatedLayout.invalidate();
            }
        }
    };
    private static final DialogInterface.OnClickListener onDeslectAllClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Lens.getLensDatabase().setActiveStateOfAllLenses(false);

            for (LensContainerData containerData : iconMap.values()) {
                containerData.inflatedLayout.setBackgroundResource(R.drawable.lens_bg_unselected);
                containerData.inflatedLayout.invalidate();
            }
        }
    };

    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final int lensListSize = (int) Lens.getLensDatabase(container.getContext()).getRowCount();
        int selectedLensSize = Lens.getLensDatabase(container.getContext()).getActiveLensCount();

        View view = inflater.inflate(R.layout.lensloader_layout,
                container, false);

        Button lensLoaderButton = (Button) view.findViewById(R.id.btnLensSelector);
        TextView totalLensesTextView = (TextView) view.findViewById(R.id.textview_total_lens_count);
        final TextView loadedLensesTextView = (TextView) view.findViewById(R.id.textview_loaded_lens_count);
        Switch loadLensSwitch = (Switch) view.findViewById(R.id.lensloader_toggle);
        Switch collectLensSwitch = (Switch) view.findViewById(R.id.lenscollector_toggle);
        Switch autoEnableSwitch = (Switch) view.findViewById(R.id.autoenable_switch);
        Switch sortBySelDate = (Switch) view.findViewById(R.id.sort_lens_by_sel_date);
        Switch hideCurrProvidedSCLenses = (Switch) view.findViewById(R.id.hide_current_snapchat_lenses);

        loadLensSwitch.setChecked(Preferences.getBool(Prefs.LENSES_LOAD));
        collectLensSwitch.setChecked(Preferences.getBool(Prefs.LENSES_COLLECT));
        autoEnableSwitch.setChecked(Preferences.getBool(Prefs.LENSES_AUTO_ENABLE));
        sortBySelDate.setChecked(Preferences.getBool(Prefs.LENSES_SORT_BY_SEL));
        hideCurrProvidedSCLenses.setChecked(Preferences.getBool(Prefs.LENSES_HIDE_CURRENTLY_PROVIDED_SC_LENSES));

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

        sortBySelDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Preferences.putBool(Prefs.LENSES_SORT_BY_SEL.key, isChecked);
            }
        });

        hideCurrProvidedSCLenses.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.putBool(Prefs.LENSES_HIDE_CURRENTLY_PROVIDED_SC_LENSES.key, isChecked);
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
        static void lensDialog(final Context context, final TextView loadedLensesTextView, LayoutInflater inflater,
                               ViewGroup container) {
            LinkedHashMap<String, Object> lensList = (LinkedHashMap<String, Object>) Lens.getLensDatabase(context).getAllLenses();

            if (lensList == null) {
                Logger.log("Tried to create dialog with no lenses");
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Select Lenses");

            View view = inflater.inflate(R.layout.lenslist_layout, container, false);

            GridLayout gridLayout = (GridLayout) view.findViewById(R.id.lensloader_gridholder);
            Button btnSelectAll = (Button) view.findViewById(R.id.btn_select_all_lenses);
            Button btnDeselectAll = (Button) view.findViewById(R.id.btn_deselect_all_lenses);

            final AlertDialog.Builder selectBuilder = new AlertDialog.Builder(context);
            selectBuilder.setNegativeButton("Cancel", null);

            btnSelectAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectBuilder.setTitle("Confirm Select All");
                    selectBuilder.setMessage("Are you sure you want to enable all lenses?");
                    selectBuilder.setPositiveButton("Select All", onSelectAllClick);
                    selectBuilder.show();
                }
            });

            btnDeselectAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectBuilder.setTitle("Confirm Deselect All");
                    selectBuilder.setMessage("Are you sure you want to disable all lenses?");
                    selectBuilder.setPositiveButton("Deselect All", onDeslectAllClick);
                    selectBuilder.show();
                }
            });

            for (Object lensObj : lensList.values()) {
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
                            boolean activeState = Lens.getLensDatabase(context).toggleLensActiveState(mCode);
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
                    //TODO Implement try/catch

                    try {
                        AsyncTaskCompat.executeParallel(new LensIconLoader.AsyncLensIconDownloader(), containerData, context);
                    } catch (Throwable e) {
                        Logger.log("Error loading lens", e);
                    }
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
                    int selectedLensSize = Lens.getLensDatabase(context).getActiveLensCount();
                    loadedLensesTextView.setText(String.format("%s", selectedLensSize));
                }
            });
            builder.setNegativeButton(Common.dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int selectedLensSize = Lens.getLensDatabase(context).getActiveLensCount();
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

        LensContainerData(LinearLayout inflatedLayout, ImageView iconImageView, TextView textView, String url, Bitmap bmp) {
            this.inflatedLayout = inflatedLayout;
            this.iconImageView = iconImageView;
            this.textView = textView;
            this.url = url;
            this.bmp = bmp;
        }
    }
}
