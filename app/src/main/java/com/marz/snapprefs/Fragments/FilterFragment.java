package com.marz.snapprefs.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.marz.snapprefs.FilterStoreUtils.TabsFragmentActivity;
import com.marz.snapprefs.R;

public class FilterFragment extends Fragment{
       @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.filter_layout, null);
           Button filters = (Button) view.findViewById(R.id.filterstore);
           filters.setOnClickListener(new Button.OnClickListener() {
               public void onClick(View v) {
                   Intent intent = new Intent(getActivity().getApplicationContext(), TabsFragmentActivity.class);
                   startActivity(intent);
               }
           });
        return view;
    }
}

