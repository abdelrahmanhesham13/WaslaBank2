package com.waslabank.wasslabank;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.VolleyError;
import com.waslabank.wasslabank.adapters.FriendsAdapter;
import com.waslabank.wasslabank.models.UserModel;
import com.waslabank.wasslabank.networkUtils.Connector;
import com.waslabank.wasslabank.utils.Helper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FriendsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    private final String TAG = FriendsActivity.class.getSimpleName();
    @BindView(R.id.friends_recycler)
    RecyclerView mFriendsRecycler;

    ArrayList<UserModel> mUserModels;

    Connector mConnector;
    ProgressDialog mProgressDialog;

    FriendsAdapter mFriendsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        ButterKnife.bind(this);
        mUserModels = new ArrayList<>();

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        mToolbar.setNavigationOnClickListener(v -> finish());

        mConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response)){
                    mUserModels.clear();
                    mUserModels.addAll(Connector.getUsers(response));
                    mFriendsAdapter.notifyDataSetChanged();
                } else {
                    Helper.showSnackBarMessage(getString(R.string.no_friends),FriendsActivity.this);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();
                Helper.showSnackBarMessage(getString(R.string.error),FriendsActivity.this);

            }
        });

        if (getIntent().hasExtra("type")) {
            if (getIntent().getStringExtra("type").equals("transfer_credit")) {
                mFriendsAdapter = new FriendsAdapter(mUserModels, this, new FriendsAdapter.OnItemClicked() {
                    @Override
                    public void setOnItemClicked(int position) {

                    }
                }, 3);
            } else {
                mFriendsAdapter = new FriendsAdapter(mUserModels, this, new FriendsAdapter.OnItemClicked() {
                    @Override
                    public void setOnItemClicked(int position) {

                    }
                }, 0);
            }
        } else {
            mFriendsAdapter = new FriendsAdapter(mUserModels, this, new FriendsAdapter.OnItemClicked() {
                @Override
                public void setOnItemClicked(int position) {

                }
            }, 0);
        }
        mFriendsRecycler.setHasFixedSize(true);
        mFriendsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mFriendsRecycler.setAdapter(mFriendsAdapter);


        mProgressDialog = Helper.showProgressDialog(this,getString(R.string.loading),false);
        mConnector.getRequest(TAG,"http://www.as.cta3.com/waslabank/api/get_friends?user_id=" + Helper.getUserSharedPreferences(this).getId());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.friends_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search){
            startActivity(new Intent(FriendsActivity.this,SearchFriendsActivity.class));
            return true;
        }
        return false;
    }
}
