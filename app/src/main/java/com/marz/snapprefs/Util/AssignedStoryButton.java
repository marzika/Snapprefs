package com.marz.snapprefs.Util;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
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

import static com.marz.snapprefs.HookedLayouts.marginValue;
import static com.marz.snapprefs.HookedLayouts.px;

import static com.marz.snapprefs.HookedLayouts.regularButtonSize;
import static de.robv.android.xposed.XposedHelpers.callMethod;

/**
 * Created by Andre on 12/10/2016.
 */

public class AssignedStoryButton extends ImageButton {
    private boolean areParamsSet = false;
    private String assignedmKey;

    /**
     * Weird offset value needed to ensure the button stays in place
     *
     * Why 26 ? Best working value i found...
     */
    private final float buttonOffset = 26f;


    public AssignedStoryButton(Context context) {
        super(context);

        this.setBackgroundColor(0);
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

        int saveButtonOpacity = Preferences.getInt(Prefs.BUTTON_OPACITY);

        // We determine our scale according the the value
        float unscaledSize = ((float )(100 - saveButtonOpacity)/100) * regularButtonSize + regularButtonSize;
        float scalingFactor = (float) (100 - saveButtonOpacity)/100 + 1 ;

        int scaledSize = px(unscaledSize, metrics.density);

        int newX;
        int newY;

        // Resize and change margins the button only if needed
        if (Preferences.getBool(Prefs.BUTTON_RESIZE)) {
            this.setScaleX(scalingFactor);
            this.setScaleY(scalingFactor);

            // Black magic margins calculation
            newX = horizontalPosition ? Math.round(px(buttonOffset,metrics.density) * (scalingFactor - 1) ) + px(marginValue,metrics.density)
                    : metrics.widthPixels - scaledSize + Math.round(px(buttonOffset,metrics.density) * (scalingFactor - 1)) - px(marginValue,metrics.density);;
            newY = metrics.heightPixels - scaledSize + Math.round(px(buttonOffset,metrics.density) * (scalingFactor - 1) - px(marginValue,metrics.density));
        } else {
            newX = horizontalPosition ? px(marginValue,metrics.density) : metrics.widthPixels - px(regularButtonSize,metrics.density) - px(marginValue,metrics.density);;
            newY = metrics.heightPixels - px(regularButtonSize,metrics.density) - px(marginValue,metrics.density);;
        }

        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);

        FrameLayout.LayoutParams newParams = (FrameLayout.LayoutParams) callMethod(frameLayout, "generateLayoutParams", layoutParams);

        //noinspection ResourceType
        newParams.setMargins(newX, newY, 0,0);
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