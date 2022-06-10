package com.apps.chat_apps.adapter;

import static com.apps.chat_apps.utils.Utils.formatDate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.chat_apps.R;
import com.apps.chat_apps.interfaces.OnConversationClicked;
import com.apps.chat_apps.model.Conversation;
import com.apps.chat_apps.model.Message;
import com.apps.chat_apps.model.User;
import com.bumptech.glide.Glide;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    private final Context mContext;
    private List<Conversation> mConversations;
    private final String mUserId;
    private final boolean mIsUserOnline;
    private final OnConversationClicked mListener;

    public ConversationAdapter(Context context, OnConversationClicked listener, List<Conversation> users, String userId, boolean isUserOnline) {
        mContext = context;
        mListener = listener;
        mUserId = userId;
        mConversations = users;
        this.mIsUserOnline = isUserOnline;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.conversation_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = mConversations.get(position);
        User user = conversation.getOtherUser();
        holder.nameTV.setText(user.getName());
        Message message = conversation.getLastMessage();
        holder.lastMessageTV.setText(message.getContent());
        if (message.getSender().equals(mUserId)) {
            if (message.isSeen()) {
                Glide.with(mContext)
                        .load(user.getImageURL())
                        .placeholder(R.drawable.default_profile_pic)
                        .into(holder.messageStateIV);
            } else {
                holder.messageStateIV.setImageResource(R.drawable.ic_unread_check);
            }
        } else {
            holder.messageStateIV.setVisibility(View.GONE);
        }

        String date = " \u00B7 " + formatDate(conversation.getLastMessage().getTimestamp());
        holder.dateTV.setText(date);
        if (user.getImageURL().equals("default")) {
            holder.profileIV.setImageResource(R.drawable.default_profile_pic);
        } else {
            Glide.with(mContext)
                    .load(user.getImageURL())
                    .placeholder(R.drawable.default_profile_pic)
                    .into(holder.profileIV);
        }
        if (mIsUserOnline) {
            if (user.getStatus().equals("online")) {
                holder.onlineIndicatorView.setVisibility(View.VISIBLE);
            } else {
                holder.onlineIndicatorView.setVisibility(View.GONE);
            }
        } else {
            holder.onlineIndicatorView.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onConversationClicked(conversation);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mConversations != null ? mConversations.size() : 0;
    }

    public void updateConversationsList(List<Conversation> conversations) {
        mConversations = conversations;
        notifyDataSetChanged();
    }

    public List<Conversation> getConversations() {
        return mConversations;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView nameTV;
        public TextView lastMessageTV;
        public TextView dateTV;
        public ImageView profileIV;
        public View onlineIndicatorView;
        public ImageView messageStateIV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.name_tv);
            lastMessageTV = itemView.findViewById(R.id.last_message_tv);
            dateTV = itemView.findViewById(R.id.date_tv);
            profileIV = itemView.findViewById(R.id.profile_image_iv);
            onlineIndicatorView = itemView.findViewById(R.id.online_indicator_view);
            messageStateIV = itemView.findViewById(R.id.message_state_iv);
        }
    }
}
