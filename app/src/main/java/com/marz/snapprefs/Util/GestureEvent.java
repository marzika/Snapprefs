package com.marz.snapprefs.Util;

import android.view.MotionEvent;
import android.view.View;

import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Saving;

/**
 * Created by Andre on 07/09/2016.
 */
public class GestureEvent
{
    private final int MIN_DISTANCE = 300;
    private float xStart;
    private float yStart;
    private float xEnd;
    private float yEnd;

    public boolean onTouch( View v, MotionEvent event ) {
        Logger.log( "Touch: " + event.getAction() );

        switch ( event.getAction() ) {
            case MotionEvent.ACTION_DOWN:
                xStart = Math.abs( event.getRawX() );
                yStart = Math.abs( event.getRawY() );
                v.getParent().requestDisallowInterceptTouchEvent( true );
                return true;
            case MotionEvent.ACTION_UP:
                xEnd = Math.abs( event.getRawX() );
                yEnd = Math.abs( event.getRawY() );

                double distance = Math.hypot( xStart - xEnd,
                                              yStart - yEnd );

                Logger.log( "Distance: " + distance );
                if ( distance > MIN_DISTANCE ) {

                    Logger.log( "Performed swipe: " + distance );
                    Saving.performS2SSave();

                    clearPoints();
                    return true;
                }

                clearPoints();
                return false;

        }

        return false;
    }


    private void clearPoints() {
        xStart = 0;
        xEnd = 0;
        yStart = 0;
        yEnd = 0;
    }
}


