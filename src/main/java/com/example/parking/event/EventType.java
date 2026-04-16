package com.example.parking.event;

public final class EventType {

    private EventType() {
    }

    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILURE = "LOGIN_FAILURE";

    public static final String SCAN = "SCAN";

    public static final String RESIDENT_CREATE = "RESIDENT_CREATE";
    public static final String RESIDENT_UPDATE = "RESIDENT_UPDATE";
    public static final String RESIDENT_DELETE = "RESIDENT_DELETE";

    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_DELETE = "USER_DELETE";
}
