package info.debuck.tonight.EventClass;

/**
 * Created by onetec on 08-05-16.
 */
public class IsSubscribedRequest {
    private int user_id;
    private int event_id;


    /* Public constructor */
    public IsSubscribedRequest(int user_id, int event_id){
        this.user_id = user_id;
        this.event_id = event_id;
    }


    /** Getters and Setters */
    public int getEvent_id() {
        return event_id;
    }

    public void setEvent_id(int event_id) {
        this.event_id = event_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
