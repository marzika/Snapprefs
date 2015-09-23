package com.marz.snapprefs;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Reedem extends Activity {
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#00a650"));
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setCustomView(R.layout.abs);
        getActionBar().setBackgroundDrawable(colorDrawable);
        final Context context = this;
        //getFragmentManager().beginTransaction().replace(android.R.id.content, new Settings()).commit();
        //PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();

        setContentView(R.layout.checklayout);
        Button submitbtn = (Button) findViewById(R.id.submit);
        final EditText cID = (EditText) findViewById(R.id.confirmationID);
        final TextView textView = (TextView) findViewById(R.id.textView);
        final Button buynow = (Button) findViewById(R.id.button);
        TextView dID = (TextView) findViewById(R.id.deviceID);
        dID.setText(deviceId);
        cID.setText(readStringPreference("confirmation_id"));
        if (readIntPreference("license_status") == 0) {
            String text = "Your license status is: <font color='blue'>Free</font>";
            textView.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);
            buynow.setText("Click here to buy a license");
            buynow.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    new AlertDialog.Builder(Reedem.this)
                            .setTitle("Buy a license")
                            .setMessage("Premium: AdFree (more will come)\n\nDeluxe: Premium + GodMode (noone can save your Snaps -- not added yet)")
                            .setPositiveButton("Premium (2.99$)", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=2AS727Q2CL7AS"));//Premium
                                    startActivity(myIntent);
                                }
                            })
                            .setNeutralButton("Deluxe (9.99$)", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=H523TP8ZJH9XY"));//Deluxe
                                    startActivity(myIntent);
                                }
                            })
                            .show();
                }
            });
            buynow.setVisibility(View.VISIBLE);
        } else if (readIntPreference("license_status") == 1) {
            String text = "Your license status is: <font color='green'>Premium</font>";
            textView.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);
            buynow.setText("Click here to upgrade your license");
            buynow.setVisibility(View.VISIBLE);
            buynow.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    new AlertDialog.Builder(Reedem.this)
                            .setTitle("Upgrade your license")
                            .setMessage("Premium: AdFree (more will come)\n\nDeluxe: Premium + GodMode (noone can save your Snaps -- not added yet)")
                            .setNeutralButton("Deluxe (9.99$)", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=H523TP8ZJH9XY"));//Deluxe
                                    startActivity(myIntent);
                                }
                            })
                            .show();
                }
            });
        } else if (readIntPreference("license_status") == 2) {
            String text = "Your license status is: <font color='#FFCC00'>Deluxe</font>";
            textView.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);
            buynow.setVisibility(View.GONE);
        }
        final String confirmationID = cID.getText().toString();
        final String deviceID = dID.getText().toString();
        if (!confirmationID.isEmpty()) {
            new Connection().execute(cID.getText().toString(), deviceID);
        }

        submitbtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                new Connection().execute(cID.getText().toString(), deviceID);
            }
        });
    }

    public void postData(final String confirmationID, final String deviceID) {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://snapprefs.com/checkuser.php");
        saveStringPreference("device_id", deviceID);
        saveStringPreference("confirmation_id", confirmationID);

        try {
            // Add your data
            List nameValuePairs = new ArrayList(2);
            nameValuePairs.add(new BasicNameValuePair("confirmationID", confirmationID));
            nameValuePairs.add(new BasicNameValuePair("deviceID", deviceID));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

            InputStream is = response.getEntity().getContent();
            BufferedInputStream bis = new BufferedInputStream(is);
            final ByteArrayBuffer baf = new ByteArrayBuffer(20);

            int current = 0;

            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }

            /* Convert the Bytes read to a String. */
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // This code will always run on the UI thread, therefore is safe to modify UI elements.
                    String text = new String(baf.toByteArray());
                    String status = null;
                    String error_msg = null;
                    TextView txtvw = (TextView) findViewById(R.id.textView);
                    TextView errorTV = (TextView) findViewById(R.id.errorTV);
                    Button buynow = (Button) findViewById(R.id.button);
                    try {

                        JSONObject obj = new JSONObject(text);
                        status = obj.getString("status");
                        error_msg = obj.getString("error_msg");
                        if (status.equals("0") && !error_msg.isEmpty()) {
                            String text2 = "Your license status is: <font color='blue'>Free</font>";
                            txtvw.setText(Html.fromHtml(text2), TextView.BufferType.SPANNABLE);
                            errorTV.setText("Error: " + error_msg);
                            buynow.setText("Click here to buy a license");
                            buynow.setOnClickListener(new Button.OnClickListener() {
                                public void onClick(View v) {
                                    new AlertDialog.Builder(Reedem.this)
                                            .setTitle("Buy a license")
                                            .setMessage("Premium: AdFree (more will come)\n\nDeluxe: Premium + GodMode (noone can save your Snaps -- not added yet)")
                                            .setPositiveButton("Premium (2.99$)", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=2AS727Q2CL7AS"));//Premium
                                                    startActivity(myIntent);
                                                }
                                            })
                                            .setNeutralButton("Deluxe (9.99$)", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=H523TP8ZJH9XY"));//Deluxe
                                                    startActivity(myIntent);
                                                }
                                            })
                                            .show();
                                }
                            });
                            buynow.setVisibility(View.VISIBLE);
                            saveIntPreference("license_status", 0);
                        }
                        if (status.equals("1") && error_msg.isEmpty()) {
                            String text2 = "Your license status is: <font color='green'>Premium</font>";
                            txtvw.setText(Html.fromHtml(text2), TextView.BufferType.SPANNABLE);
                            errorTV.setText("");
                            buynow.setText("Click here to upgrade your license");
                            buynow.setOnClickListener(new Button.OnClickListener() {
                                public void onClick(View v) {
                                    new AlertDialog.Builder(Reedem.this)
                                            .setTitle("Upgrade your license")
                                            .setMessage("Premium: AdFree (more will come)\n\nDeluxe: Premium + GodMode (noone can save your Snaps -- not added yet)")
                                            .setNeutralButton("Deluxe (9.99$)", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=H523TP8ZJH9XY"));//Deluxe
                                                    startActivity(myIntent);
                                                }
                                            })
                                            .show();
                                }
                            });
                            buynow.setVisibility(View.VISIBLE);
                            saveIntPreference("license_status", 1);
                        }
                        if (status.equals("2") && error_msg.isEmpty()) {
                            String text2 = "Your license status is: <font color='#FFCC00'>Deluxe</font>";
                            txtvw.setText(Html.fromHtml(text2), TextView.BufferType.SPANNABLE);
                            buynow.setVisibility(View.GONE);
                            errorTV.setText("");
                            saveIntPreference("license_status", 2);
                        }

                    } catch (Throwable t) {
                        Log.e("Snapprefs", "Could not parse malformed JSON: \"" + text + "\"");
                        errorTV.setText("Error while reedeming your license, bad response");
                        saveIntPreference("license_status", 0);
                    }

                }
            });
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Toast.makeText(Reedem.this, "ClientProtocolException" + e.toString(), Toast.LENGTH_SHORT).show();
            saveIntPreference("license_status", 0);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(Reedem.this, "IOException" + e.toString(), Toast.LENGTH_SHORT).show();
            saveIntPreference("license_status", 0);
        }
    }

    public void saveStringPreference(String key, String value) {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void saveIntPreference(String key, int value) {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public String readStringPreference(String key) {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        String returned = prefs.getString(key, null);
        return returned;
    }

    public int readIntPreference(String key) {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        int returned = prefs.getInt(key, 0);
        return returned;
    }

    private class Connection extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            postData(params[0], params[1]);
            return null;
        }

    }
}