package com.marz.snapprefs.Util;

import android.view.MotionEvent;
import android.view.View;

import com.marz.snapprefs.HookedLayouts;
import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Preferences;
import com.marz.snapprefs.Preferences.Prefs;
import com.marz.snapprefs.Saving;

import static com.marz.snapprefs.Util.GestureEvent.ReturnType.COMPLETED;
import static com.marz.snapprefs.Util.GestureEvent.ReturnType.FAILED;
import static com.marz.snapprefs.Util.GestureEvent.ReturnType.SAVED;
import static com.marz.snapprefs.Util.GestureEvent.ReturnType.TAP;

/**
 * Created by Andre on 07/09/2016.
 */
public class SweepSaveGesture implements GestureEvent {
    private static final int MIN_DISTANCE = 150;
    private float xStart;
    private float yStart;
    private float xEnd;
    private float yEnd;
    private int minSweepDistance;
    private boolean hasAssignedStart = false;

    public SweepSaveGesture() {
        minSweepDistance = HookedLayouts.px(MIN_DISTANCE);
    }

    public ReturnType onTouch(View v, MotionEvent event, Saving.SnapType type) {
        Logger.log("Touch: " + event.getAction());

        if (type == Saving.SnapType.STORY &&
                Preferences.getInt(Prefs.SAVEMODE_STORY) != Preferences.SAVE_S2S)
            return FAILED;
        else if (type == Saving.SnapType.SNAP && Preferences.getInt(Prefs.SAVEMODE_SNAP) != Preferences.SAVE_S2S)
            return FAILED;

        Logger.log("Position: " + event.getRawX() + " " + event.getRawY());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xStart = Math.abs(event.getRawX());
                yStart = Math.abs(event.getRawY());
                v.getParent().requestDisallowInterceptTouchEvent(true);
                hasAssignedStart = true;
                return TAP;
            case MotionEvent.ACTION_UP:
                xEnd = Math.abs(event.getRawX());
                yEnd = Math.abs(event.getRawY());
                Logger.log("Position: " + xStart + "/" + xEnd + " | " + yStart + "/" + yEnd);

                double distance = Math.hypot(xStart - xEnd,
                        yStart - yEnd);

                Logger.log("Distance: " + distance);
                if (hasAssignedStart && distance > minSweepDistance) {
                    Logger.log("Performed swipe: " + distance);
                    Saving.performS2SSave();

                    reset();
                    return SAVED;
                }

                reset();
                return distance < 10 ? TAP : COMPLETED;
        }

        return FAILED;
    }

    public void reset() {
        xStart = 0;
        xEnd = 0;
        yStart = 0;
        yEnd = 0;
        hasAssignedStart = false;
    }
}