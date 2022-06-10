package com.apps.chat_apps.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.apps.chat_apps.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResetPasswordActivity extends AppCompatActivity {
    private EditText mEmailTV;
    private TextView mErrorTV;
    private ProgressDialog mProgressDialog;
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        mEmailTV = findViewById(R.id.ReEmailTb);
        Button resetPasswordBtn = findViewById(R.id.ResetPwdBtn);
        mErrorTV = findViewById(R.id.error_tv_rp);
        mProgressDialog = new ProgressDialog(this, R.style.ProgressDialogTheme);
        mProgressDialog.setCancelable(false);
        mEmailTV.addTextChangedListener(new TextWatcher() {
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
        resetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmailTV.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    mErrorTV.setText("Please enter email address");
                } else {
                    if (validate(email)) {
                        mProgressDialog.setMessage("Please Wait..");
                        mProgressDialog.show();
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mProgressDialog.dismiss();
                                AlertDialog alertDialog = new AlertDialog.Builder(ResetPasswordActivity.this).create();
                                alertDialog.setTitle("Email sent");
                                alertDialog.setMessage("Link for reset password is been sent to " + email);
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                                new Handler().postDelayed(() -> {
                                    Intent i = new Intent(getApplicationContext(), SignInActivity.class);
                                    startActivity(i);
                                }, 5000);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mProgressDialog.dismiss();
                                mErrorTV.setText("Account does'nt exists");
                            }
                        });
                    } else {
                        mErrorTV.setText("Invalid email address");
                    }
                }
            }
        });
    }

    private boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), SignInActivity.class);
        startActivity(i);
        finish();
    }
}
