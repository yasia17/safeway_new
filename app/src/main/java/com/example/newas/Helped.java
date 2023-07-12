package com.example.newas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Helped extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helped);
    }

    public void back(View view) {
        Intent intent = new Intent(Helped.this, Signed_in.class);
        startActivity(intent);
    }
}