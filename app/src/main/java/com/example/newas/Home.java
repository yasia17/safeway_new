package com.example.newas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Home extends AppCompatActivity {

    Button settings_btn;
    Button location_btn;
    Button profile_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        settings_btn = findViewById(R.id.settings_btn);
        location_btn = findViewById(R.id.location_btn);
        profile_btn = findViewById(R.id.profile_btn);

        //on click to profile
        profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Home.this, Profile.class);
                startActivity(i);
            }
        });

        //on click to settings
        settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Home.this, Settings.class);
                startActivity(intent1);
            }
        });


        //on click to navigation
        location_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ii = new Intent(Home.this, Navigation.class);
                startActivity(ii);
            }
        });
    }
}