package com.waslabank.wasslabank.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.waslabank.wasslabank.R;
import com.waslabank.wasslabank.models.GroupMessageModel;
import com.waslabank.wasslabank.utils.Helper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupMessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<GroupMessageModel> mMessagesModel;
    private OnItemClicked onItemClicked;


    public GroupMessagesAdapter(Context mContext, ArrayList<GroupMessageModel> mMessagesModel, OnItemClicked onItemClicked) {
        this.mContext = mContext;
        this.mMessagesModel = mMessagesModel;
        this.onItemClicked = onItemClicked;
    }

    @Override
    public int getItemViewType(int position) {
        if (mMessagesModel.get(position).getUserId().equals(Helper.getUserSharedPreferences(mContext).getId())) {
            return 1;
        } else {
            return 2;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_outgoing, parent, false);
            return new MessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_incoming, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == 0){
            ((DateViewHolder)holder).mDate.setText(mMessagesModel.get(position).getMessage());
        } else {
            ((MessageViewHolder) holder).mMessageText.setText(mMessagesModel.get(position).getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return mMessagesModel.size();
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.message_text)
        TextView mMessageText;
        @BindView(R.id.message_date)
        TextView mMessageDate;

        MessageViewHolder (View itemView){
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            onItemClicked.setOnItemClicked(getAdapterPosition());
        }
    }


    public class DateViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.message_date)
        TextView mDate;

        DateViewHolder (View itemView){
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            onItemClicked.setOnItemClicked(getAdapterPosition());
        }
    }

    public interface OnItemClicked{
        void setOnItemClicked(int position);
    }


}
