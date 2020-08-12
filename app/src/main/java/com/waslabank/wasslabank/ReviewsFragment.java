package com.waslabank.wasslabank;


import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.waslabank.wasslabank.adapters.ReviewsAdapter;
import com.waslabank.wasslabank.models.ReviewModel;
import com.waslabank.wasslabank.networkUtils.Connector;
import com.waslabank.wasslabank.utils.Helper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReviewsFragment extends Fragment {

    @BindView(R.id.reviews)
    RecyclerView mReviews;

    ArrayList<ReviewModel> mReviewModels;
    ProgressDialog mProgressDialog;

    Connector mConnector;

    ReviewsAdapter mAdapter;



    public ReviewsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_reviews, container, false);
        ButterKnife.bind(this,v);

        mReviewModels = new ArrayList<>();


        mConnector = new Connector(getContext(), new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                mProgressDialog.dismiss();
                if (Connector.checkStatus(response)){
                    mReviewModels.addAll(Connector.getReviews(response));
                    mAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(),"No Reviews",Toast.LENGTH_LONG).show();
                }
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {
                mProgressDialog.dismiss();
                Toast.makeText(getContext(),getString(R.string.error),Toast.LENGTH_LONG).show();

            }
        });


        mAdapter = new ReviewsAdapter(mReviewModels, getContext(), new ReviewsAdapter.OnItemClicked() {
            @Override
            public void setOnItemClicked(int position) {

            }
        });

        mReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        mReviews.setAdapter(mAdapter);
        mReviews.setNestedScrollingEnabled(false);


        mProgressDialog = Helper.showProgressDialog(getContext(),getString(R.string.loading),false);
        mConnector.getRequest("ReviewsActivity","https://code-grow.com/waslabank/api/get_comments?user_id=" + Helper.getUserSharedPreferences(getContext()).getId());



        return v;
    }

}
