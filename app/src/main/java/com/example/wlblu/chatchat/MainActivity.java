package com.example.wlblu.chatchat;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private Toolbar mToolbar;

    //set up the fragment
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsPagerAdapter myTabPagerAdapter;
    FirebaseUser currentUser;
    private DatabaseReference UserReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get current authentication instance and store it in mAuth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        RootRef = FirebaseDatabase.getInstance().getReference();

        if(currentUser != null){
            if(!currentUser.isEmailVerified()){

                mAuth.signOut();
                logoutUser();
                Toast.makeText(MainActivity.this, "Please verify your email first.", Toast.LENGTH_LONG).show();
            }

            else {
                String online_user_id = mAuth.getCurrentUser().getUid();
                UserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
            }
        }


        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ChatChat");

        myViewPager = (ViewPager)findViewById(R.id.main_tab_pager);
        myTabPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabPagerAdapter);
        myTabLayout = (TabLayout)findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //get current user
         currentUser = mAuth.getCurrentUser();

        //if the user does not login, we send them to the startpage, either login or register a new account
        //also after they are sent to the startpage,when they click on the back button at the startpage
        // we prevent them back to main activity
        if(currentUser == null)
        {
            logoutUser();
        }

        else if(currentUser != null){
            UserReference.child("online").setValue("true");
        }
    }

    //user minimize the app
    @Override
    protected void onStop() {
        super.onStop();

        if(currentUser != null){
            UserReference.child("online").setValue(ServerValue.TIMESTAMP);
        }

    }

    private void logoutUser() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    //add the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        //when the use click on the log out button on the menu, we log him out
        if(item.getItemId()==R.id.main_log_out_button){
            if(currentUser != null){
                UserReference.child("online").setValue(ServerValue.TIMESTAMP);
            }
            mAuth.signOut();
            logoutUser();
        }

        if(item.getItemId()==R.id.main_account_settings_button){
            Intent settingIntent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(settingIntent);
        }

        if(item.getItemId()==R.id.main_all_users_button){
            Intent settingIntent = new Intent(MainActivity.this, AllUsersActivity.class);
            startActivity(settingIntent);
        }

        if(item.getItemId()==R.id.main_group_create_button){
            RequestNewGroup();
        }

        return true;
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name: ");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g Good Floks");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String groupName = groupNameField.getText().toString();

                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Please write Group Name...",Toast.LENGTH_LONG).show();
                }

                else{

                    CreateNewGroup(groupName);

                }


            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void CreateNewGroup(final String groupName) {

        RootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(MainActivity.this,groupName + " is created successfully", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

}
