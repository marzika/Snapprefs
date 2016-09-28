package com.marz.snapprefs;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.TextView;

public class OutlinedTextView extends TextView {
    public OutlinedTextView(Context context) {
        super(context);
        setShadowLayer(5, 0, 0, 0xFF000000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_SOFTWARE, getPaint());
        }
    }

    public void setOutlineColor(int color) {
        setShadowLayer(5, 0, 0, color);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        for (int i = 0; i < 5; i++)
            super.onDraw(canvas);
    }
}
