package info.debuck.tonight.EventClass;

/**
 * Created by onetec on 08-05-16.
 */
public class SubscriptionRequest {
    private int id;
    private int user_id;
    private int event_id;
    private int subscription_status_id;

    public SubscriptionRequest(int id, int user_id, int event_id, int subscription_status_id){
        this.id = id;
        this.user_id = user_id;
        this.event_id = event_id;
        this.subscription_status_id = subscription_status_id;
    }

    /** Getters and Setters */
    public int getSubscription_status_id() {
        return subscription_status_id;
    }

    public void setSubscription_status_id(int subscription_status_id) {
        this.subscription_status_id = subscription_status_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getEvent_id() {
        return event_id;
    }

    public void setEvent_id(int event_id) {
        this.event_id = event_id;
    }
}
