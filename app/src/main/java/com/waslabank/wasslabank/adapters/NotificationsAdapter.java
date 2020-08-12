package com.waslabank.wasslabank.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.waslabank.wasslabank.R;
import com.waslabank.wasslabank.models.NotificationModel;
import com.waslabank.wasslabank.models.UserModel;
import com.waslabank.wasslabank.utils.Helper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    ArrayList<NotificationModel> notifications;
    Context context;
    OnItemClicked onItemClicked;
    UserModel mUserModel;


    public NotificationsAdapter(ArrayList<NotificationModel> notifications, Context context, OnItemClicked onItemClicked) {
        this.notifications = notifications;
        this.context = context;
        this.onItemClicked = onItemClicked;
        mUserModel = Helper.getUserSharedPreferences(context);
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notification_item,viewGroup,false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder notificationViewHolder, int i) {
        notificationViewHolder.date.setText(notifications.get(i).getCreated());
        if (mUserModel.getId().equals(notifications.get(i).getUserId()))
            notificationViewHolder.title.setText(notifications.get(i).getTitle());
        else
            notificationViewHolder.title.setText(notifications.get(i).getTitleDelivery());

        if (URLUtil.isValidUrl(notifications.get(i).getmUserModel().getImage()))
            Picasso.get().load(notifications.get(i).getmUserModel().getImage()).fit().centerCrop().into(notificationViewHolder.profileImage);
        else {
            Picasso.get().load("https://code-grow.com/waslabank/prod_img/" + notifications.get(i).getmUserModel().getImage()).fit().centerCrop().into(notificationViewHolder.profileImage);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }


    class NotificationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{


        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.profile_image)
        ImageView profileImage;
        @BindView(R.id.title)
        TextView title;

        NotificationViewHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClicked.setOnItemClicked(getAdapterPosition());
        }
    }



    public interface OnItemClicked{
        void setOnItemClicked(int position);
    }
}
