package com.marz.snapprefs;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.marz.snapprefs.FilterStoreUtils.TabsFragmentActivity;

import de.cketti.library.changelog.ChangeLog;


public class MainActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#00a650"));
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setCustomView(R.layout.abs);
        getActionBar().setBackgroundDrawable(colorDrawable);
        final Context context = this;
        ChangeLog cl = new ChangeLog(context);
        if (cl.isFirstRun()) {
            cl.getLogDialog().show();
        }
        //getFragmentManager().beginTransaction().replace(android.R.id.content, new Settings()).commit();
        //PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_main);
        Button settings = (Button) findViewById(R.id.settings);
        Button filterStore = (Button) findViewById(R.id.filterStore);
        Button reedem = (Button) findViewById(R.id.reedem);
        Button donate = (Button) findViewById(R.id.donate);
        Button about = (Button) findViewById(R.id.about);
        Button legal = (Button) findViewById(R.id.legal);
        TextView SC_text = (TextView) findViewById(R.id.SC_text);
        SC_text.setPaintFlags(SC_text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        settings.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.frame_layout, new Settings()).commit();
                PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
            }
        });
        filterStore.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TabsFragmentActivity.class);
                startActivity(intent);
            }
        });
        reedem.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Reedem.class);
                startActivity(intent);
            }
        });
        about.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("About")
                        .setMessage("Thanks to: \n" + getResources().getString(R.string.pref_thanks_summary) + "\n\nVersion: " + BuildConfig.VERSION_NAME + "\n\nSupported version: " + Obfuscator.SUPPORTED_VERSION_CODENAME)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        legal.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Legal")
                        .setMessage(Html.fromHtml("<b>Snapprefs is licensed under GNU GPLv3.</b><br><br><b>Used libraries - License:</b><br><small>ckChangeLog - Apache License 2.0<br>ColorPicker for Android - Apache Lic. 2.0<br>DirChooser - Apache License 2.0<br>mp4parser - Free License<br></small><br><b>Module is based on:</b><br><small>Keepchat - GNU GPLv3<br >Snapshare - GNU GPLv3</small>"))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        })
                        .setNegativeButton("Apache Lic. 2.0", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.apache.org/licenses/LICENSE-2.0"));
                                    startActivity(myIntent);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(getApplicationContext(), "No application can handle this request." + " Please install a webbrowser", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNeutralButton("GNU GPLv3", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.gnu.org/licenses/gpl-3.0.html"));
                                    startActivity(myIntent);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(getApplicationContext(), "No application can handle this request." + " Please install a webbrowser", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        donate.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Donate")
                        .setMessage("Donations are highly appreciated and it helps to keep the motivation going! If you feel like I deserve some coffee/beer, you can donate on PayPal by clicking the 'Donate' button below.\n\nNOTE: Donations will not unlock paid features, for them check the Remove Ads page")
                        .setPositiveButton("Donate", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=SL45E99ZBUUCQ"));
                                    startActivity(myIntent);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(getApplicationContext(), "No application can handle this request." + " Please install a webbrowser", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        AdView mAdView = (AdView) findViewById(R.id.adView);
        if (readIntPreference("license_status") == 1 || readIntPreference("license_status") == 2) {
            mAdView.destroy();
            mAdView.setVisibility(View.GONE);
        } else {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("753D126B83124EE69FA573A9D07FEF54")
                    .build();
            mAdView.loadAd(adRequest);
            mAdView.setVisibility(View.VISIBLE);
        }
    }

    public int readIntPreference(String key) {
        SharedPreferences prefs = getSharedPreferences("com.marz.snapprefs_preferences", MODE_PRIVATE);
        int returned = prefs.getInt(key, 0);
        return returned;
    }
}