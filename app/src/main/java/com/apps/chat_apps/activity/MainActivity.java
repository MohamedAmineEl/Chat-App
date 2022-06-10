package com.apps.chat_apps.activity;

import static com.apps.chat_apps.constant.BundleExtraNames.CONVERSATIONS_DETAILS;
import static com.apps.chat_apps.constant.BundleExtraNames.CONVERSATION_ID;
import static com.apps.chat_apps.constant.BundleExtraNames.USER_ID;
import static com.apps.chat_apps.constant.FirebaseDataPaths.CONVERSATIONS;
import static com.apps.chat_apps.constant.FirebaseDataPaths.MESSAGES;
import static com.apps.chat_apps.constant.FirebaseDataPaths.TIMESTAMP;
import static com.apps.chat_apps.constant.FirebaseDataPaths.TOKENS;
import static com.apps.chat_apps.constant.FirebaseDataPaths.USERS;
import static com.apps.chat_apps.utils.Utils.showToast;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.chat_apps.R;
import com.apps.chat_apps.adapter.ConversationAdapter;
import com.apps.chat_apps.interfaces.OnConversationClicked;
import com.apps.chat_apps.model.Conversation;
import com.apps.chat_apps.model.ConversationDetails;
import com.apps.chat_apps.model.Message;
import com.apps.chat_apps.model.User;
import com.apps.chat_apps.notification.Token;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements OnConversationClicked {

    private CircleImageView mProfileIV;
    private DatabaseReference mDatabaseReference;
    private ConversationAdapter mConversationAdapter;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        initView();
        initUserData();
        fetchConversations();
    }

    private void initView() {
        findViewById(R.id.change_profile_img_btn).setOnClickListener(v -> {
            startActivity(new Intent(this, ChangeProfileImageActivity.class));
        });
        mProfileIV = findViewById(R.id.profile_image_iv);
        findViewById(R.id.searchBarBtn).setOnClickListener(b -> {
            if (mConversationAdapter.getConversations() != null) {
                Intent searchIntent = new Intent(this, SearchActivity.class);
                Bundle bd = new Bundle();
                searchIntent.putParcelableArrayListExtra(CONVERSATIONS_DETAILS, generateConversationsDetails());
                startActivity(searchIntent);
            } else {
                showToast(this, "Loading conversations...");
            }
        });
        RecyclerView conversationsRV = findViewById(R.id.recycler_view);
        conversationsRV.setHasFixedSize(true);
        conversationsRV.setLayoutManager(new LinearLayoutManager(this));
        mConversationAdapter = new ConversationAdapter(this, this, null, mUser.getUid(), true);
        conversationsRV.setAdapter(mConversationAdapter);
    }

    private void initUserData() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    updateToken(task.getResult());
                } else {
                    Toast.makeText(MainActivity.this,
                            "Task for getting token not successful",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mDatabaseReference = FirebaseDatabase.getInstance().getReference(USERS).child(mUser.getUid());
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    if (user.getImageURL().equals("default")) {
                        mProfileIV.setImageResource(R.drawable.default_profile_pic);
                    } else {
                        Glide.with(MainActivity.this)
                                .load(user.getImageURL())
                                .placeholder(R.drawable.default_profile_pic)
                                .into(mProfileIV);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void status(String status) {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(USERS).child(mUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        mDatabaseReference.updateChildren(hashMap);
    }

    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(TOKENS);
        Token token1 = new Token(token);
        reference.child(mUser.getUid()).setValue(token1);
    }

    private void fetchConversations() {
        ArrayList<Conversation> conversations = new ArrayList<>();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(USERS);
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot usersSnapshot) {
                mDatabaseReference = FirebaseDatabase.getInstance().getReference(CONVERSATIONS);
                mDatabaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot conversationsSnapshot) {
                        conversations.clear();
                        for (DataSnapshot snapshot : conversationsSnapshot.getChildren()) {
                            String user1Id = snapshot.child("user_1").getValue(String.class);
                            String user2Id = snapshot.child("user_2").getValue(String.class);
                            if ((mUser.getUid().equals(user2Id) || mUser.getUid().equals(user1Id) && snapshot.child(MESSAGES).hasChildren())) {
                                String otherUserId = mUser.getUid().equals(user2Id) ? user1Id : user2Id;
                                Message lastMessage = getLastMessage(snapshot.child(MESSAGES));
                                User otherUser = usersSnapshot.child(otherUserId).getValue(User.class);
                                String id = snapshot.getKey();
                                conversations.add(new Conversation(id, otherUser, lastMessage));
                            }
                        }
                        sortListByLastMessage(conversations);
                        mConversationAdapter.updateConversationsList(conversations);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private Message getLastMessage(DataSnapshot snapshot) {
        long maxTimestamp = 0;
        String messageId = null;
        for (DataSnapshot snapshotItem : snapshot.getChildren()) {
            Long timestamp = snapshotItem.child(TIMESTAMP).getValue(Long.class);
            if (timestamp != null) {
                if (timestamp > maxTimestamp) {
                    maxTimestamp = timestamp;
                    messageId = snapshotItem.getKey();
                }
            }
        }
        if (messageId == null) {
            return null;
        }
        Message message = snapshot.child(messageId).getValue(Message.class);
        if (message == null) {
            return null;
        }
        String content = message.getContent();
        if (mUser.getUid().equals(message.getSender())) {
            content = "You: " + content;
            message.setContent(content);
        }
        return message;
    }


    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    public void onConversationClicked(Conversation conversation) {
        Intent intent = new Intent(this, MessagingActivity.class);
        intent.putExtra(USER_ID, conversation.getOtherUser().getId());
        intent.putExtra(CONVERSATION_ID, conversation.getId());
        startActivity(intent);
    }

    private void sortListByLastMessage(List<Conversation> conversationsList) {
        Collections.sort(conversationsList, (conversation1, conversation2) -> {
            return (int) (conversation2.getLastMessage().getTimestamp() - conversation1.getLastMessage().getTimestamp());
        });
    }

    private ArrayList<ConversationDetails> generateConversationsDetails() {
        ArrayList<ConversationDetails> detailsList = new ArrayList<>();
        for (Conversation conversation : mConversationAdapter.getConversations()) {
            ConversationDetails details = new ConversationDetails(conversation.getId(), conversation.getOtherUser().getId());
            detailsList.add(details);
        }
        return detailsList;
    }
}