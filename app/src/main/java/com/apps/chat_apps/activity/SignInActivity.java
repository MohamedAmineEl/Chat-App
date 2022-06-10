package com.apps.chat_apps.activity;

import android.app.ProgressDialog;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignInActivity extends AppCompatActivity {
    private EditText mEmailTV;
    private EditText mPasswordTV;
    private TextView mErrorTV;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog mProgressDialog;
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private static int SP = 5000;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mProgressDialog = new ProgressDialog(this, R.style.ProgressDialogTheme);
        mProgressDialog.setCancelable(false);
        TextView resetpwdtv = findViewById(R.id.ResetPwdLink);
        mEmailTV = findViewById(R.id.EmailTb2);
        mPasswordTV = findViewById(R.id.PasswordTb2);
        Button loginbutton = findViewById(R.id.LoginBtn);
        TextView signuplinktextview = findViewById(R.id.SignUpLink);
        mErrorTV = findViewById(R.id.error_login_tv);
        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogin();
            }
        });
        signuplinktextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SignUpPageActivity.class);
                startActivity(i);
            }
        });
        resetpwdtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ResetPasswordActivity.class);
                startActivity(i);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
        keyLess(mEmailTV);
        keyLess(mPasswordTV);
    }

    private boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    private void userLogin() {
        String email = mEmailTV.getText().toString().trim();
        String password = mPasswordTV.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            mErrorTV.setText("Please enter email address");
        } else {

            if (TextUtils.isEmpty(password)) {
                mErrorTV.setText("Please enter password");
            } else {
                if (validate(email)) {
                    mProgressDialog.setMessage("Authenticating Please Wait..");
                    mProgressDialog.show();
                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                mProgressDialog.dismiss();
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(i);
                            } else {
                                mProgressDialog.dismiss();
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthInvalidUserException invalidEmail) {
                                    mErrorTV.setText("Account doesn't exist");
                                } catch (FirebaseAuthInvalidCredentialsException wrongPassword) {
                                    mErrorTV.setText("Invalid password");
                                } catch (Exception e) {
                                    Toast.makeText(SignInActivity.this, "Couldn't able to login please try again", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                } else {
                    mErrorTV.setText("Invalid email address");
                }
            }
        }
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
        finishAffinity();
    }
}
