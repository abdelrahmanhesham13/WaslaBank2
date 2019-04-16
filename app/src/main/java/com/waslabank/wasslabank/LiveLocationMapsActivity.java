package com.waslabank.wasslabank;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
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
import com.waslabank.wasslabank.models.StatusModel;
import com.waslabank.wasslabank.networkUtils.Connector;
import com.waslabank.wasslabank.utils.Helper;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LiveLocationMapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    private ArrayList<Polyline> polylines;
    LatLng start, end, userPoin, car;
    int countr = 0;
    Button picked;
    String Request_id, user_id = "";
    String driver_id = "";
    MarkerOptions options1, options2, options3, options4;
    Marker markerUser, markerUpdated, markerStart, markerEnd;
    Marker currentMarker = null;
    FirebaseDatabase database;
    DatabaseReference myRef;
    LatLngModel latLngModel;
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
        Request_id = getIntent().getStringExtra("Request_id");
        picked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickUser(""+user_id,""+Request_id);
                markerUser.remove();
                markerStart.remove();
            }
        });
        mapFragment.getMapAsync(this);
        polylines = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Trips");


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

        locationManager.requestLocationUpdates(provider, 0, 50, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
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
                    mMap.moveCamera(center);
                    latLngModel = new LatLngModel(location.getLatitude() + "", "" + location.getLongitude());
                    myRef.child(driver_id).setValue(latLngModel);

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

    public void drawRoute(LatLng start, LatLng end) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(start));
        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(start, end)
                .key("AIzaSyAcazeBKVO9e7HvHB9ssU1jc9NhTj_AFsQ")
                .build();
        routing.execute();
    }

    public void drawRoute1(LatLng start, LatLng end) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(start));
        Routing routing1 = new Routing.Builder()
                .travelMode(Routing.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(start, end)
                .key("AIzaSyAcazeBKVO9e7HvHB9ssU1jc9NhTj_AFsQ")
                .build();
        routing1.execute();
    }

    ///Routing Lisenrs
    @Override
    public void onRoutingFailure(RouteException e) {
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

        if (polylines.size() > 1) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

        }
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
                    // Log.d("TTTT", "onResponse: lat-->" + statusModel.getRequest().getLatitude() + "long-->" + statusModel.getRequest().getLongitude());

                    if(statusModel.getRequest().getUserId()
                            .equals(Helper.getUserSharedPreferences(LiveLocationMapsActivity.this).getId())){
                        if (statusModel.getRequest().getPicked().equals("0")) {
                            picked.setVisibility(View.VISIBLE);
                        }
                    }
                    driver_id = statusModel.getRequest().getUserId();
                    if (statusModel.getRequest().getPicked().equals("1")) {
                        picked.setVisibility(View.GONE);
                    }/*else {
                        picked.setVisibility(View.VISIBLE);

                    }*/
                    if (statusModel.getRequest().getFromId().equals(user_id)) {
                        picked.setVisibility(View.GONE);
                    }
                    start = new LatLng(Double.parseDouble(statusModel.getRequest().getLatitude())
                            , Double.parseDouble(statusModel.getRequest().getLongitude()));
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
                    /////
                    Log.d("TTTT", "onResponse start: lat-->" + start.latitude + "long-->" + start.longitude);
                    //
                    Log.d("TTTT", "onResponse end: lat-->" + end.latitude + "long-->" + end.longitude);
                    //
                    Log.d("TTTT", "onResponse user: lat-->" + userPoin.latitude + "long-->" + userPoin.longitude);

                    drawRoute(start, userPoin);
                    drawRoute1(userPoin, end);
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
                    Toast.makeText(LiveLocationMapsActivity.this, "Picked", Toast.LENGTH_SHORT).show();
                    Log.d("TTT", "onResponse: On Updated");

                }
            }

            @Override
            public void onFailure(Call<StatusModel> call, Throwable t) {

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
                    Toast.makeText(LiveLocationMapsActivity.this, "Updated", Toast.LENGTH_SHORT).show();
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
                    LatLngModel latLngModel = dataSnapshot.getValue(LatLngModel.class);
                   // Log.d("TTTT", "onDataChange: "+latLngModel.getLat()+"teeest");
                    if (latLngModel != null && latLngModel.getLat() != null) {
                        markerUpdated.remove();
                        options4.position(new LatLng(Double.parseDouble(latLngModel.getLat() + "")
                                , Double.parseDouble(latLngModel.getLng())));
                        markerUpdated = mMap.addMarker(options4);
                        markerUpdated.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.update));
                    }
                    if(markerStart.isVisible()){
                        markerStart.setVisible(false);
                        markerUser.setVisible(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("TTT", "Failed to read value.", databaseError.toException());


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
