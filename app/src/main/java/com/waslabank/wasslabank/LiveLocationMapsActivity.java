package com.waslabank.wasslabank;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;

import androidx.annotation.NonNull;

import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.waslabank.wasslabank.models.LatLngModel;
import com.waslabank.wasslabank.models.RequestModel;
import com.waslabank.wasslabank.models.StatusModel;
import com.waslabank.wasslabank.networkUtils.Connector;
import com.waslabank.wasslabank.utils.Helper;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LiveLocationMapsActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    private ArrayList<Polyline> polylines;
    LatLng start, end, userPoin, car;
    int countr = 0;
    Button picked,finished;
    String Request_id, user_id = "";
    String driver_id = "";
    MarkerOptions options1, options2, options3, options4;
    Marker markerUser, markerUpdated, markerStart, markerEnd;
    Marker currentMarker = null;
    FirebaseDatabase database;
    DatabaseReference myRef;
    private ProgressDialog dialog;
    LatLngModel latLngModel;
    RequestModel requestModel;
    Location lastLocation;

    private static final String TAG = "LiveLocationMapsActivit";

    Connector mConnectorRate;

    float mRatingNumber;
    AlertDialog alertDialog;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark, R.color.colorPrimary
            , R.color.colorPrimarylight, R.color.colorAccent, R.color.primary_dark_material_light};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_live_location_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        user_id = Helper.getUserSharedPreferences(this).getId();
        picked = findViewById(R.id.picked);
        finished = findViewById(R.id.finish);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait..");
        finished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                dialog.show();
//                finishTrip(Request_id, "");
                show();
            }
        });
        Request_id = getIntent().getStringExtra("Request_id");
        picked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickUser(""+user_id,""+Request_id);
                markerUser.remove();
                markerStart.remove();
                drawRoute1(car,end,null,null);
            }
        });


        mConnectorRate = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                alertDialog.dismiss();
                                dialog.show();
               finishTrip(Request_id, "");
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                alertDialog.dismiss();
                Helper.showSnackBarMessage(getString(R.string.error), LiveLocationMapsActivity.this);
            }
        });
        mapFragment.getMapAsync(this);
        polylines = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Trips");


    }

    private void finishTrip(String id, String status) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Connector.connectionServices.BaseURL)
                .addConverterFactory(GsonConverterFactory
                        .create(new Gson())).build();
        Connector.connectionServices connectionService =
                retrofit.create(Connector.connectionServices.class);

        Location start = new Location("");
        start.setLatitude(this.start.latitude);
        start.setLongitude(this.start.longitude);

        connectionService.update_request_status(String.valueOf(lastLocation.getLongitude()),String.valueOf(lastLocation.getLatitude()),Helper.getUserSharedPreferences(this).getId(),id + "","4", String.valueOf(start.distanceTo(lastLocation) / 1000.0) , "extra").enqueue(new Callback<StatusModel>() {
            @Override
            public void onResponse(Call<StatusModel> call, Response<StatusModel> response) {
                dialog.dismiss();
                StatusModel statusModel = response.body();
                if (statusModel.getStatus()) {
                    //finish();
                    startActivity(new Intent(LiveLocationMapsActivity.this, WhereYouGoActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                }else{
                    Toast.makeText(LiveLocationMapsActivity.this, "false"+id, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StatusModel> call, Throwable t) {
                dialog.dismiss();

                Toast.makeText(LiveLocationMapsActivity.this, "faslse ++" +t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        options1 = new MarkerOptions();
        options2 = new MarkerOptions();
        options3 = new MarkerOptions();
        options4 = new MarkerOptions();
        getIndividualRequest("" + Request_id);


        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        // getting GPS status
        boolean isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        String provider;
        if (!isGPSEnabled) {
            provider = LocationManager.NETWORK_PROVIDER;
            Log.d("TTT", "onMapReady:network prodvider " + provider);
        } else {
            provider = LocationManager.GPS_PROVIDER;
            Log.d("TTT", "onMapReady:GPS prodvider " + provider);

        }

        locationManager.requestLocationUpdates(provider, 0, 1, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lastLocation = location;
                //userID -->Da el swaa2
                Log.d("TTT", "Over all onLocationChanged: lat-->" + location.getLatitude() + " long--> " + location.getLongitude());

                if (driver_id.equals(user_id)) {
                    Log.d("TTT", "onLocationChanged: lat-->" + location.getLatitude() + " long--> " + location.getLongitude());
                    sendNewLocation("" + user_id, "" + location.getLatitude(), "" + location.getLongitude(), "",Request_id);
                    options4.position(new LatLng(location.getLatitude(), location.getLongitude())).title("Car Position");
                    markerUpdated.remove();
                    markerUpdated = mMap.addMarker(options4);
                    markerUpdated.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.update));
                    CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                    //mMap.animateCamera(center);
                    latLngModel = new LatLngModel(location.getLatitude() + "", "" + location.getLongitude());
                    //myRef.child(driver_id).setValue(latLngModel);
                    myRef.child(driver_id).child("lat").setValue(latLngModel.getLat());
                    myRef.child(driver_id).child("lng").setValue(latLngModel.getLng());
                }
               /* Log.d("TTT", "onLocationChanged: lat-->"+location.getLatitude()+" long--> "+location.getLongitude());
                sendNewLocation("1",""+location.getLatitude(),""+location.getLongitude(),"");
                options3.position(new LatLng(location.getLatitude(), location.getLongitude()));
                markerUser.remove();
                mMap.addMarker(options3);
                CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                mMap.moveCamera(center);
*/
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("TTT", "onStatusChanged: " + provider + status);

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });
//get realtime Position firebase


    }

    public void drawRoute1(LatLng start,LatLng point,LatLng point2, LatLng end) {
        if (point2 == null) {
            Routing routing1 = new Routing.Builder()
                    .travelMode(Routing.TravelMode.DRIVING)
                    .withListener(this)
                    .waypoints(start, point)
                    .key("AIzaSyCE29pCYj3ntftgARbTP0FA8xZyLBCF7f8")
                    .build();
            routing1.execute();
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(start));
            Routing routing1 = new Routing.Builder()
                    .travelMode(Routing.TravelMode.DRIVING)
                    .withListener(this)
                    .waypoints(start, point, point2, end)
                    .key("AIzaSyCE29pCYj3ntftgARbTP0FA8xZyLBCF7f8")
                    .build();
            routing1.execute();
        }
    }

    ///Routing Lisenrs
    @Override
    public void onRoutingFailure(RouteException e) {
        e.printStackTrace();
        Routing routing1 = new Routing.Builder()
                .travelMode(Routing.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(start, car, end)
                .key("AIzaSyCE29pCYj3ntftgARbTP0FA8xZyLBCF7f8")
                .build();
        routing1.execute();
        if (e != null) {
            // Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("TTT", "onRoutingFailure: " + e.getMessage());
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if (polylines.size() >= 1) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.width(5);
            polyOptions.color(Color.parseColor("#27B0D5"));
            polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

    }

    @Override
    public void onRoutingCancelled() {

    }

    private void getIndividualRequest(String id) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Connector.connectionServices.BaseURL)
                .addConverterFactory(GsonConverterFactory
                        .create(new Gson())).build();
        Connector.connectionServices connectionService =
                retrofit.create(Connector.connectionServices.class);
//helper.getuserSharedprefe;
        connectionService.get_request(id + "").enqueue(new Callback<StatusModel>() {
            @Override
            public void onResponse(Call<StatusModel> call, Response<StatusModel> response) {
                StatusModel statusModel = response.body();

                if (statusModel.getStatus()) {
                    requestModel = response.body().getRequest();
                    // Log.d("TTTT", "onResponse: lat-->" + statusModel.getRequest().getLatitude() + "long-->" + statusModel.getRequest().getLongitude());

                    if(statusModel.getRequest().getUserId()
                            .equals(Helper.getUserSharedPreferences(LiveLocationMapsActivity.this).getId())){
                        if (statusModel.getRequest().getPicked().equals("0") && statusModel.getRequest().getStart().equals("1")) {
                            picked.setVisibility(View.VISIBLE);
                        }
                    }/*else {
                        picked.setVisibility(View.VISIBLE);

                    }*/

                    start = new LatLng(Double.parseDouble(statusModel.getRequest().getLatitude())
                            , Double.parseDouble(statusModel.getRequest().getLongitude()));
                    lastLocation = new Location("");
                    lastLocation.setLongitude(start.longitude);
                    lastLocation.setLatitude(start.latitude);
                    userPoin = new LatLng(Double.parseDouble(statusModel.getRequest().getUserLatitude())
                            , Double.parseDouble(statusModel.getRequest().getUserLongitude()));
                    end = new LatLng(Double.parseDouble(statusModel.getRequest().getLatitudeTo())
                            , Double.parseDouble(statusModel.getRequest().getLongitudeTo()));
                    car = new LatLng(Double.parseDouble(statusModel.getRequest().getLatitudeUpdate())
                            , Double.parseDouble(statusModel.getRequest().getLongitudeUpdate()));

                    options4.position(car);

                    options1.position(start);

                    CameraUpdate Sart = CameraUpdateFactory.newLatLngZoom(start, 12.0f);
                    mMap.moveCamera(Sart);

                    /*userPoin = new LatLng(Double.parseDouble(statusModel.getRequest().getUserLatitude())
                            , Double.parseDouble(statusModel.getRequest().getUserLongitude()));*/

                    options2.position(end);
                    options3.position(userPoin);
                    ////
                    markerStart = mMap.addMarker(options1);
                    markerStart.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.start));
                    markerUser = mMap.addMarker((options3).title("User Point"));
                    markerUser.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.user));
                    markerEnd = mMap.addMarker((options2).title("End Point"));
                    markerEnd.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.endpoint));
                    markerUpdated = mMap.addMarker(options4);
                    markerUpdated.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.update));

                    driver_id = statusModel.getRequest().getUserId();
                    if (statusModel.getRequest().getPicked().equals("1")) {
                        picked.setVisibility(View.GONE);
                        finished.setVisibility(View.VISIBLE);
                        markerStart.remove();
                        markerUser.remove();
                    }

                    if (statusModel.getRequest().getFromId().equals(user_id)) {
                        picked.setVisibility(View.GONE);
                        finished.setVisibility(View.GONE);
                    }
                    /////
                    Log.d("TTTT", "onResponse start: lat-->" + start.latitude + "long-->" + start.longitude);
                    //
                    Log.d("TTTT", "onResponse end: lat-->" + end.latitude + "long-->" + end.longitude);
                    //
                    Log.d("TTTT", "onResponse user: lat-->" + userPoin.latitude + "long-->" + userPoin.longitude);

                    if (response.body().getRequest().getPicked().equals("1")) {
                        drawRoute1(car, end, null, null);
                    } else {
                        drawRoute1(start, userPoin, car, end);
                    }
                    updatedData();
                    /*

                     */

                } else {
                    Toast.makeText(LiveLocationMapsActivity.this, "" + getString(R.string.thereIsNoRides), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StatusModel> call, Throwable t) {
                Toast.makeText(LiveLocationMapsActivity.this, "" + getString(R.string.PleaseCheckyourInternetConnection), Toast.LENGTH_SHORT).show();


            }
        });
    }

    private void pickUser(String userid,String id){
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(Connector.connectionServices.BaseURL)
            .addConverterFactory(GsonConverterFactory
                    .create(new Gson())).build();
        Connector.connectionServices connectionService =
                retrofit.create(Connector.connectionServices.class);

        connectionService.update_request_status(""+userid,"1",""+id).enqueue(new Callback<StatusModel>() {
            @Override
            public void onResponse(Call<StatusModel> call, Response<StatusModel> response) {
                StatusModel statusModel1 = response.body();
                if (statusModel1.getStatus()) {
                    picked.setVisibility(View.GONE);
                    finished.setVisibility(View.VISIBLE);
                    markerStart.remove();
                    markerUser.remove();
                    Toast.makeText(LiveLocationMapsActivity.this, "Picked", Toast.LENGTH_SHORT).show();
                    Log.d("TTT", "onResponse: On Updated");

                }
            }

            @Override
            public void onFailure(Call<StatusModel> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }
    private void sendNewLocation(String userid, String lati, String longt, String pickeed,String id) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Connector.connectionServices.BaseURL)
                .addConverterFactory(GsonConverterFactory
                        .create(new Gson())).build();
        Connector.connectionServices connectionService =
                retrofit.create(Connector.connectionServices.class);

        connectionService.update_request_status(longt+"", lati+"", userid+"", id+"").enqueue(new Callback<StatusModel>() {

            @Override
            public void onResponse(Call<StatusModel> call, Response<StatusModel> response) {
                StatusModel statusModel1 = response.body();
                Log.d("TTT", "onResponse: On method");
                if (statusModel1.getStatus()) {
                    //picked.setVisibility(View.GONE);
                    Log.d("TTT", "onResponse: On Updated");

                }
            }

            @Override
            public void onFailure(Call<StatusModel> call, Throwable t) {
                Log.d("TTT", "onResponse: On faild" + t.getMessage());


            }
        });
    }

    private void updatedData() {
        myRef.child(driver_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                   // Log.d("TTTT", "onDataChange: "+dataSnapshot.getValue());
                    String lat = (String) dataSnapshot.child("lat").getValue();
                    String lng = (String) dataSnapshot.child("lng").getValue();
                    LatLngModel latLngModel = new LatLngModel(lat,lng);
                   // Log.d("TTTT", "onDataChange: "+latLngModel.getLat()+"teeest");
                    if (latLngModel != null && latLngModel.getLat() != null) {
                        markerUpdated.remove();
                        options4.position(new LatLng(Double.parseDouble(latLngModel.getLat() + "")
                                , Double.parseDouble(latLngModel.getLng())));
                        markerUpdated = mMap.addMarker(options4);
                        markerUpdated.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.update));
                    }
                    if(markerStart.isVisible()){
                        //markerStart.setVisible(false);
                        //markerUser.setVisible(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("TTT", "Failed to read value.", databaseError.toException());


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
                    Helper.showSnackBarMessage(getString(R.string.enter_comment), LiveLocationMapsActivity.this);
                } else {
                    if (requestModel != null) {
                        if (Helper.getUserSharedPreferences(LiveLocationMapsActivity.this).getId().equals(requestModel.getFromId())) {
                            if (getIntent() != null && getIntent().hasExtra("ride_2")) {
                                mConnectorRate.getRequest(TAG, "http://www.as.cta3.com/waslabank/api/add_comment?comment=" + Uri.encode(commentText) + "&rating=" + mRatingNumber + "&request_id=" + requestModel.getId() + "&from_id=" + Helper.getUserSharedPreferences(LiveLocationMapsActivity.this).getId() + "&user_id=" + requestModel.getUserId());
                            } else {
                                mConnectorRate.getRequest(TAG, "http://www.as.cta3.com/waslabank/api/add_comment?comment=" + Uri.encode(commentText) + "&rating=" + mRatingNumber + "&request_id=" + requestModel.getId() + "&from_id=" + Helper.getUserSharedPreferences(LiveLocationMapsActivity.this).getId() + "&user_id=" + requestModel.getUserId());

                            }
                        } else {
                            if (getIntent() != null && getIntent().hasExtra("ride_2")) {
                                mConnectorRate.getRequest(TAG, "http://www.as.cta3.com/waslabank/api/add_comment?comment=" + Uri.encode(commentText) + "&rating=" + mRatingNumber + "&request_id=" + requestModel.getId() + "&from_id=" + Helper.getUserSharedPreferences(LiveLocationMapsActivity.this).getId() + "&user_id=" + requestModel.getFromId());
                            } else {
                                mConnectorRate.getRequest(TAG, "http://www.as.cta3.com/waslabank/api/add_comment?comment=" + Uri.encode(commentText) + "&rating=" + mRatingNumber + "&request_id=" + requestModel.getId() + "&from_id=" + Helper.getUserSharedPreferences(LiveLocationMapsActivity.this).getId() + "&user_id=" + requestModel.getFromId());

                            }
                        }
                    }
                }
            }
        });


    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
