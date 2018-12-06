package com.example.wlblu.chatchat;

import android.app.Notification;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wlblu.chatchat.Messages;
import com.example.wlblu.chatchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;

    public MessageAdapter(List<Messages> userMessageList) {
        this.userMessageList = userMessageList;
    }

    public class  MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView senderMessageText,receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView senderMessagePicture, receiverMessagePicture;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = (TextView)itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView)itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView)itemView.findViewById(R.id.message_profile_image);

            senderMessagePicture = (ImageView)itemView.findViewById(R.id.sender_message_image_view);
            receiverMessagePicture = (ImageView)itemView.findViewById(R.id.receiver_message_image_view);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message_layout_of_user,viewGroup,false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {

        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(i);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("user_image")){

                    String receiverImage = dataSnapshot.child("user_thumb_image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.default_profile).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(fromMessageType.equals("text")){
            messageViewHolder.receiverProfileImage.setVisibility(View.INVISIBLE);
            messageViewHolder.receiverMessageText.setVisibility(View.INVISIBLE);

            if(fromUserID.equals(messageSenderID)){
                messageViewHolder.receiverMessagePicture.setVisibility(View.GONE);
                messageViewHolder.senderMessagePicture.setVisibility(View.GONE);

                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.message_text_background);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageText.setText(messages.getMessage());


            }

            else {
                messageViewHolder.receiverMessagePicture.setVisibility(View.GONE);
                messageViewHolder.senderMessagePicture.setVisibility(View.GONE);

                messageViewHolder.senderMessageText.setVisibility(View.INVISIBLE);

                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.message_text_background_two);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setText(messages.getMessage());



            }
        }

        else
        {
            messageViewHolder.receiverProfileImage.setVisibility(View.INVISIBLE);
            messageViewHolder.receiverMessageText.setVisibility(View.INVISIBLE);

            if(fromUserID.equals(messageSenderID)){
                messageViewHolder.receiverMessageText.setVisibility(View.INVISIBLE);
                messageViewHolder.senderMessageText.setVisibility(View.INVISIBLE);

                messageViewHolder.senderMessagePicture.setVisibility(View.VISIBLE);

                //messageViewHolder.receiverMessagePicture.setVisibility(View.INVISIBLE);

                messageViewHolder.senderMessageText.setPadding(0,0,0,0);

                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.default_profile).into(messageViewHolder.senderMessagePicture);
            }
            else{
                messageViewHolder.receiverMessageText.setVisibility(View.INVISIBLE);
                messageViewHolder.senderMessageText.setVisibility(View.INVISIBLE);

                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessagePicture.setVisibility(View.VISIBLE);
                //messageViewHolder.senderMessagePicture.setVisibility(View.INVISIBLE);
                messageViewHolder.receiverMessageText.setPadding(0,0,0,0);

                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.default_profile).into(messageViewHolder.receiverMessagePicture);
            }

        }

    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }




}
