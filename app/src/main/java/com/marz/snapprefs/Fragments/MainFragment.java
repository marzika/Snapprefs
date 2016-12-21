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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.marz.snapprefs.BuildConfig;
import com.marz.snapprefs.Obfuscator;
import com.marz.snapprefs.R;


public class MainFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_layout,
                container, false);
        ImageButton donate = (ImageButton) view.findViewById(R.id.paypal);
        TextView build = (TextView) view.findViewById(R.id.build_version);
        TextView sc_version = (TextView) view.findViewById(R.id.sc_version);
        TextView currentBranch = (TextView) view.findViewById(R.id.branch_name);
        donate.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=SL45E99ZBUUCQ"));
                    startActivity(myIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity().getApplicationContext(), "No application can handle this request." + " Please install a web browser", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
        build.setText(build.getText() + " " + BuildConfig.VERSION_NAME);

        String currentBranchStr = BuildConfig.JENKINS_BRANCH;
        if(currentBranchStr == "N/A") {
            currentBranch.setVisibility(View.GONE);
        } else {
            currentBranch.setText(currentBranch.getText() + " " + currentBranchStr);
        }
        sc_version.setText(sc_version.getText() + " " + Obfuscator.SUPPORTED_VERSION_CODENAME);

        return view;
    }
}
