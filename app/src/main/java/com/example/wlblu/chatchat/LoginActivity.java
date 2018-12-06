package com.example.wlblu.chatchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rengwuxian.materialedittext.MaterialEditText;

import static com.google.firebase.database.FirebaseDatabase.getInstance;


public class LoginActivity extends AppCompatActivity
{
    //firebase authenication
    private FirebaseAuth mAuth;

    //add toolbar
    private Toolbar mToolbar;

    //Login function setup
    private Button LoginButton;
    private MaterialEditText LoginEmail;
    private MaterialEditText LoginPassword;
    private TextView NeedNewAccountLink;
    private TextView ResetPasswordLink;

    //loading progress
    private ProgressDialog LoadingBar;

    //store device token
    private DatabaseReference userReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");

        //enbale the toolbar function and display it
        mToolbar = (Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //connect with UI
        LoadingBar = new ProgressDialog(this);
        LoginButton = (Button)findViewById(R.id.login_button);
        LoginEmail =  findViewById(R.id.login_email);
        LoginPassword = findViewById(R.id.login_password);
        NeedNewAccountLink = (TextView)findViewById(R.id.need_new_account_link);
        ResetPasswordLink = (TextView)findViewById(R.id.forget_password_link);

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //take the user input of email and password
                String email = LoginEmail.getText().toString();
                String password = LoginPassword.getText().toString();

                loginUserAccount(email,password);
            }
        });

        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        ResetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resetPasswordIntent = new Intent(LoginActivity.this,ResetPasswordActivity.class);
                 startActivity(resetPasswordIntent);

            }
        });

        }


    //check validation of user input
    private void loginUserAccount(String email, String password)
    {
        if(TextUtils.isEmpty(email)){
            Toast.makeText(LoginActivity.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(LoginActivity.this, "Please enter your password.", Toast.LENGTH_SHORT).show();
        }

        else {
            //display loadingbar
            LoadingBar.setTitle("Login Account");
            LoadingBar.setMessage("Please wait, while we are preparing for your login");
            LoadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if(task.isSuccessful())
                    {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String online_user_id = mAuth.getCurrentUser().getUid();
                        String DeviceToken = FirebaseInstanceId.getInstance().getToken();

                        userReference.child(online_user_id).child("device_token").setValue(DeviceToken)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid)
                                    {
                                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();

                                    }
                                });

                    }

                    else
                    {
                        Toast.makeText(LoginActivity.this, "Error occurred! Please check your email and password.", Toast.LENGTH_SHORT).show();
                    }


                    }




                    });
            //after login in, clear the loadingbar
            LoadingBar.dismiss();

        }
    }

}
