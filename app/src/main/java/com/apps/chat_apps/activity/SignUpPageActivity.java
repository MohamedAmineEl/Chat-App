package com.apps.chat_apps.activity;

import static com.apps.chat_apps.constant.FirebaseDataPaths.USERS;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.apps.chat_apps.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpPageActivity extends AppCompatActivity {
    private EditText mEmailTV, mPasswordTV, mUserNameTV;
    private TextView mErrorTV;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog mProgressDialog;
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        firebaseAuth = FirebaseAuth.getInstance();
        mEmailTV = findViewById(R.id.EmailTb2);
        mPasswordTV = findViewById(R.id.PasswordTb2);
        mUserNameTV = findViewById(R.id.UserNameTB);
        mErrorTV = findViewById(R.id.error_tv);
        TextView loginBtn = findViewById(R.id.LoginLink);
        Button registerBtn = findViewById(R.id.RegisterBtn);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(i);
            }
        });
        mProgressDialog = new ProgressDialog(this, R.style.ProgressDialogTheme);
        mProgressDialog.setCancelable(false);

        keyLess(mEmailTV);
        keyLess(mPasswordTV);
    }

    private void registerUser() {
        mErrorTV.setText("");
        String email = mEmailTV.getText().toString().trim();
        String password = mPasswordTV.getText().toString().trim();
        final String username = mUserNameTV.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            mErrorTV.setText("Please enter email");
        } else {
            if (validate(email)) {
                if (TextUtils.isEmpty(password)) {
                    mErrorTV.setText("Please enter password");
                } else {
                    if (TextUtils.isEmpty(username)) {
                        mErrorTV.setText("Please enter password");
                    } else {
                        mProgressDialog.setMessage("Register Please Wait..");
                        mProgressDialog.show();
                        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                                String userid = firebaseUser.getUid();
                                                reference = FirebaseDatabase.getInstance().getReference(USERS).child(userid);
                                                HashMap<String, String> hashMap = new HashMap<>();
                                                hashMap.put("id", userid);
                                                hashMap.put("name", username);
                                                hashMap.put("imageURL", "default");
                                                hashMap.put("status", "offline");
                                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        mProgressDialog.dismiss();
                                                        AlertDialog alertDialog = new AlertDialog.Builder(SignUpPageActivity.this).create();
                                                        alertDialog.setTitle("Verify by Email");
                                                        alertDialog.setMessage("Registered successfully. Please check email for verification.");
                                                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        dialog.dismiss();
                                                                        FirebaseAuth.getInstance().signOut();
                                                                        Intent i = new Intent(getApplicationContext(), SignInActivity.class);
                                                                        startActivity(i);
                                                                    }
                                                                });
                                                        alertDialog.show();
                                                        mEmailTV.setText("");
                                                        mPasswordTV.setText("");
                                                        mErrorTV.setText("");
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        mProgressDialog.dismiss();
                                                        mErrorTV.setText("Error in registering,Please try again later");
                                                    }
                                                });

                                            } else {
                                                mProgressDialog.dismiss();
                                                Toast.makeText(SignUpPageActivity.this, "Couldn't register make you are connected to internet", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                } else {
                                    try {
                                        throw task.getException();
                                    } catch (FirebaseAuthWeakPasswordException weakPassword) {
                                        mProgressDialog.dismiss();
                                        mErrorTV.setText("Weak Password");
                                    } catch (FirebaseAuthUserCollisionException existEmail) {
                                        mProgressDialog.dismiss();
                                        mErrorTV.setText("This email is already registered");
                                    } catch (Exception e) {
                                        mProgressDialog.dismiss();
                                        Toast.makeText(SignUpPageActivity.this, "Could'nt register make you are connected to internet", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
                    }
                }
            } else {
                mErrorTV.setText("Invalid email address");
            }
        }
    }

    private boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    private void keyLess(EditText e) {

        e.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mErrorTV.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), SignInActivity.class);
        startActivity(i);
        finish();
    }
}
