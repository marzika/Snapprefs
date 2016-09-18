package com.marz.snapprefs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.marz.snapprefs.Util.FileUtils;
import com.marz.snapprefs.Util.LensData;
import com.marz.snapprefs.Util.LensDatabaseHelper;
import com.marz.snapprefs.Util.LensIconLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import de.robv.android.xposed.XSharedPreferences;

public class Dialogs {
    static XSharedPreferences prefs;
    static Random rColor = new Random();
    private static Context SnapContext;
    private static TextView editText;
    private static double editDouble;
    private static HashMap<String, LensButtonPair> iconMap = new HashMap<>();

    public static boolean SpeedDialog(final Context context) {
        SnapContext = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(SnapContext);
        builder.setTitle("Enter Speed(m/s)");
        LinearLayout linearLayout = new LinearLayout(SnapContext);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        Preferences.refreshPreferences();

        final EditText eText = new EditText(SnapContext);
        eText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        linearLayout.addView(eText);
        builder.setView(linearLayout);
        builder.setPositiveButton(Common.dialog_done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (eText.getText().toString().trim().length() > 0 && Double.parseDouble(eText.getText().toString()) * 2.237D <= 9999.9F) {
                    double editDouble = Double.parseDouble(eText.getText().toString());
                    CharSequence text = editDouble * 3.6 + " KPH\n" + editDouble * 2.2369 + " MPH";
                    Spoofing.speed = (float) editDouble;
                    Toast.makeText(SnapContext, text, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SnapContext, "You must enter a valid number", Toast.LENGTH_SHORT).show();
                }

            }
        });
        builder.setNegativeButton(Common.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });
        builder.show();
        return true;
    }

    public static boolean WeatherDialog(final Context context) {
        SnapContext = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(SnapContext);
        builder.setTitle("Enter Temperature value");
        LinearLayout linearLayout = new LinearLayout(SnapContext);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        Preferences.refreshPreferences();

        final EditText eText = new EditText(SnapContext);
        eText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        linearLayout.addView(eText);
        builder.setView(linearLayout);
        builder.setPositiveButton(Common.dialog_done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (eText.getText().toString().trim().length() > 0) {
                    int editDouble = Integer.parseInt(eText.getText().toString());
                    CharSequence text = "Temperature set to " + editDouble;
                    //Spoofing.temp = (float) editDouble;
                    FileUtils.writeToFile(String.valueOf(editDouble), SnapContext, "weather");
                    Toast.makeText(SnapContext, text, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SnapContext, "You must enter a valid number", Toast.LENGTH_SHORT).show();
                }

            }
        });
        builder.setNegativeButton(Common.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });
        builder.show();
        return true;
    }

    public static void lensDialog(final Context context, final TextView loadedLensesTextView) {
        if (MainActivity.lensDBHelper == null)
            MainActivity.lensDBHelper = new LensDatabaseHelper(context);

        ArrayList<LensData> lensList = MainActivity.lensDBHelper.getAllLenses();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Lenses");
        GridLayout.LayoutParams gridLayoutParams = new GridLayout.LayoutParams();
        gridLayoutParams.setGravity(Gravity.CENTER);
        gridLayoutParams.columnSpec = GridLayout.spec(Gravity.CENTER);
        GridLayout gridLayout = new GridLayout(context);
        gridLayout.setLayoutParams(gridLayoutParams);
        gridLayout.setColumnCount(4);

        for (LensData lensData : lensList) {
            ImageButton button = new ImageButton(context);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageButton btn = (ImageButton) v;
                    String mCode = (String) btn.getTag();
                    Log.d("snapchat", "Something happening: " + mCode);
                    try {
                        boolean activeState = MainActivity.lensDBHelper.toggleLensActiveState(mCode);
                        btn.setBackgroundColor(Color.argb(150, 200, 200, activeState ? 0 : 200));
                        btn.invalidate();
                    } catch (Exception e) {
                        Log.d("snapchat", "No lens found with code: " + mCode + "\n" + e.getMessage());
                    }
                }
            });

            button.setMinimumHeight(50);
            button.setMinimumWidth(50);
            button.setBackgroundColor(Color.argb(150, 200, 200, lensData.mActive ? 0 : 200));
            button.setTag(lensData.mCode);

            LensButtonPair buttonPair = iconMap.get(lensData.mCode);

            if (buttonPair == null || buttonPair.bmp == null) {
                buttonPair = new LensButtonPair(button, null, lensData.mIconLink);
                iconMap.put(lensData.mCode, buttonPair);
                new LensIconLoader.AsyncLensIconDownloader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, buttonPair, context);
            } else {
                button.setImageBitmap(buttonPair.bmp);
                button.invalidate();
            }

            gridLayout.addView(button);
        }


        builder.setView(gridLayout);
        builder.setPositiveButton(Common.dialog_done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedLensSize = MainActivity.lensDBHelper.getActiveLensCount();
                loadedLensesTextView.setText(String.format("%s", selectedLensSize));
            }
        });
        builder.setNegativeButton(Common.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedLensSize = MainActivity.lensDBHelper.getActiveLensCount();
                loadedLensesTextView.setText(String.format("%s", selectedLensSize));
            }
        });

        builder.show();
    }

    public static class LensButtonPair {
        public ImageButton button;
        public Bitmap bmp;
        public String url;

        public LensButtonPair(ImageButton button, Bitmap bmp, String url) {
            this.button = button;
            this.bmp = bmp;
            this.url = url;
        }
    }
} 
