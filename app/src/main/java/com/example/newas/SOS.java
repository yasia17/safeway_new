package com.example.newas;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.constraintlayout.widget.ConstraintLayout; // Import the ConstraintLayout class

import com.example.newas.Navigation;
import com.example.newas.R;

public class SOS extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        // Get references to the ConstraintLayouts
        ConstraintLayout callPolice = findViewById(R.id.callPolice);
        ConstraintLayout callMom = findViewById(R.id.callMom);
        ConstraintLayout callSis = findViewById(R.id.callSis);
        ConstraintLayout callFriend = findViewById(R.id.callFriend);
        Button GoBack = findViewById(R.id.GoBackBtn);

        // Set OnClickListener for each ConstraintLayout
        callPolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SOS.this, "Calling Police...", Toast.LENGTH_LONG).show();
            }
        });

        callMom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SOS.this, "Calling Mom...", Toast.LENGTH_LONG).show();
            }
        });

        callSis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SOS.this, "Calling Sister...", Toast.LENGTH_LONG).show();
            }
        });

        callFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SOS.this, "Calling Friend...", Toast.LENGTH_LONG).show();
            }
        });

        GoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SOS.this, Navigation.class);
                startActivity(i);
            }
        });
    }
}
