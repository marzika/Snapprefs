package com.marz.snapprefs.Util;

import android.support.v4.view.VelocityTrackerCompat;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import com.marz.snapprefs.HookedLayouts;
import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Preferences;
import com.marz.snapprefs.Preferences.Prefs;
import com.marz.snapprefs.Saving;

import static com.marz.snapprefs.Util.GestureEvent.ReturnType.FAILED;
import static com.marz.snapprefs.Util.GestureEvent.ReturnType.PROCESSING;
import static com.marz.snapprefs.Util.GestureEvent.ReturnType.SAVED;
import static com.marz.snapprefs.Util.GestureEvent.ReturnType.TAP;

/**
 * Created by Andre on 07/09/2016.
 */
public class FlingSaveGesture implements GestureEvent {
    private final int DENSITY_INDEPENDENT_THRESHOLD = 2100;
    private int minVelocityThreshhold;
    private boolean hasAssignedStart = false;
    private boolean hasMoved = false;
    private boolean hasSaved = false;
    private VelocityTracker velocityTracker;

    public FlingSaveGesture() {
        minVelocityThreshhold = HookedLayouts.px(DENSITY_INDEPENDENT_THRESHOLD);
    }

    public ReturnType onTouch(View v, MotionEvent event, Saving.SnapType type) {
        Logger.log("Touch: " + event.getAction());

        if (type == Saving.SnapType.STORY && Preferences.getInt(Prefs.SAVEMODE_STORY) != Preferences.SAVE_F2S)
            return FAILED;
        else if (type == Saving.SnapType.SNAP && Preferences.getInt(Prefs.SAVEMODE_SNAP) != Preferences.SAVE_F2S)
            return FAILED;

        Logger.log("Position: " + event.getRawX() + " " + event.getRawY());

        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (velocityTracker == null)
                    velocityTracker = VelocityTracker.obtain();
                else
                    velocityTracker.clear();

                v.getParent().requestDisallowInterceptTouchEvent(true);
                hasAssignedStart = true;
                return TAP;
            case MotionEvent.ACTION_MOVE:
                v.getParent().requestDisallowInterceptTouchEvent(true);
                velocityTracker.addMovement(event);

                velocityTracker.computeCurrentVelocity(1000, minVelocityThreshhold);

                float xVelocity = VelocityTrackerCompat.getXVelocity(velocityTracker, pointerId);
                float yVelocity = VelocityTrackerCompat.getYVelocity(velocityTracker, pointerId);
                double velocityHypot = Math.hypot(xVelocity, yVelocity);

                Logger.log("XVel: " + xVelocity + "  YVel: " + yVelocity);
                Logger.log("Total Velocity: " + velocityHypot + " / " + minVelocityThreshhold);

                hasMoved = velocityHypot > 100;

                if (hasAssignedStart && !hasSaved && velocityHypot > minVelocityThreshhold) {
                    Logger.log("Performed swipe: " + velocityHypot);
                    Saving.performS2SSave();
                    hasSaved = true;

                    return SAVED;
                }

                return PROCESSING;
            case MotionEvent.ACTION_UP:
                if (!hasMoved) {
                    reset();
                    return TAP;
                }
            case MotionEvent.ACTION_CANCEL:
                reset();
        }

        return FAILED;
    }

    public void reset() {
        hasAssignedStart = false;
        hasMoved = false;
        hasSaved = false;
    }
}


