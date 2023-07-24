package com.example.newas;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.newas.R;
import com.example.newas.User;

import java.util.ArrayList;

public class UserArrayAdapter extends ArrayAdapter<User> {
    private Context context;
    private int resource;


    public UserArrayAdapter(@NonNull Context context, int resource, ArrayList<User> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(this.context).inflate(this.resource, parent, false);
        }

        User user = getItem(position);
        if (user!=null) {
            TextView user_name = convertView.findViewById(R.id.name_row);
            TextView user_age = convertView.findViewById(R.id.age_row);
            user_name.setText(user.getFirstName());
            user_age.setText(user.getAge());

        }

        return convertView;

    }
}

