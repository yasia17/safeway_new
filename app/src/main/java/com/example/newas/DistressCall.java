package com.example.newas;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.maps.model.LatLng;

@IgnoreExtraProperties
public class DistressCall {

    private String callerFirstName;
    private String callerLastName;
    private String destination;

    public DistressCall() {
    }

    public DistressCall(String callerFirstName, String callerLastName, String destination) {
        this.callerFirstName = callerFirstName;
        this.callerLastName = callerLastName;
        this.destination = destination;
    }


    public String getCallerFirstName() {
        return callerFirstName;
    }

    public String getCallerLastName() {
        return callerLastName;
    }

    public String getDestination() {
        return destination;
    }

    public void setCallerFirstName(String callerFirstName) {
        this.callerFirstName = callerFirstName;
    }

    public void setCallerLastName(String callerLastName) {
        this.callerLastName = callerLastName;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        return callerFirstName + " " + callerLastName;
    }
}
