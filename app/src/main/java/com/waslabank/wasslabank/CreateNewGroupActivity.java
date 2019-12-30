package com.waslabank.wasslabank;

import android.app.ProgressDialog;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.waslabank.wasslabank.adapters.SelectFriendsAdapter;
import com.waslabank.wasslabank.models.StatusResponseModel;
import com.waslabank.wasslabank.models.UserModel;
import com.waslabank.wasslabank.networkUtils.Connector;
import com.waslabank.wasslabank.utils.Helper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CreateNewGroupActivity extends AppCompatActivity {

    private static final String TAG = "CreateNewGroupActivity";

    @BindView(R.id.friends_recycler)
    RecyclerView mFriendsRecycler;
    @BindView(R.id.group_name)
    EditText mGroupNameEditText;

    Connector mConnector;
    ProgressDialog mProgressDialog;

    ArrayList<UserModel> mUserModels;

    SelectFriendsAdapter mFriendsAdapter;

    String mType = "createNewGroup";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_group);
        ButterKnife.bind(this);
        if (getIntent().hasExtra("type"))
            mType = getIntent().getStringExtra("type");

        if (mType.equals("addMember")) {
            setTitle("Add Member");
            mGroupNameEditText.setVisibility(View.GONE);
        } else {
            setTitle("Create New Group");
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0);
        }

        mUserModels = new ArrayList<>();
        mFriendsAdapter = new SelectFriendsAdapter(mUserModels, this, new SelectFriendsAdapter.OnItemClicked() {
            @Override
            public void setOnItemClicked(int position) {

            }
        });


        mConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response)) {
                    mUserModels.clear();
                    mUserModels.addAll(Connector.getUsers(response));
                    mFriendsAdapter.notifyDataSetChanged();
                } else {
                    Helper.showSnackBarMessage(getString(R.string.no_friends), CreateNewGroupActivity.this);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();
                Helper.showSnackBarMessage(getString(R.string.error), CreateNewGroupActivity.this);

            }
        });


        mFriendsRecycler.setHasFixedSize(true);
        mFriendsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mFriendsRecycler.setAdapter(mFriendsAdapter);


        mProgressDialog = Helper.showProgressDialog(this, getString(R.string.loading), false);

        if (mType.equals("addMember")) {
            mConnector.getRequest(TAG, "https://www.cta3.com/waslabank/api/friend_not_group?user_id=" + Helper.getUserSharedPreferences(this).getId()
                    + "&group_id=" + getIntent().getStringExtra("group_id"));
        } else {
            mConnector.getRequest(TAG, "https://www.cta3.com/waslabank/api/get_friends?user_id=" + Helper.getUserSharedPreferences(this).getId());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_new_group_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.create) {
            String name = mGroupNameEditText.getText().toString();
            if (name.isEmpty() && !mType.equals("addMember")) {
                Helper.showLongTimeToast(CreateNewGroupActivity.this, "Enter Name");
            } else if (mFriendsAdapter.getSelectedUsersIds().size() == 0) {
                Helper.showLongTimeToast(CreateNewGroupActivity.this, "Choose Friends");
            } else {
                if (mType.equals("addMember")) {
                    addMember(Helper.getUserSharedPreferences(CreateNewGroupActivity.this).getId(), getIntent().getStringExtra("group_id"), mFriendsAdapter.getSelectedUsersIds());
                } else {
                    addGroup(Helper.getUserSharedPreferences(CreateNewGroupActivity.this).getId(), name, mFriendsAdapter.getSelectedUsersIds());
                }
            }
        }
        return false;
    }


    private void addGroup(String userId, String name, ArrayList<String> selectedIds) {
        ProgressDialog progressDialog = Helper.showProgressDialog(this, getString(R.string.loading), false);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Connector.connectionServices.BaseURL)
                .addConverterFactory(GsonConverterFactory
                        .create(new Gson())).build();
        Connector.connectionServices connectionService =
                retrofit.create(Connector.connectionServices.class);

        connectionService.addGroup(userId, name, selectedIds).enqueue(new Callback<StatusResponseModel>() {
            @Override
            public void onResponse(@NonNull Call<StatusResponseModel> call, @NonNull Response<StatusResponseModel> response) {
                progressDialog.dismiss();
                if (response.body() != null) {
                    if (response.body().getStatus()) {
                        finish();
                    } else {
                        Helper.showLongTimeToast(CreateNewGroupActivity.this, getString(R.string.error));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<StatusResponseModel> call, @NonNull Throwable t) {
                Helper.showLongTimeToast(CreateNewGroupActivity.this, getString(R.string.error));
            }
        });
    }


    private void addMember(String userId, String groupId, ArrayList<String> selectedIds) {
        ProgressDialog progressDialog = Helper.showProgressDialog(this, getString(R.string.loading), false);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Connector.connectionServices.BaseURL)
                .addConverterFactory(GsonConverterFactory
                        .create(new Gson())).build();
        Connector.connectionServices connectionService =
                retrofit.create(Connector.connectionServices.class);

        connectionService.addMember(userId, groupId, selectedIds).enqueue(new Callback<StatusResponseModel>() {
            @Override
            public void onResponse(@NonNull Call<StatusResponseModel> call, @NonNull Response<StatusResponseModel> response) {
                progressDialog.dismiss();
                if (response.body() != null) {
                    if (response.body().getStatus()) {
                        finish();
                    } else {
                        Helper.showLongTimeToast(CreateNewGroupActivity.this, getString(R.string.error));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<StatusResponseModel> call, @NonNull Throwable t) {
                Helper.showLongTimeToast(CreateNewGroupActivity.this, getString(R.string.error));
            }
        });
    }


}
