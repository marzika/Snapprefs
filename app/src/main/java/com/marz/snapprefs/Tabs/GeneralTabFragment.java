package com.marz.snapprefs.Tabs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.marz.snapprefs.R;
import com.marz.snapprefs.Settings.BaseSettings;
import com.marz.snapprefs.Settings.MiscSettings;
import com.marz.snapprefs.Settings.StickerSettings;

public class GeneralTabFragment extends Fragment {

    public static TabLayout tabLayout;
    public static ViewPager viewPager;
    public static int int_items = 5 ;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         *Inflate tab_layout and setup Views.
         */
        View x =  inflater.inflate(R.layout.tab_layout,container, false);
        tabLayout = (TabLayout) x.findViewById(R.id.tabs);
        viewPager = (ViewPager) x.findViewById(R.id.viewpager);

        /**
         *Set an Adapter for the View Pager
         */
        viewPager.setAdapter(new MyAdapter(getChildFragmentManager()));

        /**
         * Now , this is a workaround ,
         * The setupWithViewPager dose't works without the runnable .
         * Maybe a Support Library Bug .
         */

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });

        return x;

    }

    class MyAdapter extends CachedFragmentPager{

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Return fragment with respect to Position .
         */

        @Override
        public Fragment initItem(int position)
        {
            switch (position){
                case 0 : return new BaseSettings().setPreferenceId(R.xml.timer_prefs);
                case 1 : return new BaseSettings().setPreferenceId(R.xml.select_prefs);
                case 2 : return new StickerSettings().setPreferenceId(R.xml.stickers_prefs);
                case 3 : return new BaseSettings().setPreferenceId(R.xml.filters_prefs);
                case 4 : return new MiscSettings().setPreferenceId(R.xml.misc_prefs);
            }
            return null;
        }

        @Override
        public int getCount() {

            return int_items;

        }

        /**
         * This method returns the title of the tab according to the position.
         */

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position){
                case 0 :
                    return "Timer";
                case 1 :
                    return "Select all";
                case 2 :
                    return "Stickers";
                case 3 :
                    return "Filters";
                case 4 :
                    return "Misc.";
            }
            return null;
        }
    }

}