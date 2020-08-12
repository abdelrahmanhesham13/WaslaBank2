package com.waslabank.wasslabank;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.waslabank.wasslabank.adapters.ChatsAdapter;
import com.waslabank.wasslabank.models.ChatModel;
import com.waslabank.wasslabank.models.RideModel;
import com.waslabank.wasslabank.models.UserModel;
import com.waslabank.wasslabank.networkUtils.Connector;
import com.waslabank.wasslabank.utils.Helper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatsActivity extends AppCompatActivity {

    @BindView(R.id.chats)
    RecyclerView mChatsRecycler;

    ArrayList<ChatModel> chats;
    Connector mConnector;
    ProgressDialog mProgressDialog;
    ChatsAdapter mAdapter;
    Connector mConnectorGetRide;

    RideModel mRideModel;

    UserModel model;
    ChatModel mChatModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        ButterKnife.bind(this);
        setTitle("Chats");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        chats = new ArrayList<>();
        mConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response)) {
                    chats.addAll(Connector.getChats(response));
                    mAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ChatsActivity.this, "No Chats", Toast.LENGTH_LONG).show();
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();
                Toast.makeText(ChatsActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
            }
        });

        mConnectorGetRide = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response)) {
                    mRideModel = Connector.getRequest(response);
                    startActivity(new Intent(ChatsActivity.this, ChatActivity.class).putExtra("chat", mChatModel).putExtra("user", model).putExtra("ride_1", mRideModel));

                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();

            }
        });

        mAdapter = new ChatsAdapter(chats, this, new ChatsAdapter.OnItemClicked() {
            @Override
            public void setOnItemClicked(int position) {
                model = new UserModel();
                model.setImage(chats.get(position).getImage());
                mChatModel = chats.get(position);
                mProgressDialog.show();
                mConnectorGetRide.getRequest("ChatsActivity", "https://code-grow.com/waslabank/api/get_request?id=" + chats.get(position).getRequestId());

            }
        });

        mChatsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mChatsRecycler.setAdapter(mAdapter);

        mProgressDialog = Helper.showProgressDialog(this, getString(R.string.loading), false);
        mConnector.getRequest("ChatsActivity", "https://code-grow.com/waslabank/api/get_chat?user_id=" + Helper.getUserSharedPreferences(this).getId());

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }
}
