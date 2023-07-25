package com.example.newas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Home extends AppCompatActivity {

    Button settings_btn;
    Button location_btn;
    Button profile_btn;
    Button walk_btn;
    private ListView list_view;
    private ArrayList<User> userArrayList;
    private ArrayAdapter<User> userArrayAdapter;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        settings_btn = findViewById(R.id.settings_btn);
        location_btn = findViewById(R.id.location_btn);
        profile_btn = findViewById(R.id.profile_btn);
        walk_btn =findViewById(R.id.walk_btn);

//        list initialize
//        list_view = findViewById(R.id.list_view);
//        userArrayList = new ArrayList<User>();
//        userArrayAdapter = new ArrayAdapter<>(this, R.layout.custom_row, userArrayList);
//        list_view.setAdapter(userArrayAdapter);
//
//        mAuth = FirebaseAuth.getInstance();
//        database = FirebaseDatabase.getInstance();
//
//        Log.d("main", "before");
//
//        database.getReference("Users").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Log.d("main", "inside");
//                userArrayList = new ArrayList<User>();
//                for (DataSnapshot data:snapshot.getChildren()) {
//                    User user = data.getValue(User.class);
//                    userArrayList.add(user);
//                }
//                list_view = findViewById(R.id.list_view);
//                userArrayAdapter = new UserArrayAdapter(Home.this, R.layout.custom_row, userArrayList);
//                list_view.setAdapter(userArrayAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.d("main", "database error");
//
//            }
//        });

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


        walk_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Home.this, Navigation.class);
                startActivity(i);
            }
        });
    }
}