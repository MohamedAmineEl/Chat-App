package com.apps.chat_apps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.apps.chat_apps.model.Message;
import com.apps.chat_apps.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private final Context mContext;
    private List<Message> mMessages;
    private final String imageurl;
    private String mCurrentUserId;
    private int mLastSeenIndex;

    public MessageAdapter(Context mContext, List<Message> mMessage, String userId, String imageUrl) {
        this.mContext = mContext;
        this.mMessages = mMessage;
        this.imageurl = imageUrl;
        mCurrentUserId = userId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageViewHolder(view, false);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageViewHolder(view, true);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = mMessages.get(position);
        holder.messageTV.setText(message.getContent());
        if (position == mMessages.size() - 1 || mLastSeenIndex == position) {
            holder.seenImgIndicatorIV.setVisibility(View.VISIBLE);
            switch (getItemViewType(position)) {
                case MSG_TYPE_LEFT:
                    Glide.with(mContext)
                            .load(imageurl)
                            .placeholder(R.drawable.default_profile_pic)
                            .into(holder.seenImgIndicatorIV);
                    break;
                case MSG_TYPE_RIGHT:
                    if (message.isSeen()) {
                        Glide.with(mContext)
                                .load(imageurl)
                                .placeholder(R.drawable.default_profile_pic)
                                .into(holder.seenImgIndicatorIV);
                    } else {
                        Glide.with(mContext).
                                load(R.drawable.ic_unread_check)
                                .into(holder.seenImgIndicatorIV);
                    }
                    break;
            }
        } else {
            holder.seenImgIndicatorIV.setVisibility(View.INVISIBLE);
        }
        if (getItemViewType(position) == MSG_TYPE_LEFT) {
            Glide.with(mContext)
                    .load(imageurl)
                    .placeholder(R.drawable.default_profile_pic)
                    .into(holder.userProfile);
        }

    }

    @Override
    public int getItemCount() {
        return mMessages != null ? mMessages.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (mMessages.get(position).getSender().equals(mCurrentUserId)) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    public void updateMessagesList(List<Message> list) {
        mMessages = list;
        notifyDataSetChanged();
    }

    public void updateLastSeenByOtherUser(int index) {
        mLastSeenIndex = index;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTV;
        public ImageView seenImgIndicatorIV;
        public ImageView userProfile;

        public MessageViewHolder(@NonNull View itemView, boolean isLeft) {
            super(itemView);
            messageTV = itemView.findViewById(R.id.message_content_tv);
            seenImgIndicatorIV = itemView.findViewById(R.id.seen_indicator_iv);
            if (isLeft) {
                userProfile = itemView.findViewById(R.id.left_user_profile_iv);
            }
        }
    }

}
