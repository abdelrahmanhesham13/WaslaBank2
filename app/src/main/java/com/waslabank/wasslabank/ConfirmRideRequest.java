package com.waslabank.wasslabank;
// -->> To Do -> 73ml hena call ll individual Request (RequestID) lw fe offer 73ml button ywdy ll notification

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.waslabank.wasslabank.models.ChatModel;
import com.waslabank.wasslabank.models.MyRideModel;
import com.waslabank.wasslabank.models.NotificationModel;
import com.waslabank.wasslabank.models.OfferModel;
import com.waslabank.wasslabank.models.RideModel;
import com.waslabank.wasslabank.models.SingleRequestModel.Example;
import com.waslabank.wasslabank.models.SingleRequestModel.User;
import com.waslabank.wasslabank.models.StatusModel;
import com.waslabank.wasslabank.models.UserModel;
import com.waslabank.wasslabank.networkUtils.Connector;
import com.waslabank.wasslabank.utils.GPSTracker;
import com.waslabank.wasslabank.utils.Helper;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConfirmRideRequest extends AppCompatActivity {

    private final String TAG = ConfirmRideRequest.class.getSimpleName();
    @BindView(R.id.profile_image)
    ImageView mProfileImage;
    @BindView(R.id.offers)
    LinearLayout offers;
    @BindView(R.id.name)
    TextView mNameTextView;
    @BindView(R.id.car)
    TextView mCarDetailsTextView;
    @BindView(R.id.from_place)
    TextView mFromPlaceTextView;
    @BindView(R.id.to_place)
    TextView mToPlaceTextView;
    @BindView(R.id.date)
    TextView mDateTextView;
    @BindView(R.id.time)
    TextView mTimeTextView;
    @BindView(R.id.distance)
    TextView mDistanceTextView;
    @BindView(R.id.confirm_button)
    Button mConfirmButton;
    @BindView(R.id.cancell)
    Button cancell;
    @BindView(R.id.mapLiveLocation)
    Button mapLiveLocation;
    @BindView(R.id.Call)
    Button Call;
    @BindView(R.id.finishTrip)
    Button finishTrip;
    @BindView(R.id.number_button)
    ElegantNumberButton mNumberButton;
    GPSTracker mTracker;
    private ProgressDialog dialog;

    RideModel mRideModel;
    MyRideModel mMyRideMode;
    NotificationModel mNotificationModel;

    @BindView(R.id.offers_parent)
    LinearLayout offersParent;

    Connector mConnector;
    Connector mConnectorGetRequest;
    Connector mConnectorAcceptOffer;
    Connector mConnectorGetUser;
    ProgressDialog mProgressDialog;
    Connector mConnectorRate;

    float mRatingNumber;
    Connector mConnectorSendMessage;
    AlertDialog alertDialog;

    boolean mLocated = false;
    private ChatModel mChatModel;

    UserModel mUserModel;
    UserModel mFromUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_ride_request);
        ButterKnife.bind(this);

        mUserModel = Helper.getUserSharedPreferences(this);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait..");

        mConnectorSendMessage = new Connector(ConfirmRideRequest.this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (getIntent().getStringExtra("type").equals("offer")) {
                    if (mUserModel.getId().equals(mRideModel.getUserId()))
                        mChatModel = Connector.getChatModelJson(response, mFromUser.getName(), mRideModel.getFromId(), mUserModel.getId());
                    else
                        mChatModel = Connector.getChatModelJson(response, mRideModel.getUser().getName(), mRideModel.getUserId(), mUserModel.getId());

                    startActivity(new Intent(ConfirmRideRequest.this, ChatActivity.class).putExtra("chat", mChatModel).putExtra("user", mFromUser).putExtra("ride_1", mRideModel));

                } else {
                    if (mUserModel.getId().equals(mMyRideMode.getUserId()))
                        mChatModel = Connector.getChatModelJson(response, mFromUser.getName(), mMyRideMode.getFromId(), mUserModel.getId());
                    else
                        mChatModel = Connector.getChatModelJson(response, mMyRideMode.getUser().getName(), mMyRideMode.getUserId(), mUserModel.getId());

                    startActivity(new Intent(ConfirmRideRequest.this, ChatActivity.class).putExtra("chat", mChatModel).putExtra("user", mFromUser).putExtra("ride_2", mMyRideMode));

                }
                //Intent returnIntent = new Intent();
                //returnIntent.putExtra("chat",mChatModel);


                //setResult(Activity.RESULT_OK,returnIntent);
                //finish();
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                Helper.showSnackBarMessage("خطأ من فضلك اعد المحاوله", ConfirmRideRequest.this);
            }
        });

        mConnectorRate = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                alertDialog.dismiss();
                dialog.show();
                finishTrip(getIntent().getStringExtra("RequestID"), "");
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                alertDialog.dismiss();
                Helper.showSnackBarMessage(getString(R.string.error), ConfirmRideRequest.this);
            }
        });

        mConnectorGetUser = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response)) {
                    mFromUser = Connector.getUser(response);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();

            }
        });

        mConnectorGetRequest = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    mRideModel = Connector.getRequest(response);
                    mFromPlaceTextView.setText(mRideModel.getAddress());
                    mToPlaceTextView.setText(mRideModel.getAddressTo());
                    mDateTextView.setText(mRideModel.getRequestDate());
                    mTimeTextView.setText(mRideModel.getRequestTime());
                    mDistanceTextView.setText(String.format(Locale.ENGLISH, "%.2f KM", Float.valueOf(mRideModel.getDistance())));
                    if (getIntent().getStringExtra("type").equals("offer")) {
                        if (mUserModel.getId().equals(mRideModel.getUserId()))
                            mConnectorGetUser.getRequest(TAG, "https://code-grow.com/waslabank/api/get_user?id=" + mRideModel.getFromId());
                        else
                            mConnectorGetUser.getRequest(TAG, "https://code-grow.com/waslabank/api/get_user?id=" + mRideModel.getUserId());

                    } else {
                        if (mUserModel.getId().equals(mMyRideMode.getUserId()))
                            mConnectorGetUser.getRequest(TAG, "https://code-grow.com/waslabank/api/get_user?id=" + mMyRideMode.getFromId());
                        else
                            mConnectorGetUser.getRequest(TAG, "https://code-grow.com/waslabank/api/get_user?id=" + mMyRideMode.getUserId());
                    }
                } else {
                    Helper.showSnackBarMessage(getString(R.string.error), ConfirmRideRequest.this);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                Helper.showSnackBarMessage(getString(R.string.error), ConfirmRideRequest.this);
            }
        });

        mConnector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response)) {
                    finish();
                    startActivity(new Intent(ConfirmRideRequest.this, MyRidesActivity.class));
                    Toast.makeText(ConfirmRideRequest.this, getString(R.string.registered_successfully)
                            + getString(R.string.youwillbeable), Toast.LENGTH_LONG).show();
                } else {
                    Helper.showSnackBarMessage(getString(R.string.error), ConfirmRideRequest.this);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();
                Helper.showSnackBarMessage(getString(R.string.error), ConfirmRideRequest.this);

            }
        });

        mConnectorAcceptOffer = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response)) {
                    finish();
                } else {
                    Helper.showSnackBarMessage(getString(R.string.error), ConfirmRideRequest.this);

                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();
                Helper.showSnackBarMessage(getString(R.string.error), ConfirmRideRequest.this);

            }
        });
        mNumberButton.setRange(1, 4);
        if (getIntent().hasExtra("Seats")) {
            mNumberButton.setRange(1, Integer.parseInt(getIntent().getStringExtra("Seats")));
        }
        mNumberButton.setNumber("1");
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent() != null) {
            if (getIntent().getStringExtra("type").equals("show")) {
                getIndividualRequest(getIntent().getStringExtra("RequestID"));
                mMyRideMode = new MyRideModel();
                mMyRideMode.setUser(new UserModel());
                offers.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        startActivity(new Intent(ConfirmRideRequest.this, NotificationsActivity.class));
                    }
                });
                setTitle(getString(R.string.ride_details));
                if (getIntent().hasExtra("request"))
                    mMyRideMode = (MyRideModel) getIntent().getSerializableExtra("request");
                if (URLUtil.isValidUrl(mMyRideMode.getUser().getImage()))
                    Picasso.get().load(mMyRideMode.getUser().getImage()).fit().centerCrop().into(mProfileImage);
                else {
                    Picasso.get().load("https://code-grow.com/waslabank/prod_img/" + mMyRideMode.getUser().getImage()).fit().centerCrop().into(mProfileImage);
                }
                mNameTextView.setText(mMyRideMode.getUser().getName());
                mCarDetailsTextView.setText(mMyRideMode.getUser().getCarName());
                mFromPlaceTextView.setText(mMyRideMode.getAddress());
                mToPlaceTextView.setText(mMyRideMode.getAddressTo());
                mDateTextView.setText(mMyRideMode.getRequestDate());
                mTimeTextView.setText(mMyRideMode.getRequestTime());
                mDistanceTextView.setText(String.format(Locale.ENGLISH, "%.2f KM", Float.valueOf(mMyRideMode.getDistance())));
                mNumberButton.setVisibility(View.GONE);
                mProgressDialog = Helper.showProgressDialog(this, getString(R.string.loading), false);
                if (mUserModel.getId().equals(mMyRideMode.getUserId()))
                    mConnectorGetUser.getRequest(TAG, "https://code-grow.com/waslabank/api/get_user?id=" + mMyRideMode.getFromId());
                else
                    mConnectorGetUser.getRequest(TAG, "https://code-grow.com/waslabank/api/get_user?id=" + mMyRideMode.getUserId());

                if ((mMyRideMode.getStatus().equals("1") || mMyRideMode.getStatus().equals("2")) && mMyRideMode.isUpcoming()) {
                    mConfirmButton.setText(getString(R.string.message));
                    Call.setVisibility(View.VISIBLE);
                    Call.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (getIntent().getStringExtra("user_id").equals(mUserModel.getId())) {
                                getUser(getIntent().getStringExtra("from_id"));
                                dialog.show();
                            } else {
                                getUser(getIntent().getStringExtra("user_id"));
                                dialog.show();

                            }
                        }
                    });
                    if (!getIntent().getStringExtra("Status").equals("4")) {
                        finishTrip.setVisibility(View.GONE);
                    }
                    finishTrip.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            show();
                        }
                    });
                    if ((getIntent().getStringExtra("user_id").equals(Helper.getUserSharedPreferences(this).getId()))) {
                    } else {
                        finishTrip.setVisibility(View.GONE);
                    }
                    mapLiveLocation.setVisibility(View.VISIBLE);
                    mapLiveLocation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(ConfirmRideRequest.this, LiveLocationMapsActivity.class)
                                    .putExtra("Request_id", "" + getIntent().getStringExtra("RequestID")));

                        }
                    });

                } else {
                    mConfirmButton.setVisibility(View.GONE);
                }
            } else if (getIntent().getStringExtra("type").equals("offer")) {
                setTitle(getString(R.string.ride_details));
                mNotificationModel = (NotificationModel) getIntent().getSerializableExtra("notification");
                if (URLUtil.isValidUrl(mNotificationModel.getmUserModel().getImage()))
                    Picasso.get().load(mNotificationModel.getmUserModel().getImage()).fit().centerCrop().into(mProfileImage);
                else {
                    Picasso.get().load("https://code-grow.com/waslabank/prod_img/" + mNotificationModel.getmUserModel().getImage()).fit().centerCrop().into(mProfileImage);
                   /* Picasso.get().load("https://code-grow.com/waslabank/prod_img/" + mMyRideMode.getUser()
                            .getImage()).fit().centerCrop().into(mProfileImage);*/
                }
                mNameTextView.setText(mNotificationModel.getmUserModel().getName());
                mCarDetailsTextView.setText(mNotificationModel.getmUserModel().getCarName());
                mNumberButton.setVisibility(View.GONE);
                mProgressDialog = Helper.showProgressDialog(this, getString(R.string.loading), false);
                mConnectorGetRequest.getRequest(TAG, "https://code-grow.com/waslabank/api/get_request?id=" + mNotificationModel.getRequestId());
                if (mNotificationModel.getStatus().equals("0")) {
                    cancell.setVisibility(View.VISIBLE);
                    mConfirmButton.setText(getString(R.string.accept));
                    cancell.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            reject_offer("" + mNotificationModel.getUserId(), "" + mNotificationModel.getFromId(), "" + mNotificationModel.getRequestId());
                        }
                    });
                } else {
                    mConfirmButton.setText(getString(R.string.message));
                    ///Comment
                    Call.setVisibility(View.VISIBLE);
                    Call.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (getIntent().getStringExtra("user_id").equals(mUserModel.getId())) {
                                getUser(getIntent().getStringExtra("from_id"));
                                dialog.show();
                            } else {
                                getUser(getIntent().getStringExtra("user_id"));
                                dialog.show();

                            }
                        }
                    });
                    if (!getIntent().getStringExtra("Status").equals("4")) {
                        finishTrip.setVisibility(View.GONE);
                    }
                    finishTrip.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            show();
                        }
                    });
                    if ((getIntent().getStringExtra("user_id").equals(Helper.getUserSharedPreferences(this).getId()))) {
                    } else {
                        finishTrip.setVisibility(View.GONE);
                    }
                    mapLiveLocation.setVisibility(View.VISIBLE);
                    mapLiveLocation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(ConfirmRideRequest.this, LiveLocationMapsActivity.class)
                                    .putExtra("Request_id", "" + getIntent().getStringExtra("RequestID")));

                        }
                    });
////////////////////////////////////////////////////////////////////////////////////////////////////
                }
            } else {
                setTitle(getString(R.string.confirm_ride_request));
                mRideModel = (RideModel) getIntent().getSerializableExtra("request");
                if (URLUtil.isValidUrl(mRideModel.getUser().getImage()))
                    Picasso.get().load(mRideModel.getUser().getImage()).fit().centerCrop().into(mProfileImage);
                else {
                    Picasso.get().load("https://code-grow.com/waslabank/prod_img/" + mRideModel.getUser().getImage()).fit().centerCrop().into(mProfileImage);
                }
                mNameTextView.setText(mRideModel.getUser().getName());
                mCarDetailsTextView.setText(mRideModel.getUser().getCarName());
                mFromPlaceTextView.setText(mRideModel.getAddress());
                mToPlaceTextView.setText(mRideModel.getAddressTo());
                mDateTextView.setText(mRideModel.getRequestDate());
                mTimeTextView.setText(mRideModel.getRequestTime());
                mDistanceTextView.setText(String.format(Locale.ENGLISH, "%.2f KM", Float.valueOf(mRideModel.getDistance())));
                mProgressDialog = Helper.showProgressDialog(this, getString(R.string.loading), false);
                if (mUserModel.getId().equals(mRideModel.getUserId()))
                    mConnectorGetUser.getRequest(TAG, "https://code-grow.com/waslabank/api/get_user?id=" + mRideModel.getFromId());
                else
                    mConnectorGetUser.getRequest(TAG, "https://code-grow.com/waslabank/api/get_user?id=" + mRideModel.getUserId());
            }

        }

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mConfirmButton.getText().equals(getString(R.string.confirm))) {
                    getLocation();
                } else if (mConfirmButton.getText().equals(getString(R.string.accept))) {
                    mProgressDialog.show();
                    mConnectorAcceptOffer.getRequest(TAG, "https://code-grow.com/waslabank/api/accept_offer?id=" + mNotificationModel.getRequestId() + "&from_id=" + mNotificationModel.getFromId() + "&user_id=" + mNotificationModel.getUserId());
                } else {
                    if (getIntent().getStringExtra("type").equals("offer")) {
                        if (mUserModel.getId().equals(mRideModel.getUserId())) {
                            String url = "https://code-grow.com/waslabank/api/start_chat" + "?message=&user_id=" + mUserModel.getId() + "&to_id=" + mRideModel.getFromId() + "&request_id=" + mRideModel.getId();
                            Helper.writeToLog(url);
                            mConnectorSendMessage.getRequest(TAG, url);
                        } else {
                            String url = "https://code-grow.com/waslabank/api/start_chat" + "?message=&user_id=" + mUserModel.getId() + "&to_id=" + mRideModel.getUserId() + "&request_id=" + mRideModel.getId();
                            Helper.writeToLog(url);
                            mConnectorSendMessage.getRequest(TAG, url);
                        }
                    } else {
                        if (mUserModel.getId().equals(mMyRideMode.getUserId())) {
                            String url = "https://code-grow.com/waslabank/api/start_chat" + "?message=&user_id=" + mUserModel.getId() + "&to_id=" + mMyRideMode.getFromId() + "&request_id=" + mMyRideMode.getId();
                            Helper.writeToLog(url);
                            mConnectorSendMessage.getRequest(TAG, url);
                        } else {
                            String url = "https://code-grow.com/waslabank/api/start_chat" + "?message=&user_id=" + mUserModel.getId() + "&to_id=" + mMyRideMode.getUserId() + "&request_id=" + mMyRideMode.getId();
                            Helper.writeToLog(url);
                            mConnectorSendMessage.getRequest(TAG, url);
                        }
                    }
                }
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;

    }

    private void getLocation() {
        mTracker = new GPSTracker(ConfirmRideRequest.this, new GPSTracker.OnGetLocation() {
            @Override
            public void onGetLocation(double lat, double lon) {
                if (lat != 0 && lon != 0 && !mLocated) {
                    mLocated = true;
                    mProgressDialog = Helper.showProgressDialog(ConfirmRideRequest.this, getString(R.string.loading), false);
                    mConnector.getRequest(TAG, "https://code-grow.com/waslabank/api/send_offer?request_id=" + mRideModel.getId() + "&user_id=" + mRideModel.getUserId() + "&longitude=" + lon + "&latitude=" + lat + "&address=address&distance=100&from_id=" + Helper.getUserSharedPreferences(ConfirmRideRequest.this).getId() + "&seats=" + mNumberButton.getNumber());
                    mTracker.stopUsingGPS();
                }
            }
        });
        if (mTracker.canGetLocation()) {
            Location location = mTracker.getLocation();
            if (location != null) {
                if (location.getLatitude() != 0 && location.getLongitude() != 0 && !mLocated) {
                    mProgressDialog = Helper.showProgressDialog(ConfirmRideRequest.this, getString(R.string.loading), false);
                    mConnector.getRequest(TAG, "https://code-grow.com/waslabank/api/send_offer?request_id=" + mRideModel.getId() + "&user_id=" + mRideModel.getUserId() + "&longitude=" + location.getLongitude() + "&latitude=" + location.getLatitude() + "&address=address&distance=100&from_id=" + Helper.getUserSharedPreferences(ConfirmRideRequest.this).getId() + "&seats=" + mNumberButton.getNumber());
                    mLocated = true;
                    mTracker.stopUsingGPS();
                }
            }
        }
    }

    private void finishTrip(String id, String status) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Connector.connectionServices.BaseURL)
                .addConverterFactory(GsonConverterFactory
                        .create(new Gson())).build();
        Connector.connectionServices connectionService =
                retrofit.create(Connector.connectionServices.class);

        connectionService.update_request_status("4", id + "").enqueue(new Callback<StatusModel>() {
            @Override
            public void onResponse(Call<StatusModel> call, Response<StatusModel> response) {
                dialog.dismiss();
                StatusModel statusModel = response.body();
                if (statusModel.getStatus()) {
                    finish();
                } else {
                    Toast.makeText(ConfirmRideRequest.this, "false" + id, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StatusModel> call, Throwable t) {
                dialog.dismiss();

                Toast.makeText(ConfirmRideRequest.this, "faslse ++" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUser(String ID) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Connector.connectionServices.BaseURL)
                .addConverterFactory(GsonConverterFactory
                        .create(new Gson())).build();
        Connector.connectionServices connectionService =
                retrofit.create(Connector.connectionServices.class);

        connectionService.get_user(ID).enqueue(new Callback<StatusModel>() {
            @Override
            public void onResponse(Call<StatusModel> call, Response<StatusModel> response) {
                dialog.dismiss();
                StatusModel statusModel = response.body();
                Intent intent = new Intent(Intent.ACTION_CALL, Uri
                        .parse("tel:" + statusModel.getUser().getMobile()));
                Helper.writeToLog(statusModel.getUser().getMobile());
                if (ActivityCompat.checkSelfPermission(ConfirmRideRequest.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(intent);

            }

            @Override
            public void onFailure(Call<StatusModel> call, Throwable t) {
                dialog.dismiss();

            }
        });
    }

    private void getIndividualRequest(String id) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Connector.connectionServices.BaseURL)
                .addConverterFactory(GsonConverterFactory
                        .create(new Gson())).build();
        Connector.connectionServices connectionService =
                retrofit.create(Connector.connectionServices.class);

        connectionService.getSingleRequest(id).enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {
                Example example = response.body();
                if (example.getStatus()) {
                    if (example.getRequest().getOffers().size() > 0) {
                        //offers.setVisibility(View.VISIBLE);
                        offersParent.setVisibility(View.VISIBLE);
                        for (int i = 0; i < example.getRequest().getOffers().size(); i++) {
                            OfferModel offer = example.getRequest().getOffers().get(i).getOffer();
                            User from = example.getRequest().getOffers().get(i).getClient();
                            View v = getLayoutInflater().inflate(R.layout.list_offer_item, null);
                            ((TextView) v.findViewById(R.id.name)).setText(example.getRequest().getOffers().get(i).getClient().getName());
                            ((TextView) v.findViewById(R.id.car)).setText(example.getRequest().getOffers().get(i).getClient().getCarName());
                            ((TextView) v.findViewById(R.id.from_place)).setText(mFromPlaceTextView.getText());
                            ((TextView) v.findViewById(R.id.to_place)).setText(mToPlaceTextView.getText());
                            if (URLUtil.isValidUrl(example.getRequest().getOffers().get(i).getClient().getImage())) {
                                Picasso.get().load(example.getRequest().getOffers().get(i).getClient().getImage()).into((ImageView) v.findViewById(R.id.profile_image));
                            } else {
                                Picasso.get().load("https://code-grow.com/waslabank/prod_img/" + example.getRequest().getOffers().get(i).getClient().getImage()).into((ImageView) v.findViewById(R.id.profile_image));

                            }
                            ((Button) v.findViewById(R.id.accept)).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mConnectorAcceptOffer.getRequest(TAG, "https://code-grow.com/waslabank/api/accept_offer?id=" + offer.getRequestId() + "&from_id=" + from.getId() + "&user_id=" + Helper.getUserSharedPreferences(ConfirmRideRequest.this).getId());
                                }
                            });

                            ((Button) v.findViewById(R.id.reject)).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    reject_offer(Helper.getUserSharedPreferences(ConfirmRideRequest.this).getId(), from.getId(), offer.getRequestId());
                                }
                            });

                            offersParent.addView(v);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {

            }
        });

    }

    private void reject_offer(String userId, String fromId, String Id) {
        ProgressDialog p = new ProgressDialog(ConfirmRideRequest.this);
        p.setMessage("Loading...");
        p.show();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Connector.connectionServices.BaseURL)
                .addConverterFactory(GsonConverterFactory
                        .create(new Gson())).build();
        Connector.connectionServices connectionService =
                retrofit.create(Connector.connectionServices.class);

        connectionService.reject_offer(userId, fromId, Id).enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {
                Log.d("TTTT", "onResponse: raw" + response.raw());
                Log.d("TTTT", "onResponse: raw" + response.toString());
                p.dismiss();
                Example example = response.body();
                if (example.getStatus()) {
                    Toast.makeText(ConfirmRideRequest.this, "Request Rejected Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {
                p.dismiss();
                Toast.makeText(ConfirmRideRequest.this, "Something wrong happen , please try again", Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void show() {
        final android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_rating, null);
        dialogBuilder.setView(dialogView);
        final RatingBar rating = dialogView.findViewById(R.id.rating_bar_2);
        final Button rate = dialogView.findViewById(R.id.btn_rate);
        final EditText comment = dialogView.findViewById(R.id.comment);
        rating.setIsIndicator(false);
        alertDialog = dialogBuilder.create();
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        alertDialog.show();
        rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                mRatingNumber = rating;
            }
        });
        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = comment.getText().toString();
                if (TextUtils.isEmpty(commentText)) {
                    Helper.showSnackBarMessage(getString(R.string.enter_comment), ConfirmRideRequest.this);
                } else {
                    if (mMyRideMode != null) {
                        if (mUserModel.getId().equals(mMyRideMode.getFromId())) {
                            if (getIntent() != null && getIntent().hasExtra("ride_2")) {
                                mConnectorRate.getRequest(TAG, "https://code-grow.com/waslabank/api/add_comment?comment=" + Uri.encode(commentText) + "&rating=" + mRatingNumber + "&request_id=" + mMyRideMode.getId() + "&from_id=" + mUserModel.getId() + "&user_id=" + mMyRideMode.getUserId());
                            } else {
                                mConnectorRate.getRequest(TAG, "https://code-grow.com/waslabank/api/add_comment?comment=" + Uri.encode(commentText) + "&rating=" + mRatingNumber + "&request_id=" + mMyRideMode.getId() + "&from_id=" + mUserModel.getId() + "&user_id=" + mMyRideMode.getUserId());

                            }
                        } else {
                            if (getIntent() != null && getIntent().hasExtra("ride_2")) {
                                mConnectorRate.getRequest(TAG, "https://code-grow.com/waslabank/api/add_comment?comment=" + Uri.encode(commentText) + "&rating=" + mRatingNumber + "&request_id=" + mMyRideMode.getId() + "&from_id=" + mUserModel.getId() + "&user_id=" + mMyRideMode.getFromId());
                            } else {
                                mConnectorRate.getRequest(TAG, "https://code-grow.com/waslabank/api/add_comment?comment=" + Uri.encode(commentText) + "&rating=" + mRatingNumber + "&request_id=" + mMyRideMode.getId() + "&from_id=" + mUserModel.getId() + "&user_id=" + mMyRideMode.getFromId());

                            }
                        }
                    } else {
                        if (mRideModel != null) {
                            if (mUserModel.getId().equals(mRideModel.getFromId())) {
                                mConnectorRate.getRequest(TAG, "https://code-grow.com/waslabank/api/add_comment?comment=" + Uri.encode(commentText) + "&rating=" + mRatingNumber + "&request_id=" + mRideModel.getId() + "&from_id=" + mUserModel.getId() + "&user_id=" + mMyRideMode.getFromId());
                            } else {
                                mConnectorRate.getRequest(TAG, "https://code-grow.com/waslabank/api/add_comment?comment=" + Uri.encode(commentText) + "&rating=" + mRatingNumber + "&request_id=" + mRideModel.getId() + "&from_id=" + mUserModel.getId() + "&user_id=" + mMyRideMode.getFromId());

                            }
                        } else {
                            if (getIntent() != null && getIntent().hasExtra("ride_2")) {
                                mConnectorRate.getRequest(TAG, "https://code-grow.com/waslabank/api/add_comment?comment=" + Uri.encode(commentText) + "&rating=" + mRatingNumber + "&request_id=" + mMyRideMode.getId() + "&from_id=" + mUserModel.getId());
                            } else {
                                mConnectorRate.getRequest(TAG, "https://code-grow.com/waslabank/api/add_comment?comment=" + Uri.encode(commentText) + "&rating=" + mRatingNumber + "&request_id=" + mMyRideMode.getId() + "&from_id=" + mUserModel.getId());

                            }
                        }
                    }
                }
            }
        });


    }

}
