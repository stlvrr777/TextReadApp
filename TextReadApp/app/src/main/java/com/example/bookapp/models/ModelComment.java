package com.example.bookapp.models;

public class ModelComment {
    String id, comment, bookId, timestamp, uid;

    public ModelComment() {

    }

    public ModelComment(String id, String comment, String bookId, String timestamp, String uid) {
        this.id = id;
        this.comment = comment;
        this.bookId = bookId;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
