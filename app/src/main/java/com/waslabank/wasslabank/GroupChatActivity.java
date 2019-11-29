package com.waslabank.wasslabank;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.waslabank.wasslabank.adapters.GroupMessagesAdapter;
import com.waslabank.wasslabank.models.GroupMessageModel;
import com.waslabank.wasslabank.models.GroupMessagesResponseModel;
import com.waslabank.wasslabank.models.MemberModel;
import com.waslabank.wasslabank.models.StatusResponseModel;
import com.waslabank.wasslabank.networkUtils.Connector;
import com.waslabank.wasslabank.utils.Helper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GroupChatActivity extends AppCompatActivity {

    private static final String TAG = "GroupChatActivity";

    @BindView(R.id.members_parent)
    LinearLayout mMembersParent;
    @BindView(R.id.group_chat_messages)
    RecyclerView mGroupChatMessages;
    @BindView(R.id.send_btn)
    ImageView mSendButton;
    @BindView(R.id.message_text)
    EditText mMessageEditText;

    GroupMessagesAdapter groupMessagesAdapter;
    ArrayList<GroupMessageModel> messages;

    String groupId;

    GroupMessageBroadcastReceiver groupMessageBroadcastReceiver = new GroupMessageBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        ButterKnife.bind(this);
        setTitle("Chat");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0);
        }

        messages = new ArrayList<>();
        groupMessagesAdapter = new GroupMessagesAdapter(this, messages, new GroupMessagesAdapter.OnItemClicked() {
            @Override
            public void setOnItemClicked(int position) {

            }
        });
        mGroupChatMessages.setHasFixedSize(true);
        mGroupChatMessages.setLayoutManager(new LinearLayoutManager(this));
        mGroupChatMessages.setAdapter(groupMessagesAdapter);
        groupId = getIntent().getStringExtra("group_id");


        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mMessageEditText.getText().toString();
                if (message.isEmpty()) {
                    Helper.showLongTimeToast(GroupChatActivity.this, getString(R.string.type_message));
                } else {
                    sendChatMessage(Helper.getUserSharedPreferences(GroupChatActivity.this).getId(), groupId, message);
                }
            }
        });


        Log.d(TAG, "onCreate: " + groupId + " " + Helper.getUserSharedPreferences(this).getId());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.refresh_button) {
            getGroupMessages(Helper.getUserSharedPreferences(this).getId(), groupId);
            return true;
        } else if (item.getItemId() == R.id.add_member_button) {
            startActivity(new Intent(GroupChatActivity.this, CreateNewGroupActivity.class).putExtra("type", "addMember").putExtra("group_id", groupId));
            return true;
        }
        return false;
    }

    private void showMembers(List<MemberModel> memberModels) {
        mMembersParent.removeAllViews();
        for (int i = 0; i < memberModels.size(); i++) {
            MemberModel memberModel = memberModels.get(i);
            View memberView = getLayoutInflater().inflate(R.layout.list_member_item, null);
            memberView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            ((TextView) memberView.findViewById(R.id.username)).setText(memberModel.getName());
            if (URLUtil.isValidUrl(memberModel.getImage()))
                Picasso.get().load(memberModel.getImage()).fit().centerCrop().error(R.drawable.com_facebook_profile_picture_blank_portrait).into(((ImageView) memberView.findViewById(R.id.profile_image)));
            else {
                Picasso.get().load("http://www.as.cta3.com/waslabank/prod_img/" + memberModel.getImage()).fit().error(R.drawable.com_facebook_profile_picture_blank_portrait).centerCrop().into(((ImageView) memberView.findViewById(R.id.profile_image)));
            }
            Log.d(TAG, "showMembers: " + memberModel.getId());
            mMembersParent.addView(memberView);
        }
    }

    private void sendChatMessage(String userId, String groupId, String message) {
        ProgressDialog progressDialog = Helper.showProgressDialog(this, getString(R.string.loading), false);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Connector.connectionServices.BaseURL)
                .addConverterFactory(GsonConverterFactory
                        .create(new Gson())).build();
        Connector.connectionServices connectionService =
                retrofit.create(Connector.connectionServices.class);

        connectionService.sendChatMessage(userId, groupId, message).enqueue(new Callback<StatusResponseModel>() {
            @Override
            public void onResponse(@NonNull Call<StatusResponseModel> call, @NonNull Response<StatusResponseModel> response) {
                progressDialog.dismiss();
                if (response.body() != null) {
                    if (!response.body().getStatus()) {
                        Helper.showLongTimeToast(GroupChatActivity.this, getString(R.string.error));
                    } else {
                        mMessageEditText.setText("");
                        addNewMessage(message);
                        Helper.hideKeyboard(GroupChatActivity.this, mSendButton);
                        scrollToBottom();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<StatusResponseModel> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Helper.showLongTimeToast(GroupChatActivity.this, getString(R.string.error));
            }
        });
    }

    private void addNewMessage(String message) {
        GroupMessageModel groupMessageModel = new GroupMessageModel();
        groupMessageModel.setGroupId(groupId);
        groupMessageModel.setMessage(message);
        groupMessageModel.setUserId(Helper.getUserSharedPreferences(GroupChatActivity.this).getId());
        messages.add(groupMessageModel);
        groupMessagesAdapter.notifyItemInserted(messages.indexOf(groupMessageModel));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (groupId != null)
            getGroupMessages(Helper.getUserSharedPreferences(this).getId(), groupId);

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(groupMessageBroadcastReceiver,new IntentFilter("groupMessage"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(groupMessageBroadcastReceiver);
    }

    private void getGroupMessages(String userId, String groupId) {
        ProgressDialog progressDialog = Helper.showProgressDialog(this, getString(R.string.loading), false);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Connector.connectionServices.BaseURL)
                .addConverterFactory(GsonConverterFactory
                        .create(new Gson())).build();
        Connector.connectionServices connectionService =
                retrofit.create(Connector.connectionServices.class);

        connectionService.getGroupMessages(userId, groupId).enqueue(new Callback<GroupMessagesResponseModel>() {
            @Override
            public void onResponse(@NonNull Call<GroupMessagesResponseModel> call, @NonNull Response<GroupMessagesResponseModel> response) {
                progressDialog.dismiss();
                if (response.body() != null) {
                    if (response.body().getStatus()) {
                        showMembers(response.body().getMembers());
                        messages.clear();
                        messages.addAll(response.body().getMessages());
                        groupMessagesAdapter.notifyDataSetChanged();
                        scrollToBottom();
                    } else
                        finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GroupMessagesResponseModel> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Helper.showLongTimeToast(GroupChatActivity.this, getString(R.string.error));
            }
        });
    }

    private void scrollToBottom() {
        mGroupChatMessages.post(() -> mGroupChatMessages.scrollToPosition(messages.size() - 1));
    }


    class GroupMessageBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String chatId = intent.getStringExtra("chat_id");
            String userId = intent.getStringExtra("user_id");
            if (!userId.equals(Helper.getUserSharedPreferences(GroupChatActivity.this).getId()) && chatId.equals(groupId)) {
                getGroupMessages(Helper.getUserSharedPreferences(GroupChatActivity.this).getId(), groupId);
            }
        }

    }
}
