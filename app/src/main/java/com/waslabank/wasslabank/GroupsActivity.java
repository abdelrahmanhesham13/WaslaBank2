package com.waslabank.wasslabank;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.waslabank.wasslabank.adapters.GroupChatsAdapter;
import com.waslabank.wasslabank.models.GroupChatModel;
import com.waslabank.wasslabank.models.GroupsResponseModel;
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

public class GroupsActivity extends AppCompatActivity {

    @BindView(R.id.groups_recycler)
    RecyclerView mGroupsRecycler;
    @BindView(R.id.linearLayout2)
    View mCreateNewGroup;

    GroupChatsAdapter groupChatsAdapter;
    ArrayList<GroupChatModel> chats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);


        ButterKnife.bind(this);
        setTitle("Groups");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0);
        }

        chats = new ArrayList<>();

        groupChatsAdapter = new GroupChatsAdapter(chats, this, new GroupChatsAdapter.OnItemClicked() {
            @Override
            public void setOnItemClicked(int position) {
                startActivity(new Intent(GroupsActivity.this, GroupChatActivity.class).putExtra("group_id", chats.get(position).getId()));
            }
        });
        mGroupsRecycler.setHasFixedSize(true);
        mGroupsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mGroupsRecycler.setAdapter(groupChatsAdapter);

        mCreateNewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GroupsActivity.this,CreateNewGroupActivity.class));
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        chats.clear();
        getGroupChats(Helper.getUserSharedPreferences(this).getId());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }


    private void getGroupChats(String userId) {
        ProgressDialog progressDialog = Helper.showProgressDialog(this, getString(R.string.loading), false);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Connector.connectionServices.BaseURL)
                .addConverterFactory(GsonConverterFactory
                        .create(new Gson())).build();
        Connector.connectionServices connectionService =
                retrofit.create(Connector.connectionServices.class);

        connectionService.getGroupChats(userId).enqueue(new Callback<GroupsResponseModel>() {
            @Override
            public void onResponse(@NonNull Call<GroupsResponseModel> call, @NonNull Response<GroupsResponseModel> response) {
                progressDialog.dismiss();
                if (response.body() != null) {
                    if (response.body().getStatus()) {
                        chats.addAll(response.body().getGroups());
                        groupChatsAdapter.notifyDataSetChanged();
                    } else {
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GroupsResponseModel> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Helper.showLongTimeToast(GroupsActivity.this, getString(R.string.error));
            }
        });
    }


}
