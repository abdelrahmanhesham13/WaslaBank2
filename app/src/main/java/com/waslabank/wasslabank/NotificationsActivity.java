package com.waslabank.wasslabank;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.waslabank.wasslabank.adapters.NotificationsAdapter;
import com.waslabank.wasslabank.models.NotificationModel;
import com.waslabank.wasslabank.networkUtils.Connector;
import com.waslabank.wasslabank.utils.Helper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationsActivity extends AppCompatActivity {

    private final String TAG = NotificationsActivity.class.getSimpleName();
    @BindView(R.id.notification_recycler)
    RecyclerView mNotificationRecycler;

    NotificationsAdapter mNotificationsAdapter;
    ArrayList<NotificationModel> mNotificationsModels;

    Connector mConnector;
    Connector mConnectorJoin;
    Connector mConnectorCancelOffer;
    ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        ButterKnife.bind(this);
        setTitle(getString(R.string.notifications));
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNotificationsModels = new ArrayList<>();

        mConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response)) {
                    mNotificationsModels.clear();
                    mNotificationsModels.addAll(Connector.getMyNotifications(response, NotificationsActivity.this));
                    mNotificationsAdapter.notifyDataSetChanged();
                } else {
                    Helper.showSnackBarMessage(getString(R.string.no_notifications), NotificationsActivity.this);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();
                Helper.showSnackBarMessage(getString(R.string.error), NotificationsActivity.this);
            }
        });

        mConnectorCancelOffer = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    Toast.makeText(NotificationsActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                    mProgressDialog.show();
                    mConnector.getRequest(TAG, "http://www.as.cta3.com/waslabank/api/get_notifications?user_id=" + Helper.getUserSharedPreferences(NotificationsActivity.this).getId());
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {

            }
        });

        mConnectorJoin = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response)) {
                    Helper.showSnackBarMessage(getString(R.string.accept), NotificationsActivity.this);
                    mProgressDialog.show();
                    mConnector.getRequest(TAG, "http://www.as.cta3.com/waslabank/api/get_notifications?user_id=" + Helper.getUserSharedPreferences(NotificationsActivity.this).getId());
                } else {
                    Helper.showSnackBarMessage(getString(R.string.error), NotificationsActivity.this);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();
                Helper.showSnackBarMessage(getString(R.string.error), NotificationsActivity.this);

            }
        });

        mNotificationsAdapter = new NotificationsAdapter(mNotificationsModels, this, position -> {
            if (mNotificationsModels.get(position).getType().equals("join") && mNotificationsModels.get(position).getUserId().equals(Helper.getUserSharedPreferences(NotificationsActivity.this).getId()) && mNotificationsModels.get(position).getStatus().equals("0")) {
                Helper.showAlertDialog(NotificationsActivity.this, getString(R.string.do_you_accept_the_join), "", true, getString(R.string.yes), getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mProgressDialog.show();
                        mConnectorJoin.getRequest(TAG, "http://www.as.cta3.com/waslabank/api/respond_join?name=" + Uri.encode(mNotificationsModels.get(position).getmUserModel().getName()) + "&id=" + mNotificationsModels.get(position).getId() + "&from_id=" + mNotificationsModels.get(position).getFromId() + "&user_id=" + mNotificationsModels.get(position).getUserId() + "&status=1");
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mProgressDialog.show();
                        mConnectorJoin.getRequest(TAG, "http://www.as.cta3.com/waslabank/api/respond_join?name=" + Uri.encode(mNotificationsModels.get(position).getmUserModel().getName()) + "&id=" + mNotificationsModels.get(position).getId() + "&from_id=" + mNotificationsModels.get(position).getFromId() + "&user_id=" + mNotificationsModels.get(position).getUserId() + "&status=0");
                    }
                });
            } else if (mNotificationsModels.get(position).getType().equals("offer")) {
                if (Helper.getUserSharedPreferences(NotificationsActivity.this).getId().equals(mNotificationsModels.get(position).getUserId()))
                    startActivity(new Intent(NotificationsActivity.this, ConfirmRideRequest.class)
                            .putExtra("type", "offer")
                            .putExtra("RequestID", mNotificationsModels.get(position).getRequestId())
                            .putExtra("Status", mNotificationsModels.get(position).getStatus())
                            .putExtra("user_id", mNotificationsModels.get(position).getUserId())
                            .putExtra("from_id", mNotificationsModels.get(position).getFromId())
                            .putExtra("notification", mNotificationsModels.get(position)));
                else if (mNotificationsModels.get(position).getStatus().equals("1"))
                    startActivity(new Intent(NotificationsActivity.this, ConfirmRideRequest.class)
                            .putExtra("type", "offer")
                            .putExtra("RequestID", mNotificationsModels.get(position).getRequestId())
                            .putExtra("user_id", mNotificationsModels.get(position).getUserId())
                            .putExtra("from_id", mNotificationsModels.get(position).getFromId())
                            .putExtra("Status", mNotificationsModels.get(position).getStatus())
                            .putExtra("notification", mNotificationsModels.get(position)));
                else {
                    Helper.showAlertDialog(NotificationsActivity.this, "Do you want to cancel the offer?", "Offer Cancellation", true, "Yes", "No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mProgressDialog.show();
                            mConnectorCancelOffer.getRequest(TAG, "http://www.as.cta3.com/waslabank/api/cancel_offer?id=" + mNotificationsModels.get(position).getRequestId() + "&user_id=" + mNotificationsModels.get(position).getUserId() + "&from_id=" + mNotificationsModels.get(position).getFromId());
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                }
            } else if (mNotificationsModels.get(position).getType().equals("accept_offer")) {
                if (Helper.getUserSharedPreferences(NotificationsActivity.this).getId().equals(mNotificationsModels.get(position).getUserId()))
                    startActivity(new Intent(NotificationsActivity.this, ConfirmRideRequest.class)
                            .putExtra("type", "offer")
                            .putExtra("Status", mNotificationsModels.get(position).getStatus())
                            .putExtra("RequestID", mNotificationsModels.get(position).getRequestId())
                            .putExtra("user_id", mNotificationsModels.get(position).getUserId())
                            .putExtra("from_id", mNotificationsModels.get(position).getFromId())
                            .putExtra("notification", mNotificationsModels.get(position)));
                else if (mNotificationsModels.get(position).getStatus().equals("1"))
                    startActivity(new Intent(NotificationsActivity.this, ConfirmRideRequest.class)
                            .putExtra("type", "offer")
                            .putExtra("Status", mNotificationsModels.get(position).getStatus())
                            .putExtra("RequestID", mNotificationsModels.get(position).getRequestId())
                            .putExtra("user_id", mNotificationsModels.get(position).getUserId())
                            .putExtra("from_id", mNotificationsModels.get(position).getFromId())
                            .putExtra("notification", mNotificationsModels.get(position)));
            } else if (mNotificationsModels.get(position).getType().equals("trip_started")) {
                startActivity(new Intent(NotificationsActivity.this, LiveLocationMapsActivity.class).putExtra("Request_id", "" + mNotificationsModels.get(position).getRequestId()).putExtra("notification", mNotificationsModels.get(position)));

            }
        });


        mNotificationRecycler.setLayoutManager(new LinearLayoutManager(this));
        mNotificationRecycler.setAdapter(mNotificationsAdapter);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }


    @Override
    protected void onStart() {
        super.onStart();
        mProgressDialog = Helper.showProgressDialog(this, getString(R.string.loading), false);
        mConnector.getRequest(TAG, "http://www.as.cta3.com/waslabank/api/get_notifications?user_id=" + Helper.getUserSharedPreferences(this).getId());

    }
}
