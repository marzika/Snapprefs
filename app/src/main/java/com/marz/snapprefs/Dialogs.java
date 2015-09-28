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

        HookMethods.refreshPreferences();

        final EditText eText = new EditText(SnapContext);
        eText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        linearLayout.addView(eText);
        builder.setView((View) linearLayout);
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

        HookMethods.refreshPreferences();

        final EditText eText = new EditText(SnapContext);
        eText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        linearLayout.addView(eText);
        builder.setView((View) linearLayout);
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

    public static boolean MainDialog(Context context, TextView editText) {
        SnapContext = context;
        Dialogs.editText = editText;
        final ColorDrawable cd_bground = (ColorDrawable) editText.getBackground();
        AlertDialog.Builder builder = new AlertDialog.Builder(SnapContext);
        builder.setTitle(Common.dialog_title);
        LinearLayout linearLayout = new LinearLayout(SnapContext);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        HookMethods.refreshPreferences();


        if (HookMethods.txtcolours == true) {
            Button button_txtcolour = new Button(SnapContext);
            button_txtcolour.setText(Common.dialog_txtcolour);
            button_txtcolour.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    ColorPickerDialog colorPickerDialog = new ColorPickerDialog(Dialogs.SnapContext, Color.WHITE, new ColorPickerDialog.OnColorSelectedListener() {

                        @Override
                        public void onColorSelected(int color) {
                            // TODO Auto-generated method stub
                            Dialogs.editText.setTextColor(color);
                        }
                    });
                    colorPickerDialog.setButton(-3, Common.dialog_default, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            // TODO Auto-generated method stub
                            Dialogs.editText.setTextColor((Color.parseColor("#00ffffff")));
                            Dialogs.editText.setAlpha(1);
                        }
                    });
                    colorPickerDialog.setTitle(Common.dialog_txtcolour);
                    colorPickerDialog.show();
                }
            });
            linearLayout.addView((View) button_txtcolour);
        }

        if (HookMethods.txtstyle == true) {
            Button button_txtstyle = new Button(SnapContext);
            button_txtstyle.setText(Common.dialog_txtstyle);
            button_txtstyle.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(SnapContext);
                    Button button_bold = new Button(SnapContext);
                    Button button_italic = new Button(SnapContext);
                    Button button_bolditalic = new Button(SnapContext);
                    Button button_normal = new Button(SnapContext);
                    LinearLayout linearLayout2 = new LinearLayout(SnapContext);
                    linearLayout2.setOrientation(1);
                    button_bold.setText(Common.dialog_bold);
                    button_bold.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            Dialogs.editText.setTypeface(null, Typeface.BOLD);
                        }
                    });
                    button_italic.setText(Common.dialog_italic);
                    button_italic.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            Dialogs.editText.setTypeface(null, Typeface.ITALIC);
                        }
                    });
                    button_bolditalic.setText(Common.dialog_bolditalic);
                    button_bolditalic.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            Dialogs.editText.setTypeface(null, Typeface.BOLD_ITALIC);
                        }
                    });
                    button_normal.setText(Common.dialog_normal);
                    button_normal.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            Dialogs.editText.setTypeface(null, Typeface.NORMAL);
                        }
                    });
                    linearLayout2.addView((View) button_bold);
                    linearLayout2.addView((View) button_italic);
                    linearLayout2.addView((View) button_bolditalic);
                    linearLayout2.addView((View) button_normal);
                    builder2.setView((View) linearLayout2);
                    builder2.setPositiveButton(Common.dialog_done, null);
                    builder2.show();
                }
            });
            linearLayout.addView((View) button_txtstyle);
        }

        if (HookMethods.txtgravity == true) {
            Button button_txtgravity = new Button(SnapContext);
            button_txtgravity.setText(Common.dialog_txtgravity);
            button_txtgravity.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    AlertDialog.Builder builder3 = new AlertDialog.Builder(SnapContext);
                    Button button_left = new Button(SnapContext);
                    Button button_center = new Button(SnapContext);
                    Button button_right = new Button(SnapContext);
                    LinearLayout linearLayout3 = new LinearLayout(SnapContext);
                    linearLayout3.setOrientation(1);
                    button_left.setText(Common.dialog_left);
                    button_left.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            Dialogs.editText.setGravity(Gravity.LEFT);
                        }
                    });
                    button_center.setText(Common.dialog_center);
                    button_center.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            Dialogs.editText.setGravity(Gravity.CENTER);
                        }
                    });
                    button_right.setText(Common.dialog_right);
                    button_right.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            Dialogs.editText.setGravity(Gravity.RIGHT);
                        }
                    });
                    linearLayout3.addView((View) button_left);
                    linearLayout3.addView((View) button_center);
                    linearLayout3.addView((View) button_right);
                    builder3.setView((View) linearLayout3);
                    builder3.setPositiveButton(Common.dialog_done, null);
                    builder3.show();
                }
            });
            linearLayout.addView((View) button_txtgravity);
        }

        if (HookMethods.transparency == true) {
            Button button_transparency = new Button(SnapContext);
            button_transparency.setText(Common.dialog_transparency);
            button_transparency.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SnapContext);
                    SeekBar seekBar2 = new SeekBar(SnapContext);
                    seekBar2.setMax(100);
                    seekBar2.setProgress((int) Dialogs.editText.getAlpha() * 100);
                    seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        public void onProgressChanged(SeekBar seekBar2, int n, boolean bl) {
                            float alpha = (float) n / 100;
                            Dialogs.editText.setAlpha(alpha);
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar arg0) {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar arg0) {
                            // TODO Auto-generated method stub

                        }

                    });
                    builder.setNeutralButton(Common.dialog_default, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Dialogs.editText.setAlpha(1);
                        }
                    });
                    builder.setPositiveButton(Common.dialog_done, null);
                    builder.setView((View) seekBar2);
                    builder.show();
                }
            });
            linearLayout.addView((View) button_transparency);
        }
        if (HookMethods.bgcolours == true) {
            Button button_bgcolour = new Button(SnapContext);
            button_bgcolour.setText(Common.dialog_bgcolour);
            button_bgcolour.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    ColorPickerDialog colorPickerDialog = new ColorPickerDialog(Dialogs.SnapContext, Color.BLACK, new ColorPickerDialog.OnColorSelectedListener() {

                        @Override
                        public void onColorSelected(int color) {
                            // TODO Auto-generated method stub
                            Dialogs.editText.setBackgroundColor(color);
                        }
                    });

                    colorPickerDialog.setButton(-3, Common.dialog_default, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            // TODO Auto-generated method stub
                            Dialogs.editText.setBackgroundColor((Color.parseColor("#99000000")));
                        }
                    });
                    colorPickerDialog.setTitle(Common.dialog_bgcolour);
                    colorPickerDialog.show();
                }
            });
            linearLayout.addView((View) button_bgcolour);
        }

        if (HookMethods.bg_transparency == true) {
            Button button_bg_transparency = new Button(SnapContext);
            button_bg_transparency.setText(Common.dialog_bgtransparency);
            button_bg_transparency.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SnapContext);
                    SeekBar seekBar3 = new SeekBar(SnapContext);
                    seekBar3.setMax(255);
                    seekBar3.setProgress((int) cd_bground.getAlpha());
                    seekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        public void onProgressChanged(SeekBar seekBar3, int n, boolean bl) {
                            cd_bground.setAlpha(n);
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar arg0) {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar arg0) {
                            // TODO Auto-generated method stub

                        }

                    });
                    builder.setNeutralButton(Common.dialog_default, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cd_bground.setAlpha(153);
                        }
                    });
                    builder.setPositiveButton(Common.dialog_done, null);
                    builder.setView((View) seekBar3);
                    builder.show();
                }
            });
            linearLayout.addView((View) button_bg_transparency);
        }


        if (HookMethods.size == true) {
            Button button_size = new Button(SnapContext);
            button_size.setText(Common.dialog_size);
            button_size.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SnapContext);
                    SeekBar seekBar = new SeekBar(SnapContext);
                    seekBar.setMax(300);
                    seekBar.setProgress((int) Dialogs.editText.getTextSize());
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        public void onProgressChanged(SeekBar seekBar, int n, boolean bl) {
                            Dialogs.editText.setTextSize(n);
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar arg0) {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar arg0) {
                            // TODO Auto-generated method stub

                        }

                    });
                    builder.setNeutralButton(Common.dialog_default, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Dialogs.editText.setTextSize(21.0f);
                        }
                    });
                    builder.setPositiveButton(Common.dialog_done, null);
                    builder.setView((View) seekBar);
                    builder.show();
                }
            });
            linearLayout.addView((View) button_size);
        }

        if (HookMethods.rainbow == true) {
            Button button_rainbow = new Button(SnapContext);
            button_rainbow.setText(Common.dialog_rainbow);
            button_rainbow.setOnClickListener(new OnClickListener() {

                public void onClick(View view) {

                    String tempText = Dialogs.editText.getText().toString();
                    HookMethods.logging("~~~~~DEBUG: original " + tempText);
                    tempText.replace("\\n", "<br>");
                    tempText.replace("\\r", "<br>");
                    HookMethods.logging("~~~~~DEBUG: replaced " + tempText);
                    String[] tempTextArray = tempText.split("");
                    String newText = "";

                    for (int i = 0; i <= tempText.length(); i++) {
                        if (!tempTextArray[i].equals(" ")) {
                            int c = rColor.nextInt(329);
                            newText = newText + "<font color=" + Common.colors[c] + ">" + tempTextArray[i] + "</font>";
                        } else {
                            newText = newText + " ";
                        }
                    }
                    Dialogs.editText.setText(Html.fromHtml(newText));
                }
            });
            linearLayout.addView((View) button_rainbow);
        }
        builder.setView((View) linearLayout);
        builder.setPositiveButton(Common.dialog_done, null);
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
