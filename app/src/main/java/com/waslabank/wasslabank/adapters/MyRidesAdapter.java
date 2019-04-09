package com.waslabank.wasslabank.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.waslabank.wasslabank.ConfirmRideRequest;
import com.waslabank.wasslabank.LiveLocationMapsActivity;
import com.waslabank.wasslabank.R;
import com.waslabank.wasslabank.models.MyRideModel;
import com.waslabank.wasslabank.models.StatusModel;
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

public class MyRidesAdapter extends RecyclerView.Adapter<MyRidesAdapter.MyRideViewHolder> {


    ArrayList<MyRideModel> rides;
    Context context;
    OnItemClicked onItemClicked;
    Connector mConnector;
    ProgressDialog dialog;
    private final String TAG = MyRidesAdapter.class.getSimpleName();

    public MyRidesAdapter(ArrayList<MyRideModel> rides, Context context, OnItemClicked onItemClicked) {
        this.rides = rides;
        this.context = context;
        this.onItemClicked = onItemClicked;
        mConnector = new Connector(context, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    Toast.makeText(context, "Started", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {

            }
        });
    }

    @NonNull
    @Override
    public MyRideViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.my_ride_item, viewGroup, false);
        return new MyRideViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyRideViewHolder myRideViewHolder, int i) {
        dialog = new ProgressDialog(context);
        dialog.setMessage("Loading..");
        myRideViewHolder.itemView.setTag(i);
        myRideViewHolder.car.setText(rides.get(i).getUser().getCarName());
        myRideViewHolder.fromDate.setText(rides.get(i).getRequestTime());
        myRideViewHolder.fromPlace.setText(rides.get(i).getAddress());
        myRideViewHolder.toDate.setText(rides.get(i).getRequestTime());
        myRideViewHolder.toPlace.setText(rides.get(i).getAddressTo());
        myRideViewHolder.name.setText(rides.get(i).getUser().getName());
        //myRideViewHolder.profileImage.setImageResource(rides.get(i).getImage());
        if (URLUtil.isValidUrl(rides.get(i).getUser().getImage()))
            Picasso.get().load(rides.get(i).getUser().getImage()).fit().centerCrop().into(myRideViewHolder.profileImage);
        else {
            Picasso.get().load("http://www.cta3.com/waslabank/prod_img/" + rides.get(i).getUser().getImage()).fit().centerCrop().into(myRideViewHolder.profileImage);
        }
        //myRideViewHolder.seats.setText(rides.get(i).getNumOfSeats());

/*
        rides.get(i).getStatus()
*/

        //
        myRideViewHolder.timeExpected.setText(rides.get(i).getRequestTime());
        myRideViewHolder.exactDate.setText(rides.get(i).getRequestDate());
        //myRideViewHolder.distance.setText(rides.get(i).getDistance());
        if (rides.get(i).getStatus().equals("4")) {
            myRideViewHolder.state.setText(context.getString(R.string.Finished));
            myRideViewHolder.state.setBackgroundResource(R.drawable.bg_rectangle_my_rides);
        } else if (rides.get(i).getStart().equals("1")) {
            myRideViewHolder.state.setText(context.getString(R.string.RideStarted));
            myRideViewHolder.state.setBackgroundResource(R.drawable.bg_rectangle_my_rides_orange);

        } else if (rides.get(i).getFromId().equals("0") || rides.get(i).getUserId().equals("0")) {
            myRideViewHolder.state.setBackgroundResource(R.drawable.bg_rectangle_my_rides);
            myRideViewHolder.state.setText(context.getString(R.string.SingleRide));

        } else {
            myRideViewHolder.state.setText(context.getString(R.string.SharedRide));
            myRideViewHolder.state.setBackgroundResource(R.drawable.bg_rectangle_my_rides_orange);

        }


        if (rides.get(i).isUpcoming() && rides.get(i).getStart().equals("0")) {
            myRideViewHolder.startRide.setVisibility(View.VISIBLE);
        } else {
            myRideViewHolder.startRide.setVisibility(View.INVISIBLE);
        }

        if (!rides.get(i).isUpcoming()) {
            myRideViewHolder.startRide.setVisibility(View.GONE);
        }
        if ((rides.get(i).getUserId().equals(Helper.getUserSharedPreferences(context).getId()))&&rides.get(i).getStart().equals("0")) {

        }else {
            myRideViewHolder.startRide.setText(context.getString(R.string.open_map));
        }
        myRideViewHolder.startRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myRideViewHolder.startRide.getText().equals(context.getString(R.string.open_map))) {
                    //   finishTrip(rides.get(i).getId());
                    // dialog.show()

                    context.startActivity(new Intent(context, LiveLocationMapsActivity.class)
                            .putExtra("Request_id", "" + rides.get(i).getId()));
                } else {
                    mConnector.getRequest(TAG, "http://www.cta3.com/waslabank/api/start_request?id=" + rides.get(i).getId());
                    if (rides.get(i).getFromId().equals("0") || rides.get(i).getUserId().equals("0")) {
                    } else {
                        myRideViewHolder.startRide.setText(context.getString(R.string.open_map));
                    }
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return rides.size();
    }

    class MyRideViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.profile_image)
        ImageView profileImage;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.car)
        TextView car;
        @BindView(R.id.from_date)
        TextView fromDate;
        @BindView(R.id.from_place)
        TextView fromPlace;
        @BindView(R.id.to_date)
        TextView toDate;
        @BindView(R.id.to_place)
        TextView toPlace;
        @BindView(R.id.seats)
        TextView seats;
        @BindView(R.id.distance)
        TextView distance;
        @BindView(R.id.exact_date)
        TextView exactDate;
        @BindView(R.id.time_expected)
        TextView timeExpected;
        @BindView(R.id.state)
        TextView state;
        @BindView(R.id.start_ride_button)
        Button startRide;

        MyRideViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClicked.setOnItemClicked(getAdapterPosition());
        }
    }


    public interface OnItemClicked {
        void setOnItemClicked(int position);
    }

    private void finishTrip(String id) {
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

                }
            }

            @Override
            public void onFailure(Call<StatusModel> call, Throwable t) {
                dialog.dismiss();

            }
        });
    }

}
