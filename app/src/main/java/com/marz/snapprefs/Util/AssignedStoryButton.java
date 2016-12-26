package com.marz.snapprefs.Util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.marz.snapprefs.HookMethods;
import com.marz.snapprefs.HookedLayouts;
import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Logger.LogType;
import com.marz.snapprefs.Preferences;
import com.marz.snapprefs.Preferences.Prefs;
import com.marz.snapprefs.Saving;

import static com.marz.snapprefs.HookedLayouts.px;
import static de.robv.android.xposed.XposedHelpers.callMethod;

/**
 * Created by Andre on 12/10/2016.
 */

public class AssignedStoryButton extends ImageButton {
    private boolean areParamsSet = false;
    private String assignedmKey;

    public AssignedStoryButton(Context context) {
        super(context);

        this.setBackgroundColor(0);
        this.setAlpha(Preferences.getBool(Prefs.STEALTH_SAVING_BUTTON) ? 0f : 0.8f);
        this.setImageBitmap(HookMethods.saveImg);

        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.printTitle("Performing Button Save", LogType.SAVING);
                Saving.performButtonSave(assignedmKey);
            }
        });
    }

    public boolean canBeReassigned() {
        return this.getParent() == null || !this.isShown();
    }

    public void removeParent() {
        Object parent = this.getParent();

        if (parent != null && parent instanceof FrameLayout) {
            ((FrameLayout) parent).removeView(this);
            Logger.log("Removing buttons previous parent");
        }
    }

    public void buildParams(FrameLayout frameLayout, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        boolean horizontalPosition = Preferences.getBool(Preferences.Prefs.BUTTON_POSITION);
        int unscaledSize = Preferences.getBool(Prefs.STEALTH_SAVING_BUTTON) ? HookedLayouts.stealthButtonSize : 65;
        int scaledSize = px(unscaledSize, metrics.density);
        int newX = horizontalPosition ? 0 : metrics.widthPixels - scaledSize;
        int newY = metrics.heightPixels - scaledSize;

        FrameLayout.LayoutParams layoutParams;

        if (Preferences.getBool(Prefs.STEALTH_SAVING_BUTTON)) {
            layoutParams =
                    new FrameLayout.LayoutParams(scaledSize, scaledSize);
        } else {
            layoutParams =
                    new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT);
        }

        FrameLayout.LayoutParams newParams = (FrameLayout.LayoutParams) callMethod(frameLayout, "generateLayoutParams", layoutParams);


        //noinspection ResourceType
        newParams.setMargins(newX, newY, newX, newY);

        super.setLayoutParams(newParams);
        this.setAdjustViewBounds(true);
        super.setPadding(0, 0, 0, 0);

        areParamsSet = true;
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        Logger.log("OVERRIDDEN: setLayoutParams");
    }

    public String getAssignedmKey() {
        return this.assignedmKey;
    }

    public void setAssignedmKey(String assignedmKey) {
        this.assignedmKey = assignedmKey;
    }

    public boolean areParamsSet() {
        return areParamsSet;
    }
}
