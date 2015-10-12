package com.marz.snapprefs;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by daltonding on 9/27/15.
 */
public class FilterStore extends Activity {

    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#00a650"));
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setCustomView(R.layout.abs);
        getActionBar().setBackgroundDrawable(colorDrawable);

        setContentView(R.layout.filterstorelayout);

        final Button changeFilter = (Button) findViewById(R.id.change_filter);
        changeFilter.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                //Intent intent = new Intent(getApplicationContext(), FilterStore.class);
                //startActivity(intent);
                //TODO add file picker with images
            }
        });
    }
}
