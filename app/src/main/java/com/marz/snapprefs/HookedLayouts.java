package com.marz.snapprefs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.marz.snapprefs.Logger.LogType;
import com.marz.snapprefs.Preferences.Prefs;
import com.marz.snapprefs.Util.AssignedStoryButton;
import com.marz.snapprefs.Util.FlingSaveGesture;
import com.marz.snapprefs.Util.GestureEvent;
import com.marz.snapprefs.Util.NotificationUtils;
import com.marz.snapprefs.Util.SweepSaveGesture;
import com.marz.snapprefs.Util.TypefaceUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.marz.snapprefs.Dialogs.rColor;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;

/**
 * Created by MARZ on 2016. 04. 08..
 */
public class HookedLayouts {
    public static final int regularButtonSize = 50;
    public static final int marginValue = 10;
    public static ImageButton upload = null;
    public static RelativeLayout outerOptionsLayout = null;
    public static ImageButton saveSnapButton;
    public static ArrayList<AssignedStoryButton> storyButtonQueue = new ArrayList<>();

    public static void initIntegration(XC_LoadPackage.LoadPackageParam lpparam,
                                       final XModuleResources mResources) {
        findAndHookMethod("com.snapchat.android.fragments.addfriends.ProfileFragment", lpparam.classLoader, "onCreateView", LayoutInflater.class, ViewGroup.class, Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                TableLayout navigation =
                        (TableLayout) ((LinearLayout) XposedHelpers.getObjectField(param.thisObject, "z")).getChildAt(0);//prev. C
                ImageView orig =
                        (ImageView) ((TableRow) navigation.getChildAt(0)).getChildAt(0);
                TextView orig1 =
                        (TextView) ((TableRow) navigation.getChildAt(0)).getChildAt(1);
                TableRow row = new TableRow(navigation.getContext());
                row.setTag("Snapprefs Link");
                row.setLayoutParams(navigation.getChildAt(0).getLayoutParams());
                ImageView iv = new ImageView(navigation.getContext());
                iv.setImageDrawable(mResources.getDrawable(R.drawable.profile_snapprefs));
                iv.setLayoutParams(orig.getLayoutParams());
                TextView textView = new TextView(navigation.getContext());
                textView.setText("Open Snapprefs");
                textView.setTextColor(orig1.getCurrentTextColor());
                textView.setTextSize(24);

                textView.setLayoutParams(orig1.getLayoutParams());
                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (HookMethods.context == null) {
                            Logger.log("CONTEXT IS NULL IN INITINTEGRATION");
                        }
                        //Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.marz.snapprefs");
                        Intent launchIntent = new Intent(Intent.ACTION_MAIN);
                        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        launchIntent.setComponent(new ComponentName("com.marz.snapprefs", "com.marz.snapprefs.MainActivity"));
                        HookMethods.context.startActivity(launchIntent);
                    }
                });
                row.addView(iv);
                row.addView(textView);
                navigation.addView(row);

                boolean containsRow = false;
                for (int index = 0; index < navigation.getChildCount(); ++index) {
                    View nextChild = navigation.getChildAt(index);

                    if (nextChild.getTag() != null && nextChild.getTag() instanceof String) {
                        if (nextChild.getTag().equals("Snapprefs Link")) {
                            if (containsRow)
                                navigation.removeView(nextChild);
                            else
                                containsRow = true;
                        }
                    }
                }
            }
        });
    }

    public static void fullScreenFilter(
            XC_InitPackageResources.InitPackageResourcesParam resparam) {
        try {
            resparam.res.hookLayout(Common.PACKAGE_SNAP, "layout", "battery_view", new XC_LayoutInflated() {
                LinearLayout.LayoutParams batteryLayoutParams =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

                @Override
                public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                    View battery =
                            liparam.view.findViewById(liparam.res.getIdentifier("battery_icon", "id", "com.snapchat.android"));
                    battery.setLayoutParams(batteryLayoutParams);
                    battery.setPadding(0, 0, 0, 0);
                    Logger.log("fullScreenFilter", true);
                }
            });
        } catch (Resources.NotFoundException ignore) {

        }
    }

    public static void addShareIcon(final XC_InitPackageResources.InitPackageResourcesParam resparam) {
        try {
            resparam.res.hookLayout(Common.PACKAGE_SNAP, "layout", "camera_preview", new XC_LayoutInflated() {
                public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                    final int padding = px(10f);
                    final int topMargin = 0;
                    final int size = px((int) resparam.res.getDimension(resparam.res.getIdentifier("profile_picture_button_size", "dimen", Common.PACKAGE_SNAP)));

                    final RelativeLayout relativeLayout =
                            (RelativeLayout) liparam.view.findViewById(liparam.res.getIdentifier("camera_preview_layout", "id", Common.PACKAGE_SNAP));
                    //final RelativeLayout.LayoutParams lParams =
                    //        new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("camera_take_snap_button", "id", Common.PACKAGE_SNAP)).getLayoutParams());
                    final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT | RelativeLayout.ALIGN_PARENT_TOP);
                    layoutParams.rightMargin = px(50);
                    layoutParams.topMargin = topMargin;
                    upload = new ImageButton(HookMethods.SnapContext);
                    upload.setLayoutParams(layoutParams);
                    upload.setPadding(padding, padding, padding, padding);
                    upload.setBackgroundColor(0);
                    //Drawable uploadimg = HookMethods.SnapContext.getResources().getDrawable(+(int) Long.parseLong(Obfuscator.sharing.UPLOAD_ICON.substring(2), 16));
                    //upload.setImageDrawable(mResources.getDrawable(R.drawable.triangle));
                    String[] projection = new String[]{
                            MediaStore.Images.ImageColumns._ID,
                            MediaStore.Images.ImageColumns.DATA,
                            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                            MediaStore.Images.ImageColumns.DATE_TAKEN,
                            MediaStore.Images.ImageColumns.MIME_TYPE
                    };
                    final Cursor cursor = HookMethods.SnapContext.getContentResolver()
                            .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                                    null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
                    if (cursor != null && cursor.moveToFirst()) {
                        String imageLocation = cursor.getString(1);
                        File imageFile = new File(imageLocation);
                        if (imageFile.exists()) {
                            Bitmap bm = BitmapFactory.decodeFile(imageLocation);
                            Bitmap resized = Bitmap.createScaledBitmap(bm, size, size, false);
                            int w = resized.getWidth();
                            int h = resized.getHeight();

                            int strokeWidth = px(8);
                            int radius = Math.min(h / 2, w / 2);
                            Bitmap output = Bitmap.createBitmap(w + strokeWidth, h + strokeWidth, Bitmap.Config.ARGB_8888);

                            Paint p = new Paint();
                            p.setAntiAlias(true);

                            Canvas c = new Canvas(output);
                            c.drawARGB(0, 0, 0, 0);
                            p.setStyle(Paint.Style.FILL);

                            c.drawCircle((w / 2) + (strokeWidth / 2), (h / 2) + (strokeWidth / 2), radius, p);

                            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

                            c.drawBitmap(resized, (strokeWidth / 2), (strokeWidth / 2), p);
                            p.setXfermode(null);
                            p.setStyle(Paint.Style.STROKE);
                            p.setColor(Color.WHITE);
                            p.setStrokeWidth(strokeWidth);
                            c.drawCircle((w / 2) + (strokeWidth / 2), (h / 2) + (strokeWidth / 2), radius, p);
                            upload.setImageDrawable(new BitmapDrawable(output));
                        }

                        cursor.close();
                    }
                    //upload.setImageDrawable(uploadimg);
                    upload.setScaleX((float) 0.65);
                    upload.setScaleY((float) 0.65);
                    upload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent launchIntent = new Intent(Intent.ACTION_RUN);
                            launchIntent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            launchIntent.setComponent(new ComponentName("com.marz.snapprefs", "com.marz.snapprefs.PickerActivity"));
                            HookMethods.context.startActivity(launchIntent);
                        }
                    });

                    relativeLayout.addView(upload);
                }
            });
        } catch (Resources.NotFoundException ignore) {
        }
    }

    public static void addSaveButtonsAndGestures(
            XC_InitPackageResources.InitPackageResourcesParam resparam,
            XModuleResources mResources, final Context localContext
    ) {
/*
        int intIconID = resparam.res.getIdentifier("aa_snap_preview_save", "drawable", Common
                .PACKAGE_SNAP);

        final BitmapDrawable drawable = (BitmapDrawable) resparam.res.getDrawable(intIconID);*/
        if (HookMethods.saveImg == null)
            throw new NullPointerException("Button Image not found");

        int horizontalPosition = Preferences.getBool(Prefs.BUTTON_POSITION) ? Gravity.START : Gravity.END;

        FrameLayout.LayoutParams scaledLayoutParams = null;


        // We get the opacity level and then we calculate the scale accordingly
        int saveButtonOpacity = Preferences.getInt(Prefs.BUTTON_OPACITY);

        DisplayMetrics metrics = localContext.getResources().getDisplayMetrics();

        // We define the scale of our button so that it will be scaled as opacity decrease
        // 100% opacity --> regular size
        // 0% opacity --> 2x regular size
        float unscaledSize = Preferences.getBool(Prefs.BUTTON_RESIZE) ?
                ((float )(100 - saveButtonOpacity)/100) * regularButtonSize + regularButtonSize
                : regularButtonSize;
        int scaledSize =   px(unscaledSize, metrics.density);
        int margins = px(marginValue,metrics.density);
        scaledLayoutParams =  new FrameLayout.LayoutParams(scaledSize, scaledSize,
                Gravity.BOTTOM | horizontalPosition);

        scaledLayoutParams.setMargins(margins,margins,margins,margins);

        final FrameLayout.LayoutParams layoutParams = scaledLayoutParams;

        resparam.res.hookLayout(Common.PACKAGE_SNAP, "layout", "view_snap", new XC_LayoutInflated() {
            private GestureEvent gestureEvent;

            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam)
                    throws Throwable {
                Logger.log("Updating view_snap.snap_container layout");
                final FrameLayout frameLayout = (FrameLayout) liparam.view.findViewById(
                        liparam.res.getIdentifier("snap_container", "id", Common.PACKAGE_SNAP)
                ).getParent();

                int saveMode = Preferences.getInt(Prefs.SAVEMODE_SNAP);

                if(saveMode == Preferences.SAVE_BUTTON) {
                    saveSnapButton = new ImageButton(localContext);
                    saveSnapButton.setLayoutParams(layoutParams);
                    saveSnapButton.setScaleType(ImageView.ScaleType.FIT_XY);
                    saveSnapButton.setPadding(0,0,0,0);
                    saveSnapButton.setAdjustViewBounds(true);
                    saveSnapButton.setBackgroundColor(0);
                    saveSnapButton.setAlpha((float) Preferences.getInt(Prefs.BUTTON_OPACITY) / 100);
                    saveSnapButton.setImageBitmap(HookMethods.saveImg);
                    saveSnapButton.setVisibility(View.VISIBLE);

                    frameLayout.addView(saveSnapButton);
                    saveSnapButton.bringToFront();

                    saveSnapButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Logger.printTitle("Performing Button Save", LogType.SAVING);
                            Saving.performButtonSave();
                        }
                    });
                } else if (saveMode == Preferences.SAVE_S2S ||
                        saveMode == Preferences.SAVE_F2S) {

                    frameLayout.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (gestureEvent == null) {
                                if (Preferences.getInt(Prefs.SAVEMODE_SNAP) == Preferences.SAVE_S2S)
                                    gestureEvent = new SweepSaveGesture();
                                else if (Preferences.getInt(Prefs.SAVEMODE_SNAP) == Preferences.SAVE_F2S)
                                    gestureEvent = new FlingSaveGesture();
                                else {
                                    Logger.log("No gesture method provided");
                                    return false;
                                }
                            }

                            if (gestureEvent.onTouch(v, event, Saving.SnapType.SNAP) == GestureEvent.ReturnType.TAP) {
                                Logger.log("Performed TAP?", LogType.SAVING);
                                return false;
                            } else
                                return true;
                        }
                    });
                }
            }
        });
    }

    // HookedLayouts.assignStoryButton(snapContainer, snapContext, mKey);
    static void assignStoryButton(FrameLayout frameLayout, Context context, final String mKey) {

        AssignedStoryButton storyButton = new AssignedStoryButton(context);
        storyButton.buildParams(frameLayout, context);

        Logger.log("Parent: " + storyButton.getParent());

        if (storyButton.getParent() == null) {
            frameLayout.addView(storyButton);
            storyButton.setAssignedmKey(mKey);
            setAdditionalInstanceField(frameLayout, "mKey", mKey);
        }

        storyButton.setVisibility(Preferences.getInt(Preferences.Prefs.SAVEMODE_STORY) == Preferences.SAVE_BUTTON
                ? View.VISIBLE : View.INVISIBLE);
        storyButton.setAlpha((float) Preferences.getInt(Prefs.BUTTON_OPACITY) / 100);
        storyButton.bringToFront();
        storyButton.invalidate();
        frameLayout.invalidate();

        Logger.log("brought to front");
    }

    private static AssignedStoryButton retrieveStoryButton(FrameLayout layout, Context context, String mKey) {
        for (AssignedStoryButton button : storyButtonQueue) {
            Logger.log("Checking if button can be reassigned");

            if (button.getParent().equals(layout))
                return button;

            if ((button.getAssignedmKey() != null && button.getAssignedmKey().equals(mKey)) ||
                    button.canBeReassigned()) {
                Logger.log("Found button to reassign");

                if (!button.isShown())
                    button.removeParent();

                return button;
            }
        }

        Logger.log("No existing or assignable button available... Creating new");
        AssignedStoryButton storyButton = new AssignedStoryButton(context);
        storyButton.buildParams(layout, context);
        storyButtonQueue.add(storyButton);
        return storyButton;
    }

    public static void initParents(View view) {
        if (view.getParent() != null) {
            View viewParent = (View) view.getParent();
            Log.d("snapprefs", "ViewId: " + viewParent);

            initParents(viewParent);
        }
    }

    public static void initView(View view) {
        Log.d("snapprefs", "ID: " + view.getId());

        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View obj = ((ViewGroup) view).getChildAt(i);

                Log.d("snapprefs", "ViewId: " + obj.getId());
            }
        }
    }

    public static void addIcons(XC_InitPackageResources.InitPackageResourcesParam resparam,
                                final XModuleResources mResources) {
        resparam.res.hookLayout(Common.PACKAGE_SNAP, "layout", "snap_preview", new XC_LayoutInflated() {
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                final RelativeLayout relativeLayout =
                        (RelativeLayout) liparam.view.findViewById(liparam.res.getIdentifier("snap_preview_header", "id", Common.PACKAGE_SNAP)).getParent();

                outerOptionsLayout = new RelativeLayout(HookMethods.SnapContext);
                final GridView innerOptionsView = new GridView(HookMethods.SnapContext);
                innerOptionsView.setAdapter(new OptionsAdapter(HookMethods.SnapContext, mResources));
                innerOptionsView.setNumColumns(3);
                innerOptionsView.setHorizontalSpacing(px(2.0f));
                innerOptionsView.setVerticalSpacing(px(5.0f));
                innerOptionsView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
                innerOptionsView.setPadding(0, px(7.5f), px(1.0f), px(7.5f));
                final RelativeLayout.LayoutParams outerOptionsLayoutParams =
                        new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                outerOptionsLayoutParams.topMargin = px(55.0f);
                outerOptionsLayoutParams.bottomMargin = px(300.0f);
                outerOptionsLayoutParams.leftMargin = px(75.0f);
                outerOptionsLayoutParams.rightMargin = px(10.0f);
                outerOptionsLayout.setVisibility(View.GONE);
                outerOptionsLayout.setBackgroundDrawable(mResources.getDrawable(R.drawable.optionsbackground));
                outerOptionsLayout.addView(innerOptionsView, GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.MATCH_PARENT);

                final RelativeLayout.LayoutParams layoutParams =
                        new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("drawing_btn", "id", Common.PACKAGE_SNAP)).getLayoutParams());
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP);
                layoutParams.topMargin = px(45.0f);
                layoutParams.leftMargin = px(10.0f);
                final ImageButton textButton = new ImageButton(HookMethods.SnapContext);
                textButton.setBackgroundColor(0);
                textButton.setImageDrawable(mResources.getDrawable(R.drawable.triangle));
                textButton.setScaleX((float) 0.4);
                textButton.setScaleY((float) 0.4);
                textButton.setOnClickListener(new View.OnClickListener() {
                    boolean shouldHideOptions = true;

                    @Override
                    public void onClick(View v) {
                        if (HookMethods.editText.getText().length() >= 1) {
                            if (shouldHideOptions) {
                                outerOptionsLayout.setVisibility(View.VISIBLE);
                                shouldHideOptions = false;
                            } else {
                                outerOptionsLayout.setVisibility(View.GONE);
                                shouldHideOptions = true;
                            }
                            Logger.log("SnapPrefs: Displaying Options");
                        } else {
                            outerOptionsLayout.setVisibility(View.GONE);
                            shouldHideOptions = true;
                            Toast.makeText(HookMethods.SnapContext, "Your caption is missing", Toast.LENGTH_SHORT).show();
                            Logger.log("SnapPrefs: Not displaying Options - HookMethods" +
                                    ".editText empty");
                        }
                    }
                });
                final RelativeLayout.LayoutParams paramsSpeed =
                        new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("drawing_btn", "id", Common.PACKAGE_SNAP)).getLayoutParams());
                paramsSpeed.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP);
                paramsSpeed.topMargin = px(90.0f);
                paramsSpeed.leftMargin = px(10.0f);
                final ImageButton speed = new ImageButton(HookMethods.SnapContext);
                speed.setBackgroundColor(0);
                speed.setImageDrawable(mResources.getDrawable(R.drawable.speed));
                speed.setScaleX((float) 0.4);
                speed.setScaleY((float) 0.4);
                speed.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Dialogs.SpeedDialog(HookMethods.SnapContext);
                        Logger.log("SnapPrefs: Displaying SpeedDialog");
                    }
                });
                final RelativeLayout.LayoutParams paramsWeather =
                        new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("drawing_btn", "id", Common.PACKAGE_SNAP)).getLayoutParams());
                paramsWeather.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP);
                paramsWeather.topMargin = px(180.0f);
                paramsWeather.leftMargin = px(10.0f);
                final ImageButton weather = new ImageButton(HookMethods.SnapContext);
                weather.setBackgroundColor(0);
                weather.setImageDrawable(mResources.getDrawable(R.drawable.weather));
                weather.setScaleX((float) 0.4);
                weather.setScaleY((float) 0.4);
                weather.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Dialogs.WeatherDialog(HookMethods.SnapContext);
                        Logger.log("SnapPrefs: Displaying WeatherDialog");
                    }
                });
                final RelativeLayout.LayoutParams paramsLocation =
                        new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("drawing_btn", "id", Common.PACKAGE_SNAP)).getLayoutParams());
                paramsLocation.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP);
                paramsLocation.topMargin = px(135.0f);
                paramsLocation.leftMargin = px(10.0f);
                final ImageButton location = new ImageButton(HookMethods.SnapContext);
                location.setBackgroundColor(0);
                location.setImageDrawable(mResources.getDrawable(R.drawable.location));
                location.setScaleX((float) 0.4);
                location.setScaleY((float) 0.4);
                location.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName("com.marz.snapprefs", "com.marz.snapprefs.MapsActivity"));
                        HookMethods.SnapContext.startActivity(intent);
                        Logger.log("SnapPrefs: Displaying Map");
                    }
                });
                HookMethods.SnapContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (Preferences.getBool(Prefs.TEXT_TOOLS)) {
                            relativeLayout.addView(textButton, layoutParams);
                            relativeLayout.addView(outerOptionsLayout, outerOptionsLayoutParams);
                        }
                        if (Preferences.getBool(Prefs.SPEED)) {
                            relativeLayout.addView(speed, paramsSpeed);
                        }
                        if (Preferences.getBool(Prefs.LOCATION)) {
                            relativeLayout.addView(location, paramsLocation);
                        }
                        if (Preferences.getBool(Prefs.WEATHER)) {
                            relativeLayout.addView(weather, paramsWeather);
                        }
                    }
                });
            }
        });
    }

    public static int px(float f, float density) {
        return Math.round((f *
                density));
    }

    public static int px(float f) {
        return px(f, HookMethods.SnapContext.getResources().getDisplayMetrics().density);
    }

    public static void initVisiblity(XC_LoadPackage.LoadPackageParam lpparam) {
        XC_MethodHook hideLayout = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (outerOptionsLayout != null)
                    outerOptionsLayout.setVisibility(View.GONE);
            }
        };
        findAndHookMethod("com.snapchat.android.app.shared.analytics.ui.StickerPickerAnalytics", lpparam.classLoader, "b", hideLayout);//prev. a
        //TODO Find the new representation of this method - DONE?
        findAndHookMethod(Obfuscator.icons.CAPTIONOPENED_CLASS, lpparam.classLoader, Obfuscator.icons.CAPTIONOPENED_METHOD, hideLayout);
    }

    private static class OptionsAdapter extends BaseAdapter {
        private static LayoutInflater inflater = null;
        String[] options =
                {"Text Color", "Text Gradient", "Text Transparency",
                        "Background Color", "Background Gradient", "Background Transparency",
                        "Text Size", "Text Font", "Text Style",
                        "Text Alignment", "Rainbow Text", "Reset"};
        Context context;
        XModuleResources mRes;
        int[] optionImageId =
                {R.drawable.text_color, R.drawable.text_gradient, R.drawable.text_transparency,
                        R.drawable.bg_color, R.drawable.bg_gradient, R.drawable.bg_transparency,
                        R.drawable.text_size, R.drawable.text_font, R.drawable.text_style,
                        R.drawable.text_alignment, R.drawable.rainbow, R.drawable.reset};

        public OptionsAdapter(Activity snapContext, XModuleResources mRes) {
            this.context = snapContext;
            this.mRes = mRes;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final int[] colorsBg =
                    new int[]{Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE};
            final int[] currentItemBg = {2};
            final int[] colorsText =
                    new int[]{Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE};
            final int[] currentItemText = {2};
            Holder holder = new Holder();
            View rowView;

            rowView = inflater.inflate(mRes.getLayout(R.layout.optionlayout), null);
            holder.tv =
                    (TextView) rowView.findViewById(mRes.getIdentifier("description", "id", "com.marz.snapprefs"));
            holder.img =
                    (ImageView) rowView.findViewById(mRes.getIdentifier("textIcon", "id", "com.marz.snapprefs"));

            holder.tv.setText(options[position]);
            holder.img.setImageDrawable(mRes.getDrawable(optionImageId[position]));

            rowView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    switch (position) {
                        case 0: { //textColor
                            ColorPickerDialog colorPickerDialog =
                                    new ColorPickerDialog(context, HookMethods.editText.getCurrentTextColor(), new ColorPickerDialog.OnColorSelectedListener() {

                                        @Override
                                        public void onColorSelected(int color) {
                                            HookMethods.editText.setTextColor(color);
                                        }
                                    });
                            colorPickerDialog.setButton(-3, Common.dialog_default, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    HookMethods.editText.setTextColor(Color.WHITE);
                                    HookMethods.editText.setAlpha(1);
                                }
                            });
                            colorPickerDialog.setTitle(Common.dialog_txtcolour);
                            colorPickerDialog.show();
                            return;
                        }
                        case 1: { //textGradient
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Text Gradient");
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                            LinearLayout rootLayout = new LinearLayout(context);
                            LinearLayout.LayoutParams rootParams =
                                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                            LayoutInflater inflater =
                                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            rootLayout.addView(inflater.inflate(HookMethods.modRes.getLayout(R.layout.gradient_layout), null), rootParams);
                            final RadioGroup orientation =
                                    (RadioGroup) rootLayout.findViewById(R.id.orientation);
                            final LinearLayout listLayout =
                                    (LinearLayout) rootLayout.findViewById(R.id.itemLayout);
                            final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);

                            for (int i = 1; i <= 5; i++) {
                                Button btn = new Button(context);
                                btn.setId(+i);
                                final int id_ = btn.getId();
                                btn.setText("Color: " + id_);
                                btn.setBackgroundColor(colorsText[i - 1]);
                                listLayout.addView(btn, params);
                                final Button btn1 = ((Button) listLayout.findViewById(id_));
                                btn1.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View view) {
                                        ColorPickerDialog colorPickerDialog =
                                                new ColorPickerDialog(context, colorsText[id_ -
                                                        1], new ColorPickerDialog.OnColorSelectedListener() {
                                                    @Override
                                                    public void onColorSelected(int color) {
                                                        // TODO Auto-generated method stub
                                                        colorsText[id_ - 1] = color;
                                                        btn1.setBackgroundColor(colorsText[id_ -
                                                                1]);
                                                    }
                                                });
                                        colorPickerDialog.setTitle("Color: " + id_);
                                        colorPickerDialog.show();
                                    }
                                });
                                if (btn1.getId() <= currentItemText[0]) {
                                    btn1.setVisibility(View.VISIBLE);
                                } else {
                                    btn1.setVisibility(View.GONE);
                                }
                            }
                            Button add = (Button) rootLayout.findViewById(R.id.add);
                            add.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (currentItemText[0] < 5) {
                                        currentItemText[0]++;
                                        listLayout.findViewById(currentItemText[0]).setVisibility(View.VISIBLE);
                                    } else {
                                        Toast.makeText(context, "You cannot add more than 5 colors", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            Button remove = (Button) rootLayout.findViewById(R.id.remove);
                            remove.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (currentItemText[0] > 2) {
                                        listLayout.findViewById(currentItemText[0]).setVisibility(View.GONE);
                                        currentItemText[0]--;
                                    } else {
                                        Toast.makeText(context, "You cannot have less than 2 colors", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            builder.setView(rootLayout);
                            builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final int[] usedColors = new int[currentItemText[0]];
                                    System.arraycopy(colorsText, 0, usedColors, 0, currentItemText[0]);
                                    Shader textShader = null;
                                    int checkedID = orientation.getCheckedRadioButtonId();
                                    if (checkedID == R.id.horizontal) {
                                        double angleInRadians = Math.toRadians(90);
                                        double length = 100;

                                        double endX = Math.cos(angleInRadians) * length;
                                        double endY = Math.sin(angleInRadians) *
                                                HookMethods.editText.getHeight();
                                        WindowManager wm =
                                                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                                        Display display = wm.getDefaultDisplay();
                                        Point size = new Point();
                                        display.getSize(size);
                                        int width = size.x;
                                        textShader =
                                                new LinearGradient(0, 0, width, 0, usedColors, null, Shader.TileMode.CLAMP);
                                    } else if (checkedID == R.id.vertical) {
                                        textShader =
                                                new LinearGradient(0, 0, 0, HookMethods.editText.getHeight(), usedColors, null, Shader.TileMode.CLAMP);
                                    }
                                    HookMethods.editText.getPaint().setShader(textShader);
                                    HookMethods.editText.setText(HookMethods.editText.getText());
                                }
                            });
                            builder.show();
                            return;
                        }
                        case 2: { //textAlpha
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            SeekBar seekBar = new SeekBar(context);
                            seekBar.setMax(100);
                            seekBar.setProgress((int) HookMethods.editText.getAlpha() * 100);
                            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                public void onProgressChanged(SeekBar seekBar2, int n,
                                                              boolean bl) {
                                    float alpha = (float) n / 100;
                                    HookMethods.editText.setAlpha(alpha);
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
                                    HookMethods.editText.setAlpha(1);
                                }
                            });
                            builder.setPositiveButton(Common.dialog_done, null);
                            builder.setView(seekBar);
                            builder.show();
                            return;
                        }
                        case 3: { //bgColor
                            ColorPickerDialog colorPickerDialog =
                                    new ColorPickerDialog(context, HookMethods.editText.getSolidColor(), new ColorPickerDialog.OnColorSelectedListener() {

                                        @Override
                                        public void onColorSelected(int color) {
                                            // TODO Auto-generated method stub
                                            HookMethods.editText.setBackgroundColor(color);
                                        }
                                    });
                            colorPickerDialog.setButton(-3, Common.dialog_default, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    // TODO Auto-generated method stub
                                    HookMethods.editText.setBackgroundColor((Color.parseColor("#000000")));
                                    HookMethods.editText.setAlpha(1);
                                }
                            });
                            colorPickerDialog.setTitle(Common.dialog_bgcolour);
                            colorPickerDialog.show();
                            return;
                        }
                        case 4: { //bgGradient
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Background Gradient");
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                            final LinearLayout rootLayout = new LinearLayout(context);
                            LinearLayout.LayoutParams rootParams =
                                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                            LayoutInflater inflater =
                                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            rootLayout.addView(inflater.inflate(HookMethods.modRes.getLayout(R.layout.gradient_layout), null), rootParams);
                            final RadioGroup orientation =
                                    (RadioGroup) rootLayout.findViewById(R.id.orientation);
                            final LinearLayout listLayout =
                                    (LinearLayout) rootLayout.findViewById(R.id.itemLayout);
                            final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);

                            for (int i = 1; i <= 5; i++) {
                                Button btn = new Button(context);
                                btn.setId(+i);
                                final int id_ = btn.getId();
                                btn.setText("Color: " + id_);
                                btn.setBackgroundColor(colorsBg[i - 1]);
                                listLayout.addView(btn, params);
                                final Button btn1 = ((Button) listLayout.findViewById(id_));
                                btn1.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View view) {
                                        ColorPickerDialog colorPickerDialog =
                                                new ColorPickerDialog(context, colorsBg[id_ -
                                                        1], new ColorPickerDialog.OnColorSelectedListener() {

                                                    @Override
                                                    public void onColorSelected(int color) {
                                                        // TODO Auto-generated method stub
                                                        colorsBg[id_ - 1] = color;
                                                        btn1.setBackgroundColor(colorsBg[id_ -
                                                                1]);
                                                    }
                                                });
                                        colorPickerDialog.setTitle("Color: " + id_);
                                        colorPickerDialog.show();
                                    }
                                });
                                if (btn1.getId() <= currentItemBg[0]) {
                                    btn1.setVisibility(View.VISIBLE);
                                } else {
                                    btn1.setVisibility(View.GONE);
                                }
                            }
                            Button add = (Button) rootLayout.findViewById(R.id.add);
                            add.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (currentItemBg[0] < 5) {
                                        currentItemBg[0]++;
                                        listLayout.findViewById(currentItemBg[0]).setVisibility(View.VISIBLE);
                                    } else {
                                        Toast.makeText(context, "You cannot add more than 5 colors", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            Button remove = (Button) rootLayout.findViewById(R.id.remove);
                            remove.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (currentItemBg[0] > 2) {
                                        listLayout.findViewById(currentItemBg[0]).setVisibility(View.GONE);
                                        currentItemBg[0]--;
                                    } else {
                                        Toast.makeText(context, "You cannot have less than 2 colors", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            builder.setView(rootLayout);
                            builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final int[] usedColors = new int[currentItemBg[0]];
                                    System.arraycopy(colorsBg, 0, usedColors, 0, currentItemBg[0]);
                                    PaintDrawable p = new PaintDrawable();
                                    p.setShape(new RectShape());
                                    ShapeDrawable.ShaderFactory sf =
                                            new ShapeDrawable.ShaderFactory() {
                                                @Override
                                                public Shader resize(int width, int height) {
                                                    LinearGradient returnGradient = null;
                                                    int checkedID =
                                                            orientation.getCheckedRadioButtonId();
                                                    if (checkedID == R.id.horizontal) {
                                                        returnGradient =
                                                                new LinearGradient(0, 0, width, height,
                                                                        usedColors,
                                                                        null, Shader.TileMode.MIRROR);
                                                    } else if (checkedID == R.id.vertical) {
                                                        double angleInRadians =
                                                                Math.toRadians(90);
                                                        double length = 100;

                                                        double endX =
                                                                Math.cos(angleInRadians) * length;
                                                        double endY = Math.sin(angleInRadians) *
                                                                HookMethods.editText.getHeight();
                                                        returnGradient =
                                                                new LinearGradient(0, 0, (float) endX, (float) endY,
                                                                        usedColors,
                                                                        null, Shader.TileMode.MIRROR);
                                                    }
                                                    return returnGradient;
                                                }
                                            };
                                    p.setShaderFactory(sf);
                                    HookMethods.editText.setBackgroundDrawable(p);
                                }
                            });
                            builder.show();
                            return;

                        }
                        case 5: { //bgAlpha
                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(HookMethods.SnapContext);
                            SeekBar seekBar = new SeekBar(HookMethods.SnapContext);
                            seekBar.setMax(255);
                            int currentAPIVersion = Build.VERSION.SDK_INT;
                            if (currentAPIVersion >= Build.VERSION_CODES.KITKAT) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    seekBar.setProgress(HookMethods.editText.getBackground().getAlpha());
                                }
                            } else {
                                seekBar.setProgress(255);
                            }
                            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                public void onProgressChanged(SeekBar seekBar3, int n,
                                                              boolean bl) {
                                    HookMethods.editText.getBackground().setAlpha(n);
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
                                    HookMethods.editText.getBackground().setAlpha(153);
                                }
                            });
                            builder.setPositiveButton(Common.dialog_done, null);
                            builder.setView(seekBar);
                            builder.show();
                            return;

                        }
                        case 6: { //textSize
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            SeekBar seekBar = new SeekBar(context);
                            seekBar.setMax(150);
                            seekBar.setProgress((int) HookMethods.editText.getTextSize());
                            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                public void onProgressChanged(SeekBar seekBar, int n,
                                                              boolean bl) {
                                    HookMethods.editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, n);
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar arg0) {
                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar arg0) {
                                }

                            });
                            builder.setNeutralButton(Common.dialog_default, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    HookMethods.editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.5125f);
                                }
                            });
                            builder.setPositiveButton(Common.dialog_done, null);
                            builder.setView(seekBar);
                            builder.show();
                            return;
                        }
                        case 7: { //textFont
                            File folder = new File(Preferences.getExternalPath() +
                                    "/Snapprefs/Fonts");
                            if (folder.exists()) {
                                FilenameFilter filter = new FilenameFilter() {
                                    @Override
                                    public boolean accept(File dir, String filename) {
                                        if (filename.lastIndexOf('.') > 0) {
                                            int lastIndex = filename.lastIndexOf('.');
                                            String extension = filename.substring(lastIndex);
                                            if (extension.equalsIgnoreCase(".ttf") ||
                                                    extension.equalsIgnoreCase(".otf")) {
                                                return true;
                                            }
                                        }
                                        return false;
                                    }
                                };
                                File[] fonts = folder.listFiles(filter);
                                if (fonts.length > 0) {
                                    AlertDialog.Builder builder =
                                            new AlertDialog.Builder(context);
                                    builder.setTitle("Font list");
                                    builder.setNeutralButton("Default", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            HookMethods.editText.setTypeface(HookMethods.defTypeface);
                                        }
                                    });
                                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface,
                                                            int i) {
                                        }
                                    });
                                    LinearLayout rootLayout = new LinearLayout(context);
                                    LinearLayout.LayoutParams rootParams =
                                            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                    LayoutInflater inflater =
                                            (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                    rootLayout.addView(inflater.inflate(HookMethods.modRes.getLayout(R.layout.font_list), null), rootParams);
                                    LinearLayout listLayout =
                                            (LinearLayout) rootLayout.findViewById(R.id.fontLayout);
                                    for (final File font : fonts) {
                                        String fontName =
                                                font.getName().substring(0, font.getName().toLowerCase().lastIndexOf("."));
                                        TextView item = new TextView(context);
                                        item.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                        item.setPadding(0, 0, 0, 2);
                                        item.setText(fontName);
                                        item.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22.0f);
                                        item.setGravity(Gravity.CENTER_HORIZONTAL);
                                        item.setTypeface(TypefaceUtil.get(font));
                                        item.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                HookMethods.editText.setTypeface(TypefaceUtil.get(font));
                                            }
                                        });
                                        listLayout.addView(item);
                                    }
                                    builder.setView(rootLayout);
                                    builder.show();
                                } else {
                                    NotificationUtils.showMessage("Fonts folder is empty", Color.RED, NotificationUtils.LENGTH_SHORT, HookMethods.classLoader);
                                }
                            } else {
                                NotificationUtils.showMessage("Fonts folder is not available", Color.RED, NotificationUtils.LENGTH_SHORT, HookMethods.classLoader);
                            }
                            return;
                        }
                        case 8: { //textStyle
                            //TODO: checkboxes
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            Button button_bold = new Button(context);
                            Button button_italic = new Button(context);
                            Button button_bolditalic = new Button(context);
                            Button button_normal = new Button(context);
                            LinearLayout linearLayout = new LinearLayout(context);
                            linearLayout.setOrientation(LinearLayout.VERTICAL);
                            button_bold.setText(Common.dialog_bold);
                            button_bold.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    HookMethods.editText.setTypeface(null, Typeface.BOLD);
                                }
                            });
                            button_italic.setText(Common.dialog_italic);
                            button_italic.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    HookMethods.editText.setTypeface(null, Typeface.ITALIC);
                                }
                            });
                            button_bolditalic.setText(Common.dialog_bolditalic);
                            button_bolditalic.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    HookMethods.editText.setTypeface(null, Typeface.BOLD_ITALIC);
                                }
                            });
                            button_normal.setText(Common.dialog_normal);
                            button_normal.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    HookMethods.editText.setTypeface(null, Typeface.NORMAL);
                                }
                            });
                            linearLayout.addView(button_bold);
                            linearLayout.addView(button_italic);
                            linearLayout.addView(button_bolditalic);
                            linearLayout.addView(button_normal);
                            builder.setView(linearLayout);
                            builder.setPositiveButton(Common.dialog_done, null);
                            builder.show();
                            return;
                        }
                        case 9: { //textAlignment
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            Button button_left = new Button(context);
                            Button button_center = new Button(context);
                            Button button_right = new Button(context);
                            LinearLayout linearLayout = new LinearLayout(context);
                            linearLayout.setOrientation(LinearLayout.VERTICAL);
                            button_left.setText(Common.dialog_left);
                            button_left.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    HookMethods.editText.setGravity(Gravity.START);
                                }
                            });
                            button_center.setText(Common.dialog_center);
                            button_center.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    HookMethods.editText.setGravity(Gravity.CENTER);
                                }
                            });
                            button_right.setText(Common.dialog_right);
                            button_right.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    HookMethods.editText.setGravity(Gravity.END);
                                }
                            });
                            linearLayout.addView(button_left);
                            linearLayout.addView(button_center);
                            linearLayout.addView(button_right);
                            builder.setView(linearLayout);
                            builder.setPositiveButton(Common.dialog_done, null);
                            builder.show();
                            return;
                        }
                        case 10: { //rainbow
                            String tempText = HookMethods.editText.getText().toString();
                            tempText.replace("\\n", "<br />");
                            tempText.replace("\\r", "<br />");
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
                            HookMethods.editText.setText(Html.fromHtml(newText));
                            return;
                        }
                        case 11: { //reset
                            HookMethods.editText.setBackgroundDrawable(null);
                            HookMethods.editText.getPaint().reset();
                            HookMethods.editText.setTextColor(Color.WHITE);
                            HookMethods.editText.setBackgroundColor((Color.parseColor("#000000")));
                            HookMethods.editText.setGravity(Gravity.CENTER);
                            HookMethods.editText.setAlpha(1);
                            HookMethods.editText.getBackground().setAlpha(153);
                            HookMethods.editText.setTypeface(HookMethods.defTypeface);
                            HookMethods.editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.5125f);
                            return;
                        }
                        default:
                            return;
                    }
                }
            });

            return rowView;
        }

        public class Holder {
            TextView tv;
            ImageView img;
        }
    }
}