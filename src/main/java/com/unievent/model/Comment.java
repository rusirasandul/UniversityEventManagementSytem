package com.unievent.model;

import java.sql.Timestamp;

public class Comment {
    private int commentId;
    private int userId;
    private int eventId;
    private String content;
    private Timestamp postedAt;
    
    // Transient field for joins
    private String authorName;

    // Constructors
    public Comment() {}

    // Getters and Setters
    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getTimestamp() {
        return postedAt;
    }

    public void setTimestamp(Timestamp postedAt) {
        this.postedAt = postedAt;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
}
