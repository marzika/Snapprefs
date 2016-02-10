package com.marz.snapprefs;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.marz.snapprefs.Tabs.BuyTabFragment;
import com.marz.snapprefs.Tabs.DataTabFragment;
import com.marz.snapprefs.Tabs.DeluxeTabFragment;
import com.marz.snapprefs.Tabs.FiltersTabFragment;
import com.marz.snapprefs.Tabs.GeneralTabFragment;
import com.marz.snapprefs.Tabs.MainTabFragment;
import com.marz.snapprefs.Tabs.SavingTabFragment;
import com.marz.snapprefs.Tabs.SharingTabFragment;
import com.marz.snapprefs.Tabs.SpoofingTabFragment;
import com.marz.snapprefs.Tabs.TextTabFragment;

public class MainActivity2 extends AppCompatActivity{
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);//try now

        /**
         *Setup the DrawerLayout and NavigationView
         */

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.fuckyou);
        /**
         * Lets inflate the very first fragment
         * Here , we are inflating the MainTabFragment as the first Fragment
         */

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.containerView,new MainTabFragment()).commit();
        /**
         * Setup click events on the Navigation View Items.
         */

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();



                if (menuItem.getItemId() == R.id.nav_item_main) {
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.containerView,new MainTabFragment()).commit();
                }



                if (menuItem.getItemId() == R.id.nav_item_buy) {
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.containerView,new BuyTabFragment()).commit();
                }
                if (menuItem.getItemId() == R.id.nav_item_deluxe) {
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.containerView,new DeluxeTabFragment()).commit();
                }



                if (menuItem.getItemId() == R.id.nav_item_general) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.containerView,new GeneralTabFragment()).commit();
                }
                if (menuItem.getItemId() == R.id.nav_item_saving) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.containerView,new SavingTabFragment()).commit();
                }
                if (menuItem.getItemId() == R.id.nav_item_text) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.containerView,new TextTabFragment()).commit();
                }
                if (menuItem.getItemId() == R.id.nav_item_spoofing) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.containerView,new SpoofingTabFragment()).commit();
                }
                if (menuItem.getItemId() == R.id.nav_item_sharing) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.containerView,new SharingTabFragment()).commit();
                }
                if (menuItem.getItemId() == R.id.nav_item_data) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.containerView,new DataTabFragment()).commit();
                }
                if (menuItem.getItemId() == R.id.nav_item_filters) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.containerView,new FiltersTabFragment()).commit();
                }

                return false;
            }

        });

        /**
         * Setup Drawer Toggle of the Toolbar
         */

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout, toolbar,R.string.app_name,
                R.string.app_name);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();

    }
}