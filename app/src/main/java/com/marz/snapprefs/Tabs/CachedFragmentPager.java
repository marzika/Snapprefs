package com.marz.snapprefs.Tabs;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.HashMap;

/**
 * Created by MARZ on 2016. 02. 11..
 */
public abstract class CachedFragmentPager extends FragmentPagerAdapter {

    private HashMap<Integer, Fragment> cache = new HashMap<>();


    public CachedFragmentPager(FragmentManager fm) {
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {
        if (cache.get(position) == null) cache.put(position, initItem(position));
        return cache.get(position);
    }

    public abstract Fragment initItem(int position);

}
