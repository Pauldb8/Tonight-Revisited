package info.debuck.tonight.EventClass;

/**
 * User class
 */
public class User {
    private int id;
    private String name;
    private String lastName;
    private String email;
    private String password;
    private int profile_id;

    /* public constructor for a user */
    public User(int id, String name, String email, String password, int profile_id){
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
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
}
