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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.newas.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // Get references to UI elements
        destinationEditText = findViewById(R.id.Destination);
        Button searchButton = findViewById(R.id.Search);

        ImageButton Settings = findViewById(R.id.SettingsBtn);
        ImageButton Nav = findViewById(R.id.NavBtn);
        ImageButton Profile = findViewById(R.id.ProfileBtn);
        ImageButton Police = findViewById(R.id.CallPolice);
        helpRequestsButton = findViewById(R.id.HelpRequests);

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
        mapFragment.getMapAsync(this);

        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.Map_API_Key))
                    .build();
        }

        // Set click listener for the search button
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String destination = destinationEditText.getText().toString().trim();
                if (!destination.isEmpty()) {
                    calculateDirections(destination);
                } else {
                    Toast.makeText(Navigation.this, "Please enter a destination", Toast.LENGTH_SHORT).show();
                }
            }
        });

        distressCallListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DistressCall distressCall = distressCalls.get(position);
                distressCall.setAccepted(true);
                // Update the distress call in the database to mark it as accepted

                // Hide or disable the HelpRequests button
                helpRequestsButton.setVisibility(View.GONE);
            }
        });

        helpRequestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toggle the visibility of the distress call list
                if (distressCallListView.getVisibility() == View.VISIBLE) {
                    distressCallListView.setVisibility(View.GONE);
                } else {
                    distressCallListView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        getLocation();
    }


    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PermissionHelper.requestLocationPermission(Navigation.this);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Create LatLng object with user's location
                        userLatLng = new LatLng(latitude, longitude);

                        // Add marker at the user's location
                        googleMap.addMarker(new MarkerOptions().position(userLatLng).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                        // Move the camera to the user's location and zoom in
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));

                        // Get the address from the user's location
                        Geocoder geocoder = new Geocoder(Navigation.this);
                        try {
                            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            if (!addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                String origin = address.getAddressLine(0); // Set the origin address as the user's current location
                                sendDistressCall(origin); // Pass the origin address to the sendDistressCall() method
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void calculateDirections(String destination) {
        Log.d(TAG, "calculateDirections: calculating directions.");

        // Calculate the meeting point
        LatLng meetingPoint = calculateMeetingPoint(destination);

        // Calculate the route from user's location to the meeting point
        DirectionsApiRequest userToMeetingPoint = new DirectionsApiRequest(geoApiContext);
        userToMeetingPoint.origin(new com.google.maps.model.LatLng(userLatLng.latitude, userLatLng.longitude));
        userToMeetingPoint.destination(new com.google.maps.model.LatLng(meetingPoint.latitude, meetingPoint.longitude));
        userToMeetingPoint.setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "calculateDirections: userToMeetingPoint route: " + result.routes[0].toString());
                // Add the route to the map
                addPolylineToMap(result);

                // Calculate the route from the meeting point to the destination
                DirectionsApiRequest meetingPointToDestination = new DirectionsApiRequest(geoApiContext);
                meetingPointToDestination.origin(new com.google.maps.model.LatLng(meetingPoint.latitude, meetingPoint.longitude));
                meetingPointToDestination.destination(destination);
                meetingPointToDestination.setCallback(new PendingResult.Callback<DirectionsResult>() {
                    @Override
                    public void onResult(DirectionsResult result) {
                        Log.d(TAG, "calculateDirections: meetingPointToDestination route: " + result.routes[0].toString());
                        // Add the route to the map
                        addPolylineToMap(result);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Log.e(TAG, "calculateDirections: Failed to get meetingPointToDestination directions: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get userToMeetingPoint directions: " + e.getMessage());
            }
        });
    }

    private void addPolylineToMap(DirectionsResult result) {
        // Clear existing polylines from the map
        if (polylines.size() > 0) {
            for (Polyline polyline : polylines) {
                polyline.remove();
            }
            polylines.clear();
        }

        // Add new polylines to the map
        if (result.routes != null && result.routes.length > 0) {
            DirectionsRoute route = result.routes[0];
            EncodedPolyline encodedPolyline = route.overviewPolyline;
            List<com.google.maps.model.LatLng> decodedPath = encodedPolyline.decodePath();

            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.parseColor("#53bab9")); // LightBlue color
            polylineOptions.width(8);

            for (com.google.maps.model.LatLng latLng : decodedPath) {
                polylineOptions.add(new LatLng(latLng.lat, latLng.lng));
            }

            Polyline polyline = googleMap.addPolyline(polylineOptions);
            polylines.add(polyline);
        }
    }

    private LatLng calculateMeetingPoint(String destination) {
        LatLng destinationLatLng = getLocationFromAddress(destination);
        double meetingPointLat = (userLatLng.latitude + destinationLatLng.latitude) / 2;
        double meetingPointLng = (userLatLng.longitude + destinationLatLng.longitude) / 2;
        return new LatLng(meetingPointLat, meetingPointLng);
    }

    private LatLng getLocationFromAddress(String address) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(address, 1);
            if (!addresses.isEmpty()) {
                Address location = addresses.get(0);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                return new LatLng(latitude, longitude);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendDistressCall(String origin) {
        String uid = mAuth.getCurrentUser().getUid();

        db.getReference("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                String callerFirstName = user.getFirstName();
                String callerLastName = user.getLastName();

                String destination = destinationEditText.getText().toString(); // Get the destination from the EditText
                boolean isAccepted = false; // Set the accepted status as false initially

                // Create a new DistressCall object
                DistressCall distressCall = new DistressCall(callerFirstName, callerLastName, origin, destination, isAccepted);

                // Add the distress call to the list
                distressCalls.add(distressCall);

                // Update the UI component (e.g., ListView or RecyclerView) to display the distress calls
                updateDistressCallList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error if the retrieval is canceled or fails
            }
        });
    }




    private List<String> getAddressesInRadius(LatLng centerLatLng, List<String> addresses) {
        List<String> addressesInRadius = new ArrayList<>();

        for (String address : addresses) {
            LatLng addressLatLng = getLocationFromAddress(address);
            if (addressLatLng != null) {
                double distance = calculateDistance(centerLatLng, addressLatLng);
                if (distance <= 2.0) {
                    addressesInRadius.add(address);
                }
            }
        }

        return addressesInRadius;
    }

    private double calculateDistance(LatLng latLng1, LatLng latLng2) {
        double earthRadius = 6371; // in kilometers
        double lat1 = Math.toRadians(latLng1.latitude);
        double lon1 = Math.toRadians(latLng1.longitude);
        double lat2 = Math.toRadians(latLng2.latitude);
        double lon2 = Math.toRadians(latLng2.longitude);

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +

        Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    private void updateDistressCallList() {
        // Clear the previous list
        distressCallAdapter.clear();

        // Add all distress calls to the adapter
        for (DistressCall distressCall : distressCalls) {
            distressCallAdapter.add(distressCall);
        }

        // Make the ListView visible
        distressCallListView.setVisibility(View.VISIBLE);

        // Check if any distress call is accepted
        for (DistressCall distressCall : distressCalls) {
            if (distressCall.isAccepted()) {
                // Hide or disable the HelpRequests button
                helpRequestsButton.setVisibility(View.GONE); // or HelpRequests.setEnabled(false)
                break;
            }
        }
    }

}
