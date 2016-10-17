package com.marz.snapprefs.Util;

import android.view.MotionEvent;
import android.view.View;

import com.marz.snapprefs.Saving;

/**
 * Created by Andre on 07/09/2016.
 */
public interface GestureEvent {
    ReturnType onTouch(View v, MotionEvent event, Saving.SnapType type);

    void reset();

    enum ReturnType {
        COMPLETED, FAILED, SAVED, PROCESSING, TAP
    }
}


