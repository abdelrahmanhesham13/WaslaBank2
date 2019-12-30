package com.waslabank.wasslabank;

import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.waslabank.wasslabank.adapters.ProfileFragmentAdapter;
import com.waslabank.wasslabank.models.UserModel;
import com.waslabank.wasslabank.utils.Helper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity {

    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    @BindView(R.id.tab)
    TabLayout mTabLayout;
    @BindView(R.id.profile_image)
    ImageView mProfileImage;
    @BindView(R.id.name)
    TextView mName;
    @BindView(R.id.car)
    TextView mCar;
    @BindView(R.id.rating_bar)
    RatingBar mRatingBar;

    UserModel mUserModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        setTitle("Rider Profile");
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ProfileFragmentAdapter fragmentPagerAdapter = new ProfileFragmentAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(fragmentPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);


    }

    @Override
    protected void onStart() {
        super.onStart();
        mUserModel = Helper.getUserSharedPreferences(this);

        if (URLUtil.isValidUrl(mUserModel.getImage()))
            Picasso.get().load(mUserModel.getImage()).fit().centerCrop().into(mProfileImage);
        else {
            Picasso.get().load("https://www.cta3.com/waslabank/prod_img/" + mUserModel.getImage()).fit().centerCrop().into(mProfileImage);
        }
        mName.setText(mUserModel.getName());
        if (!mUserModel.getRating().isEmpty())
            mRatingBar.setRating(Float.parseFloat(mUserModel.getRating()));

        mCar.setText(mUserModel.getCarName());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return false;
    }
}
