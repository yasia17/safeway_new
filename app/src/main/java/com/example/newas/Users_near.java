package com.example.newas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class Users_near extends AppCompatActivity {

    private RecyclerView nameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_near);

        nameView = findViewById(R.id.nameView);

        ArrayList<Name> names = new ArrayList<>();
        names.add(new Name("hello, scroll"));
        names.add(new Name("wohoooo"));
        names.add(new Name("oh my god"));
        names.add(new Name("ofc ofc"));
        names.add(new Name("its working!!!!"));

        ViewNameAdapter adapter = new ViewNameAdapter(this);
        adapter.setNames(names);
        nameView.setAdapter(adapter);
        nameView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

    }
}