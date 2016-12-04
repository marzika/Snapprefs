package com.marz.snapprefs.Fragments;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
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
import com.marz.snapprefs.Util.LensData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Andre on 16/09/2016.
 */
public class LensesFragment extends Fragment {
    private static final List<String> stringFilter = Arrays.asList(
            "code_scheduled_lens_-_",
            "len_",
            "code_special_lens_-_"
    );
    private SparseArray<View> viewCache = new SparseArray<>();
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

    private static ArrayList<LensItemData> BuildLensItemData(LinkedHashMap<String, Object> lensMap, String partialName) {
        ArrayList<LensItemData> lensList = new ArrayList<>();

        for (Object obj : lensMap.values()) {
            LensData lensData = (LensData) obj;

            LensItemData itemData = new LensItemData();

            String nameBuilder = lensData.mCode;
            for (String filter : stringFilter)
                nameBuilder = nameBuilder.replace(filter, "");

            nameBuilder = nameBuilder.replaceAll("_", " ");

            if (partialName != null && !nameBuilder.toLowerCase().contains(partialName.toLowerCase()))
                continue;

            itemData.lensName = nameBuilder;
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
        final TextView loadedLensesTextView = (TextView) view.findViewById(R.id.textview_loaded_lens_count);
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
                    lensDialog(getContext(), loadedLensesTextView, inflater, container);
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

        ((TextView) viewCache.get(R.id.textview_total_lens_count)).setText(String.format("%s", lensListSize));
        ((TextView) viewCache.get(R.id.textview_loaded_lens_count)).setText(String.format("%s", selectedLensSize));
    }

    private void lensDialog(final Context context, final TextView loadedLensesTextView, LayoutInflater inflater,
                            ViewGroup container) {
        LinkedHashMap<String, Object> lensMap = (LinkedHashMap<String, Object>) Lens.getLensDatabase(context).getAllLenses();

        if (lensMap == null) {
            Logger.log("Tried to create dialog with no lenses");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Lenses");
        View view = inflater.inflate(R.layout.lenslist_layout, container, false);

        SetupRecyclerView(lensMap, view);
        SetupFilterSelector(view);
        SetupSelectionButtons(context, view);

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

    private void SetupRecyclerView(LinkedHashMap<String, Object> lensMap, View view) {
        ArrayList<LensItemData> itemDataList = BuildLensItemData(lensMap, null);
        lensListAdapter = new LensListAdapter(view.getContext(), itemDataList, this);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.lens_recyclerview);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(view.getContext(), 4);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(lensListAdapter);
    }

    private void SetupSelectionButtons(Context context, View view) {
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

    private void SetupFilterSelector(final View view) {
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
                    lensListAdapter.lensDataList = BuildLensItemData(lensMap, editable.toString());

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
            }
            else
                Toast.makeText(getContext(), "Found no lenses to merge!", Toast.LENGTH_SHORT).show();
        }
    }

    public static class LensItemData {
        public Bitmap lensIcon;
        public String lensCode;
        public String lensName;
        public String url;
        public boolean isActive;
    }
}
