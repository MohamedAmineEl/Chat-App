package com.apps.chat_apps.activity;

import static com.apps.chat_apps.constant.FirebaseDataPaths.USERS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.apps.chat_apps.R;
import com.apps.chat_apps.model.User;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

public class ChangeProfileImageActivity extends AppCompatActivity {
    private static final int RESULT_LOAD_IMG = 1;

    private ImageView mProfileImg;
    private ImageView mSelectImgBtn;

    private FirebaseUser mUser;

    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;

    private boolean mIsUploading;
    private Uri mImageUri;
    private String mImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile_image);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        mStorageReference = storage.getReference();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabaseReference = database.getReference(USERS).child(mUser.getUid());
        initView();
    }

    private void initView() {
        findViewById(R.id.back_btn).setOnClickListener(v -> finish());
        mProfileImg = findViewById(R.id.profile_img_iv);
        initProfileImageView();
        Button updateBtn = findViewById(R.id.update_image_btn);
        mSelectImgBtn = findViewById(R.id.pick_img_btn);
        mSelectImgBtn.setOnClickListener(b -> chooseImage());
        updateBtn.setOnClickListener(b -> {
            if (mImageUri == null) {
                Toast.makeText(this, "You haven't change your image", Toast.LENGTH_SHORT).show();
                return;
            }
            updateProfileImg();
        });
    }

    private void initProfileImageView() {
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    if (user.getImageURL().equals("default")) {
                        mProfileImg.setImageResource(R.drawable.default_profile_pic);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(user.getImageURL())
                                .placeholder(R.drawable.default_profile_pic)
                                .into(mProfileImg);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void chooseImage() {
        mImageUri = null;
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                mImageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(mImageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                mProfileImg.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(ChangeProfileImageActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(ChangeProfileImageActivity.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }

    private void updateProfileImg() {
        mIsUploading = true;
        ProgressDialog progressDialog = new ProgressDialog(this, R.style.ProgressDialogTheme);
        progressDialog.setMessage("Updating your profile...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        StorageReference ref = mStorageReference
                .child("user_pictures/" + UUID.randomUUID().toString());
        ref.putFile(mImageUri)
                .addOnSuccessListener(
                        taskSnapshot -> {
                            taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        mImageUrl = task.getResult().toString();
                                        updateUserProfileData(progressDialog);
                                    } else {
                                        mIsUploading = false;
                                        progressDialog.dismiss();
                                        Toast.makeText(ChangeProfileImageActivity.this, "Something went wrong, retry again!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        })
                .addOnFailureListener(e -> {
                    mIsUploading = false;
                    progressDialog.dismiss();
                    Toast.makeText(ChangeProfileImageActivity.this, "Something went wrong, retry again!", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserProfileData(ProgressDialog dialog) {
        mDatabaseReference.child("imageURL")
                .setValue(mImageUrl)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mIsUploading = false;
                        dialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(ChangeProfileImageActivity.this, "Post published successfully", Toast.LENGTH_SHORT).show();
                            ChangeProfileImageActivity.this.finish();
                        } else {
                            Toast.makeText(ChangeProfileImageActivity.this, "Something went wrong, retry again!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    @Override
    public void onBackPressed() {
        if (mIsUploading) {
            Toast.makeText(this, "Wait until posting finish", Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }
}