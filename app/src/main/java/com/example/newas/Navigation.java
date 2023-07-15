package com.example.newas;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.newas.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.EncodedPolyline;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Navigation extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private EditText destinationEditText;
    private GeoApiContext geoApiContext = null;
    private LatLng userLatLng;
    private List<Polyline> polylines = new ArrayList<>();

    private FirebaseAuth mAuth;

    private FirebaseDatabase db;

    private ArrayList<String> Addresses;

    private ListView distressCallListView;

    private ArrayAdapter<DistressCall> distressCallAdapter;
    private List<DistressCall> distressCalls = new ArrayList<>();

    private ImageButton helpRequestsButton;
    private TextView helpReqCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        System.out.println("navigation adfghj");
        Log.d("main", "in navigation");

        // Get references to UI elements
        destinationEditText = findViewById(R.id.Destination);
        Button searchButton = findViewById(R.id.Search);

        ImageButton Settings = findViewById(R.id.SettingsBtn);
        ImageButton Nav = findViewById(R.id.NavBtn);
        ImageButton Profile = findViewById(R.id.ProfileBtn);
        ImageButton Police = findViewById(R.id.CallPolice);
        helpRequestsButton = findViewById(R.id.HelpRequests);
        helpReqCounter = findViewById(R.id.helpReqCounter);

        distressCallListView = findViewById(R.id.distressCallListView);
        distressCallAdapter = new ArrayAdapter<>(this, R.layout.list_item_distress_call);
        distressCallListView.setAdapter(distressCallAdapter);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        db.getReference("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Addresses = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    User u = data.getValue(User.class);
                    String address = u.getAddress();
                    Addresses.add(address);

                    // Adding a marker for each address on the map
                    LatLng latLng = getLocationFromAddress(address);
                    if (latLng != null) {
                        googleMap.addMarker(new MarkerOptions().position(latLng).title(address));
                    }
                }

                // Update the help requests count
                updateHelpRequestsCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error if the retrieval is canceled or fails
            }
        });

        Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Navigation.this, Settings.class);
                startActivity(i);
            }
        });

        Nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Navigation.this, Navigation.class);
                startActivity(i);
            }
        });

        Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Navigation.this, Profile.class);
                startActivity(i);
            }
        });

        Police.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Navigation.this, "Call police", Toast.LENGTH_SHORT).show();
                // Call police functionality
            }
        });

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        try {
            mapFragment.getMapAsync(this); }
        catch (Exception e) {
        }

        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.Map_API_Key)).build();
        }

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Clear the map and polylines
                googleMap.clear();
                polylines.clear();

                // Get the destination entered by the user
                String destination = destinationEditText.getText().toString().trim();

                // Check if the destination is provided
                if (!destination.isEmpty()) {
                    // Get the location from the entered destination
                    LatLng destinationLatLng = getLocationFromAddress(destination);

                    if (destinationLatLng != null) {
                        // Add a marker for the destination
                        googleMap.addMarker(new MarkerOptions().position(destinationLatLng).title(destination));

                        // Draw the route on the map
                        drawRoute(userLatLng, destinationLatLng);

                        // Zoom the map to show both the user's location and the destination
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(userLatLng);
                        builder.include(destinationLatLng);
                        LatLngBounds bounds = builder.build();
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
                    } else {
                        Toast.makeText(Navigation.this, "Invalid destination address", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Navigation.this, "Please enter a destination", Toast.LENGTH_SHORT).show();
                }
            }
        });

        helpRequestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (distressCallListView.getVisibility() == View.VISIBLE) {
                    distressCallListView.setVisibility(View.GONE);
                } else {
                    distressCallListView.setVisibility(View.VISIBLE);
                }
            }
        });

        distressCallListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Handle distress call item click event
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if the location permission is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Request location updates
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Location location = task.getResult();
                        userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                        // Add a marker for the user's location
                        // Move the camera to the user's location
                        try {
                            googleMap.addMarker(new MarkerOptions().position(userLatLng).title("You are here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));}
                        catch (Exception e) {}



                        // Update the help requests count
                        updateHelpRequestsCount();
                    }
                }
            });
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private LatLng getLocationFromAddress(String address) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        LatLng latLng = null;
        try {
            addresses = geocoder.getFromLocationName(address, 1);
            if (!addresses.isEmpty()) {
                double latitude = addresses.get(0).getLatitude();
                double longitude = addresses.get(0).getLongitude();
                latLng = new LatLng(latitude, longitude);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return latLng;
    }

    private void drawRoute(LatLng origin, LatLng destination) {
        DirectionsApiRequest directionsApiRequest = new DirectionsApiRequest(geoApiContext);
        directionsApiRequest.origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude));
        directionsApiRequest.destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude));
        directionsApiRequest.setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                if (result.routes != null && result.routes.length > 0) {
                    DirectionsRoute route = result.routes[0];
                    EncodedPolyline encodedPolyline = route.overviewPolyline;
                    List<com.google.maps.model.LatLng> decodedPath = encodedPolyline.decodePath();

                    // Clear existing polylines
                    for (Polyline line : polylines) {
                        line.remove();
                    }
                    polylines.clear();

                    // Draw the new polyline
                    PolylineOptions polylineOptions = new PolylineOptions();
                    polylineOptions.color(Color.BLUE);
                    polylineOptions.width(10);

                    // Convert the List<LatLng> to an ArrayList<LatLng> using a traditional for loop
                    ArrayList<LatLng> path = new ArrayList<>();
                    for (com.google.maps.model.LatLng latLng : decodedPath) {
                        path.add(new LatLng(latLng.lat, latLng.lng));
                    }
                    polylineOptions.addAll(path);

                    Polyline polyline = googleMap.addPolyline(polylineOptions);
                    polylines.add(polyline);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "onFailure: " + e.getMessage());
            }
        });
    }


    private void updateHelpRequestsCount() {
        if (Addresses != null) {
            int helpRequestsCount = Addresses.size();
            helpReqCounter.setText(String.valueOf(helpRequestsCount));
        }
    }
}
