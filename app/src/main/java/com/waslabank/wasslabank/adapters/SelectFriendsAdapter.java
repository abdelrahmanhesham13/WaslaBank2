package com.waslabank.wasslabank.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;
import com.waslabank.wasslabank.FriendsRidesActivity;
import com.waslabank.wasslabank.R;
import com.waslabank.wasslabank.models.UserModel;
import com.waslabank.wasslabank.networkUtils.Connector;
import com.waslabank.wasslabank.utils.Helper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectFriendsAdapter extends RecyclerView.Adapter<SelectFriendsAdapter.UserViewHolder> {

    ArrayList<UserModel> users;
    Context context;
    OnItemClicked onItemClicked;
    ArrayList<String> selectedUsersIds;


    public SelectFriendsAdapter(ArrayList<UserModel> users, Context context, OnItemClicked onItemClicked) {
        this.users = users;
        this.context = context;
        this.onItemClicked = onItemClicked;
        selectedUsersIds = new ArrayList<>();
    }

    public ArrayList<String> getSelectedUsersIds() {
        return selectedUsersIds;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_choose_friend, viewGroup, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder userViewHolder, int i) {
        userViewHolder.name.setText(users.get(i).getName());
        if (URLUtil.isValidUrl(users.get(i).getImage()))
            Picasso.get().load(users.get(i).getImage()).fit().centerCrop().into(userViewHolder.profileImage);
        else {
            Picasso.get().load("http://www.as.cta3.com/waslabank/prod_img/" + users.get(i).getImage()).fit().centerCrop().into(userViewHolder.profileImage);
        }
        userViewHolder.mAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedUsersIds.contains(users.get(i).getId())){
                    userViewHolder.mAddFriendButton.setText("Add");
                    selectedUsersIds.remove(users.get(i).getId());
                } else {
                    userViewHolder.mAddFriendButton.setText("Remove");
                    selectedUsersIds.add(users.get(i).getId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    class UserViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.profile_image)
        ImageView profileImage;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.add_friend)
        Button mAddFriendButton;


        UserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }


    public interface OnItemClicked {
        void setOnItemClicked(int position);
    }

}
