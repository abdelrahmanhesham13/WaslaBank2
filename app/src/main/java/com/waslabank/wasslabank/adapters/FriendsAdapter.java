package com.waslabank.wasslabank.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
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
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.UserViewHolder> {


    ArrayList<UserModel> users;
    Context context;
    OnItemClicked onItemClicked;
    int type;
    Connector mConnector;


    public FriendsAdapter(ArrayList<UserModel> users, Context context, OnItemClicked onItemClicked, int type) {
        this.users = users;
        this.context = context;
        this.onItemClicked = onItemClicked;
        this.type = type;
        mConnector = new Connector(context, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response))
                    Helper.showSnackBarMessage(Connector.getMessage(response), (AppCompatActivity) context);
                else
                    Helper.showSnackBarMessage(Connector.getMessage(response), (AppCompatActivity) context);
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {

            }
        });
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.friend_item, viewGroup, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder userViewHolder, int i) {
        userViewHolder.name.setText(users.get(i).getName());
        if (type == 0 || type == 1) {
            if (type == 1) {
                userViewHolder.mAddFriendButton.setVisibility(View.VISIBLE);
            } else {
                //userViewHolder.mAddFriendButton.setVisibility(View.GONE);
            }
            if (URLUtil.isValidUrl(users.get(i).getImage()))
                Picasso.get().load(users.get(i).getImage()).fit().centerCrop().into(userViewHolder.profileImage);
            else {
                Picasso.get().load("https://code-grow.com/waslabank/prod_img/" + users.get(i).getImage()).fit().centerCrop().into(userViewHolder.profileImage);
            }

            if (type == 0) {
                userViewHolder.mShowRidesButton.setVisibility(View.VISIBLE);
                userViewHolder.mAddFriendButton.setText("Un friend");
            } else {
                userViewHolder.mShowRidesButton.setVisibility(View.GONE);
                userViewHolder.mAddFriendButton.setText("Add friend");
            }

            if (type == 1) {
                if (users.get(i).isFriend()) {
                    userViewHolder.mShowRidesButton.setVisibility(View.VISIBLE);
                    userViewHolder.mAddFriendButton.setText("Un friend");
                } else {
                    userViewHolder.mShowRidesButton.setVisibility(View.GONE);
                    userViewHolder.mAddFriendButton.setText("Add friend");
                }
            }

            userViewHolder.mShowRidesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, FriendsRidesActivity.class).putExtra("user_id", users.get(i).getId()));
                }
            });

            userViewHolder.mAddFriendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (users.get(i).isFriend()) {
                        users.get(i).setFriend(false);
                    } else {
                        users.get(i).setFriend(true);
                    }
                    notifyItemChanged(i);
                    mConnector.getRequest("", "https://code-grow.com/waslabank/api/add_friend?from_id=" + Helper.getUserSharedPreferences(context).getId() + "&to_id=" + users.get(userViewHolder.getAdapterPosition()).getId());
                }
            });
        } else {
            if (URLUtil.isValidUrl(users.get(i).getImage()))
                Picasso.get().load(users.get(i).getImage()).fit().centerCrop().into(userViewHolder.profileImage);
            else {
                Picasso.get().load("https://code-grow.com/waslabank/prod_img/" + users.get(i).getImage()).fit().centerCrop().into(userViewHolder.profileImage);
            }
            userViewHolder.mShowRidesButton.setVisibility(View.GONE);
            userViewHolder.mAddFriendButton.setVisibility(View.GONE);
            userViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog(users.get(i).getId());
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.profile_image)
        ImageView profileImage;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.add_friend)
        Button mAddFriendButton;
        @BindView(R.id.show_rides)
        Button mShowRidesButton;


        UserViewHolder(View itemView) {
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


    private void showDialog(String toId) {

        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        final EditText edittext = new EditText(context);
        alert.setMessage("Enter the credit you want to transfer");
        alert.setTitle("Transfer credit");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        edittext.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        alert.setView(edittext);


        alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //What ever you want to do with the value
                Editable YouEditTextValue = edittext.getText();
                if (!YouEditTextValue.toString().isEmpty())
                    mConnector.getRequest("", "https://code-grow.com/waslabank/api/transfer_credits?from_id=" + Helper.getUserSharedPreferences(context).getId() + "&to_id=" + toId + "&credit=" + YouEditTextValue.toString());
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
                dialog.dismiss();
            }
        });

        alert.show();
    }

}
