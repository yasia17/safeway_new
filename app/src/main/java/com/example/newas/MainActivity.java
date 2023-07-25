package com.example.newas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView SignUpLink;
    private FirebaseAuth mAuth;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    private boolean locationPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailEditText = findViewById(R.id.editText_email);
        passwordEditText = findViewById(R.id.editText_password);

        Button signInButton = findViewById(R.id.button_signin);
        Button signUp = findViewById(R.id.linkToSignUp);

        mAuth = FirebaseAuth.getInstance();


        Log.d("main", "in main activity");

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, data_info.class);
                startActivity(intent);
            }
        });
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (email.contains("@") && email.contains(".com") && password.length() >= 6) {
                    if (locationPermissionGranted) {
                        LoginUser(email, password);
                    } else {
                        requestLocationPermission();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void requestLocationPermission() {
        if (PermissionHelper.checkLocationPermission(this)) {
            locationPermissionGranted = true;
            LoginUser(emailEditText.getText().toString(), passwordEditText.getText().toString());
        } else {
            PermissionHelper.requestLocationPermission(this);
        }
    }

    public void LoginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent i = new Intent(MainActivity.this, Home.class);
                            startActivity(i);
                        } else {
                            Toast.makeText(MainActivity.this, "Auth Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}




