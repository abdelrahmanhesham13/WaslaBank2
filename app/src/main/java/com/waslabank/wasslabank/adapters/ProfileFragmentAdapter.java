package com.waslabank.wasslabank.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.waslabank.wasslabank.AboutFragment;
import com.waslabank.wasslabank.MyRidesFragment;
import com.waslabank.wasslabank.R;
import com.waslabank.wasslabank.ReviewsFragment;
import com.waslabank.wasslabank.RideHistoryFragment;

/**
 * Created by Abdelrahman Hesham on 4/22/2017.
 */

public class ProfileFragmentAdapter extends FragmentStatePagerAdapter {
    private Context mContext;

    public ProfileFragmentAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
               return new AboutFragment();
            case 1:
                return new ReviewsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "About";
            case 1:
                return "Reviews";
            default:
                return null;
        }
    }





}
