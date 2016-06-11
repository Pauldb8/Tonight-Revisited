package info.debuck.tonight.EventClass;

import android.content.Context;

import info.debuck.tonight.NetworkSingleton;

/**
 * User class
 */
public class User {
    /* Constant */
    public static int FRIENDSHIP_STATUS_PENDING = 0;
    public static int FRIENDSHIP_STATUS_MYSELF = 2;
    public static int FRIENDSHIP_STATUS_FRIEND = 1;
    public static int FRIENDSHIP_STATUS_BLOCKED = 3;
    public static int FRIENDSHIP_STATUS_NOT_FRIEND = 4;



    private int id;
    private String name;
    private String lastName;
    private String email;
    private String password;
    private String picture_url;
    private int profile_id;

    /* public constructor for a user */
    public User(int id, String name, String email, String password, String picture_url, int profile_id){
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.picture_url = picture_url;
        this.profile_id = profile_id;
    }

    /** Getters and Setters */
    public int getProfile_id() {
        return profile_id;
    }

    public void setProfile_id(int profile_id) {
        this.profile_id = profile_id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String toString(){
        return this.name + ": " + this.email;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() { return this.name + " " + this.lastName; }

    public String getPicture_url() {
        return picture_url;
    }

    public void setPicture_url(String picture_url) {
        this.picture_url = picture_url;
    }

    /* This will return this actual class in JSONified format */
    public String toJson (Context ctx){
        return NetworkSingleton.getInstance(ctx).getGson().toJson(this);
    }
}
