package com.unievent.model;

import java.sql.Timestamp;

public class Attends {
    private int userId;
    private int eventId;
    private String rsvpStatus; // 'GOING', 'NOT_GOING', 'MAYBE'
    private Timestamp rsvpDate;
    
    // Transient fields for joins
    private String userName;
    private String eventTitle;

    // Constructors
    public Attends() {}

    public Attends(int userId, int eventId, String rsvpStatus) {
        this.userId = userId;
        this.eventId = eventId;
        this.rsvpStatus = rsvpStatus;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getRsvpStatus() {
        return rsvpStatus;
    }

    public void setRsvpStatus(String rsvpStatus) {
        this.rsvpStatus = rsvpStatus;
    }

    public Timestamp getRsvpDate() {
        return rsvpDate;
    }

    public void setRsvpDate(Timestamp rsvpDate) {
        this.rsvpDate = rsvpDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }
}
