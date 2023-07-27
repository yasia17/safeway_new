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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Home extends AppCompatActivity {

    Button settings_btn;
    Button location_btn;
    Button profile_btn;
    Button walk_btn;

    Button help_btn;
    private ListView list_view;
    private ArrayList<User> userArrayList;
    private ArrayAdapter<User> userArrayAdapter;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private TextView helloUser;
    private DatabaseReference usersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        settings_btn = findViewById(R.id.settings_btn);
        location_btn = findViewById(R.id.location_btn);
        profile_btn = findViewById(R.id.profile_btn);
        walk_btn =findViewById(R.id.walk_btn);
        helloUser = findViewById(R.id.hello_user);
        help_btn = findViewById(R.id.help_btn);


        mAuth = FirebaseAuth.getInstance();

        //getting the current user and their uid
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();

        // Initialize the DatabaseReference to "Users" node
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        // Read the user data from the database
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Deserialize the user data
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    // Display the user's name in the TextView
                    String name = user.getFirstName();
                    helloUser.setText("Hello, " + name + "!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that may occur
            }
        });

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

        help_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Home.this, HelpList.class);
                startActivity(i);
            }
        });

    }



//    protected void onStart() {
//        super.onStart();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            String displayName = currentUser.getDisplayName();
//            if (displayName != null && !displayName.isEmpty()) {
//                String welcomeMessage = "Hello " + displayName;
//                hello_user.setText(welcomeMessage);
//            }
//        }
//    }
}