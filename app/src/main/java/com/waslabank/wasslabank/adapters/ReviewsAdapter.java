package com.waslabank.wasslabank.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.waslabank.wasslabank.R;
import com.waslabank.wasslabank.models.ReviewModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewHolder> {


    ArrayList<ReviewModel> reviewModels;
    Context context;
    OnItemClicked onItemClicked;

    public ReviewsAdapter(ArrayList<ReviewModel> reviewModels, Context context, OnItemClicked onItemClicked) {
        this.reviewModels = reviewModels;
        this.context = context;
        this.onItemClicked = onItemClicked;
    }

    @NonNull
    @Override
    public ReviewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.review_item, viewGroup, false);
        return new ReviewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewHolder reviewHolder, int i) {
        reviewHolder.comment.setText(reviewModels.get(i).getComment());
        reviewHolder.rating.setRating(Float.parseFloat(reviewModels.get(i).getRating()));
        reviewHolder.date.setText(reviewModels.get(i).getCreated());
    }

    @Override
    public int getItemCount() {
        return reviewModels.size();
    }


    class ReviewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.rating_bar)
        RatingBar rating;
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.comment)
        TextView comment;
        @BindView(R.id.profile_image)
        ImageView profileImage;

        ReviewHolder(View itemView) {
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
}
