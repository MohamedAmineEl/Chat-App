package com.apps.chat_apps.adapter;

import static com.apps.chat_apps.utils.Utils.showToast;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.chat_apps.R;
import com.apps.chat_apps.interfaces.OnUserClickedListener;
import com.apps.chat_apps.model.User;
import com.bumptech.glide.Glide;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context mContext;
    private List<User> mUsers;
    private OnUserClickedListener mListener;

    public UserAdapter(Context context, List<User> users, OnUserClickedListener listener) {
        mContext = context;
        mUsers = users;
        mListener = listener;
    }

    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.userNameTV.setText(user.getName());
        String hint = "Say hi to " + user.getName();
        holder.userHint.setText(hint);
        Glide.with(mContext)
                .load(user.getImageURL())
                .placeholder(R.drawable.default_profile_pic)
                .into(holder.userProfileIV);
        holder.itemView.setOnClickListener(v -> mListener.onUserClicked(user));
    }

    public void updateUserList(List<User> users) {
        mUsers = users;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mUsers != null ? mUsers.size() : 0;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public ImageView userProfileIV;
        public TextView userNameTV;
        public TextView userHint;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userProfileIV = itemView.findViewById(R.id.user_image_iv);
            userNameTV = itemView.findViewById(R.id.user_name_text_view);
            userHint = itemView.findViewById(R.id.user_hint_tv);
        }
    }
}
