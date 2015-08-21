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
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class PaintTools {
    static int color = Color.RED;
    static int width = 2;
    static Paint paint = null;
    static boolean easterEgg = false;
    static boolean shouldErase = false;
    static ImageButton eraserbutton;
    public static void initPaint(XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources modRes, final Context context) {
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
                paramsErase.topMargin = HookMethods.px(5.0f);
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

                ((RelativeLayout) colorpickerview.getParent().getParent()).addView(eraserbutton, paramsErase);
                ((RelativeLayout) colorpickerview.getParent().getParent()).addView(colorpicker, paramsPicker);
                ((RelativeLayout) colorpickerview.getParent().getParent()).addView(widthpicker, paramsWidth);
            }
        });

        findAndHookMethod("com.snapchat.android.ui.LegacyCanvasView", lpparam.classLoader, "setColor", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Logger.log("Called setColor: " + param.args[0], true);
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
