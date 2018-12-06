package com.example.wlblu.chatchat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity
{
    private DatabaseReference allDatabaseUserReference;

    private Toolbar mToolbar;
    private RecyclerView allUsersList;
    private EditText SearchInputText;
    private ImageButton SearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        allDatabaseUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        allDatabaseUserReference.keepSynced(true);

        mToolbar = (Toolbar)findViewById(R.id.all_users_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SearchButton = (ImageButton)findViewById(R.id.search_people_button);
        SearchInputText = (EditText)findViewById(R.id.search_box);

        allUsersList = (RecyclerView)findViewById(R.id.all_users_list);
        allUsersList.setHasFixedSize(true);
        allUsersList.setLayoutManager(new LinearLayoutManager(this));

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchUsername = SearchInputText.getText().toString();
                if(TextUtils.isEmpty(searchUsername)) {
                    Toast.makeText(AllUsersActivity.this,"Please write a user name to search...", Toast.LENGTH_LONG).show();
                }
                SearchForPeopleAndFriends(searchUsername);
            }
        });
    }

   private void SearchForPeopleAndFriends(String searchUserName)
   {
       Toast.makeText(this, "Searching...", Toast.LENGTH_LONG).show();

       Query searchPeopleAndFriends = allDatabaseUserReference.orderByChild("user_name").startAt(searchUserName).endAt(searchUserName + "\uf8ff");

        FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder>
                (
                        AllUsers.class,
                        R.layout.all_users_display_layout,
                        AllUsersViewHolder.class,
                        searchPeopleAndFriends
                )
        {
            @Override
            protected void populateViewHolder(AllUsersViewHolder viewHolder, AllUsers model, final int position)
            {
                viewHolder.setUser_name(model.getUser_name());
                viewHolder.setUser_status(model.getUser_status());
                viewHolder.setUser_thumb_image(model.getUser_thumb_image());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();

                        Intent profileIntent = new Intent(AllUsersActivity.this,ProfileActivity.class );
                        profileIntent.putExtra("visit_user_id",visit_user_id);
                        startActivity(profileIntent);
                    }
                });
            }
        };

        allUsersList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class AllUsersViewHolder extends RecyclerView.ViewHolder
    {

        View mView;

        public AllUsersViewHolder(@NonNull View itemView)
        {
            super(itemView);
            mView = itemView;
        }

        public void setUser_name(String user_name){
            TextView name =(TextView)mView.findViewById(R.id.all_users_username);
            name.setText(user_name);
        }

        public void setUser_status(String user_status){
            TextView status = (TextView)mView.findViewById(R.id.all_users_status);
            status.setText(user_status);

        }

        public void setUser_thumb_image(final String user_thumb_image){
            final CircleImageView thumb_image = (CircleImageView)mView.findViewById(R.id.all_users_profile_image);
            Picasso.get().load(user_thumb_image).networkPolicy(NetworkPolicy.OFFLINE).into(thumb_image, new Callback() {
                @Override
                public void onSuccess() {
                    Picasso.get().load(user_thumb_image).into(thumb_image);
                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(user_thumb_image).placeholder(R.drawable.default_profile).into(thumb_image);

                }
            });
        }

    }
}
