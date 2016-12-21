package com.marz.snapprefs.Util;

import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

public class ViewCache extends SparseArray<View> {
    public ViewCache() {
        super();
    }

    public TextView getTV(int resource) {
        return (TextView) this.get(resource);
    }

    public Button getBtn(int resource) {
        return (Button) this.get(resource);
    }
}
