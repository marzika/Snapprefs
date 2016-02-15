package com.marz.snapprefs.Tabs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.marz.snapprefs.Fragments.AboutFragment;
import com.marz.snapprefs.Fragments.SupportFragment;
import com.marz.snapprefs.Fragments.MainFragment;
import com.marz.snapprefs.R;
import com.marz.snapprefs.Settings.BaseSettings;
import com.marz.snapprefs.Settings.FolderSettings;

public class SavingTabFragment extends Fragment {

    public static TabLayout tabLayout;
    public static ViewPager viewPager;
    public static int int_items = 4;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         *Inflate tab_layout and setup Views.
         */
        View x =  inflater.inflate(R.layout.tab_layout,null);
        tabLayout = (TabLayout) x.findViewById(R.id.tabs);
        viewPager = (ViewPager) x.findViewById(R.id.viewpager);

        /**
         *Set an Apater for the View Pager
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

    class MyAdapter extends FragmentPagerAdapter{

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Return fragment with respect to Position .
         */

        @Override
        public Fragment getItem(int position)
        {
            switch (position){
                case 0 : return new BaseSettings().setPreferenceId(R.xml.saving_prefs);
                case 1 : return new BaseSettings().setPreferenceId(R.xml.chat_prefs);
                case 2 : return new BaseSettings().setPreferenceId(R.xml.feedback_prefs);
                case 3 : return new FolderSettings().setPreferenceId(R.xml.folder_prefs);
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
                    return "Saving";
                case 1 :
                    return "Chat";
                case 2 :
                    return "Feedback";
                case 3 :
                    return "Folder";
            }
            return null;
        }
    }

}