package com.marz.snapprefs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.marz.snapprefs.Util.FileUtils;

import java.util.Random;

import de.robv.android.xposed.XSharedPreferences;

public class Dialogs {

    private static final String PACKAGE_NAME = HookMethods.class.getPackage().getName();
    static XSharedPreferences prefs;
    static Random rColor = new Random();
    private static Context SnapContext;
    private static TextView editText;
    private static double editDouble;

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
} 
