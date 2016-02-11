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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

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
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        //getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsOld()).commit();
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
        final Button applygod = (Button) findViewById(R.id.god);
        final EditText name = (EditText) findViewById(R.id.username);
        final TextView god = (TextView) findViewById(R.id.god_desc);
        TextView dID = (TextView) findViewById(R.id.deviceID);
        dID.setText(deviceId);
        cID.setText(readStringPreference("confirmation_id"));
        final String deviceID = dID.getText().toString();
        final String confirmationID = cID.getText().toString();
        if (readLicense(deviceID, confirmationID) == 0) {
            String text = "Your license status is: <font color='blue'>Free</font>";
            textView.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);
            buynow.setText("Click here to buy a license");
            buynow.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    new AlertDialog.Builder(Reedem.this)
                            .setTitle("Buy a license")
                            .setMessage(Html.fromHtml(getString(R.string.buy_text)))
                            .setPositiveButton(R.string.buy_premium, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=2AS727Q2CL7AS"));//Premium
                                    startActivity(myIntent);
                                }
                            })
                            .setNeutralButton(R.string.buy_deluxe, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=H523TP8ZJH9XY"));//Deluxe
                                    startActivity(myIntent);
                                }
                            })
                            .show();
                }
            });
            buynow.setVisibility(View.VISIBLE);
        } else if (readLicense(deviceID, confirmationID) == 1) {
            String text = "Your license status is: <font color='green'>Premium</font>";
            textView.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);
            buynow.setText("Click here to upgrade your license");
            buynow.setVisibility(View.VISIBLE);
            buynow.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    new AlertDialog.Builder(Reedem.this)
                            .setTitle("Upgrade your license")
                            .setMessage(Html.fromHtml(getString(R.string.buy_text)))
                            .setNeutralButton("Deluxe (9.99$)", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=H523TP8ZJH9XY"));//Deluxe
                                    startActivity(myIntent);
                                }
                            })
                            .show();
                }
            });
        } else if (readLicense(deviceID, confirmationID) == 2) {
            String text = "Your license status is: <font color='#FFCC00'>Deluxe</font>";
            textView.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);
            buynow.setVisibility(View.GONE);
            applygod.setVisibility(View.VISIBLE);
            name.setVisibility(View.VISIBLE);
            god.setVisibility(View.VISIBLE);
        }
        if (!confirmationID.isEmpty()) {
            //new Connection().execute(cID.getText().toString(), deviceID);
        }

        submitbtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                new Connection().execute(cID.getText().toString(), deviceID);
            }
        });
        applygod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageDigest md = null;
                try {
                    md = MessageDigest.getInstance("SHA-256");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "NoSuchAlgorithm", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    md.update(name.getText().toString().getBytes("UTF-8")); // Change this to "UTF-16" if needed
                } catch (UnsupportedEncodingException e) {
                    Toast.makeText(getApplicationContext(), "Invalid username", Toast.LENGTH_SHORT).show();
                    return;
                }
                byte[] digest = md.digest();
                String hashed = String.format("%064x", new java.math.BigInteger(1, digest));
                new ConnectionGod().execute(cID.getText().toString(), hashed);
            }
        });
        AdView mAdView = (AdView) findViewById(R.id.adView2);
        if (readLicense(deviceID, confirmationID) == 1 || readLicense(deviceID, confirmationID) == 2) {
            mAdView.destroy();
            mAdView.setVisibility(View.GONE);
        } else {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("75466CD74A6342D488B5B5EA749E1265")
                    .build();
            mAdView.loadAd(adRequest);
            mAdView.setVisibility(View.VISIBLE);
        }
    }

    public void postData(final String confirmationID, final String deviceID) {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://snapprefs.com/checkuser.php");
        saveDeviceID(deviceID);
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
                                            .setMessage(Html.fromHtml(getString(R.string.buy_text)))
                                            .setPositiveButton(R.string.buy_premium, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=2AS727Q2CL7AS"));//Premium
                                                    startActivity(myIntent);
                                                }
                                            })
                                            .setNeutralButton(R.string.buy_deluxe, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=H523TP8ZJH9XY"));//Deluxe
                                                    startActivity(myIntent);
                                                }
                                            })
                                            .show();
                                }
                            });
                            buynow.setVisibility(View.VISIBLE);
                            //saveIntPreference("license_status", 0);
                            saveLicense(deviceID, confirmationID, 0);
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
                                            .setMessage(Html.fromHtml(getString(R.string.buy_text)))
                                            .setNeutralButton(R.string.buy_deluxe, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=H523TP8ZJH9XY"));//Deluxe
                                                    startActivity(myIntent);
                                                }
                                            })
                                            .show();
                                }
                            });
                            buynow.setVisibility(View.VISIBLE);
                            saveLicense(deviceID, confirmationID, 1);

                            new AlertDialog.Builder(Reedem.this)
                                    .setTitle("Apply License")
                                    .setMessage("License verification is done, you have to do a soft reboot. If you want to type in your Redeem ID, click dismiss, otherwise click Soft Reboot. Without it, you will not be able to use your license and Snapprefs properly.")
                                    .setPositiveButton("Soft reboot", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            try {
                                                Process proc = Runtime.getRuntime()
                                                        .exec(new String[]{"su", "-c", "busybox killall system_server"});
                                                proc.waitFor();
                                            } catch (Exception ex) {
                                                ex.printStackTrace();
                                            }
                                        }
                                    })
                                    .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        }
                        if (status.equals("2") && error_msg.isEmpty()) {
                            String text2 = "Your license status is: <font color='#FFCC00'>Deluxe</font>";
                            txtvw.setText(Html.fromHtml(text2), TextView.BufferType.SPANNABLE);
                            buynow.setVisibility(View.GONE);
                            errorTV.setText("");
                            saveLicense(deviceID, confirmationID, 2);
                            new AlertDialog.Builder(Reedem.this)
                                    .setTitle("Apply License")
                                    .setMessage("License verification is done, you have to do a soft reboot. If you want to type in your Redeem ID, click dismiss, otherwise click Soft Reboot. Without it, you will not be able to use your license and Snapprefs properly.")
                                    .setPositiveButton("Soft reboot", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            try {
                                                Process proc = Runtime.getRuntime()
                                                        .exec(new String[]{"su", "-c", "busybox killall system_server"});
                                                proc.waitFor();
                                            } catch (Exception ex) {
                                                ex.printStackTrace();
                                            }
                                        }
                                    })
                                    .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        }

                    } catch (Throwable t) {
                        Log.e("Snapprefs", "Could not parse malformed JSON: \"" + text + "\"");
                        errorTV.setText("Error while reedeming your license, bad response");
                        saveLicense(deviceID, confirmationID, 0);
                    }

                }
            });
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Toast.makeText(Reedem.this, "ClientProtocolException" + e.toString(), Toast.LENGTH_SHORT).show();
            saveLicense(deviceID, confirmationID, 0);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(Reedem.this, "IOException" + e.toString(), Toast.LENGTH_SHORT).show();
            saveLicense(deviceID, confirmationID, 0);
        }
    }
    public void postGod(final String confirmationID, final String username) {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://snapprefs.com/god.php");

        try {
            // Add your data
            List nameValuePairs = new ArrayList(2);
            nameValuePairs.add(new BasicNameValuePair("confirmationID", confirmationID));
            nameValuePairs.add(new BasicNameValuePair("username", username));
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
                    TextView errorTV = (TextView) findViewById(R.id.errorTV);
                    try {

                        JSONObject obj = new JSONObject(text);
                        status = obj.getString("status");
                        error_msg = obj.getString("error_msg");
                        errorTV.setText(error_msg);
                        errorTV.setVisibility(View.VISIBLE);
                    } catch (Throwable t) {
                        Log.e("Snapprefs", "Could not parse malformed JSON: \"" + text + "\"");
                        errorTV.setText("Error while applying for god, bad response");
                        errorTV.setVisibility(View.VISIBLE);
                    }

                }
            });
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Toast.makeText(Reedem.this, "ClientProtocolException" + e.toString(), Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(Reedem.this, "IOException" + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLicense(String deviceID, String confirmationID, int i) {
        if (confirmationID != null) {
            SharedPreferences.Editor editor = getSharedPreferences("com.marz.snapprefs_preferences", MODE_PRIVATE).edit();
            editor.putString("device_id", deviceID);
            editor.putInt(deviceID, i);
            editor.apply();
        }
    }

    public int readLicense(String deviceID, String confirmationID) {
        int status;
        if (confirmationID != null) {
            SharedPreferences prefs = getSharedPreferences("com.marz.snapprefs_preferences", MODE_PRIVATE);
            String dvcid = prefs.getString("device_id", null);
            if (dvcid != null && dvcid.equals(deviceID)) {
                status = prefs.getInt(deviceID, 0);
            } else {
                status = 0;
            }
        } else {
            status = 0;
        }
        return status;
    }

    public void saveStringPreference(String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences("com.marz.snapprefs_preferences", MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void saveDeviceID(String value) {
        SharedPreferences.Editor editor = getSharedPreferences("com.marz.snapprefs_preferences", MODE_PRIVATE).edit();
        editor.putString("device_id", value);
        editor.apply();
    }
    public String readStringPreference(String key) {
        SharedPreferences prefs = getSharedPreferences("com.marz.snapprefs_preferences", MODE_PRIVATE);
        String returned = prefs.getString(key, null);
        return returned;
    }

    private class Connection extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            postData(params[0], params[1]);
            return null;
        }

    }
    private class ConnectionGod extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            postGod(params[0], params[1]);
            return null;
        }

    }
}