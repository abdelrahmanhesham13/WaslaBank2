package com.waslabank.wasslabank;

import android.app.ProgressDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;

import com.android.volley.VolleyError;
import com.waslabank.wasslabank.adapters.RidesAdapter;
import com.waslabank.wasslabank.models.RideModel;
import com.waslabank.wasslabank.networkUtils.Connector;
import com.waslabank.wasslabank.utils.Helper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FriendsRidesActivity extends AppCompatActivity {

    private static final String TAG = "FriendsRidesActivity";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.rides_recycler)
    RecyclerView mRidesRecycler;

    RidesAdapter mRidesAdapter;
    ArrayList<RideModel> mRides;

    Connector mConnector;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_rides);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        mToolbar.setNavigationOnClickListener(v -> finish());

        mRides = new ArrayList<>();

        mConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response)) {
                    mRides.addAll(Connector.getRequests(response));
                    mRidesAdapter.notifyDataSetChanged();
                } else {
                    Helper.showSnackBarMessage(getString(R.string.no_results), FriendsRidesActivity.this);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();
                Log.d("Error_", "onError: "+error.getMessage());
                Helper.showSnackBarMessage(getString(R.string.error), FriendsRidesActivity.this);
            }
        });


        mRidesAdapter = new RidesAdapter(mRides, this,0,this, position -> {

        });
        mRidesAdapter.flag = 1;

        mRidesRecycler.setHasFixedSize(true);
        mRidesRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRidesRecycler.setAdapter(mRidesAdapter);

        mProgressDialog = Helper.showProgressDialog(this,getString(R.string.loading),false);
        mConnector.getRequest(TAG,"https://www.cta3.com/waslabank/api/get_requests?user_id=" + getIntent().getStringExtra("user_id") + "&type=friend");

    }
}
