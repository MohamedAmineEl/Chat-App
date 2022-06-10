package com.apps.chat_apps.model;

import androidx.annotation.NonNull;

public class User {
    private String id;
    private String username;
    private String name;
    private String imageURL = "default";
    private String status;


    public User(String id, String username, String name, String imageURL, String status) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.imageURL = imageURL;
        this.status = status;
    }

    public User() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", imageURL='" + imageURL + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
