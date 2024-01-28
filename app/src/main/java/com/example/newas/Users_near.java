package com.example.newas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import java.util.ArrayList;

public class Users_near extends AppCompatActivity {

    private RecyclerView nameView;
    private Button rotate, link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_near);

        nameView = findViewById(R.id.nameView);
        rotate = findViewById(R.id.rotate);
        link = findViewById(R.id.button2);

        Animation rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);

        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotate.startAnimation(rotateAnim);
            }
        });

        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com"));
                startActivity(i);
            }
        });

        ArrayList<Name> names = new ArrayList<>();
        names.add(new Name("User 1"));
        names.add(new Name("Ruslan"));
        names.add(new Name("Ne ruslan"));
        names.add(new Name("Ruslan4ik"));
        names.add(new Name("On slomal coedd"));

        ViewNameAdapter adapter = new ViewNameAdapter(this);
        adapter.setNames(names);
        nameView.setAdapter(adapter);
        nameView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

    }
}