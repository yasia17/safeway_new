package com.example.newas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        ImageButton Settings = findViewById(R.id.SettingsBtn);
        ImageButton Nav = findViewById(R.id.NavBtn);
        ImageButton Profile = findViewById(R.id.ProfileBtn);


        Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Settings.this,Settings.class);
                startActivity(i);
            }
        });

        Nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Settings.this,Navigation.class);
                startActivity(i);
            }
        });

        Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Settings.this,Profile.class);
                startActivity(i);
            }
        });
    }
}