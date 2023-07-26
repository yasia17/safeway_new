package com.example.newas;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Navigation extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "Navigation";
    private static final long ACCEPTANCE_TIMEOUT = 300000; // 5 minutes
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1001;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    //private EditText destinationEditText;
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
    private LinearLayout msgLayout;
    private Button cancelMsgBtn;

    private boolean isPairingEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Log.d("main", "navigation");


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_container);
        mapFragment.getMapAsync(Navigation.this);

        Places.initialize(getApplicationContext(), getString(R.string.Map_API_Key));
        geoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.Map_API_Key)).build();

//        msgLayout =findViewById(R.id.msg_layout);
//        cancelMsgBtn =findViewById(R.id.cancel_msg_btn);

        // Get references to UI elements
        // destinationEditText = findViewById(R.id.Destination);
        Button searchButton = findViewById(R.id.Search);

        ImageButton settingsButton = findViewById(R.id.SettingsBtn);
        //this button is actually a home button not navigation
        ImageButton navButton = findViewById(R.id.NavBtn);
        ImageButton profileButton = findViewById(R.id.ProfileBtn);
        //        ImageButton callPoliceButton = findViewById(R.id.CallPolice);
        //        helpRequestsButton = findViewById(R.id.HelpRequests);
        //        helpReqCounter = findViewById(R.id.helpReqCounter);
        //        userCountTextView = findViewById(R.id.userCountTextView);

//        distressCallListView = findViewById(R.id.distressCallListView);
//        distressCallAdapter = new ArrayAdapter<>(this, R.layout.list_item_distress_call);
//        distressCallListView.setAdapter(distressCallAdapter);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        distressCallsRef = database.getReference("DistressCalls");




        //        // Set the fields to specify which types of place data to
        //        // return after the user has made a selection.
        //        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
        //
        //        // Start the autocomplete intent.
        //        Intent autoCompleteIntent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
        //                .build(this);
        //        startAutocomplete.launch(autoCompleteIntent);
        //
        //
        //
        // Initialize the SDK



        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(this);





//        distressCallsRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                distressCalls.clear();
//                distressCallAdapter.clear();
//
//                for (DataSnapshot data : snapshot.getChildren()) {
//                    DistressCall distressCall = data.getValue(DistressCall.class);
//                    if (!distressCall.isAccepted()) {
//                        distressCalls.add(distressCall);
//                        distressCallAdapter.add(distressCall);
//                    }
//                }
//
//                updateHelpRequestsCount();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "Failed to read distress calls", error.toException());
//            }
//        });

       ImageButton sos_btn = findViewById(R.id.sos);

       sos_btn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent intent = new Intent(Navigation.this, SOS.class);
               startActivity(intent);
           }
       });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Navigation.this, Settings.class);
                startActivity(intent);
            }
        });

//        actually not a navbutton, its a home button
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Navigation.this, Home.class);
                startActivity(i);
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Navigation.this, Profile.class);
                startActivity(intent);
            }
        });

        //        callPoliceButton.setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View view) {
        //                Toast.makeText(Navigation.this, "Calling the police...", Toast.LENGTH_SHORT).show();
        //                // Implement your logic for calling the police
        //            }
        //        });

        // Initialize the map


        //        helpRequestsButton.setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View view) {
        //                if (distressCallListView.getVisibility() == View.GONE) {
        //                    distressCallListView.setVisibility(View.VISIBLE);
        //                    //helpRequestsButton.setImageResource(R.drawable.selected);
        //                } else {
        //                    distressCallListView.setVisibility(View.GONE);
        //                    //helpRequestsButton.setImageResource(R.drawable.not_selected);
        //                }
        //            }
        //        });

//        distressCallListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//                currentDistressCall = distressCallAdapter.getItem(position);
//                if (currentDistressCall != null) {
//                    if (!isUserAcceptingCall) {
//                        showAcceptCallDialog();
//                    } else {
//                        Toast.makeText(Navigation.this, "You are already accepting a call", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//        });

//        distressCallListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
//                DistressCall distressCall = distressCallAdapter.getItem(position);
//                if (distressCall != null) {
//                    showCancelCallDialog(distressCall);
//                    return true;
//                }
//                return false;
//            }
//        });


        //        destinationEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        //            @Override
        //            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        //
        //                if (i == EditorInfo.IME_ACTION_SEARCH || i== EditorInfo.IME_ACTION_DONE){
        //
        //                }
        //
        //                return false;
        //            }
        //        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(Navigation.this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            }
        });




//        cancelMsgBtn.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                msgLayout.setVisibility(View.GONE);
//                googleMap.clear();
//            }
//        });


        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_container);
        supportMapFragment.getMapAsync(this);

//        distressCallListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                // Handle distress call item click event
//            }
//        });
    } //end of onCreate

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
                        //updateHelpRequestsCount();
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
//                        startDistressCallsCountdown();
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

    private void addDestinationMarker(LatLng latLng,String destination) {
        if (destinationMarker != null) {
            destinationMarker.remove();
        }
        Log.d(TAG, "Adding destination marker");
        destinationMarker = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(destination)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        if (destinationMarker != null) {
            Log.d(TAG, "Destination marker added successfully");
        } else {
            Log.d(TAG, "Failed to add destination marker");
        }
    }

    private void showMeetingPoint(LatLng latLng) {
        if (meetingPointMarker != null) {
            meetingPointMarker.remove();
        }
        meetingPointMarker = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Meeting Point")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }



    private void removeOldPolylines() {
        for (Polyline polyline : polylines) {
            polyline.remove();
        }
        polylines.clear();
    }

    private void addRoutePolyline(EncodedPolyline polyline, int numSteps) {
        List<com.google.maps.model.LatLng> decodedPath = polyline.decodePath();
        List<com.google.android.gms.maps.model.LatLng> latLngList = new ArrayList<>();

        int numPoints = decodedPath.size();
        int quarterPoint = numPoints / 5; // Find the quarter point of the route

        for (int i = 0; i < numPoints; i++) {
            com.google.maps.model.LatLng decodedLatLng = decodedPath.get(i);
            com.google.android.gms.maps.model.LatLng latLng = new com.google.android.gms.maps.model.LatLng(decodedLatLng.lat, decodedLatLng.lng);
            latLngList.add(latLng);

            if (i == quarterPoint) {
                // Add meeting point marker at the quarter point of the route
                showMeetingPoint(latLng);
            }

            // Set the color based on whether the point is before or after the quarter point
            int color;
            if (i < quarterPoint) {
                color = Color.RED; // Red color for points before the quarter point
            } else {
                color = Color.BLUE; // Blue color for points after the quarter point
            }

            if (i > 0) {
                // Draw a polyline segment between consecutive points with the specified color
                PolylineOptions polylineOptions = new PolylineOptions()
                        .add(latLngList.get(i - 1), latLngList.get(i))
                        .color(color)
                        .width(15);
                Polyline segmentPolyline = googleMap.addPolyline(polylineOptions);
                polylines.add(segmentPolyline);
            }
        }

        userRoutePolyline = googleMap.addPolyline(new PolylineOptions().addAll(latLngList).color(Color.TRANSPARENT)); // Transparent polyline for the overall route
    }






//    private void showAcceptCallDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(Navigation.this);
//        builder.setTitle("Accept Distress Call");
//        builder.setMessage("Are you sure you want to accept this distress call?");
//        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                acceptCall();
//            }
//        });
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }

//    private void acceptCall() {
//        isUserAcceptingCall = true;
//        distressCallListView.setVisibility(View.GONE);
//        // helpRequestsButton.setImageResource(R.drawable.not_selected);
//        distressCallAdapter.clear();
//        currentDistressCall.setAccepted(true);
//        distressCallsRef.child(currentDistressCall.getId()).setValue(currentDistressCall);
//        updateHelpRequestsCount();
//        startPairing();
//    }

//    private void startPairing() {
//        Toast.makeText(Navigation.this, "Pairing with the caller...", Toast.LENGTH_SHORT).show();
//        // Implement your logic for pairing with the caller
//        isPairingEnabled = true;
//        startAcceptanceTimer();
//    }

//    private void startAcceptanceTimer() {
//        acceptanceTimer = new CountDownTimer(ACCEPTANCE_TIMEOUT, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                // Update the timer display
//                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
//                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;
//                String timerText = String.format("%02d:%02d", minutes, seconds);
//                showAcceptanceTimer(timerText);
//            }
//
//            @Override
//            public void onFinish() {
//                isPairingEnabled = false;
//                showNoUsersAvailableMessage();
//                // Implement your logic for connecting with SafeWay volunteers for a video call
//            }
//        }.start();
//    }

//    private void showAcceptanceTimer(String timerText) {
//        Toast.makeText(Navigation.this, "Acceptance timer: " + timerText, Toast.LENGTH_SHORT).show();
//        // Update the UI with the acceptance timer
//    }
//
//    private void showNoUsersAvailableMessage() {
//        Toast.makeText(Navigation.this, "Unfortunately, no users are available", Toast.LENGTH_SHORT).show();
//        // Implement your logic for connecting with SafeWay volunteers for a video call
//    }
//
//    private void showCancelCallDialog(DistressCall distressCall) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(Navigation.this);
//        builder.setTitle("Cancel Distress Call");
//        builder.setMessage("Are you sure you want to cancel this distress call?");
//        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                cancelCall(distressCall);
//            }
//        });
//        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }
//
//    private void cancelCall(DistressCall distressCall) {
//        distressCallsRef.child(distressCall.getId()).removeValue();
//        Toast.makeText(Navigation.this, "Distress call canceled", Toast.LENGTH_SHORT).show();
//    }
//
//    private void startDistressCallsCountdown() {
//        // Implement your logic to update the count of distress calls available within 2km radius
//    }

//    private void updateHelpRequestsCount() {
//        int distressCallCount = distressCalls.size();
//        //        helpReqCounter.setText(String.valueOf(distressCallCount));
//        // Implement your logic to update the user count in the radius
//    }
//
//    private void hideDistressCalls() {
//        distressCallListView.setVisibility(View.GONE);
//        //helpRequestsButton.setImageResource(R.drawable.not_selected);
//    }

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
    private void showMeetingPoint() {
        if (userLatLng != null && destinationMarker != null) {
            double meetingPointLatitude = (userLatLng.latitude + destinationMarker.getPosition().latitude) / 2;
            double meetingPointLongitude = (userLatLng.longitude + destinationMarker.getPosition().longitude) / 2;
            LatLng meetingPointLatLng = new LatLng(meetingPointLatitude, meetingPointLongitude);
            addMeetingPointMarker(meetingPointLatLng);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                LatLng destinationLatLng = place.getLatLng();

                try {
                    Geocoder geocoder = new Geocoder(Navigation.this);
                    List<Address> addressList = geocoder.getFromLocation(destinationLatLng.latitude, destinationLatLng.longitude, 1);
                    if (!addressList.isEmpty()) {
                        Address address = addressList.get(0);
                        String destination = address.getAddressLine(0);

                        // Show route to destination
                        showRouteToDestination(destinationLatLng);

                        // Add destination marker on the map
                        addDestinationMarker(destinationLatLng, destination);

                        // Show meeting point (which is the destination in this case)
                        showMeetingPoint(destinationLatLng);
                    } else {
                        Toast.makeText(Navigation.this, "Destination not found", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Geocoder exception: " + e.getMessage(), e);
                    Toast.makeText(Navigation.this, "Failed to search for destination", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                // Handle the error
                Log.e(TAG, "Error: " + status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // Autocomplete canceled by the user
                Log.d(TAG, "Autocomplete canceled");
            }
        }
    }



    private void addMeetingPointMarker(LatLng latLng) {
        if (meetingPointMarker != null) {
            meetingPointMarker.remove();
        }
        meetingPointMarker = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Meeting Point")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }


    private void showRouteToDestination(LatLng destinationLatLng) {
        if (userLatLng == null) {
            Toast.makeText(this, "Failed to get user location", Toast.LENGTH_SHORT).show();
            return;
        }

        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.Map_API_Key)).build();
        }
//          else {
//                Toast.makeText(Navigation.this, "geoApiContext == null", Toast.LENGTH_SHORT).show();
//            }

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
                    removeOldPolylines(); // Remove existing polylines from the map
                    addRoutePolyline(route.overviewPolyline, route.legs[0].steps.length); // Pass the number of steps in the first leg of the route
                } else {
                    Toast.makeText(Navigation.this, "Failed to get directions", Toast.LENGTH_SHORT).show();
                }
            }
        };

        // Execute the AsyncTask
        directionsTask.execute(destinationLatLng);
    }
}