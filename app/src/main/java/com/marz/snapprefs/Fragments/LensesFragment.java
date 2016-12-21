package com.marz.snapprefs.Fragments;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.marz.snapprefs.Adapters.LensListAdapter;
import com.marz.snapprefs.Common;
import com.marz.snapprefs.Databases.LensDatabaseHelper;
import com.marz.snapprefs.Databases.LensDatabaseHelper.LensEntry;
import com.marz.snapprefs.Lens;
import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Logger.LogType;
import com.marz.snapprefs.Preferences;
import com.marz.snapprefs.Preferences.Prefs;
import com.marz.snapprefs.R;
import com.marz.snapprefs.Util.BitmapCache;
import com.marz.snapprefs.Util.LensData;
import com.marz.snapprefs.Util.ViewCache;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static android.app.Activity.RESULT_OK;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */
public class LensesFragment extends Fragment {
    public static BitmapCache bitmapCache = new BitmapCache();
    public LensListAdapter lensListAdapter;
    private final DialogInterface.OnClickListener onSelectAllClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (lensListAdapter == null)
                return;

            LensDatabaseHelper dbHelper = Lens.getLensDatabase(getContext());
            ContentValues values = new ContentValues();
            values.put(LensEntry.COLUMN_NAME_ACTIVE, 1);

            //Disable logging for batch updates - Performance improvement for large updates
            Logger.disableLogging();
            for (LensItemData itemData : lensListAdapter.lensDataList) {
                if (itemData.isActive)
                    continue;

                dbHelper.updateLens(itemData.lensCode, values);
                itemData.isActive = true;
            }
            Logger.enableLogging();

            lensListAdapter.notifyDataSetChanged();
        }
    };
    private final DialogInterface.OnClickListener onDeslectAllClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (lensListAdapter == null)
                return;

            LensDatabaseHelper dbHelper = Lens.getLensDatabase(getContext());
            ContentValues values = new ContentValues();
            values.put(LensEntry.COLUMN_NAME_ACTIVE, 0);

            //Disable logging for batch updates - Performance improvement for large updates
            Logger.disableLogging();
            for (LensItemData itemData : lensListAdapter.lensDataList) {
                if (!itemData.isActive)
                    continue;

                dbHelper.updateLens(itemData.lensCode, values);
                itemData.isActive = false;
            }
            Logger.enableLogging();

            lensListAdapter.notifyDataSetChanged();
        }
    };
    private ViewCache viewCache = new ViewCache();

    private static ArrayList<LensItemData> buildLensItemData(LinkedHashMap<String, Object> lensMap, String partialName) {
        ArrayList<LensItemData> lensList = new ArrayList<>();

        for (Object obj : lensMap.values()) {
            LensData lensData = (LensData) obj;

            LensItemData itemData = new LensItemData();

            if (lensData.name == null) {
                String strippedName = Lens.stripLensName(lensData.mCode);

                if (partialName != null && !strippedName.toLowerCase().contains(partialName.toLowerCase()))
                    continue;

                itemData.lensName = strippedName;
            } else
                itemData.lensName = lensData.name;

            itemData.lensCode = lensData.mCode;
            itemData.url = lensData.mIconLink;
            itemData.isActive = lensData.mActive;
            lensList.add(itemData);
        }

        return lensList;
    }

    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final int lensListSize = (int) Lens.getLensDatabase(getContext()).getRowCount();
        int selectedLensSize = Lens.getLensDatabase(getContext()).getActiveLensCount();

        View view = inflater.inflate(R.layout.lensloader_layout,
                container, false);

        Button lensLoaderButton = (Button) view.findViewById(R.id.btnLensSelector);
        TextView totalLensesTextView = (TextView) view.findViewById(R.id.textview_total_lens_count);
        TextView loadedLensesTextView = (TextView) view.findViewById(R.id.textview_loaded_lens_count);
        Switch loadLensSwitch = (Switch) view.findViewById(R.id.lensloader_toggle);
        Switch collectLensSwitch = (Switch) view.findViewById(R.id.lenscollector_toggle);
        Switch autoEnableSwitch = (Switch) view.findViewById(R.id.autoenable_switch);
        Switch sortBySelDate = (Switch) view.findViewById(R.id.sort_lens_by_sel_date);
        Switch hideCurrProvidedSCLenses = (Switch) view.findViewById(R.id.hide_current_snapchat_lenses);
        Button btnMerger = (Button) view.findViewById(R.id.btn_db_merger);

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
                    lensDialog(getContext(), inflater, container);
                else
                    Toast.makeText(v.getContext(), "You've not collected any lenses!", Toast.LENGTH_SHORT).show();
            }
        });

        btnMerger.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("file/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Choose Slave DB to merge"), 1);
            }
        });

        viewCache.put(R.id.textview_total_lens_count, totalLensesTextView);
        viewCache.put(R.id.textview_loaded_lens_count, loadedLensesTextView);
        return view;
    }

    public void refreshLensCount() {
        final int lensListSize = (int) Lens.getLensDatabase(getContext()).getRowCount();
        int selectedLensSize = Lens.getLensDatabase(getContext()).getActiveLensCount();

        viewCache.getTV(R.id.textview_total_lens_count).setText(String.format("%s", lensListSize));
        viewCache.getTV(R.id.textview_loaded_lens_count).setText(String.format("%s", selectedLensSize));
    }

    private void lensDialog(final Context context, LayoutInflater inflater,
                            ViewGroup container) {
        LinkedHashMap<String, Object> lensMap = (LinkedHashMap<String, Object>) Lens.getLensDatabase(context).getAllLenses();

        if (lensMap == null) {
            Logger.log("Tried to create dialog with no lenses");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Lenses");
        View view = inflater.inflate(R.layout.lenslist_layout, container, false);

        RecyclerView recyclerView = setupRecyclerView(lensMap, view);
        setupLensSpanSeekbar(view, recyclerView);
        setupFilterSelector(view);
        setupSelectionButtons(context, view);

        builder.setView(view);
        builder.setPositiveButton(Common.dialog_done, null);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = builder.show().getWindow();
        if( window != null ) {
            lp.copyFrom(window.getAttributes());
//This makes the dialog take up the full width
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
    }

    private RecyclerView setupRecyclerView(LinkedHashMap<String, Object> lensMap, final View view) {
        RelativeLayout recyclerContainer = (RelativeLayout) view.findViewById(R.id.lens_list_holder);
        ArrayList<LensItemData> itemDataList = buildLensItemData(lensMap, null);
        lensListAdapter = new LensListAdapter(view.getContext(), itemDataList, this, bitmapCache);

        RecyclerView recyclerView = new RecyclerView(getContext()) {
            @Override
            protected void onDetachedFromWindow() {
                // This has been detached from Window, so clear the drawable
                super.onDetachedFromWindow();
                int selectedLensSize = Lens.getLensDatabase(getContext()).getActiveLensCount();
                TextView loadedLenses = (TextView) viewCache.get(R.id.textview_loaded_lens_count);
                loadedLenses.setText(String.format("%s", selectedLensSize));

                Logger.log("Endpoint");
            }
        };

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(view.getContext(), Preferences.getInt(Prefs.LENS_SELECTOR_SPAN));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        recyclerView.setLayoutParams(layoutParams);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(lensListAdapter);
        recyclerView.setVerticalScrollBarEnabled(true);

        recyclerContainer.addView(recyclerView);
        return recyclerView;
    }

    private void setupLensSpanSeekbar(final View view, final RecyclerView recyclerView) {
        SeekBar spanCount = (SeekBar) view.findViewById(R.id.lens_seek_span);
        spanCount.setProgress(Preferences.getInt(Prefs.LENS_SELECTOR_SPAN) - 1);

        spanCount.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(view.getContext(), i + 1);
                recyclerView.setLayoutManager(layoutManager);
                Preferences.setPref(Prefs.LENS_SELECTOR_SPAN, i + 1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupSelectionButtons(Context context, View view) {
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
    }

    private void setupFilterSelector(final View view) {
        final TextView txt_filter = (TextView) view.findViewById(R.id.lens_filter);

        txt_filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                LensDatabaseHelper dbHelper = Lens.getLensDatabase(view.getContext());
                LinkedHashMap<String, Object> lensMap = (LinkedHashMap<String, Object>) dbHelper.getAllWithPartial(editable.toString());

                Logger.log("Checking for partial: " + editable.toString());
                if (lensMap == null)
                    lensListAdapter.lensDataList.clear();
                else
                    lensListAdapter.lensDataList = buildLensItemData(lensMap, editable.toString());

                lensListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);

        if (reqCode == 1 && resCode == RESULT_OK && data != null) {
            String filePath = data.getData().getPath();
            Logger.log("File path: " + filePath);
            String filenameArray[] = filePath.split("\\.");

            if (filenameArray.length <= 0) {
                Logger.log("Couldn't find file extension: " + filePath, LogType.LENS);
                Toast.makeText(getContext(), "Couldn't find file extension", Toast.LENGTH_SHORT).show();
                return;
            }

            String extension = filenameArray[filenameArray.length - 1];
            Logger.log("Extension: " + extension);

            if (!extension.equals("db")) {
                Logger.log("Incorrect filetype: " + extension, LogType.LENS);
                Toast.makeText(getContext(), "Incorrect filetype supplied: " + extension, Toast.LENGTH_SHORT).show();
                return;
            }

            LensDatabaseHelper masterDB = Lens.getLensDatabase(getContext());
            LensDatabaseHelper slaveDB = new LensDatabaseHelper(getContext(), filePath);

            int mergedLenses = LensDatabaseHelper.mergeLensDatabases(masterDB, slaveDB);

            if (mergedLenses > 0) {
                refreshLensCount();
                Toast.makeText(getContext(), "Successfully merged " + mergedLenses + " lenses!", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(getContext(), "Found no lenses to merge!", Toast.LENGTH_SHORT).show();
        }
    }

    public static class LensItemData {
        public String lensCode;
        public String lensName;
        public String url;
        public boolean isActive;
    }
}
