package com.marz.snapprefs.Fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.marz.snapprefs.R;
import com.marz.snapprefs.VisualActivity;


public class VisualFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.visual_layout,null);
        Button visualfilter = (Button) view.findViewById(R.id.visualstore);
        visualfilter.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), VisualActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
