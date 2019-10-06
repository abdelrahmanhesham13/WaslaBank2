package com.waslabank.wasslabank.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.waslabank.wasslabank.MyRidesFragment;
import com.waslabank.wasslabank.R;
import com.waslabank.wasslabank.RideHistoryFragment;

/**
 * Created by Abdelrahman Hesham on 4/22/2017.
 */

public class FragmentAdapter extends FragmentStatePagerAdapter {
    private Context mContext;

    public FragmentAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
               return new MyRidesFragment();
            case 1:
                return new RideHistoryFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemPosition(Object object){
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.my_rides_small);
            case 1:
                return mContext.getString(R.string.ride_history);
            default:
                return null;
        }
    }





}
