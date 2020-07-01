package com.playtv.go;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;


public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    boolean isFavView;
    private ArrayList<String> data;

    public ViewPagerAdapter(FragmentManager fm, ArrayList<String> data, boolean isFavView) {
        super(fm);
        this.data = data;
        this.isFavView = isFavView;
    }

    @Override
    public Fragment getItem(int position) {
        return DynamicFragment.newInstance(data.get(position), isFavView);
    }

    String getCategoryName_real(int postion) {
        String[] check = {"period", "dollarsign", "leftsquarebracket", "rightsquarebracket", "poundsign", "forwardslash"};
        String[] repcheck = {".", "$", "[", "]", "#", "/"};
        String newCatName = data.get(postion);
        for (int i = 0; i < check.length; i++) {
            newCatName = newCatName.replace(check[i], repcheck[i]);

        }
        return newCatName;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return getCategoryName_real(position);
    }
}