package com.waslabank.wasslabank;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.model.LatLng;
import com.waslabank.wasslabank.networkUtils.Connector;
import com.waslabank.wasslabank.utils.GPSTracker;
import com.waslabank.wasslabank.utils.Helper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import se.arbitur.geocoding.Callback;
import se.arbitur.geocoding.Response;
import se.arbitur.geocoding.ReverseGeocoding;

public class WhereYouGoActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener
        , DatePickerDialog.OnDateSetListener, RoutingListener {

    private String mAddress, mCity, mCountry;

    private final String TAG = WhereYouGoActivity.class.getSimpleName();
    @BindView(R.id.find_a_ride_button)
    Button mFindARideButton;
    @BindView(R.id.post_a_ride_button)
    Button mPostARideButton;
    @BindView(R.id.current_location)
    EditText mCurrentLocationEditText;
    @BindView(R.id.to_location)
    EditText mToLocationEditText;
    @BindView(R.id.seats)
    EditText mSeatsEditText;
    @BindView(R.id.male_radio)
    RadioButton mMaleRadio;
    @BindView(R.id.female_radio)
    RadioButton mFemaleRadio;
    @BindView(R.id.date)
    TextView mDate;
    @BindView(R.id.time)
    TextView mTime;
    @BindView(R.id.menu)
    ImageView mMenuButton;
    @BindView(R.id.daily)
    CheckBox mDaily;
    @BindView(R.id.days)
    Spinner mDaysSpinner;
    @BindView(R.id.gender)
    RadioGroup mGender;

    Connector mConnectorPostRide;

    String mAddressFrom;
    String mCityFrom;
    String mCountryFrom;
    double mLatFrom = 0;
    double mLonFrom = 0;

    String mAddressTo;
    String mCityTo;
    String mCountryTo;
    double mLatTo = 0;
    double mLonTo = 0;
    double distance;

    ProgressDialog mProgressDialog;

    String mSelectedDay = null;

    int mSelectedGender = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_where_you_go);
        ButterKnife.bind(this);

        getCurrentLocation(this);
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        mDate.setText("" + date);

        ArrayList<String> carNames = new ArrayList<>();
        carNames.add(0, getString(R.string.enter_day));
        carNames.add("Saturday");
        carNames.add("Sunday");
        carNames.add("Monday");
        carNames.add("Tuesday");
        carNames.add("Wednesday");
        carNames.add("Thursday");
        carNames.add("Friday");
        ArrayAdapter adapterNames = new ArrayAdapter<String>(this, R.layout.spinner_item, carNames) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {

                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        adapterNames.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDaysSpinner.setAdapter(adapterNames);
        mDaysSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (carNames.size() > 1) {
                    mSelectedDay = String.valueOf(i - 1);
                    Helper.writeToLog(mSelectedDay);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mFindARideButton.setOnClickListener(view -> {
            if (mLonTo == 0) {
                Helper.showSnackBarMessage(getString(R.string.enter_destination), WhereYouGoActivity.this);
            } else if (mLatFrom == 0) {
                Helper.showSnackBarMessage(getString(R.string.enter_current_location), WhereYouGoActivity.this);
            } else {
                if (!mSeatsEditText.getText().toString().equals("")) {
                    if (Integer.parseInt(mSeatsEditText.getText().toString()) <= 0 || Integer.parseInt(mSeatsEditText.getText().toString()) > 4) {
                        Helper.showSnackBarMessage(getString(R.string.enter_valid_seats), WhereYouGoActivity.this);
                    } else {
                        startActivity(new Intent(WhereYouGoActivity.this, FindRideActivity.class)
                                .putExtra("latFrom", mLatFrom)
                                .putExtra("lonFrom", mLonFrom)
                                .putExtra("latTo", mLatTo)
                                .putExtra("lonTo", mLonTo)
                                .putExtra("Seats", Integer.parseInt(mSeatsEditText.getText().toString())));

                    }
                } else
                    startActivity(new Intent(WhereYouGoActivity.this, FindRideActivity.class).putExtra("latFrom", mLatFrom).putExtra("lonFrom", mLonFrom).putExtra("latTo", mLatTo).putExtra("lonTo", mLonTo));
            }
        });
        mPostARideButton.setOnClickListener(view -> {


            if (mDaily.isChecked()) {

                if (Helper.getUserSharedPreferences(WhereYouGoActivity.this).getStatus().equals("0")) {
                    startActivity(new Intent(WhereYouGoActivity.this, VerifyDriverAccountActivity.class));
                } else {
                    if (mCurrentLocationEditText.getText().toString().isEmpty()) {
                        Helper.showSnackBarMessage(getString(R.string.enter_current_location), WhereYouGoActivity.this);
                    } else if (mToLocationEditText.getText().toString().isEmpty()) {
                        Helper.showSnackBarMessage(getString(R.string.enter_destination), WhereYouGoActivity.this);
                    } else if (mTime.getText().toString().equals(getString(R.string.time))) {
                        Helper.showSnackBarMessage(getString(R.string.enter_time), WhereYouGoActivity.this);
                    } else if (mSelectedDay.equals("-1")) {
                        Helper.showSnackBarMessage(getString(R.string.enter_day), WhereYouGoActivity.this);
                    } else if (mSeatsEditText.getText().toString().equals("0") || mSeatsEditText.getText().toString().equals("٠")) {
                        Helper.showSnackBarMessage(getString(R.string.seats_zero), WhereYouGoActivity.this);
                    } else if (Integer.parseInt(mSeatsEditText.getText().toString()) < 0 || Integer.parseInt(mSeatsEditText.getText().toString()) > 4) {
                        Helper.showSnackBarMessage(getString(R.string.enter_valid_seats), WhereYouGoActivity.this);
                    } else {
                        mProgressDialog = Helper.showProgressDialog(WhereYouGoActivity.this, getString(R.string.loading), false);
                        Location fromLocation = new Location("From");
                        fromLocation.setLongitude(mLonFrom);
                        fromLocation.setLatitude(mLatFrom);

                        Location toLocation = new Location("To");
                        toLocation.setLongitude(mLonTo);
                        toLocation.setLatitude(mLatTo);

                        distance = fromLocation.distanceTo(toLocation) / 1000.0;
                        Routing routing = new Routing.Builder()
                                .travelMode(Routing.TravelMode.DRIVING)
                                .withListener(WhereYouGoActivity.this)
                                .waypoints(new LatLng(fromLocation.getLatitude(), fromLocation.getLongitude()), new LatLng(toLocation.getLatitude(), toLocation.getLongitude()))
                                .alternativeRoutes(false)
                                .key("AIzaSyAcazeBKVO9e7HvHB9ssU1jc9NhTj_AFsQ")
                                .build();
                        routing.execute();

                    }
                }
            } else {
                if (Helper.getUserSharedPreferences(WhereYouGoActivity.this).getStatus().equals("0")) {
                    startActivity(new Intent(WhereYouGoActivity.this, VerifyDriverAccountActivity.class));
                } else {
                    if (mCurrentLocationEditText.getText().toString().isEmpty()) {
                        Helper.showSnackBarMessage(getString(R.string.enter_current_location), WhereYouGoActivity.this);
                    } else if (mToLocationEditText.getText().toString().isEmpty()) {
                        Helper.showSnackBarMessage(getString(R.string.enter_destination), WhereYouGoActivity.this);
                    } else if (mDate.getText().toString().equals(getString(R.string.date))) {
                        Helper.showSnackBarMessage(getString(R.string.enter_date), WhereYouGoActivity.this);
                    } else if (mTime.getText().toString().equals(getString(R.string.time))) {
                        Helper.showSnackBarMessage(getString(R.string.enter_time), WhereYouGoActivity.this);
                    } else if (mSeatsEditText.getText().toString().equals("0") || mSeatsEditText.getText().toString().equals("٠") || mSeatsEditText.getText().toString().equals("")) {
                        Helper.showSnackBarMessage(getString(R.string.seats_zero), WhereYouGoActivity.this);
                    } else if (mSeatsEditText.getText().toString().equals("") && (Integer.parseInt(mSeatsEditText.getText().toString()) < 0 || Integer.parseInt(mSeatsEditText.getText().toString()) > 4)) {
                        Helper.showSnackBarMessage(getString(R.string.enter_valid_seats), WhereYouGoActivity.this);
                    } else {
                        Location fromLocation = new Location("From");
                        fromLocation.setLongitude(mLonFrom);
                        fromLocation.setLatitude(mLatFrom);

                        Location toLocation = new Location("To");
                        toLocation.setLongitude(mLonTo);
                        toLocation.setLatitude(mLatTo);

                        distance = fromLocation.distanceTo(toLocation) / 1000.0;
                        mProgressDialog = Helper.showProgressDialog(WhereYouGoActivity.this, getString(R.string.loading), false);
                        Routing routing = new Routing.Builder()
                                .travelMode(Routing.TravelMode.DRIVING)
                                .withListener(WhereYouGoActivity.this)
                                .waypoints(new LatLng(fromLocation.getLatitude(), fromLocation.getLongitude()), new LatLng(toLocation.getLatitude(), toLocation.getLongitude()))
                                .alternativeRoutes(false)
                                .key("AIzaSyAcazeBKVO9e7HvHB9ssU1jc9NhTj_AFsQ")
                                .build();
                        routing.execute();
                    }
                }
            }

        });
        mCurrentLocationEditText.setOnClickListener(view -> {
            startActivityForResult(new Intent(WhereYouGoActivity.this, MapActivity.class), 1);
        });
        mToLocationEditText.setOnClickListener(view -> startActivityForResult(new Intent(WhereYouGoActivity.this, MapActivity.class), 2));
        mTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                new TimePickerDialog(WhereYouGoActivity.this, WhereYouGoActivity.this, hour, minute,
                        DateFormat.is24HourFormat(WhereYouGoActivity.this)).show();
            }
        });

        mDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(WhereYouGoActivity.this, WhereYouGoActivity.this, year, month, day);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });

        mConnectorPostRide = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response)) {
                    Toast.makeText(WhereYouGoActivity.this, getString(R.string.posted), Toast.LENGTH_LONG).show();
                    mCurrentLocationEditText.setText("");
                    mToLocationEditText.setText("");
                    mDate.setText(getString(R.string.date));
                    mTime.setText(getString(R.string.time));
                    mSeatsEditText.setText("");
                    mGender.clearCheck();
                    mSelectedGender = -1;
                    mSelectedDay = "-1";
                    mDaysSpinner.setSelection(0);
                    mDaily.setChecked(false);
                    Log.d(TAG, "onComplete: " + response);
                    startActivity(new Intent(WhereYouGoActivity.this, MyRidesActivity.class));
                } else {

                    Helper.showSnackBarMessage(getString(R.string.error) + Connector.getMessage(response), WhereYouGoActivity.this);
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();
                Helper.showSnackBarMessage(getString(R.string.error), WhereYouGoActivity.this);
            }
        });

        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WhereYouGoActivity.this, MoreActivity.class));
            }
        });

       /* mMaleRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    mSelectedGender = 0;

            }
        });*/
        mMaleRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMaleRadio.isChecked() && mSelectedGender == 0) {
                    mMaleRadio.setChecked(false);
                    mSelectedGender = -1;
                } else {
                    mMaleRadio.setChecked(true);
                    mSelectedGender = 0;

                }
            }
        });

       /* mFemaleRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    mSelectedGender = 1;
              *//*  if (mFemaleRadio.isChecked()) {
                    mFemaleRadio.setChecked(false);
                }*//*

            }
        });*/
        mFemaleRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mFemaleRadio.isChecked() && mSelectedGender == 1) {
                    mFemaleRadio.setChecked(false);
                    mSelectedGender = -1;
                } else {
                    mFemaleRadio.setChecked(true);
                    mSelectedGender = 1;

                }

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    mAddressFrom = data.getStringExtra("address");
                    mCityFrom = data.getStringExtra("city");
                    mCountryFrom = data.getStringExtra("country");
                    mLatFrom = data.getDoubleExtra("lat", 0);
                    mLonFrom = data.getDoubleExtra("lon", 0);
                    mCurrentLocationEditText.setText(mAddressFrom);
                }
            }
        } else if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    mAddressTo = data.getStringExtra("address");
                    mCityTo = data.getStringExtra("city");
                    mCountryTo = data.getStringExtra("country");
                    mLatTo = data.getDoubleExtra("lat", 0);
                    mLonTo = data.getDoubleExtra("lon", 0);
                    mToLocationEditText.setText(mAddressTo);
                }
            }
        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        String hour = Integer.toString(i);
        String min = Integer.toString(i1);

        if (i < 10)
            hour = "0" + hour;
        if (i1 < 10)
            min = "0" + min;

        mTime.setText(hour + ":" + min);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {

        String monthString = Integer.toString(month);
        String dayString = Integer.toString(month);


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);


        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        Date dateRepresentation = cal.getTime();

        mDate.setText(dateFormat.format(dateRepresentation));
    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {
        String duration = String.valueOf((arrayList.get(i).getDurationValue()) / 60) + " M";
        if (mDaily.isChecked()) {
            mConnectorPostRide.getRequest(TAG, "http://www.cta3.com/waslabank/api/add_daily?user_id=" + Helper.getUserSharedPreferences(WhereYouGoActivity.this).getId() + "&longitude=" + mLonFrom + "&latitude=" + mLatFrom + "&city_id=" + Uri.encode(mCityFrom) + "&address=" + Uri.encode(mAddressFrom) + "&time=" + Uri.encode(mTime.getText().toString()) + "&latitude_to=" + mLatTo + "&city_id_to=" + Uri.encode(mCityTo) + "&address_to=" + Uri.encode(mAddressTo) + "&longitude_to=" + mLonTo + "&weekday=" + mSelectedDay + "&seats=" + Uri.encode(mSeatsEditText.getText().toString()) + "&gender=" + mSelectedGender + "&ref_id=" + Helper.getUserSharedPreferences(WhereYouGoActivity.this).getRefId() + "&distance=" + Uri.encode(String.valueOf(distance)) + "&duration=" + Uri.encode(duration));
        } else {
            mConnectorPostRide.getRequest(TAG, "http://www.cta3.com/waslabank/api/add_request?user_id=" + Helper.getUserSharedPreferences(WhereYouGoActivity.this).getId() + "&longitude=" + mLonFrom + "&latitude=" + mLatFrom + "&city_id=" + Uri.encode(mCityFrom) + "&address=" + Uri.encode(mAddressFrom) + "&time=" + Uri.encode(mTime.getText().toString()) + "&date=" + Uri.encode(mDate.getText().toString()) + "&latitude_to=" + mLatTo + "&city_id_to=" + Uri.encode(mCityTo) + "&address_to=" + Uri.encode(mAddressTo) + "&longitude_to=" + mLonTo + "&seats=" + Uri.encode(mSeatsEditText.getText().toString()) + "&gender=" + mSelectedGender + "&ref_id=" + Helper.getUserSharedPreferences(WhereYouGoActivity.this).getRefId() + "&distance=" + Uri.encode(String.valueOf(distance)) + "&duration=" + Uri.encode(duration));
        }
    }

    @Override
    public void onRoutingCancelled() {

    }

    public void getAddress(LatLng latLng) {
        new ReverseGeocoding(latLng.latitude, latLng.longitude, "AIzaSyATc3Nte8Pj1oWTFKAbLWUiJbzSIJEDzxc")
                .setLanguage("en")
                .fetch(new Callback() {
                    @Override
                    public void onResponse(Response response) {
                        mProgressDialog.dismiss();
                        mAddress = response.getResults()[0].getFormattedAddress();
                        mAddressFrom = response.getResults()[0].getFormattedAddress();
                        for (int i = 0; i < response.getResults()[0].getAddressComponents().length; i++) {
                            for (int j = 0; j < response.getResults()[0].getAddressComponents()[i].getAddressTypes().length; j++) {
                                switch (response.getResults()[0].getAddressComponents()[i].getAddressTypes()[j]) {
                                    case "administrative_area_level_1":
                                        mCity = response.getResults()[0].getAddressComponents()[i].getLongName();
                                        mCityFrom = response.getResults()[0].getAddressComponents()[i].getLongName();
                                        break;
                                    case "country":
                                        mCountry = response.getResults()[0].getAddressComponents()[i].getLongName();
                                        mCountryFrom = response.getResults()[0].getAddressComponents()[i].getLongName();
                                        break;
                                }
                            }
                        }
                        if (mAddress == null) {
                            mAddress = "";
                            mAddressFrom = "";
                        }
                        if (mCity == null) {
                            mCity = "";
                            mCityFrom = "";
                        }
                        if (mCountry == null) {
                            mCountry = "";
                            mCountryFrom = "";
                        }
                        mCurrentLocationEditText.setText(mAddress);
                    }

                    @Override
                    public void onFailed(Response response, IOException e) {
                        mProgressDialog.dismiss();

                    }
                });

    }

    private void getCurrentLocation(Context context) {
        Log.d("Location", "my location is " + "tessssst");
        mProgressDialog = Helper.showProgressDialog(WhereYouGoActivity.this, getString(R.string.loading), false);

        SingleShotLocationProvider.requestSingleUpdate(context,
                new SingleShotLocationProvider.LocationCallback() {
                    @Override
                    public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                        Log.d("Location", "my location is " + location.toString());
                        Log.d("Location", "my location is " + location.toString() + "lat-->" + location.latitude
                                + "--long-->" + location.longitude);
                        mLatFrom = location.latitude;
                        mLonFrom = location.longitude;
                        mCurrentLocationEditText.setText(mAddressFrom);
                        LatLng latLng = new LatLng(location.latitude, location.longitude);
                        getAddress(latLng);
                    }
                });

    }

}
