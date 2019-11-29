package com.waslabank.wasslabank.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.waslabank.wasslabank.R;
import com.waslabank.wasslabank.models.GroupChatModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupChatsAdapter extends RecyclerView.Adapter<GroupChatsAdapter.GroupChatHolder> {

    private ArrayList<GroupChatModel> chats;
    private Context mContext;
    private OnItemClicked onItemClicked;

    public GroupChatsAdapter(ArrayList<GroupChatModel> chats, Context mContext, OnItemClicked onItemClicked) {
        this.chats = chats;
        this.mContext = mContext;
        this.onItemClicked = onItemClicked;
    }

    @NonNull
    @Override
    public GroupChatsAdapter.GroupChatHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_group_chat_item, viewGroup, false);
        return new GroupChatsAdapter.GroupChatHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull GroupChatsAdapter.GroupChatHolder chatHolder, int i) {
        chatHolder.name.setText(chats.get(i).getName());
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public interface OnItemClicked {
        void setOnItemClicked(int position);
    }


    class GroupChatHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.open_chat)
        Button openChat;

        GroupChatHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            openChat.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClicked.setOnItemClicked(getAdapterPosition());
        }
    }
}
