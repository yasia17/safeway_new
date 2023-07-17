package com.example.newas;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.google.maps.PendingResult;
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
    private ArrayAdapter<String> distressCallAdapter;
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
    private List<LatLng> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

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

        userList = new ArrayList<>(); // Initialize the user list with actual LatLng objects
        userList.add(new LatLng(31.751546, 35.179309)); // User in the sample scenario

        double radius = 2.0; // Radius in kilometers
        int userCount = calculateUsersInRadius(radius);
        userCountTextView.setText(String.valueOf(userCount));

        // Inside the onCreate method, after adding the user coordinates to the userList
        LatLng meetingPointLatLng = new LatLng(31.750725, 35.178477); // Meeting point coordinates
        LatLng destinationLatLng = new LatLng(31.749724, 35.174114); // Destination coordinates

// Call the following methods to add markers and draw polylines for the meeting point and destination
        addMeetingPointMarker(meetingPointLatLng);
        showRouteToMeetingPoint(meetingPointLatLng, destinationLatLng);


        distressCallsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                distressCalls.clear();
                distressCallAdapter.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    DistressCall distressCall = data.getValue(DistressCall.class);
                    if (!distressCall.isAccepted()) {
                        distressCalls.add(distressCall);
                        distressCallAdapter.add(distressCall.getCallerFirstName() + " " + distressCall.getCallerLastName() + "\n" +
                                "From: " + distressCall.getOrigin() + "\n" +
                                "To: " + distressCall.getDestination());
                    }
                }

                updateHelpRequestsCount();
                distressCallAdapter.notifyDataSetChanged();
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
                } else {
                    distressCallListView.setVisibility(View.GONE);
                }
            }
        });

        distressCallListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentDistressCall = distressCalls.get(position);
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
                DistressCall distressCall = distressCalls.get(position);
                if (distressCall != null) {
                    showCancelCallDialog(distressCall.getId());
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
                            hideDistressCalls();
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
                DistressCall distressCall = distressCalls.get(position);
                if (distressCall != null) {
                    // Handle distress call item click event
                    acceptDistressCall(distressCall);
                }
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
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
                        } catch (Exception e) {
                        }

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
        }
        DirectionsApiRequest directionsApiRequest = new DirectionsApiRequest(geoApiContext);
        directionsApiRequest.alternatives(false);
        directionsApiRequest.origin(new com.google.maps.model.LatLng(userLatLng.latitude, userLatLng.longitude));
        directionsApiRequest.destination(new com.google.maps.model.LatLng(destinationLatLng.latitude, destinationLatLng.longitude));
        directionsApiRequest.setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                if (result.routes != null && result.routes.length > 0) {
                    DirectionsRoute route = result.routes[0];
                    addRoutePolyline(route.overviewPolyline);
                    createMeetingPoint(route);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "Failed to get directions: " + e.getMessage(), e);
                Toast.makeText(Navigation.this, "Failed to get directions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addRoutePolyline(EncodedPolyline polyline) {
        List<com.google.maps.model.LatLng> decodedPath = polyline.decodePath();
        List<LatLng> path = new ArrayList<>();
        for (com.google.maps.model.LatLng latLng : decodedPath) {
            path.add(new LatLng(latLng.lat, latLng.lng));
        }

        if (userRoutePolyline != null) {
            userRoutePolyline.remove();
        }

        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(path)
                .color(Color.BLUE)
                .width(10);
        userRoutePolyline = googleMap.addPolyline(polylineOptions);
    }

    private void createMeetingPoint(DirectionsRoute route) {
        List<LatLng> path = new ArrayList<>();
        List<com.google.maps.model.LatLng> latLngs = route.overviewPolyline.decodePath();
        for (com.google.maps.model.LatLng latLng : latLngs) {
            path.add(new LatLng(latLng.lat, latLng.lng));
        }

        int meetingPointIndex = path.size() / 2;
        LatLng meetingPoint = path.get(meetingPointIndex);

        if (meetingPointMarker != null) {
            meetingPointMarker.remove();
        }

        meetingPointMarker = googleMap.addMarker(new MarkerOptions()
                .position(meetingPoint)
                .title("Meeting Point")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // Add route polyline from user location to meeting point
        List<LatLng> userRoutePath = path.subList(0, meetingPointIndex + 1);
        if (userRoutePolyline != null) {
            userRoutePolyline.remove();
        }
        PolylineOptions userRoutePolylineOptions = new PolylineOptions()
                .addAll(userRoutePath)
                .color(Color.BLUE)
                .width(10);
        userRoutePolyline = googleMap.addPolyline(userRoutePolylineOptions);

        // Add route polyline from meeting point to destination
        List<LatLng> accepterRoutePath = path.subList(meetingPointIndex, path.size());
        if (accepterRoutePolyline != null) {
            accepterRoutePolyline.remove();
        }
        PolylineOptions accepterRoutePolylineOptions = new PolylineOptions()
                .addAll(accepterRoutePath)
                .color(Color.RED)
                .width(10);
        accepterRoutePolyline = googleMap.addPolyline(accepterRoutePolylineOptions);
    }

    private void showDistressCalls() {
        for (Marker marker : distressCallMarkers) {
            marker.setVisible(true);
        }
    }

    private void hideDistressCalls() {
        for (Marker marker : distressCallMarkers) {
            marker.setVisible(false);
        }
    }

    private void updateHelpRequestsCount() {
        int count = distressCalls.size();
        if (count > 0) {
            helpReqCounter.setText(String.valueOf(count));
            helpReqCounter.setVisibility(View.VISIBLE);
        } else {
            helpReqCounter.setVisibility(View.GONE);
        }
    }

    private void startDistressCallsCountdown() {
        acceptanceTimer = new CountDownTimer(ACCEPTANCE_TIMEOUT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the user count text view with the remaining time
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(minutes);
                String time = String.format("%02d:%02d", minutes, seconds);
                userCountTextView.setText(time);
            }

            @Override
            public void onFinish() {
                // Handle the timeout when no distress calls are accepted
                userCountTextView.setText("00:00");
                isPairingEnabled = false;
                Toast.makeText(Navigation.this, "No distress calls were accepted", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    private void showAcceptCallDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Accept Distress Call");
        builder.setMessage("Do you want to accept the distress call from " + currentDistressCall.getCallerFirstName() + " " + currentDistressCall.getCallerLastName() + "?");
        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                acceptDistressCall(currentDistressCall);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void showCancelCallDialog(final String distressCallId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel Distress Call");
        builder.setMessage("Do you want to cancel the distress call?");
        builder.setPositiveButton("Cancel Distress Call", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelDistressCall(distressCallId);
            }
        });
        builder.setNegativeButton("Keep Distress Call", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void acceptDistressCall(DistressCall distressCall) {
        isUserAcceptingCall = true;
        isPairingEnabled = false;
        distressCall.setAccepterId(currentUser.getUid());
        distressCall.setAccepted(true);
        distressCallsRef.child(distressCall.getId()).setValue(distressCall, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@NonNull DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    Toast.makeText(Navigation.this, "Distress call accepted", Toast.LENGTH_SHORT).show();
                    isPairingEnabled = true;
                    if (acceptanceTimer != null) {
                        acceptanceTimer.cancel();
                    }
                    showMeetingPointMarker();
                    hideDistressCalls();
                } else {
                    Toast.makeText(Navigation.this, "Failed to accept distress call", Toast.LENGTH_SHORT).show();
                    isUserAcceptingCall = false;
                }
            }
        });
    }

    private void cancelDistressCall(String distressCallId) {
        distressCallsRef.child(distressCallId).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@NonNull DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    Toast.makeText(Navigation.this, "Distress call canceled", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Navigation.this, "Failed to cancel distress call", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showMeetingPointMarker() {
        if (meetingPointMarker != null) {
            meetingPointMarker.setVisible(true);
        }
        if (accepterRoutePolyline != null) {
            accepterRoutePolyline.setVisible(true);
        }
    }

    private void hideMeetingPointMarker() {
        if (meetingPointMarker != null) {
            meetingPointMarker.setVisible(false);
        }
        if (accepterRoutePolyline != null) {
            accepterRoutePolyline.setVisible(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (acceptanceTimer != null) {
            acceptanceTimer.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        if (distressCallListView.getVisibility() == View.VISIBLE) {
            distressCallListView.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
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

    private int calculateUsersInRadius(double radius) {
        int count = 0;
        for (LatLng userLatLng : userList) {
            double distance = calculateDistance(userLatLng, userLatLng);
            if (distance <= radius) {
                count++;
            }
        }
        return count;
    }

    private double calculateDistance(LatLng startLatLng, LatLng endLatLng) {
        double lat1 = Math.toRadians(startLatLng.latitude);
        double lon1 = Math.toRadians(startLatLng.longitude);
        double lat2 = Math.toRadians(endLatLng.latitude);
        double lon2 = Math.toRadians(endLatLng.longitude);

        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;

        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        double r = 6371;

        return c * r;
    }
    // Method to add the meeting point marker
    private void addMeetingPointMarker(LatLng meetingPointLatLng) {
        if (meetingPointMarker != null) {
            meetingPointMarker.remove();
        }
        meetingPointMarker = googleMap.addMarker(new MarkerOptions()
                .position(meetingPointLatLng)
                .title("Meeting Point")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }

    // Method to draw polyline from the user location to the meeting point
    private void showRouteToMeetingPoint(LatLng meetingPointLatLng, LatLng destinationLatLng) {
        if (userLatLng == null) {
            Toast.makeText(this, "Failed to get user location", Toast.LENGTH_SHORT).show();
            return;
        }

        List<LatLng> path = new ArrayList<>();
        path.add(userLatLng);
        path.add(meetingPointLatLng);
        path.add(destinationLatLng);

        if (userRoutePolyline != null) {
            userRoutePolyline.remove();
        }

        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(path)
                .color(Color.BLUE)
                .width(10);
        userRoutePolyline = googleMap.addPolyline(polylineOptions);
    }

}
