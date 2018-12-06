package com.example.wlblu.chatchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingActivity extends AppCompatActivity {
    //store the database reference
    private DatabaseReference getUserDataReference;
    //user authentication
    private FirebaseAuth mAuth;
    //image storage reference
    private StorageReference storageProfileImageReference;

    private StorageReference thumbImageReference;

    private CircleImageView settingsDisplayProfileImage;
    private TextView settingDisplayName;
    private TextView settingDisplayStatus;
    private Button settingsChangeProfileButton;
    private Button settingsChangeStatusButton;
    private ProgressDialog loadingBar;
    private Toolbar mToolbar;

    private final static int GALLERY_pICK = 1;

    Bitmap thumb_bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //get the data of current user by id
        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar)findViewById(R.id.setting_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Setting");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get online user's unique id
        String online_user_id = mAuth.getCurrentUser().getUid();

        getUserDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        getUserDataReference.keepSynced(true);

        storageProfileImageReference = FirebaseStorage.getInstance().getReference().child("profile_image");

        thumbImageReference = FirebaseStorage.getInstance().getReference().child("thumb_image");

        //UI connection
        settingsDisplayProfileImage = (CircleImageView) findViewById(R.id.settings_profile_image);
        settingDisplayName = (TextView) findViewById(R.id.settings_user_name);
        settingDisplayStatus = (TextView) findViewById(R.id.settings_user_status);
        settingsChangeProfileButton = (Button) findViewById(R.id.setting_change_image_button);
        settingsChangeStatusButton = (Button) findViewById(R.id.setting_change_status_button);
        loadingBar = new ProgressDialog(this);


        getUserDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //get the user data from database and store it in our variables
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                final String image = dataSnapshot.child("user_image").getValue().toString();
                final String thumb_image = dataSnapshot.child("user_thumb_image").getValue().toString();

                settingDisplayName.setText(name);
                settingDisplayStatus.setText(status);

                //if the user choose their own profile image, display it
                //otherwise, display default profile
                if(!image.equals("default_profile"))
                {
                    Picasso.get().load(thumb_image).placeholder(R.drawable.default_profile).into(settingsDisplayProfileImage);

                    /*
                    Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile).into(settingsDisplayProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            Picasso.get().load(thumb_image).placeholder(R.drawable.default_profile).into(settingsDisplayProfileImage);
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    }); */
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        settingsChangeProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send the user to their mobile phone gallery
                //pick image from user gallery
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_pICK);
            }
        });

        settingsChangeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldStatus = settingDisplayStatus.getText().toString();
                Intent statusIntent = new Intent(SettingActivity.this,StatusActivity.class);
                statusIntent.putExtra("user_status",oldStatus);
                startActivity(statusIntent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //crop image
        if (requestCode == GALLERY_pICK && resultCode == RESULT_OK && data != null) {
            Uri ImageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        //crop the image user picked from their gallery to firebase database
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Updating profile Image");
                loadingBar.setMessage("Please wait, while we are updating your profile image");
                loadingBar.show();

                Uri resultUri = result.getUri();

                //create a new path to store the compress image
                File thumb_fiepathUri = new File(resultUri.getPath());

                String userId = mAuth.getCurrentUser().getUid();

                //set the size and quality of the thumb_image
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(50)
                            .compressToBitmap(thumb_fiepathUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //store the thumbe image to firebase database
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                final byte[] thumb_byte = byteArrayOutputStream.toByteArray();

                StorageReference filePath = storageProfileImageReference.child(userId + ".jpg");

                final StorageReference thumb_filePath = thumbImageReference.child(userId + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingActivity.this, "Saving your profile image to Firebase Storage...", Toast.LENGTH_LONG).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                    if (task.isSuccessful())
                                    {
                                        Map update_user_data = new HashMap();
                                        update_user_data.put("user_image", downloadUrl);
                                        update_user_data.put("user_thumb_image", thumb_downloadUrl);

                                        getUserDataReference.updateChildren(update_user_data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(SettingActivity.this, "Profile Image uploaded successfully...", Toast.LENGTH_LONG).show();
                                                loadingBar.dismiss();
                                            }
                                        });
                                    }
                                }

                            });

                        }
                        else
                        {
                            Toast.makeText(SettingActivity.this, "Error occurred, while uploading your profile image", Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();
                        }
                    }

                });

            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}