package com.marz.snapprefs.Util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.marz.snapprefs.HookMethods;
import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Preferences;
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
        this.setAlpha(0.8f);
        this.setImageBitmap(HookMethods.saveImg);
        super.setVisibility(Preferences.getInt(Preferences.Prefs.SAVEMODE_SNAP) == Preferences.SAVE_BUTTON
                ? View.VISIBLE : View.INVISIBLE);

        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.printTitle("Performing Button Save");
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
        Logger.log("Building params");
        final FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);

        FrameLayout.LayoutParams newParams = (FrameLayout.LayoutParams) callMethod(frameLayout, "generateLayoutParams", layoutParams);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        boolean horizontalPosition = Preferences.getBool(Preferences.Prefs.BUTTON_POSITION);
        int scaledSize = px(70, metrics.density);
        int newX = horizontalPosition ? 0 : metrics.widthPixels - scaledSize;
        int newY = metrics.heightPixels - scaledSize;

        newParams.setMargins(newX, newY, newX, newY);

        Logger.log("Margins: " + newParams.leftMargin + " " + newParams.topMargin + " " + newParams.rightMargin + " " + newParams.bottomMargin);

        Logger.log("newParams: " + newParams);
        super.setLayoutParams(newParams);
        this.setAdjustViewBounds(true);
        super.setPadding(0, 0, 0, 0);

        areParamsSet = true;
    }

    @Override
    public void setPadding(int x, int y, int x2, int y2) {
        Logger.log("OVERRIDDEN: setPadding");
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        Logger.log("OVERRIDDEN: setLayoutParams");
    }

    @Override
    public void setVisibility(int VISIBILITY) {
        super.setVisibility(VISIBILITY);
        Logger.log("OVERRIDDEN: setVisibility");
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
