package com.marz.snapprefs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.MaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
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
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
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

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class PaintTools {
    static int color = Color.RED;
    static int width = 2;
    static int alpha = 255;
    static List<Integer> colorList = new ArrayList<Integer>();
    static Paint paint = null;
    static boolean useShader = false;
    static boolean shouldErase = false;
    static boolean shouldBlur = false;
    public static boolean hidden = false;
    static Context context;
    public static final String START_POINT = "startPoint";
    public static final String END_POINT = "endPoint";
    private static final String TYPE = "type";
    private static DrawingType type = DrawingType.DEFAULT;
    static boolean once = false;
    static RelativeLayout outerOptionsLayout;
    static Shader shader;

    enum DrawingType {
        DEFAULT, LINE, RECTANGLE, CIRCLE, STAR
    }


    public static void initPaint(final XC_LoadPackage.LoadPackageParam lpparam, final XModuleResources mResources) {
        final Bitmap[] background = new Bitmap[1];
        findAndHookConstructor("com.snapchat.android.model.Mediabryo", lpparam.classLoader, findClass("com.snapchat.android.model.Mediabryo$a", lpparam.classLoader), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                background[0] = (Bitmap) getObjectField(param.thisObject, "mRawImageBitmap");
            }
        });
        colorList.add(Color.RED);
        //Use method for setting last point
        XposedHelpers.findAndHookMethod("com.snapchat.android.ui.LegacyCanvasView", lpparam.classLoader, "a", float.class, float.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Object i = XposedHelpers.getObjectField(param.thisObject, "j");
                if (i != null && type != null && type != DrawingType.DEFAULT) {//only if there is object being drawn
                    XposedHelpers.callMethod(i, "b", param.args[0], param.args[1]);//was a
                }
            }
        });
        //it's not normally method onMove but i made it work like that xD (normally it just sets start of drawing)
        XposedHelpers.findAndHookMethod(Obfuscator.paint.LEGACYCANVASVIEW_A, lpparam.classLoader, "b", float.class, float.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                if (type == DrawingType.DEFAULT || type == null) return;
                if (XposedHelpers.getAdditionalInstanceField(methodHookParam.thisObject, TYPE) == null) {
                    XposedHelpers.setAdditionalInstanceField(methodHookParam.thisObject, TYPE, type);
                }
                if (XposedHelpers.getAdditionalInstanceField(methodHookParam.thisObject, START_POINT) == null) {
                    PointF p = new PointF();
                    p.set((float) methodHookParam.args[0] - 0.1f, (float) methodHookParam.args[1] - 0.1f);
                    XposedHelpers.setAdditionalInstanceField(methodHookParam.thisObject, START_POINT, p);
                }
                if (XposedHelpers.getAdditionalInstanceField(methodHookParam.thisObject, END_POINT) == null) {
                    XposedHelpers.setAdditionalInstanceField(methodHookParam.thisObject, END_POINT, new PointF());
                }
                PointF startPoint = (PointF) XposedHelpers.getAdditionalInstanceField(methodHookParam.thisObject, START_POINT);
                PointF endPoint = (PointF) XposedHelpers.getAdditionalInstanceField(methodHookParam.thisObject, END_POINT);
                endPoint.set((float) methodHookParam.args[0], (float) methodHookParam.args[1]);
                if (XposedHelpers.getAdditionalInstanceField(methodHookParam.thisObject, TYPE) == DrawingType.STAR) {
                    //FIXME: this shit needs fixing or removing
                    //Right now draws star, but it's not well scaled (not following straight the touch, test it to get the idea)
                    float size = Math.max(Math.abs(endPoint.x - startPoint.x), Math.abs(endPoint.y - startPoint.y));
                    float size1 = size * 2;
                    if (XposedHelpers.getAdditionalInstanceField(methodHookParam.thisObject, "path") == null) {
                        XposedHelpers.setAdditionalInstanceField(methodHookParam.thisObject, "path", new Path());
                    }
                    Path path = (Path) XposedHelpers.getAdditionalInstanceField(methodHookParam.thisObject, "path");
                    path.reset();
                    path.moveTo(startPoint.x + size1 * 0.5f - size, startPoint.y + size1 * 0.84f - size);
                    path.lineTo(startPoint.x + size1 * 1.5f - size, startPoint.y + size1 * 0.84f - size);
                    path.lineTo(startPoint.x + size1 * 0.68f - size, startPoint.y + size1 * 1.45f - size);
                    path.lineTo(startPoint.x + size1 * 1.0f - size, startPoint.y + size1 * 0.5f - size);
                    path.lineTo(startPoint.x + size1 * 1.32f - size, startPoint.y + size1 * 1.45f - size);
                    path.lineTo(startPoint.x + size1 * 0.5f - size, startPoint.y + size1 * 0.84f - size);
                }
                methodHookParam.setResult(null);
            }
        });
        XposedHelpers.findAndHookMethod(Obfuscator.paint.LEGACYCANVASVIEW_A, lpparam.classLoader, "a", Canvas.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                DrawingType dType = (DrawingType) XposedHelpers.getAdditionalInstanceField(methodHookParam.thisObject, TYPE);
                if (dType == DrawingType.DEFAULT || dType == null) return;
                Canvas c = (Canvas) methodHookParam.args[0];
                PointF startPoint = (PointF) XposedHelpers.getAdditionalInstanceField(methodHookParam.thisObject, START_POINT);
                PointF endPoint = (PointF) XposedHelpers.getAdditionalInstanceField(methodHookParam.thisObject, END_POINT);
                if (dType == DrawingType.RECTANGLE)
                    c.drawRect(startPoint.x, startPoint.y, endPoint.x, endPoint.y, (Paint) XposedHelpers.getObjectField(methodHookParam.thisObject, "a"));
                else if (dType == DrawingType.CIRCLE) {
                    float radius = (float) Math.sqrt(Math.pow(startPoint.x - endPoint.x, 2) + Math.pow(startPoint.y - endPoint.y, 2));
                    c.drawCircle(startPoint.x, startPoint.y, radius, (Paint) XposedHelpers.getObjectField(methodHookParam.thisObject, "a"));
                } else if (dType == DrawingType.STAR) {
                    Path path = (Path) XposedHelpers.getAdditionalInstanceField(methodHookParam.thisObject, "path");
                    c.drawPath(path, (Paint) XposedHelpers.getObjectField(methodHookParam.thisObject, "a"));
                } else if (dType == DrawingType.LINE)
                    c.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, (Paint) XposedHelpers.getObjectField(methodHookParam.thisObject, "a"));
                methodHookParam.setResult(null);
            }
        });

        XposedHelpers.findAndHookConstructor("com.snapchat.android.ui.LegacyCanvasView", lpparam.classLoader, Context.class, boolean.class,new XC_MethodHook() {
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
        XposedHelpers.findAndHookConstructor(Obfuscator.paint.LEGACYCANVASVIEW_A, lpparam.classLoader, int.class, float.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Logger.log("CanvasView - ORIGINAL, setColor: " + param.args[0] + " setStrokeWidth: " + param.args[1], true);
                //param.args[2] = width;
                //Logger.log("CanvasView - NEW setColor: " + color + " setStrokeWidth: " + width, true);
                param.args[0] = color;
                param.args[1] = width;
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                paint = (Paint) getObjectField(param.thisObject, "a");
                if (paint == null) {
                    Logger.log("CanvasView-launched -- paint = null", true);
                } else {
                    MaskFilter oldMF = paint.getMaskFilter();
                    if (shouldErase) {
                        paint.setColor(0x00000000);
                        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                        paint.setAlpha(0x00);
                    } else {
                        paint.setXfermode(null);
                        paint.setAlpha(alpha);
                    }
                    if (shouldBlur) {
                        if (background[0] != null) {
                            paint.setColor(0x00000000);
                            paint.setShader(new BitmapShader(background[0], Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
                        } else {
                            paint.setColor(0xccFFFFFF);
                        }
                        paint.setMaskFilter(new BlurMaskFilter(25, BlurMaskFilter.Blur.NORMAL));
                    } else {
                        paint.setMaskFilter(oldMF);
                    }
                    if (useShader) {
                        Matrix matrix = new Matrix();
                        shader.setLocalMatrix(matrix);
                        paint.setShader(shader);
                    }
                }
            }
        });

        findAndHookMethod("com.snapchat.android.analytics.AnalyticsEvents", lpparam.classLoader, "h", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try{
                    outerOptionsLayout.setVisibility(View.VISIBLE);
                }catch (NullPointerException ignore){
                    //This method is being called on every button press, before the ColorPickerView constructor is called for the first time
                    //Therefore the first press after the capture will throw a NPE
                }
            }
        });
        once = false;
        XposedHelpers.findAndHookConstructor("com.snapchat.android.app.shared.feature.preview.ui.view.ColorPickerView", lpparam.classLoader, Context.class, AttributeSet.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                if (!once){
                    View colorPickerView = (View) getObjectField(param.thisObject, "j");
                    outerOptionsLayout = new RelativeLayout(HookMethods.SnapContext);
                    final GridView innerOptionsView = new GridView(HookMethods.SnapContext);
                    innerOptionsView.setAdapter(new OptionsAdapter(HookMethods.SnapContext, mResources));
                    innerOptionsView.setNumColumns(5);
                    innerOptionsView.setHorizontalSpacing(px(2.0f));
                    innerOptionsView.setVerticalSpacing(px(5.0f));
                    innerOptionsView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
                    innerOptionsView.setPadding(0,px(7.5f), px(1.0f), px(7.5f));
                    final RelativeLayout.LayoutParams outerOptionsLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    outerOptionsLayoutParams.leftMargin = px(75.0f);
                    outerOptionsLayoutParams.rightMargin = px(25.0f);
                    outerOptionsLayout.setVisibility(View.VISIBLE);
                    outerOptionsLayout.setBackgroundDrawable(mResources.getDrawable(R.drawable.drawingbackground));
                    outerOptionsLayout.addView(innerOptionsView, GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.MATCH_PARENT);
                    ((RelativeLayout)colorPickerView.getParent().getParent()).addView(outerOptionsLayout, outerOptionsLayoutParams);
                    once = true;
                }
            }
        });
        findAndHookMethod("com.snapchat.android.ui.LegacyCanvasView", lpparam.classLoader, "setColor", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Logger.log("Called setColor: " + param.args[0], true);
                if (!colorList.contains(param.args[0])) {
                    colorList.add((Integer) param.args[0]);
                }
                color = (Integer) param.args[0];
                if (shouldErase == true) {
                    //eraserbutton.setImageDrawable(modRes.getDrawable(R.drawable.eraser));
                    shouldErase = false;
                }
                if (shouldBlur == true) {
                    //eraserbutton.setImageDrawable(modRes.getDrawable(R.drawable.eraser));
                    shouldBlur = false;
                }
            }
        });

    }
    public static int px(float f) {
        return Math.round((f * HookMethods.SnapContext.getResources().getDisplayMetrics().density));
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
    private static class OptionsAdapter extends BaseAdapter {
        String[] options = {"alpha", "eraser", "color", "gradient", "history", "hex", "shape", "blur" , "width", "hide"};
        Context context;
        XModuleResources mRes;
        int [] optionImageId = {R.drawable.alpha, R.drawable.eraser, R.drawable.colorpicker, R.drawable.draw_gradient, R.drawable.history, R.drawable.hashtag, R.drawable.shape, R.drawable.blur, R.drawable.width, R.drawable.hide};
        private static LayoutInflater inflater=null;

        public OptionsAdapter(Activity snapContext, XModuleResources mRes) {
            this.context = snapContext;
            this.mRes = mRes;
            inflater = ( LayoutInflater ) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return options.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public class Holder
        {
            TextView tv;
            ImageView img;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Holder holder=new Holder();
            View rowView;
            final int[] colorsGrad = new int[]{Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE};
            final int[] currentItem = {2};

            rowView = inflater.inflate(mRes.getLayout(R.layout.optionlayout), null);
            holder.tv=(TextView) rowView.findViewById(mRes.getIdentifier("description", "id", "com.marz.snapprefs"));
            holder.img=(ImageView) rowView.findViewById(mRes.getIdentifier("textIcon", "id", "com.marz.snapprefs"));

            holder.tv.setText(options[position]);
            holder.img.setImageDrawable(mRes.getDrawable(optionImageId[position]));

            rowView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    switch (position) {
                        case 0: { //alpha
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
                            builder.setView(linearLayout);
                            builder.show();
                            return;
                        }
                        case 1: {//eraser
                            type = DrawingType.DEFAULT;
                            shouldErase = true;
                            shouldBlur = false;
                            return;
                        }
                        case 2: {//color
                            useShader = false;
                            shouldErase = false;
                            shouldBlur = false;

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
                            return;
                        }
                        case 3: {//gradient
                            shouldErase = false;
                            shouldBlur = false;
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Drawing Gradient");
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                            LinearLayout rootLayout = new LinearLayout(context);
                            LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            rootLayout.addView(inflater.inflate(HookMethods.modRes.getLayout(R.layout.gradient_layout), null), rootParams);
                            final RadioGroup orientation = (RadioGroup) rootLayout.findViewById(R.id.orientation);
                            final LinearLayout listLayout = (LinearLayout) rootLayout.findViewById(R.id.itemLayout);
                            final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);

                            for (int i = 1; i <= 5; i++) {
                                Button btn = new Button(context);
                                btn.setId(i);
                                final int id_ = btn.getId();
                                btn.setText("Color: " + id_);
                                btn.setBackgroundColor(colorsGrad[i - 1]);
                                listLayout.addView(btn, params);
                                final Button btn1 = ((Button) listLayout.findViewById(id_));
                                btn1.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View view) {
                                        ColorPickerDialog colorPickerDialog = new ColorPickerDialog(context, colorsGrad[id_-1], new ColorPickerDialog.OnColorSelectedListener() {
                                            @Override
                                            public void onColorSelected(int color) {
                                                // TODO Auto-generated method stub
                                                colorsGrad[id_-1] = color;
                                                btn1.setBackgroundColor(colorsGrad[id_-1]);
                                            }
                                        });
                                        colorPickerDialog.setTitle("Color: " + id_);
                                        colorPickerDialog.show();
                                    }
                                });
                                if (btn1.getId() <= currentItem[0]) {
                                    btn1.setVisibility(View.VISIBLE);
                                } else {
                                    btn1.setVisibility(View.GONE);
                                }
                            }
                            Button add = (Button) rootLayout.findViewById(R.id.add);
                            add.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (currentItem[0] < 5) {
                                        currentItem[0]++;
                                        listLayout.findViewById(currentItem[0]).setVisibility(View.VISIBLE);
                                    } else {
                                        Toast.makeText(context, "You cannot add more than 5 colors", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            Button remove = (Button) rootLayout.findViewById(R.id.remove);
                            remove.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (currentItem[0] > 2) {
                                        listLayout.findViewById(currentItem[0]).setVisibility(View.GONE);
                                        currentItem[0]--;
                                    } else {
                                        Toast.makeText(context, "You cannot have less than 2 colors", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            builder.setView(rootLayout);
                            builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final int[] usedColors = new int[currentItem[0]];
                                    System.arraycopy(colorsGrad, 0, usedColors, 0, currentItem[0]);
                                    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                                    Display display = wm.getDefaultDisplay();
                                    Point size = new Point();
                                    display.getSize(size);
                                    Shader textShader = null;
                                    int checkedID = orientation.getCheckedRadioButtonId();
                                    if(checkedID == R.id.horizontal){
                                        textShader = new LinearGradient(0, 0, size.x, 0, usedColors, null, Shader.TileMode.CLAMP);
                                    } else if(checkedID == R.id.vertical){
                                        textShader = new LinearGradient(0, 0, 0, size.y, usedColors, null, Shader.TileMode.CLAMP);
                                    }
                                    shader = textShader;
                                    useShader = true;
                                }
                            });
                            builder.show();
                            return;
                        }
                        case 4: {//history
                            useShader = false;
                            shouldErase = false;
                            shouldBlur = false;

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
                            builder.setView(linearLayout);
                            builder.setPositiveButton(Common.dialog_done, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }
                            );
                            builder.show();
                            return;
                        }
                        case 5: {//hex
                            useShader = false;
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

                                    //*If the String length is bigger than zero and it's not composed only by the following characters: A to F and/or 0 to 9 *//*
                                    if (!text.matches("[a-fA-F0-9]+") && length > 0) {
                                        //Delete the last character
                                        s.delete(length - 1, length);
                                    }
                                }
                            });
                            eText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                            linearLayout.addView(eText);
                            builder.setView(linearLayout);
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
                            return;
                        }
                        case 6: {//shape
                            shouldErase = false;
                            shouldBlur = false;
                            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Choose shape type");
                            LinearLayout linearLayout = new LinearLayout(context);
                            linearLayout.setOrientation(LinearLayout.VERTICAL);

                            final Button rectangle = new Button(context);
                            final Button circle = new Button(context);
                            final Button star = new Button(context);
                            final Button line = new Button(context);
                            final Button default_btn = new Button(context);
                            switch (type){
                                case RECTANGLE:
                                    rectangle.setTextColor(Color.GREEN);
                                    circle.setTextColor(Color.BLACK);
                                    star.setTextColor(Color.BLACK);
                                    line.setTextColor(Color.BLACK);
                                    default_btn.setTextColor(Color.BLACK);
                                    break;
                                case CIRCLE:
                                    rectangle.setTextColor(Color.BLACK);
                                    circle.setTextColor(Color.GREEN);
                                    star.setTextColor(Color.BLACK);
                                    line.setTextColor(Color.BLACK);
                                    default_btn.setTextColor(Color.BLACK);
                                    break;
                                case STAR:
                                    rectangle.setTextColor(Color.BLACK);
                                    circle.setTextColor(Color.BLACK);
                                    star.setTextColor(Color.GREEN);
                                    line.setTextColor(Color.BLACK);
                                    default_btn.setTextColor(Color.BLACK);
                                    break;
                                case LINE:
                                    rectangle.setTextColor(Color.BLACK);
                                    circle.setTextColor(Color.BLACK);
                                    star.setTextColor(Color.BLACK);
                                    line.setTextColor(Color.GREEN);
                                    default_btn.setTextColor(Color.BLACK);
                                    break;
                                case DEFAULT:
                                    rectangle.setTextColor(Color.BLACK);
                                    circle.setTextColor(Color.BLACK);
                                    star.setTextColor(Color.BLACK);
                                    line.setTextColor(Color.BLACK);
                                    default_btn.setTextColor(Color.GREEN);
                                    break;
                                default:
                                    break;
                            }
                            rectangle.setText("Rectangle");
                            rectangle.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    type = DrawingType.RECTANGLE;
                                    rectangle.setTextColor(Color.GREEN);
                                    circle.setTextColor(Color.BLACK);
                                    star.setTextColor(Color.BLACK);
                                    line.setTextColor(Color.BLACK);
                                    default_btn.setTextColor(Color.BLACK);
                                    builder.create().cancel();
                                }
                            });

                            circle.setText("Circle");
                            circle.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    type = DrawingType.CIRCLE;
                                    rectangle.setTextColor(Color.BLACK);
                                    circle.setTextColor(Color.GREEN);
                                    star.setTextColor(Color.BLACK);
                                    line.setTextColor(Color.BLACK);
                                    default_btn.setTextColor(Color.BLACK);
                                }
                            });
                            line.setText("Line");
                            line.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    type = DrawingType.LINE;
                                    rectangle.setTextColor(Color.BLACK);
                                    circle.setTextColor(Color.BLACK);
                                    star.setTextColor(Color.BLACK);
                                    line.setTextColor(Color.GREEN);
                                    default_btn.setTextColor(Color.BLACK);
                                }
                            });
                            star.setText("Star");
                            star.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    type = DrawingType.STAR;
                                    rectangle.setTextColor(Color.BLACK);
                                    circle.setTextColor(Color.BLACK);
                                    star.setTextColor(Color.GREEN);
                                    line.setTextColor(Color.BLACK);
                                    default_btn.setTextColor(Color.BLACK);
                                }
                            });
                            default_btn.setText("Default");
                            default_btn.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    rectangle.setTextColor(Color.BLACK);
                                    circle.setTextColor(Color.BLACK);
                                    star.setTextColor(Color.BLACK);
                                    line.setTextColor(Color.BLACK);
                                    default_btn.setTextColor(Color.GREEN);
                                    type = DrawingType.DEFAULT;
                                }
                            });
                            linearLayout.addView(rectangle);
                            linearLayout.addView(circle);
                            linearLayout.addView(line);
                            linearLayout.addView(star);
                            linearLayout.addView(default_btn);
                            builder.setView(linearLayout);
                            builder.setNegativeButton(Common.dialog_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO Auto-generated method stub

                                }
                            });
                            builder.show();
                            return;
                        }
                        case 7: {//blur
                            type = DrawingType.DEFAULT;
                            shouldErase = false;
                            shouldBlur = true;
                            return;
                        }
                        case 8: {//width
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
                            builder.setView(linearLayout);
                            builder.show();
                            return;
                        }
                        case 9: {//hide
                            outerOptionsLayout.setVisibility(View.GONE);
                            return;
                        }
                        default:
                            return;
                    }
                }
            });

            return rowView;
        }
    }
}