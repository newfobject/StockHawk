package com.sam_chordas.android.stockhawk.rest;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.sam_chordas.android.stockhawk.ui.TabsFragment;

import java.util.List;


public class TabsAdapter extends FragmentPagerAdapter {
    List<String> titleList;


    public TabsAdapter(FragmentManager fm, List<String> titleList) {
        super(fm);
        this.titleList = titleList;

    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0 : return TabsFragment.newInstance(0);
            case 1 : return TabsFragment.newInstance(1);
            case 2 : return TabsFragment.newInstance(2);
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titleList.get(position);
    }
}
