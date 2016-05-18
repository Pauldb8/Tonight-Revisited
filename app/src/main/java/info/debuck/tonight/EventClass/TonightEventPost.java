package info.debuck.tonight.EventClass;

/**
 * Created by onetec on 15-05-16.
 */
public class TonightEventPost {

    /* Info from post of events */
    private int id;
    private int event_id; /* The event id to which this post is refering to */
    /* If this is a direct post to an event, then it's 0, else it represents the parent post id */
    private int parent_id;
    private int creator_id; /* user who wrote the post */
    private String date; /* Date of post */
    private String message; /* message of the post */

    /* Public constructor, direct to post, no parent */
    public TonightEventPost(int event_id, int creator_id, String date, String message){
        this.event_id = event_id;
        this.creator_id = creator_id;
        this.parent_id = 0;
        this.date = date;
        this.message = message;
    }

    /* Public constructor, child of a post, parent is another post */
    public TonightEventPost(int event_id, int parent_id, int creator_id, String date, String message){
        this.event_id = event_id;
        this.parent_id = parent_id;
        this.creator_id = creator_id;
        this.date = date;
        this.message = message;
    }

    public String toString(){
        return "TonightEventPost@event_id=" + this.event_id + "&parent_id=" + this.parent_id +
                "&creator_id=" + this.creator_id + "&date=" + this.date + "&message=" + this.message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEvent_id() {
        return event_id;
    }

    public void setEvent_id(int event_id) {
        this.event_id = event_id;
    }

    public int getParent_id() {
        return parent_id;
    }

    public void setParent_id(int parent_id) {
        this.parent_id = parent_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(int creator_id) {
        this.creator_id = creator_id;
    }
}
