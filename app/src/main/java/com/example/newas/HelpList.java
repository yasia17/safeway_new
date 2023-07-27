package com.example.newas;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.newas.DistressCall;
import com.example.newas.DistressCallAdapter;
import com.example.newas.Home;
import com.example.newas.Navigation;
import com.example.newas.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class HelpList extends AppCompatActivity {

    private LatLng userLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_list);

        Button goBack = findViewById(R.id.GoBackBtn);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i =  new Intent(HelpList.this, Home.class);
                startActivity(i);
            }
        });

        // Sample data: Replace this with your actual list of DistressCall objects
        List<DistressCall> distressCallList = new ArrayList<>();
        distressCallList.add(new DistressCall("Example", "One", "place 1"));
        distressCallList.add(new DistressCall("Example", "Two", "place 2"));
        distressCallList.add(new DistressCall("Example", "Three", "place 3"));

        // Create the custom adapter and set it to the ListView
        DistressCallAdapter adapter = new DistressCallAdapter(this, distressCallList);
        ListView listView = findViewById(R.id.HelpList);
        listView.setAdapter(adapter);

        // Set the item click listener for the ListView
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//                // Get the selected DistressCall object from the adapter
//                DistressCall selectedDistressCall = distressCallList.get(position);
//
//                Intent ii=new Intent(HelpList.this, Navigation.class);
//                ii.putExtra("list_click", "clicked");
//                startActivity(ii);
//
//            }
//        });
    }


}
