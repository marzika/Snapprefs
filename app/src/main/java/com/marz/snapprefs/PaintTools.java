package com.marz.snapprefs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.XModuleResources;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class PaintTools {
    static int color = Color.RED;
    static int width = 2;
    static int alpha = 255;
    static List<Integer> colorList = new ArrayList<Integer>();
    static Paint paint = null;
    static boolean easterEgg = false;
    static boolean shouldErase = false;
    static ImageButton eraserbutton;
    static Context context;


    public static void initPaint(XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes) {
        colorList.add(Color.RED);
        XposedHelpers.findAndHookConstructor("com.snapchat.android.ui.LegacyCanvasView", lpparam.classLoader, Context.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args[0] != null) {
                    context = (Context) param.args[0];
                } else {
                    Logger.log("Got the Context to use, but it's null :(", true);
                }
            }
        });
        Class<?> legacyCanvasView = findClass("com.snapchat.android.ui.LegacyCanvasView", lpparam.classLoader);
        XposedHelpers.findAndHookConstructor("com.snapchat.android.ui.LegacyCanvasView$a", lpparam.classLoader, legacyCanvasView, int.class, float.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Logger.log("CanvasView - ORIGINAL, setColor: " + param.args[1] + " setStrokeWidth: " + param.args[2], true);
                //param.args[2] = width;
                //Logger.log("CanvasView - NEW setColor: " + color + " setStrokeWidth: " + width, true);
                param.args[1] = color;
                param.args[2] = width;
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                paint = (Paint) getObjectField(param.thisObject, "a");
                if (paint == null) {
                    Logger.log("CanvasView-launched -- paint = null", true);
                } else {
                    if (shouldErase) {
                        paint.setColor(0x00000000);
                        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                        paint.setAlpha(0x00);
                    } else {
                        paint.setXfermode(null);
                        paint.setAlpha(alpha);
                    }
                    if (easterEgg) {
                        int[] rainbow = getRainbowColors();
                        Shader shader = new LinearGradient(0, 0, 0, 720, rainbow, null, Shader.TileMode.MIRROR);
                        Matrix matrix = new Matrix();
                        matrix.setRotate(90);
                        shader.setLocalMatrix(matrix);
                        paint.setShader(shader);
                    }
                }
            }
        });
        XposedHelpers.findAndHookConstructor("com.snapchat.android.ui.ColorPickerView", lpparam.classLoader, Context.class, AttributeSet.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                View colorpickerview = (View) getObjectField(param.thisObject, "h");
                if (colorpickerview == null) {
                    Logger.log("colorPickerView-launched -- colorpickerview = null", true);
                } else {
                    Logger.log("colorPickerView-launched -- colorpickerview = NOT null", true);
                }
                eraserbutton = new ImageButton(context);
                eraserbutton.setBackgroundColor(0);
                eraserbutton.setImageDrawable(modRes.getDrawable(R.drawable.eraser));
                eraserbutton.setScaleX((float) 0.4);
                eraserbutton.setScaleY((float) 0.4);
                eraserbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shouldErase = true;
                        eraserbutton.setImageDrawable(modRes.getDrawable(R.drawable.eraser_clicked));
                    }
                });
                RelativeLayout.LayoutParams paramsErase = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                paramsErase.topMargin = HookMethods.px(0.0f);
                paramsErase.rightMargin = HookMethods.px(5.0f);
                paramsErase.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                ImageButton colorpicker = new ImageButton(context);
                colorpicker.setBackgroundColor(0);
                colorpicker.setImageDrawable(modRes.getDrawable(R.drawable.colorpicker));
                colorpicker.setScaleX((float) 0.4);
                colorpicker.setScaleY((float) 0.4);
                colorpicker.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Toast.makeText(context, "EasterEgg found!", Toast.LENGTH_SHORT).show();
                        easterEgg = true;
                        return true;
                    }
                });
                colorpicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        eraserbutton.setImageDrawable(modRes.getDrawable(R.drawable.eraser));
                        easterEgg = false;
                        shouldErase = false;
                        ColorPickerDialog colorPickerDialog = new ColorPickerDialog(context, color, new ColorPickerDialog.OnColorSelectedListener() {

                            @Override
                            public void onColorSelected(int n) {
                                // TODO Auto-generated method stub
                                color = n;
                                if (!colorList.contains(n)) {
                                    colorList.add(n);
                                }
                            }
                        });
                        colorPickerDialog.setTitle("Select stroke color");
                        colorPickerDialog.show();
                    }
                });
                RelativeLayout.LayoutParams paramsPicker = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                paramsPicker.topMargin = HookMethods.px(-40.0f);
                paramsPicker.rightMargin = HookMethods.px(5.0f);
                paramsPicker.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                ImageButton widthpicker = new ImageButton(context);
                widthpicker.setBackgroundColor(0);
                widthpicker.setImageDrawable(modRes.getDrawable(R.drawable.width));
                widthpicker.setScaleX((float) 0.4);
                widthpicker.setScaleY((float) 0.4);
                widthpicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        LinearLayout linearLayout = new LinearLayout(context);
                        linearLayout.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                        final TextView tv = new TextView(context);
                        tv.setText("Currently selected width: " + width);
                        final SeekBar seekBar2 = new SeekBar(context);
                        seekBar2.setMax(30);
                        seekBar2.setProgress(width);
                        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            public void onProgressChanged(SeekBar seekBar2, int n, boolean bl) {
                                if (n == 0) {
                                    n = n + 1;
                                }
                                tv.setText("Currently selected width: " + n);
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
                                width = 2;
                            }
                        });
                        builder.setPositiveButton(Common.dialog_done, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                width = seekBar2.getProgress();
                                if (width == 0) {
                                    width = width + 1;
                                }
                            }
                        });
                        linearLayout.addView(tv, params);
                        linearLayout.addView(seekBar2, params);
                        builder.setView((View) linearLayout);
                        builder.show();
                    }
                });
                RelativeLayout.LayoutParams paramsWidth = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                paramsWidth.topMargin = HookMethods.px(-40.0f);
                paramsWidth.rightMargin = HookMethods.px(55.0f);
                paramsWidth.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                ImageButton alphabutton = new ImageButton(context);
                alphabutton.setBackgroundColor(0);
                alphabutton.setImageDrawable(modRes.getDrawable(R.drawable.opacity));
                alphabutton.getDrawable().setDither(true);
                alphabutton.setScaleX((float) 0.4);
                alphabutton.setScaleY((float) 0.4);
                alphabutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        LinearLayout linearLayout = new LinearLayout(context);
                        linearLayout.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                        final TextView tv = new TextView(context);
                        tv.setText("Currently selected transparency: " + alpha);
                        final SeekBar seekBar2 = new SeekBar(context);
                        seekBar2.setMax(255);
                        seekBar2.setProgress(alpha);
                        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            public void onProgressChanged(SeekBar seekBar2, int n, boolean bl) {
                                int m = n + 1;
                                tv.setText("Currently selected transparency: " + m);
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
                                alpha = 255;
                            }
                        });
                        builder.setPositiveButton(Common.dialog_done, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alpha = seekBar2.getProgress();
                            }
                        });
                        linearLayout.addView(tv, params);
                        linearLayout.addView(seekBar2, params);
                        builder.setView((View) linearLayout);
                        builder.show();
                    }
                });
                RelativeLayout.LayoutParams paramsAlpha = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                paramsAlpha.topMargin = HookMethods.px(0.0f);
                paramsAlpha.rightMargin = HookMethods.px(55.0f);
                paramsAlpha.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                ImageButton hexinput = new ImageButton(context);
                hexinput.setBackgroundColor(0);
                hexinput.setImageDrawable(modRes.getDrawable(R.drawable.hashtag));
                hexinput.setScaleX((float) 0.4);
                hexinput.setScaleY((float) 0.4);
                hexinput.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    eraserbutton.setImageDrawable(modRes.getDrawable(R.drawable.eraser));
                                                    easterEgg = false;
                                                    shouldErase = false;

                                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                                    builder.setTitle("Enter HEX color - #AARRGGBB");
                                                    LinearLayout linearLayout = new LinearLayout(context);
                                                    linearLayout.setOrientation(LinearLayout.VERTICAL);

                                                    final EditText eText = new EditText(context);
                                                    //eText.setKeyListener(DigitsKeyListener.getInstance("0123456789ABCDEF"));
                                                    //eText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                                                    eText.addTextChangedListener(new TextWatcher() {
                                                        @Override
                                                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                        }

                                                        @Override
                                                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                                        }

                                                        //Right after the text is changed
                                                        @Override
                                                        public void afterTextChanged(Editable s) {
                                                            //Store the text on a String
                                                            String text = s.toString();

                                                            //Get the length of the String
                                                            int length = s.length();

                /*If the String length is bigger than zero and it's not
                composed only by the following characters: A to F and/or 0 to 9 */
                                                            if (!text.matches("[a-fA-F0-9]+") && length > 0) {
                                                                //Delete the last character
                                                                s.delete(length - 1, length);
                                                            }
                                                        }
                                                    });
                                                    eText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                                                    linearLayout.addView(eText);
                                                    builder.setView((View) linearLayout);
                                                    builder.setPositiveButton(Common.dialog_done, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    //Create and initialize the int 'color' with the white color.
                                                                    int typedColor = 0xFFFFFFFF;

                                                                    try {
                                                                        //Get the text at the EditText field and save it in a String
                                                                        String colorValue = eText.getText().toString();

                                    /*Convert the String into a Color ARGB hexadecimal integer.*/
                                                                        typedColor = Integer.parseInt(colorValue.substring(2), 16) + (Integer.parseInt(colorValue.substring(0, 2), 16) << 24);
                                                                        color = typedColor;
                                                                        alpha = Color.alpha(color);
                                                                        if (!colorList.contains(typedColor)) {
                                                                            colorList.add(typedColor);
                                                                        }
                                                                        //Toast.makeText(context, colorValue, Toast.LENGTH_SHORT).show();
                                                                        Toast toast = Toast.makeText(context, "           ", Toast.LENGTH_LONG);
                                                                        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                                                                        View view = toast.getView();
                                                                        view.setBackgroundColor(typedColor);
                                                                        v.setTextColor(typedColor);
                                                                        toast.show();
                                                                    } catch (Exception e) //Something went wrong while parsing the String into an Integer
                                                                    {
                                                                        //Reset the EditText field to white
                                                                        //Display a toast message
                                                                        Toast.makeText(context, "You must enter a valid HEX color in AARRGGBB format", Toast.LENGTH_SHORT).show();
                                                                    }

                                                                }

                                                            }
                                                    );
                                                    builder.setNegativeButton(Common.dialog_cancel, new DialogInterface.OnClickListener()

                                                            {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    // TODO Auto-generated method stub

                                                                }
                                                            }

                                                    );
                                                    builder.show();
                                                }
            }

                );
                RelativeLayout.LayoutParams paramsHex = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                paramsHex.topMargin = HookMethods.px(50.0f);
                paramsHex.rightMargin = HookMethods.px(55.0f);
                paramsHex.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                ImageButton colorhistory = new ImageButton(context);
                colorhistory.setBackgroundColor(0);
                colorhistory.setImageDrawable(modRes.getDrawable(R.drawable.history));
                colorhistory.setScaleX((float) 0.4);
                colorhistory.setScaleY((float) 0.4);
                colorhistory.setOnClickListener(new View.OnClickListener()

                                                {
                                                    @Override
                                                    public void onClick(View v) {
                                                        eraserbutton.setImageDrawable(modRes.getDrawable(R.drawable.eraser));
                                                        easterEgg = false;
                                                        shouldErase = false;

                                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                                        SpannableStringBuilder spanBuilder = new SpannableStringBuilder();
                                                        String size;
                                                        if (colorList.size() == 0) {
                                                            size = "Empty";
                                                        } else {
                                                            size = String.valueOf(colorList.size());
                                                        }
                                                        builder.setTitle("Color History - Size: " + size);
                                                        LinearLayout linearLayout = new LinearLayout(context);
                                                        LinearLayout.LayoutParams paramColors = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                                        linearLayout.setOrientation(LinearLayout.VERTICAL);
                                                        linearLayout.setGravity(Gravity.CENTER);
                                                        TextView colors = new TextView(context);
                                                        colors.setMaxLines(20);
                                                        colors.setVerticalScrollBarEnabled(true);
                                                        //colors.setScrollBarStyle();
                                                        colors.setMovementMethod(new ScrollingMovementMethod());
                                                        int i = 0;
                                                        for (Iterator<Integer> it = colorList.iterator(); it.hasNext(); i++) {
                                                            Integer s = it.next();
                                                            if (colors.getText().equals("")) {
                                                                //colors.setText(i + ": " + s);
                                                                int ii = i + 1;
                                                                String hexColor = String.format("#%08X", (0xFFFFFFFF & s));
                                                                String first = ii + ": " + hexColor + "  -";
                                                                String second = "-  " + hexColor;
                                                                SpannableString firstSpannable = new SpannableString(first);
                                                                SpannableString secondSpannable = new SpannableString(second);
                                                                firstSpannable.setSpan(new BackgroundColorSpan(s), 0, first.length(), 0);
                                                                firstSpannable.setSpan(new ForegroundColorSpan(Color.BLACK), 0, first.length(), 0);
                                                                secondSpannable.setSpan(new BackgroundColorSpan(s), 0, second.length(), 0);
                                                                secondSpannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, second.length(), 0);
                                                                spanBuilder.append(firstSpannable);
                                                                spanBuilder.append(secondSpannable);
                                                                colors.setText(spanBuilder, TextView.BufferType.SPANNABLE);
                                                            } else {
                                                                //colors.setText(colors.getText() + "\n"+ i + ": " + s);
                                                                int ii = i + 1;
                                                                String hexColor = String.format("#%08X", (0xFFFFFFFF & s));
                                                                String first = "\n" + ii + ": " + hexColor + "  -";
                                                                String second = "-  " + hexColor;
                                                                SpannableString firstSpannable = new SpannableString(first);
                                                                SpannableString secondSpannable = new SpannableString(second);
                                                                firstSpannable.setSpan(new BackgroundColorSpan(s), 0, first.length(), 0);
                                                                firstSpannable.setSpan(new ForegroundColorSpan(Color.BLACK), 0, first.length(), 0);
                                                                secondSpannable.setSpan(new BackgroundColorSpan(s), 0, second.length(), 0);
                                                                secondSpannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, second.length(), 0);
                                                                spanBuilder.append(firstSpannable);
                                                                spanBuilder.append(secondSpannable);
                                                                colors.setText(spanBuilder, TextView.BufferType.SPANNABLE);
                                                            }
                                                        }
                                                        colors.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20.0f);
                                                        linearLayout.addView(colors, paramColors);
                                                        builder.setView((View) linearLayout);
                                                        builder.setPositiveButton(Common.dialog_done, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                    }
                                                                }
                                                        );
                                                        builder.show();
                                                    }
                                                }

                );
                RelativeLayout.LayoutParams paramsHistory = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                paramsHistory.topMargin = HookMethods.px(50.0f);
                paramsHistory.rightMargin = HookMethods.px(5.0f);
                paramsHistory.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);


                ((RelativeLayout) colorpickerview.getParent().getParent()).addView(alphabutton, paramsAlpha);
                ((RelativeLayout) colorpickerview.getParent().getParent()).addView(eraserbutton, paramsErase);
                ((RelativeLayout) colorpickerview.getParent().getParent()).addView(colorpicker, paramsPicker);
                ((RelativeLayout) colorpickerview.getParent().getParent()).addView(widthpicker, paramsWidth);
                ((RelativeLayout) colorpickerview.getParent().getParent()).addView(colorhistory, paramsHistory);
                ((RelativeLayout) colorpickerview.getParent().getParent()).addView(hexinput, paramsHex);
            }
        });

        findAndHookMethod("com.snapchat.android.ui.LegacyCanvasView", lpparam.classLoader, "setColor", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Logger.log("Called setColor: " + param.args[0], true);
                if (!colorList.contains((Integer) param.args[0])) {
                    colorList.add((Integer) param.args[0]);
                }
                color = (Integer) param.args[0];
                if (shouldErase == true) {
                    eraserbutton.setImageDrawable(modRes.getDrawable(R.drawable.eraser));
                    shouldErase = false;
                }
            }
        });

    }

    private static int[] getRainbowColors() {
        return new int[]{
                Color.RED,
                Color.YELLOW,
                Color.GREEN,
                Color.BLUE,
                Color.rgb(111, 74, 207)
        };
    }
}
