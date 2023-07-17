package com.example.newas;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class DistressCall {
    private String id;
    private String callerFirstName;
    private String callerLastName;
    private String origin;
    private String destination;
    private boolean isAccepted;

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

    @Override
    public String toString() {
        return callerFirstName + " " + callerLastName;
    }
}
