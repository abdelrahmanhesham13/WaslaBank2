package com.waslabank.wasslabank.networkUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.waslabank.wasslabank.models.CarModel;
import com.waslabank.wasslabank.models.ChatModel;
import com.waslabank.wasslabank.models.ColorModel;
import com.waslabank.wasslabank.models.DailyRideModel;
import com.waslabank.wasslabank.models.GroupMessagesResponseModel;
import com.waslabank.wasslabank.models.GroupsResponseModel;
import com.waslabank.wasslabank.models.MessageModel;
import com.waslabank.wasslabank.models.MyRideModel;
import com.waslabank.wasslabank.models.NotificationModel;
import com.waslabank.wasslabank.models.ReviewModel;
import com.waslabank.wasslabank.models.RideModel;
import com.waslabank.wasslabank.models.SingleRequestModel.Example;
import com.waslabank.wasslabank.models.StatusModel;
import com.waslabank.wasslabank.models.StatusResponseModel;
import com.waslabank.wasslabank.models.UpdateToken;
import com.waslabank.wasslabank.models.UserModel;
import com.waslabank.wasslabank.utils.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Abdelrahman Hesham on 3/13/2018.
 */

public class Connector {

    private Context mContext;
    private LoadCallback mLoadCallback;
    private ErrorCallback mErrorCallback;
    private RequestQueue mQueue;
    private Map<String, String> mMap;


    public interface LoadCallback {

        void onComplete(String tag, String response);

    }

    public interface ErrorCallback {

        void onError(VolleyError error);

    }

    public Connector(Context mContext, LoadCallback mLoadCallback, ErrorCallback mErrorCallback) {
        this.mContext = mContext;
        this.mLoadCallback = mLoadCallback;
        this.mErrorCallback = mErrorCallback;
    }


    public void getRequest(final String tag, final String url) {
        Helper.writeToLog(url);
        String response = "";
        if (isOnline(mContext)) {
            mQueue = Volley.newRequestQueue(mContext);
            StringRequest mStringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Helper.writeToLog(response);
                            mLoadCallback.onComplete(tag, response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    mErrorCallback.onError(error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    return mMap;
                }
            };
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    50000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mStringRequest.setTag(tag);
            mQueue.add(mStringRequest);
        } else {
            mErrorCallback.onError(new NoConnectionError());
        }


    }

    public void cancelAllRequests(final String tag) {
        if (mQueue != null) {
            mQueue.cancelAll(tag);
        }
    }


    public static boolean checkStatus(String response) {
        boolean status = false;
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                status = jsonObject.optBoolean("status");
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return status;
    }

    public static String getMessage(String response) {
        String message = null;
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                message = jsonObject.optString("message");
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return message;
    }


    public static boolean checkImages(String response) {
        boolean status = false;
        JSONArray images;
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                images = jsonObject.optJSONArray("images");
                status = true;
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return status;
    }

    public static ArrayList<String> getImages(String response) {
        ArrayList<String> imagesPaths = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray images = jsonObject.optJSONArray("images");
                for (int i = 0; i < images.length(); i++) {
                    imagesPaths.add(images.getString(i));
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return imagesPaths;
    }


    public static ArrayList<CarModel> getCars(String response) {
        ArrayList<CarModel> carModels = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray cars = jsonObject.optJSONArray("datas");
                for (int i = 0; i < cars.length(); i++) {
                    JSONObject car = cars.getJSONObject(i);
                    String name = car.getString("name");
                    String id = car.getString("id");
                    String nameAr = car.getString("name_ar");
                    carModels.add(new CarModel(id, name, nameAr));
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return carModels;
    }


    public static ArrayList<ColorModel> getColors(String response) {
        ArrayList<ColorModel> colorModels = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray colors = jsonObject.optJSONArray("datas");
                for (int i = 0; i < colors.length(); i++) {
                    JSONObject car = colors.getJSONObject(i);
                    String name = car.getString("name");
                    String id = car.getString("id");
                    String nameAr = car.getString("name_ar");
                    colorModels.add(new ColorModel(name, nameAr, id));
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return colorModels;
    }


    public static UserModel getUser(String response) {
        UserModel userModel = null;
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject user = jsonObject.getJSONObject("user");
                String name = user.optString("name");
                String username = user.optString("username");
                String token = user.optString("token");
                String birthDate = user.optString("birth_date");
                String password = user.optString("password");
                String mobile = user.optString("mobile");
                String longitude = user.optString("longitude");
                String latitude = user.optString("latitude");
                String city = user.optString("city_id");
                String country = user.optString("country");
                String image = user.optString("image");
                String id = user.optString("id");
                String gender = user.optString("gender");
                String rating = user.optString("rating");
                int orders = jsonObject.optInt("orders");
                int comments = jsonObject.optInt("comments");
                String status = user.optString("status");
                String carName = user.optString("car_name");
                String refId = user.optString("ref_id");
                String credit = user.optString("credit");
                userModel = new UserModel(name, username, token, password, mobile, longitude, latitude, city, country, gender, image, rating, id, orders, comments, rating, status, carName, refId, credit);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return userModel;
    }


    public static ArrayList<UserModel> getUsers(String response) {
        ArrayList<UserModel> mUserModels = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray users = jsonObject.getJSONArray("datas");
                for (int i = 0; i < users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);
                    String name = user.optString("name");
                    String username = user.optString("username");
                    String token = user.optString("token");
                    String birthDate = user.optString("birth_date");
                    String password = user.optString("password");
                    String mobile = user.optString("mobile");
                    String longitude = user.optString("longitude");
                    String latitude = user.optString("latitude");
                    String city = user.optString("city_id");
                    String country = user.optString("country");
                    String image = user.optString("image");
                    String id = user.optString("id");
                    String gender = user.optString("gender");
                    String rating = user.optString("rating");
                    int orders = jsonObject.optInt("orders");
                    int comments = jsonObject.optInt("comments");
                    String status = user.optString("status");
                    String carName = user.optString("car_name");
                    String refId = user.optString("ref_id");
                    String credit = user.optString("credit");
                    mUserModels.add(new UserModel(name, username, token, password, mobile, longitude, latitude, city, country, gender, image, rating, id, orders, comments, rating, status, carName, refId, credit));
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return mUserModels;
    }


    public static ArrayList<UserModel> getUsersAdd(String response) {
        ArrayList<UserModel> mUserModels = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray users = jsonObject.getJSONArray("users");
                for (int i = 0; i < users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);
                    String name = user.optString("name");
                    String username = user.optString("username");
                    String token = user.optString("token");
                    String birthDate = user.optString("birth_date");
                    String password = user.optString("password");
                    String mobile = user.optString("mobile");
                    String longitude = user.optString("longitude");
                    String latitude = user.optString("latitude");
                    String city = user.optString("city_id");
                    String country = user.optString("country");
                    String image = user.optString("image");
                    String id = user.optString("id");
                    String gender = user.optString("gender");
                    String rating = user.optString("rating");
                    int orders = jsonObject.optInt("orders");
                    int comments = jsonObject.optInt("comments");
                    String status = user.optString("status");
                    String carName = user.optString("car_name");
                    boolean friend = user.optBoolean("friend");
                    mUserModels.add(new UserModel(name, username, token, password, mobile, longitude, latitude, city, country, gender, image, rating, id, orders, comments, rating, status, carName, friend));
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return mUserModels;
    }


    public static ArrayList<MyRideModel> getMyRequests(String response, Context context) {
        ArrayList<MyRideModel> requestsModels = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray requests = jsonObject.optJSONArray("requests");
                for (int i = 0; i < requests.length(); i++) {
                    JSONObject request = requests.getJSONObject(i);
                    String id = request.optString("id");
                    String cityId = request.optString("city_id");
                    String address = request.optString("address");
                    String lon = request.optString("longitude");
                    String lat = request.optString("latitude");
                    String lonTo = request.optString("longitude_to");
                    String latTo = request.optString("latitude_to");
                    String status = request.optString("status");
                    String created = request.optString("created");
                    String updated = request.optString("updated");
                    String userId = request.optString("user_id");
                    String addressTo = request.optString("address_to");
                    String cityTo = request.optString("city_id_to");
                    String requestTime = request.optString("request_time");
                    String view = request.optString("views");
                    String start = request.optString("start");
                    String requestDate = request.optString("request_date");
                    String lonUpdate = request.optString("longitude_update");
                    String latUpdate = request.optString("latitude_update");
                    String fromId = request.optString("from_id");
                    boolean upcoming = request.optBoolean("upcoming");
                    JSONObject user = request.optJSONObject("user");
                    String user_Id = user.optString("id");
                    String userName = user.optString("name");
                    String rating = user.optString("rating");
                    String carName = user.optString("car_name");
                    String image = user.optString("image");
                    String distance = request.optString("distance");
                    String seats = request.optString("seats");
                    UserModel userModel = new UserModel(userName, user_Id, rating, carName);
                    userModel.setImage(image);
                    if (upcoming) {
                        requestsModels.add(new MyRideModel(id, cityId, address, lon, lat, lonTo, latTo, status, created, updated, userId, addressTo
                                , cityTo, requestTime, view, requestDate, lonUpdate, latUpdate, fromId, userModel, upcoming, distance, seats, start));

                    }
                }

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return requestsModels;
    }



    public static MyRideModel getMyRequest(String response, Context context) {
        MyRideModel model = null;
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject request = jsonObject.optJSONObject("request");
                String id = request.optString("id");
                String cityId = request.optString("city_id");
                String address = request.optString("address");
                String lon = request.optString("longitude");
                String lat = request.optString("latitude");
                String lonTo = request.optString("longitude_to");
                String latTo = request.optString("latitude_to");
                String status = request.optString("status");
                String created = request.optString("created");
                String updated = request.optString("updated");
                String userId = request.optString("user_id");
                String addressTo = request.optString("address_to");
                String cityTo = request.optString("city_id_to");
                String requestTime = request.optString("request_time");
                String view = request.optString("views");
                String start = request.optString("start");
                String requestDate = request.optString("request_date");
                String lonUpdate = request.optString("longitude_update");
                String latUpdate = request.optString("latitude_update");
                String fromId = request.optString("from_id");
                boolean upcoming = request.optBoolean("upcoming");
                JSONObject user = request.optJSONObject("user");
                String user_Id = user.optString("id");
                String userName = user.optString("name");
                String rating = user.optString("rating");
                String carName = user.optString("car_name");
                String image = user.optString("image");
                String distance = request.optString("distance");
                String seats = request.optString("seats");
                UserModel userModel = new UserModel(userName, user_Id, rating, carName);
                userModel.setImage(image);
                model = new MyRideModel(id, cityId, address, lon, lat, lonTo, latTo, status, created, updated, userId, addressTo
                            , cityTo, requestTime, view, requestDate, lonUpdate, latUpdate, fromId, userModel, upcoming, distance, seats, start);

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return model;
    }

    public static ArrayList<DailyRideModel> getMyRequestsDaily(String response, Context context) {
        ArrayList<DailyRideModel> requestsModels = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray requests = jsonObject.optJSONArray("data");
                for (int i = 0; i < requests.length(); i++) {
                    JSONObject request = requests.getJSONObject(i);
                    String id = request.optString("id");
                    String address = request.optString("address");
                    String lon = request.optString("longitude");
                    String lat = request.optString("latitude");
                    String lonTo = request.optString("longitude_to");
                    String latTo = request.optString("latitude_to");
                    String weekDay = request.optString("weekday");
                    String status = request.optString("status");
                    String created = request.optString("created");
                    String userId = request.optString("user_id");
                    String addressTo = request.optString("address_to");
                    String requestTime = request.optString("request_time");
                    requestsModels.add(new DailyRideModel(id, requestTime, lon, lat, lonTo, latTo, address, addressTo, weekDay, status, userId, created));
                }

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return requestsModels;
    }


    public static ArrayList<MyRideModel> getMyRequestsHistory(String response, Context context) {
        ArrayList<MyRideModel> requestsModels = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray requests = jsonObject.optJSONArray("requests");
                for (int i = 0; i < requests.length(); i++) {
                    JSONObject request = requests.getJSONObject(i);
                    String id = request.optString("id");
                    String cityId = request.optString("city_id");
                    String address = request.optString("address");
                    String lon = request.optString("longitude");
                    String lat = request.optString("latitude");
                    String lonTo = request.optString("longitude_to");
                    String latTo = request.optString("latitude_to");
                    String status = request.optString("status");
                    String created = request.optString("created");
                    String updated = request.optString("updated");
                    String userId = request.optString("user_id");
                    String addressTo = request.optString("address_to");
                    String cityTo = request.optString("city_id_to");
                    String requestTime = request.optString("request_time");
                    String view = request.optString("views");
                    String start = request.optString("start");
                    String requestDate = request.optString("request_date");
                    String lonUpdate = request.optString("longitude_update");
                    String latUpdate = request.optString("latitude_update");
                    String fromId = request.optString("from_id");
                    boolean upcoming = request.optBoolean("upcoming");
                    JSONObject user = request.optJSONObject("user");
                    String user_Id = user.optString("id");
                    String userName = user.optString("name");
                    String rating = user.optString("rating");
                    String carName = user.optString("car_name");
                    String image = user.optString("image");
                    String distance = request.optString("distance");
                    String seats = request.optString("seats");
                    UserModel userModel = new UserModel(userName, user_Id, rating, carName);
                    userModel.setImage(image);
                    if (!upcoming) {
                        requestsModels.add(new MyRideModel(id, cityId, address, lon, lat, lonTo, latTo, status, created, updated, userId, addressTo
                                , cityTo, requestTime, view, requestDate, lonUpdate, latUpdate, fromId, userModel, upcoming, distance, seats, start));

                    }
                }

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return requestsModels;
    }

    public static ArrayList<ReviewModel> getReviews(String response) {
        ArrayList<ReviewModel> reviewModels = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray reviews = jsonObject.getJSONArray("comments");
                for (int i = 0; i < reviews.length(); i++) {
                    JSONObject review = reviews.getJSONObject(i);
                    String id = review.getString("id");
                    String userId = review.getString("user_id");
                    String fromId = review.getString("from_id");
                    String requestId = review.getString("request_id");
                    String created = review.getString("created");
                    String comment = review.getString("comment");
                    String rating = review.getString("rating");
                    reviewModels.add(new ReviewModel(id, userId, fromId, requestId, created, comment, rating));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return reviewModels;
    }


    public static ArrayList<ChatModel> getChats(String response) {
        ArrayList<ChatModel> chats = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray reviews = jsonObject.getJSONArray("chats");
                for (int i = 0; i < reviews.length(); i++) {
                    JSONObject review = reviews.getJSONObject(i);
                    String id = review.getString("chat_id");
                    String lastMessage = review.getString("last_message");
                    String created = review.getString("created");
                    String name = review.getString("name");
                    String to_id = review.getString("to_id");
                    String image = review.getString("image");
                    String username = review.getString("username");
                    String message_sender_id = review.getString("message_sender_id");
                    String requestId = review.getString("request_id");
                    chats.add(new ChatModel(id, lastMessage, "false", name, to_id, username, message_sender_id, image, requestId));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return chats;
    }


    public static ArrayList<NotificationModel> getMyNotifications(String response, Context context) {
        ArrayList<NotificationModel> notificationModels = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray notifications = jsonObject.optJSONArray("notifications");
                for (int i = 0; i < notifications.length(); i++) {
                    JSONObject notification = notifications.getJSONObject(i);
                    String id = notification.optString("id");
                    String title = notification.optString("title");
                    String type = notification.optString("type");
                    String userId = notification.optString("user_id");
                    String created = notification.optString("created");
                    String offerId = notification.optString("offer_id");
                    String status = notification.optString("status");
                    String requestId = notification.optString("request_id");
                    String fromId = notification.optString("from_id");

                    JSONObject user = notification.optJSONObject("user");
                    String user_Id = user.optString("id");
                    String userName = user.optString("name");
                    String rating = user.optString("rating");
                    String carName = user.optString("car_name");
                    String image = user.optString("image");
                    String titleDelivery = notification.optString("title_delivery");
                    UserModel userModel = new UserModel(userName, user_Id, rating, carName);
                    userModel.setImage(image);


                    notificationModels.add(new NotificationModel(id, title, type, userId, created, offerId, status, requestId, fromId, titleDelivery, userModel));

                }

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return notificationModels;
    }


    public static ArrayList<RideModel> getRequests(String response) {
        ArrayList<RideModel> requestsModels = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray requests = jsonObject.optJSONArray("requests");
                Log.d("TTTT", "getRequests: " + requests.length());
                for (int i = 0; i < requests.length(); i++) {
                    JSONObject request = requests.getJSONObject(i);
                    String id = request.optString("id");
                    String cityId = request.optString("city_id");
                    String address = request.optString("address");
                    String lon = request.optString("longitude");
                    String lat = request.optString("latitude");
                    String lonTo = request.optString("longitude_to");
                    String latTo = request.optString("latitude_to");
                    String status = request.optString("status");
                    String created = request.optString("created");
                    String updated = request.optString("updated");
                    String userId = request.optString("user_id");
                    String duration = request.optString("duration");
                    String addressTo = request.optString("address_to");
                    String start = request.optString("start");
                    String cityTo = request.optString("city_id_to");
                    String requestTime = request.optString("request_time");
                    String view = request.optString("views");
                    String requestDate = request.optString("request_date");
                    String lonUpdate = request.optString("longitude_update");
                    String latUpdate = request.optString("latitude_update");
                    String fromId = request.optString("from_id");
                    JSONObject user = request.optJSONObject("user");
                    String user_Id = user.optString("id");
                    String userName = user.optString("name");
                    String rating = user.optString("rating");
                    String carName = user.optString("car_name");
                    String image = user.optString("image");
                    String distance = request.optString("distance");
                    String seats = request.optString("seats");
                    UserModel userModel = new UserModel(userName, user_Id, rating, carName);
                    requestsModels.add(new RideModel(id, cityId, address, lon, lat, lonTo, latTo, status, created, updated, userId, addressTo
                            , cityTo, requestTime, view, requestDate, lonUpdate, latUpdate, fromId, userModel, distance, seats, duration, start));
                    userModel.setImage(image);
                }

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return requestsModels;
    }


    public static RideModel getRequest(String response) {
        RideModel rideModel = null;
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject request = jsonObject.optJSONObject("request");
                String id = request.optString("id");
                String cityId = request.optString("city_id");
                String address = request.optString("address");
                String lon = request.optString("longitude");
                String lat = request.optString("latitude");
                String lonTo = request.optString("longitude_to");
                String latTo = request.optString("latitude_to");
                String status = request.optString("status");
                String created = request.optString("created");
                String updated = request.optString("updated");
                String userId = request.optString("user_id");
                String duration = request.optString("duration");
                String addressTo = request.optString("address_to");
                String start = request.optString("start");
                String cityTo = request.optString("city_id_to");
                String requestTime = request.optString("request_time");
                String view = request.optString("views");
                String requestDate = request.optString("request_date");
                String lonUpdate = request.optString("longitude_update");
                String latUpdate = request.optString("latitude_update");
                String fromId = request.optString("from_id");
                JSONObject user = request.optJSONObject("user");
                String user_Id = user.optString("id");
                String userName = user.optString("name");
                String rating = user.optString("rating");
                String carName = user.optString("car_name");
                String image = user.optString("image");
                String distance = request.optString("distance");
                String seats = request.optString("seats");
                UserModel userModel = new UserModel(userName, user_Id, rating, carName);
                rideModel = new RideModel(id, cityId, address, lon, lat, lonTo, latTo, status, created, updated, userId, addressTo
                        , cityTo, requestTime, view, requestDate, lonUpdate, latUpdate, fromId, userModel, distance, seats, duration, start);
                userModel.setImage(image);

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return rideModel;
    }


    private static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnectedOrConnecting();
        } else {
            return false;
        }
    }

    public static ArrayList<MessageModel> getChatMessagesJson(String response, UserModel userModel) {
        ArrayList<MessageModel> list = new ArrayList<>();
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray messages = jsonObject.getJSONArray("chats");
                for (int i = 0; i < messages.length(); i++) {
                    JSONObject message = messages.getJSONObject(i);
                    String chatId = message.getString("chat_id");
                    String messageText = message.getString("message");
                    String toId = message.getString("to_id");
                    String fromId = message.getString("from_id");
                    String date = message.getString("date");
                    if (userModel.getId().equals(fromId)) {
                        list.add(new MessageModel(chatId, toId, fromId, date, messageText, true));
                    } else {
                        list.add(new MessageModel(chatId, toId, fromId, date, messageText, false));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static ChatModel getChatModelJson(String response, String sellerName, String sellerId, String userId) {
        ChatModel chatModel = null;
        if (Helper.isJSONValid(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                String chatId = jsonObject.getString("chat_id");
                chatModel = new ChatModel(chatId, null, null, sellerName, sellerId, null, userId, null, "-1");
                return chatModel;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return chatModel;
    }


    public void setMap(Map<String, String> mMap) {
        this.mMap = mMap;
    }

    public interface connectionServices {
        public String BaseURL = "https://www.cta3.com/";

        @GET("waslabank/api/get_request")
        Call<StatusModel> get_request(
                @Query("id") String user_id

        );

        @GET("waslabank/api/get_groups")
        Call<GroupsResponseModel> getGroupChats(@Query("user_id") String userId);

        @GET("waslabank/api/get_group_messages")
        Call<GroupMessagesResponseModel> getGroupMessages(@Query("user_id") String userId,@Query("group_id") String groupId);

        @GET("waslabank/api/send_chat_message")
        Call<StatusResponseModel> sendChatMessage(@Query("user_id") String userId,@Query("group_id") String groupId,@Query("message") String message);

        @GET("waslabank/api/get_user")
        Call<StatusModel> get_user(
                @Query("id") String user_id

        );

        @GET("waslabank/api/add_group")
        Call<StatusResponseModel> addGroup(@Query("user_id") String userId,@Query("name") String name,@Query("selected[]") ArrayList<String> selected);


        @GET("waslabank/api/add_group_member")
        Call<StatusResponseModel> addMember(@Query("user_id") String userId,@Query("group_id") String groupId,@Query("selected[]") ArrayList<String> selected);

        @GET("waslabank/api/update_request_status")
        Call<StatusModel> update_request_status(
                @Query("longitude") String longitude
                , @Query("latitude") String latitude
                , @Query("user_id") String user_id
                , @Query("picked") String picked
                , @Query("status") String status
                , @Query("id") String id

        );

        @GET("waslabank/api/update_request_status")
        Call<StatusModel> update_request_status(
                @Query("longitude") String longitude
                , @Query("latitude") String latitude
                , @Query("user_id") String user_id
                , @Query("id") String id

        );


        @GET("waslabank/api/update_request_status")
        Call<StatusModel> update_request_status(
                @Query("longitude") String longitude
                , @Query("latitude") String latitude
                , @Query("user_id") String user_id
                , @Query("id") String id
                , @Query("status") String status
                , @Query("distance") String distance
                ,@Query("extra") String extra
        );

        @GET("waslabank/api/update_request_status")
        Call<StatusModel> update_request_status(
                @Query("user_id") String user_id
                , @Query("picked") String picked
                , @Query("id") String id

        );

        //o finish
        @GET("waslabank/api/update_request_status")
        Call<StatusModel> update_request_status(
                @Query("status") String status
                , @Query("id") String id

        );

        @GET("waslabank/api/get_chat_messages")
        Call<StatusModel> chatMsgs(
                @Query("chat_id") String chat_id
        );

        @GET("waslabank/api/get_request")
        Call<Example> getSingleRequest(
                @Query("id") String id
        );
        //reject_offer(user_id,from_id , id=>request)
        @GET("waslabank/api/reject_offer")
        Call<Example> reject_offer(
                @Query("user_id") String user_id
                ,@Query("from_id") String from_id
                ,@Query("id") String id
        );


        @GET("waslabank/api/update_token")
        Call<UpdateToken> updateToken(@Query("id") String id, @Query("token") String token);
    }
}
