package com.example.wlblu.chatchat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference UsersReference;
    private DatabaseReference FriendRequestReference;
    private FirebaseAuth mAuth;
    private DatabaseReference FriendsReference;
    private DatabaseReference NotificationReference;

    private String receiver_user_id;
    private String sender_user_id;
    private String CURRENT_STATE;

    private CircleImageView userProfileImage;
    private TextView profileName;
    private TextView profileStatus;
    private Button SendFriendRequestButton;
    private Button DeclineFriendRequestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth =FirebaseAuth.getInstance();

        UsersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        FriendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        FriendRequestReference.keepSynced(true);

        FriendsReference = FirebaseDatabase.getInstance().getReference().child("Friend");
        FriendsReference.keepSynced(true);

        NotificationReference = FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiver_user_id = getIntent().getExtras().get("visit_user_id").toString();
        sender_user_id = mAuth.getCurrentUser().getUid();

        userProfileImage = (CircleImageView) findViewById(R.id.profile_visit_user_image);
        profileName = (TextView)findViewById(R.id.profile_visit_username);
        profileStatus = (TextView)findViewById(R.id.profile_visit_user_status);
        SendFriendRequestButton = (Button)findViewById(R.id.profile_visit_send_req_btn);
        DeclineFriendRequestButton = (Button)findViewById(R.id.profile_decline_friend_req_btn);

        CURRENT_STATE = "not_friends";

        UsersReference.child(receiver_user_id).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userImage = dataSnapshot.child("user_image").getValue().toString();
                String userName = dataSnapshot.child("user_name").getValue().toString();
                String userStatus = dataSnapshot.child("user_status").getValue().toString();

                Picasso.get().load(userImage).placeholder(R.drawable.default_profile).into(userProfileImage);
                profileName.setText(userName);
                profileStatus.setText(userStatus);

                FriendRequestReference.child(sender_user_id).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        //not friends
                        if (dataSnapshot.hasChild(receiver_user_id)) {
                            String req_type = dataSnapshot.child(receiver_user_id).child("request_type").getValue().toString();
                            if (req_type.equals("sent")) {
                                CURRENT_STATE = "request_sent";
                                SendFriendRequestButton.setText("Cancel Friend Request");
                            }
                            else if (req_type.equals("received")) {
                                CURRENT_STATE = "request_received";
                                SendFriendRequestButton.setText("Accept Friend Request");

                                DeclineFriendRequestButton.setVisibility(View.VISIBLE);
                                DeclineFriendRequestButton.setEnabled(true);

                                DeclineFriendRequestButton.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        DeclineFriendRequest();
                                    }
                                    });
                                }
                            }
                        //already friends
                       else
                            {
                                FriendsReference.child(sender_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild(receiver_user_id)){
                                            CURRENT_STATE = "friends";
                                            SendFriendRequestButton.setText("Unfriend This Person");

                                            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                            DeclineFriendRequestButton.setEnabled(false);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
        DeclineFriendRequestButton.setEnabled(false);


        if(!sender_user_id.equals(receiver_user_id)){
            SendFriendRequestButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    SendFriendRequestButton.setEnabled(false);

                    if(CURRENT_STATE.equals("not_friends"))
                    {
                        SendFriendRequestToAPerson();
                    }

                    if(CURRENT_STATE.equals("request_sent")){
                        CancelFriendRequest();
                    }

                    if(CURRENT_STATE.equals("request_received")){
                        AcceptFriendRequest();
                    }

                    if(CURRENT_STATE.equals("friends"))
                    {
                        UnfriendAFriend();
                    }
                }
            });
        }

        else{
            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
            SendFriendRequestButton.setVisibility(View.INVISIBLE);
        }
        }

    private void DeclineFriendRequest()
    {
        FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                SendFriendRequestButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendFriendRequestButton.setText("Send Friend Request");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });

                        }
                    }
                });
    }


    private void UnfriendAFriend() {
        FriendsReference.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendsReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                SendFriendRequestButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendFriendRequestButton.setText("Send Friend Request");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }

                    }
                });
    }


    private void AcceptFriendRequest() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        final String saveCurrentDate = currentDate.format(calForDate.getTime());

        //record of the date sender and receiver become friends
        FriendsReference.child(sender_user_id).child(receiver_user_id).child("date").setValue(saveCurrentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        //the date receiver and sender become friends
                        FriendsReference.child(receiver_user_id).child(sender_user_id).child("date").setValue(saveCurrentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                SendFriendRequestButton.setEnabled(true);
                                                                                CURRENT_STATE = "friends";
                                                                                SendFriendRequestButton.setText("Unfriend");

                                                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                                DeclineFriendRequestButton.setEnabled(false);
                                                                            }
                                                                        }
                                                                    });
                                                        }

                                                    }
                                                });

                                    }
                                });
                    }
                });

    }

    private void SendFriendRequestToAPerson()
    {
        FriendRequestReference.child(sender_user_id).child(receiver_user_id)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    FriendRequestReference.child(receiver_user_id).child(sender_user_id)
                            .child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        HashMap<String,String> notificationData = new HashMap<String, String>();
                                        notificationData.put("from",sender_user_id);
                                        notificationData.put("type","request");

                                        NotificationReference.child(receiver_user_id).push().setValue(notificationData)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            SendFriendRequestButton.setEnabled(true);
                                                            CURRENT_STATE = "request_sent";
                                                            SendFriendRequestButton.setText("Cancel Friend Request");

                                                            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                            DeclineFriendRequestButton.setEnabled(false);
                                                        }
                                                    }
                                                });

                                    }
                                }
                            });
                }
            }
        });

    }

    private void CancelFriendRequest()
    {
        FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                SendFriendRequestButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendFriendRequestButton.setText("Send Friend Request");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });

                        }
                    }
                });
    }


}

