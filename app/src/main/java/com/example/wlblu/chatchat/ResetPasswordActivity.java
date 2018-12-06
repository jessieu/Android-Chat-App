package com.example.wlblu.chatchat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private Toolbar mToolBar;
    private TextView emailAdress;
    private Button SendEmailButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();

        mToolBar = (Toolbar)findViewById(R.id.reset_password_app_bar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Reset Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        emailAdress = findViewById(R.id.reset_password_email);
        SendEmailButton = (Button)findViewById(R.id.reset_password_button);

        SendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = emailAdress.getText().toString();

                if(TextUtils.isEmpty(userEmail)){
                    Toast.makeText(ResetPasswordActivity.this, "Please Enter Your Email Address...", Toast.LENGTH_LONG).show();
                }

                else {
                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ResetPasswordActivity.this,"Reset Password Link Sent. Please Check Your Email.", Toast.LENGTH_LONG).show();
                                Intent loginIntent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                startActivity(loginIntent);
                            }

                            else {
                                String message = task.getException().toString();
                                Toast.makeText(ResetPasswordActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

    }
}
