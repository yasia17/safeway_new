package com.example.newas;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@IgnoreExtraProperties
public class DistressCall {
    private String id;
    private String callerFirstName;
    private String callerLastName;
    private String origin;
    private String destination;
    private boolean isAccepted;
    private String accepterId;

    public DistressCall() {
    }

    public DistressCall(String id, String callerFirstName, String callerLastName, String origin, String destination, boolean isAccepted) {
        this.id = id;
        this.callerFirstName = callerFirstName;
        this.callerLastName = callerLastName;
        this.origin = origin;
        this.destination = destination;
        this.isAccepted = isAccepted;
    }

    public String getId() {
        return id;
    }

    public String getCallerFirstName() {
        return callerFirstName;
    }

    public String getCallerLastName() {
        return callerLastName;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    public String getAccepterId() {
        return accepterId;
    }

    public void setAccepterId(String accepterId) {
        this.accepterId = accepterId;
    }

    @Override
    public String toString() {
        return callerFirstName + " " + callerLastName;
    }

    public double getCallerLatitude(Context context) {
        return getLatitudeFromAddress(context, origin);
    }

    public double getCallerLongitude(Context context) {
        return getLongitudeFromAddress(context, origin);
    }

    public double getDestinationLatitude(Context context) {
        return getLatitudeFromAddress(context, destination);
    }

    public double getDestinationLongitude(Context context) {
        return getLongitudeFromAddress(context, destination);
    }

    private double getLatitudeFromAddress(Context context, String address) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (!addresses.isEmpty()) {
                return addresses.get(0).getLatitude();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private double getLongitudeFromAddress(Context context, String address) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (!addresses.isEmpty()) {
                return addresses.get(0).getLongitude();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
