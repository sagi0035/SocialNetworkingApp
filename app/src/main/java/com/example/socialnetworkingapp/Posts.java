package com.example.socialnetworkingapp;

public class Posts {
    // were here we will be passing all of the information in firebase database
    public String uId;
    public String contents;
    public String date;
    public String fullname;
    public String postImage;
    public String time;
    public String profileImage;

    public Posts() {

    }

    public Posts(String uId, String contents, String date, String fullname, String postImage, String time, String profileImage) {
        this.uId = uId;
        this.contents = contents;
        this.date = date;
        this.fullname = fullname;
        this.postImage = postImage;
        this.time = time;
        this.profileImage = profileImage;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
