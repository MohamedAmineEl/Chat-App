package com.apps.chat_apps.activity;

import static com.apps.chat_apps.constant.BundleExtraNames.CONVERSATION_ID;
import static com.apps.chat_apps.constant.BundleExtraNames.USER_ID;
import static com.apps.chat_apps.constant.Constants.BASE_URL;
import static com.apps.chat_apps.constant.FirebaseDataPaths.CONVERSATIONS;
import static com.apps.chat_apps.constant.FirebaseDataPaths.MESSAGES;
import static com.apps.chat_apps.constant.FirebaseDataPaths.SEEN;
import static com.apps.chat_apps.constant.FirebaseDataPaths.TOKENS;
import static com.apps.chat_apps.constant.FirebaseDataPaths.USERS;
import static com.apps.chat_apps.utils.Utils.showToast;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.chat_apps.R;
import com.apps.chat_apps.notification.Data;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.apps.chat_apps.adapter.MessageAdapter;
import com.apps.chat_apps.service.APIService;
import com.apps.chat_apps.model.Message;
import com.apps.chat_apps.model.User;
import com.apps.chat_apps.notification.Client;
import com.apps.chat_apps.notification.MyResponse;
import com.apps.chat_apps.notification.Sender;
import com.apps.chat_apps.notification.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessagingActivity extends AppCompatActivity {

    private CircleImageView mProfileIV;
    private TextView mUserNameTV;
    private FirebaseUser mUser;
    private DatabaseReference mDatabaseReference;
    private EditText mMessageET;
    private RecyclerView mMessagesRV;

    private MessageAdapter mMessageAdapter;
    private List<Message> mMessages;
    private String mUserId;
    private String mConversationId;
    private boolean mIsConversationHasMessages;
    private ValueEventListener mSeenListener;

    private APIService mAPIService;
    private boolean mNotify = false;

    private boolean mIsConversationCreated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        mAPIService = Client.getClient(BASE_URL).create(APIService.class);
        initView();
        Intent intent = getIntent();
        mUserId = intent.getStringExtra(USER_ID);
        mConversationId = intent.getStringExtra(CONVERSATION_ID);
        mIsConversationCreated = mConversationId != null;
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        initUserViews();
        if (mIsConversationCreated) {
            mIsConversationHasMessages = true;
            seenMessage(mUserId);
        } else {
            createConversation();
        }
    }

    private void initAdapter(String imageUrl) {
        mMessageAdapter = new MessageAdapter(this, null, mUser.getUid(), imageUrl);
        mMessagesRV.setAdapter(mMessageAdapter);
    }

    private void initView() {
        findViewById(R.id.back_btn).setOnClickListener(b -> finish());
        mMessagesRV = findViewById(R.id.messages_recycler_view);
        mMessagesRV.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        mMessagesRV.setLayoutManager(linearLayoutManager);
        mProfileIV = findViewById(R.id.user_profile_iv);
        mUserNameTV = findViewById(R.id.user_name_iv);
        ImageView sendBtn = findViewById(R.id.send_btn);
        mMessageET = findViewById(R.id.message_tv);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNotify = true;
                String msg = mMessageET.getText().toString();
                if (!msg.equals("")) {
                    if (mIsConversationCreated) {
                        sendMessage(mUser.getUid(), mUserId, msg);
                    } else {
                        showToast(MessagingActivity.this, "initializing...");
                    }
                } else {
                    Toast.makeText(MessagingActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                mMessageET.setText("");
            }
        });
    }

    private void initUserViews() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(USERS).child(mUserId);
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                mUserNameTV.setText(user.getName());
                if (user.getImageURL().equals("default")) {
                    mProfileIV.setImageResource(R.drawable.default_profile_pic);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(mProfileIV);
                }
                initAdapter(user.getImageURL());
                if (mConversationId != null) {
                    fetchAllMessages();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenMessage(final String userid) {
        mDatabaseReference = FirebaseDatabase.getInstance()
                .getReference(CONVERSATIONS)
                .child(mConversationId)
                .child(MESSAGES);
        mSeenListener = mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message.getReceiver().equals(mUser.getUid()) && message.getSender().equals(userid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put(SEEN, true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, final String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(CONVERSATIONS)
                .child(mConversationId)
                .child(MESSAGES);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("content", message);
        hashMap.put("timestamp", System.currentTimeMillis());
        hashMap.put("seen", false);
        reference.push().setValue(hashMap);

        final String msg = message;
        reference = FirebaseDatabase.getInstance().getReference(USERS).child(mUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (mNotify) {
                    sendNotification(receiver, user.getUsername(), msg);
                }
                mNotify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(String receiver, final String username, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(TOKENS);
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(mUser.getUid(), R.drawable.default_profile_pic, username + ": " + message, "New Message", mUserId);

                    Sender sender = new Sender(data, token.getToken());

                    mAPIService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code() == 200) {
                                if (response.body().success != 1) {
//                                    Toast.makeText(MessagingActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchAllMessages() {
        mMessages = new ArrayList<>();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(CONVERSATIONS)
                .child(mConversationId)
                .child(MESSAGES);
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMessages.clear();
                int index = -1;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    mMessages.add(message);
                    if (message != null) {
                        if (message.getSender().equals(mUserId) || message.isSeen()) {
                            index = mMessages.size() - 1;
                        }
                    }
                    mMessageAdapter.updateMessagesList(mMessages);
                }
                mMessageAdapter.updateLastSeenByOtherUser(index);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void currentUser(String userid) {
        SharedPreferences.Editor editor = getSharedPreferences("PREPS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    private void status(String status) {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(USERS).child(mUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        mDatabaseReference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(mUserId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsConversationHasMessages) {
            mDatabaseReference.removeEventListener(mSeenListener);
        }
        status("offline");
        currentUser("none");
    }

    private void createConversation() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(CONVERSATIONS);
        mConversationId = mDatabaseReference.push().getKey();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("user_1", mUser.getUid());
        hashMap.put("user_2", mUserId);
        mDatabaseReference.child(mConversationId).setValue(hashMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mIsConversationCreated = true;
                showToast(MessagingActivity.this, "You can start you conversation");
            }
        });
    }
}
