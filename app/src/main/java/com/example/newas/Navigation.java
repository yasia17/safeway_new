package com.example.newas;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.EncodedPolyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Navigation extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "Navigation";
    private static final long ACCEPTANCE_TIMEOUT = 300000; // 5 minutes

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private EditText destinationEditText;
    private GeoApiContext geoApiContext = null;
    private LatLng userLatLng;
    private List<Polyline> polylines = new ArrayList<>();
    private List<Marker> distressCallMarkers = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference distressCallsRef;

    private ArrayList<String> addresses;
    private Marker userMarker;

    private ListView distressCallListView;
    private ArrayAdapter<DistressCall> distressCallAdapter;
    private List<DistressCall> distressCalls = new ArrayList<>();

    private ImageButton helpRequestsButton;
    private TextView helpReqCounter;
    private TextView userCountTextView;

    private Marker destinationMarker;
    private Marker meetingPointMarker;
    private Polyline userRoutePolyline;
    private Polyline accepterRoutePolyline;
    private CountDownTimer acceptanceTimer;

    private DistressCall currentDistressCall;
    private boolean isUserAcceptingCall = false;

    private boolean isPairingEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Log.d("main", "navigation");

        // Get references to UI elements
        destinationEditText = findViewById(R.id.Destination);
        Button searchButton = findViewById(R.id.Search);

        ImageButton settingsButton = findViewById(R.id.SettingsBtn);
        ImageButton navButton = findViewById(R.id.NavBtn);
        ImageButton profileButton = findViewById(R.id.ProfileBtn);
        ImageButton callPoliceButton = findViewById(R.id.CallPolice);
        helpRequestsButton = findViewById(R.id.HelpRequests);
        helpReqCounter = findViewById(R.id.helpReqCounter);
        userCountTextView = findViewById(R.id.userCountTextView);

        distressCallListView = findViewById(R.id.distressCallListView);
        distressCallAdapter = new ArrayAdapter<>(this, R.layout.list_item_distress_call);
        distressCallListView.setAdapter(distressCallAdapter);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        distressCallsRef = database.getReference("DistressCalls");

        distressCallsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                distressCalls.clear();
                distressCallAdapter.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    DistressCall distressCall = data.getValue(DistressCall.class);
                    if (!distressCall.isAccepted()) {
                        distressCalls.add(distressCall);
                        distressCallAdapter.add(distressCall);
                    }
                }

                updateHelpRequestsCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read distress calls", error.toException());
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Navigation.this, Settings.class);
                startActivity(intent);
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Do nothing, already in the Navigation activity
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Navigation.this, Profile.class);
                startActivity(intent);
            }
        });

        callPoliceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Navigation.this, "Calling the police...", Toast.LENGTH_SHORT).show();
                // Implement your logic for calling the police
            }
        });

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_container);
        mapFragment.getMapAsync(this);

        helpRequestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (distressCallListView.getVisibility() == View.GONE) {
                    distressCallListView.setVisibility(View.VISIBLE);
                    //helpRequestsButton.setImageResource(R.drawable.selected);
                } else {
                    distressCallListView.setVisibility(View.GONE);
                    //helpRequestsButton.setImageResource(R.drawable.not_selected);
                }
            }
        });

        distressCallListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                currentDistressCall = distressCallAdapter.getItem(position);
                if (currentDistressCall != null) {
                    if (!isUserAcceptingCall) {
                        showAcceptCallDialog();
                    } else {
                        Toast.makeText(Navigation.this, "You are already accepting a call", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        distressCallListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                DistressCall distressCall = distressCallAdapter.getItem(position);
                if (distressCall != null) {
                    showCancelCallDialog(distressCall);
                    return true;
                }
                return false;
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String destination = destinationEditText.getText().toString().trim();
                if (!destination.isEmpty()) {
                    Geocoder geocoder = new Geocoder(Navigation.this);
                    try {
                        List<Address> addressList = geocoder.getFromLocationName(destination, 1);
                        if (!addressList.isEmpty()) {
                            Address address = addressList.get(0);
                            LatLng destinationLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                            addDestinationMarker(destinationLatLng);
                            showRouteToDestination(destinationLatLng);
                            //hideDistressCalls();
                        } else {
                            Toast.makeText(Navigation.this, "Destination not found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Geocoder exception: " + e.getMessage(), e);
                        Toast.makeText(Navigation.this, "Failed to search for destination", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Navigation.this, "Please enter a destination", Toast.LENGTH_SHORT).show();
                }
            }
        });

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_container);
        supportMapFragment.getMapAsync(this);

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
                        addUserMarker(userLatLng);
                        moveCamera(userLatLng);

                        // Update the help requests count
                        updateHelpRequestsCount();
                    }
                }
            });
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    private void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    if (location != null) {
                        userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        moveCamera(userLatLng);
                        addUserMarker(userLatLng);
                        startDistressCallsCountdown();
                    }
                } else {
                    Log.e(TAG, "Failed to get current location", task.getException());
                    Toast.makeText(Navigation.this, "Failed to get current location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void moveCamera(LatLng latLng) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    private void addUserMarker(LatLng latLng) {
        if (userMarker != null) {
            userMarker.remove();
        }

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        userMarker = googleMap.addMarker(markerOptions);
    }

    private void addDestinationMarker(LatLng latLng) {
        if (destinationMarker != null) {
            destinationMarker.remove();
        }
        destinationMarker = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    private void showRouteToDestination(LatLng destinationLatLng) {
        if (userLatLng == null) {
            Toast.makeText(this, "Failed to get user location", Toast.LENGTH_SHORT).show();
            return;
        }
        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.Map_API_Key)).build();
        } else {
            Toast.makeText(Navigation.this, "geoApiContext == null", Toast.LENGTH_SHORT).show();
        }

        // Create an AsyncTask to perform the directions API request
        AsyncTask<LatLng, Void, DirectionsResult> directionsTask = new AsyncTask<LatLng, Void, DirectionsResult>() {
            @Override
            protected DirectionsResult doInBackground(LatLng... latLngs) {
                DirectionsApiRequest directionsApiRequest = new DirectionsApiRequest(geoApiContext);
                directionsApiRequest.alternatives(false);
                directionsApiRequest.origin(new com.google.maps.model.LatLng(userLatLng.latitude, userLatLng.longitude));
                directionsApiRequest.destination(new com.google.maps.model.LatLng(latLngs[0].latitude, latLngs[0].longitude));
                try {
                    return directionsApiRequest.await();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(DirectionsResult directionsResult) {
                if (directionsResult != null && directionsResult.routes != null && directionsResult.routes.length > 0) {
                    DirectionsRoute route = directionsResult.routes[0];
                    addRoutePolyline(route.overviewPolyline);
                } else {
                    Toast.makeText(Navigation.this, "Failed to get directions", Toast.LENGTH_SHORT).show();
                }
            }
        };

        // Execute the AsyncTask
        directionsTask.execute(destinationLatLng);
    }

    private void addRoutePolyline(EncodedPolyline polyline) {
        List<com.google.maps.model.LatLng> decodedPath = polyline.decodePath();
        List<com.google.android.gms.maps.model.LatLng> latLngList = new ArrayList<>();

        for (com.google.maps.model.LatLng decodedLatLng : decodedPath) {
            com.google.android.gms.maps.model.LatLng latLng = new com.google.android.gms.maps.model.LatLng(
                    decodedLatLng.lat, decodedLatLng.lng);
            latLngList.add(latLng);
        }

        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(latLngList)
                .color(Color.BLUE)
                .width(10);
        userRoutePolyline = googleMap.addPolyline(polylineOptions);
    }


    private void showAcceptCallDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Navigation.this);
        builder.setTitle("Accept Distress Call");
        builder.setMessage("Are you sure you want to accept this distress call?");
        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                acceptCall();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void acceptCall() {
        isUserAcceptingCall = true;
        distressCallListView.setVisibility(View.GONE);
        // helpRequestsButton.setImageResource(R.drawable.not_selected);
        distressCallAdapter.clear();
        currentDistressCall.setAccepted(true);
        distressCallsRef.child(currentDistressCall.getId()).setValue(currentDistressCall);
        updateHelpRequestsCount();
        startPairing();
    }

    private void startPairing() {
        Toast.makeText(Navigation.this, "Pairing with the caller...", Toast.LENGTH_SHORT).show();
        // Implement your logic for pairing with the caller
        isPairingEnabled = true;
        startAcceptanceTimer();
    }

    private void startAcceptanceTimer() {
        acceptanceTimer = new CountDownTimer(ACCEPTANCE_TIMEOUT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the timer display
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;
                String timerText = String.format("%02d:%02d", minutes, seconds);
                showAcceptanceTimer(timerText);
            }

            @Override
            public void onFinish() {
                isPairingEnabled = false;
                showNoUsersAvailableMessage();
                // Implement your logic for connecting with SafeWay volunteers for a video call
            }
        }.start();
    }

    private void showAcceptanceTimer(String timerText) {
        Toast.makeText(Navigation.this, "Acceptance timer: " + timerText, Toast.LENGTH_SHORT).show();
        // Update the UI with the acceptance timer
    }

    private void showNoUsersAvailableMessage() {
        Toast.makeText(Navigation.this, "Unfortunately, no users are available", Toast.LENGTH_SHORT).show();
        // Implement your logic for connecting with SafeWay volunteers for a video call
    }

    private void showCancelCallDialog(DistressCall distressCall) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Navigation.this);
        builder.setTitle("Cancel Distress Call");
        builder.setMessage("Are you sure you want to cancel this distress call?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelCall(distressCall);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void cancelCall(DistressCall distressCall) {
        distressCallsRef.child(distressCall.getId()).removeValue();
        Toast.makeText(Navigation.this, "Distress call canceled", Toast.LENGTH_SHORT).show();
    }

    private void startDistressCallsCountdown() {
        // Implement your logic to update the count of distress calls available within 2km radius
    }

    private void updateHelpRequestsCount() {
        int distressCallCount = distressCalls.size();
        helpReqCounter.setText(String.valueOf(distressCallCount));
        // Implement your logic to update the user count in the radius
    }

    private void hideDistressCalls() {
        distressCallListView.setVisibility(View.GONE);
        //helpRequestsButton.setImageResource(R.drawable.not_selected);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLastLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (googleMap != null) {
            googleMap.clear();
            if (userMarker != null) {
                userMarker.remove();
            }
            if (destinationMarker != null) {
                destinationMarker.remove();
            }
            if (meetingPointMarker != null) {
                meetingPointMarker.remove();
            }
            if (userRoutePolyline != null) {
                userRoutePolyline.remove();
            }
            if (accepterRoutePolyline != null) {
                accepterRoutePolyline.remove();
            }
            if (!polylines.isEmpty()) {
                for (Polyline polyline : polylines) {
                    polyline.remove();
                }
                polylines.clear();
            }
            fetchLastLocation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (acceptanceTimer != null) {
            acceptanceTimer.cancel();
        }
    }
}
