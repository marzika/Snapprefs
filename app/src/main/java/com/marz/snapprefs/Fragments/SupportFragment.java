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
import android.widget.Toast;

import com.marz.snapprefs.R;


public class SupportFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.support_layout,
                container, false);

        ImageButton facebook = (ImageButton) view.findViewById(R.id.facebook);
        ImageButton website = (ImageButton) view.findViewById(R.id.website);
        ImageButton xda = (ImageButton) view.findViewById(R.id.xda);
        ImageButton documentation = (ImageButton) view.findViewById(R.id.docs);
        ImageButton googlePlusBtn  = (ImageButton) view.findViewById(R.id.gplus);
        ImageButton reddit = (ImageButton) view.findViewById(R.id.reddit);

        facebook.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/snapprefs/"));
                    startActivity(myIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity().getApplicationContext(), "No application can handle this request." + " Please install a web browser", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
        website.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.snapprefs.com"));
                    startActivity(myIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity().getApplicationContext(), "No application can handle this request." + " Please install a web browser", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
        xda.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/xposed/modules/app-snapprefs-ultimate-snapchat-utility-t2947254/"));
                    startActivity(myIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity().getApplicationContext(), "No application can handle this request." + " Please install a web browser", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
        documentation.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://snapprefs.readthedocs.io"));
                    startActivity(myIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity().getApplicationContext(), "No application can handle this request." + " Please install a web browser", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
        reddit.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.reddit.com/r/snapprefs"));
                    startActivity(myIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity().getApplicationContext(), "No application can handle this request." + " Please install a web browser", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
        googlePlusBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://plus.google.com/u/0/communities/111884042638955665569"));
                    startActivity(myIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity().getApplicationContext(), "No application can handle this request." + " Please install a webbrowser", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });

        return view;
    }
}
