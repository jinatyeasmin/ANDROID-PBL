package com.example.eduquiz.models;

import java.io.Serializable;

public class FeedbackModel implements Serializable {
    private String id;
    private String userId;
    private String userName;
    private float rating;
    private String message;
    private long timestamp;
    private String dateString;

    public FeedbackModel() {
    }

    public FeedbackModel(String id, String userId, String userName, float rating, String message, long timestamp, String dateString) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.message = message;
        this.timestamp = timestamp;
        this.dateString = dateString;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }
}
