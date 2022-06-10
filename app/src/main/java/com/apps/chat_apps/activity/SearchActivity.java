package com.apps.chat_apps.activity;

import static com.apps.chat_apps.constant.BundleExtraNames.CONVERSATIONS_DETAILS;
import static com.apps.chat_apps.constant.BundleExtraNames.CONVERSATION_ID;
import static com.apps.chat_apps.constant.BundleExtraNames.USER_ID;
import static com.apps.chat_apps.constant.FirebaseDataPaths.USERS;
import static com.apps.chat_apps.utils.Utils.showToast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;

import com.apps.chat_apps.R;
import com.apps.chat_apps.adapter.UserAdapter;
import com.apps.chat_apps.interfaces.OnUserClickedListener;
import com.apps.chat_apps.model.ConversationDetails;
import com.apps.chat_apps.model.User;
import com.apps.chat_apps.notification.Data;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements OnUserClickedListener {
    private EditText mSearchET;
    private UserAdapter mUserAdapter;
    private DatabaseReference mDatabaseReference;
    private List<User> mSearchResultList;
    private RecyclerView mResultUsersRV;
    private ProgressBar mProgressBar;
    private String mQuery;

    private FirebaseUser mUser;
    private List<ConversationDetails> mConversationDetails;

    private ValueEventListener mValueEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(USERS);
        mConversationDetails = getIntent().getParcelableArrayListExtra(CONVERSATIONS_DETAILS);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mSearchResultList = new ArrayList<>();
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mSearchResultList.clear();
                showLoadingBar(false);
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    if (user.getName().toLowerCase().contains(mQuery.toLowerCase()) && !user.getId().equals(mUser.getUid())) {
                        mSearchResultList.add(user);
                        mUserAdapter.updateUserList(mSearchResultList);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        initView();
    }

    private void initView() {
        mSearchET = findViewById(R.id.search_input_edit_text);
        mSearchET.requestFocus();
        findViewById(R.id.clear_search_input_iv).setOnClickListener(v -> {
            mSearchET.setText("");
        });
        mProgressBar = findViewById(R.id.progressBar);
        findViewById(R.id.back_btn).setOnClickListener(v -> finish());
        mResultUsersRV = findViewById(R.id.result_users_recycler_view);
        mResultUsersRV.setLayoutManager(new LinearLayoutManager(this));
        mUserAdapter = new UserAdapter(this, mSearchResultList, this);
        mResultUsersRV.setAdapter(mUserAdapter);
        mSearchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mDatabaseReference.removeEventListener(mValueEventListener);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString();
                if (query.length() != 0) {
                    searchFor(s.toString());
                } else {
                    mSearchResultList.clear();
                    mUserAdapter.updateUserList(mSearchResultList);
                }
            }
        });
    }

    private void searchFor(String query) {
        mQuery = query;
        showLoadingBar(true);
        mDatabaseReference.addValueEventListener(mValueEventListener);
    }

    private void showLoadingBar(boolean isLoading) {
        if (isLoading) {
            mResultUsersRV.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mResultUsersRV.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }
    }


    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(this, MessagingActivity.class);
        intent.putExtra(USER_ID, user.getId());
        String conversationId = getConversationId(user.getId());
        if (conversationId != null) {
            intent.putExtra(CONVERSATION_ID, conversationId);
        }
        startActivity(intent);
    }

    private String getConversationId(String id) {
        for (ConversationDetails details : mConversationDetails) {
            if (details.getOtherUserId().equals(id)) {
                return details.getConversationId();
            }
        }
        return null;
    }
}