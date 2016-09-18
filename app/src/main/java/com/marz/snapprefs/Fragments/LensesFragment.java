package com.marz.snapprefs.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.marz.snapprefs.Dialogs;
import com.marz.snapprefs.MainActivity;
import com.marz.snapprefs.R;

/**
 * Created by Andre on 16/09/2016.
 */
public class LensesFragment extends Fragment {
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int lensListSize = (int)MainActivity.lensDBHelper.getRowCount();
        int selectedLensSize = MainActivity.lensDBHelper.getActiveLensCount();

        View view = inflater.inflate(R.layout.lensloader_layout,
                container, false);

        Button lensLoaderButton = (Button) view.findViewById(R.id.btnLensSelector);
        TextView totalLensesTextView = (TextView) view.findViewById(R.id.textview_total_lens_count);
        final TextView loadedLensesTextView = (TextView) view.findViewById(R.id.textview_loaded_lens_count);

        totalLensesTextView.setText(String.format("%s", lensListSize));
        loadedLensesTextView.setText(String.format("%s", selectedLensSize));

        lensLoaderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogs.lensDialog(getContext(), loadedLensesTextView);
            }
        });
        return view;
    }
}
