package com.example.wlblu.chatchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class StartPageActivity extends AppCompatActivity
{

    //declare buttons
    private Button AlreadyHaveAccountButton;
    private Button NeedNewAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);

        //connect button
        AlreadyHaveAccountButton = (Button)findViewById(R.id.already_have_account_button);
        NeedNewAccountButton = (Button)findViewById(R.id.need_new_account_button);

        //wait for user click on the button
        AlreadyHaveAccountButton.setOnClickListener(new View.OnClickListener()
        {
            //when user click on the button, send them to login page
            @Override
            public void onClick(View v)
            {
                Intent loginIntent = new Intent(StartPageActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        NeedNewAccountButton.setOnClickListener(new View.OnClickListener()
        {
            //when user click on the button, send them to register page
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(StartPageActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });
    }
}
