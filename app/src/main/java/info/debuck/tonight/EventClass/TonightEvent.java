package info.debuck.tonight.EventClass;

import android.content.Context;
import android.text.Html;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import info.debuck.tonight.NetworkSingleton;

/**
 * Public class containing all the information relative to a TonightEvent, the names of the
 */
public class TonightEvent {
    private int _ID;
    private String name;
    private String start_date;
    private String end_date;
    private int max_people = 0; //No limite by default
    private double price = 0;    //Free by default
    private String picture_url;
    private String description;
    private String last_update;
    //Foreign keys, we only keep the IDs
    private int user_id;
    private int event_type_id;
    private int event_status_id;
    private int event_location_id;

    //PTo format date into Sam. 21 Jan 2015
    /* We don't want this to be serialized */
    private transient SimpleDateFormat fDateEvent = new SimpleDateFormat("EEE d MMM yyyy");
    private transient SimpleDateFormat stringDate = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
    private transient SimpleDateFormat fHourEvent = new SimpleDateFormat("kk:mm");

    /**
     * Empty constructor
     */
    public TonightEvent() {
    }

    /**
     * Public constructor for a TonightEvent with full parameters
     *
     * @param _ID
     * @param name
     * @param start_date
     * @param end_date
     * @param max_people
     * @param price
     * @param picture_url
     * @param description
     * @param last_update
     * @param user_id
     * @param event_type_id
     * @param event_status_id
     * @param event_location_id
     */
    public TonightEvent(int _ID, String name, String start_date, String end_date, int max_people,
                        double price, String picture_url, String description, String last_update,
                        int user_id, int event_type_id, int event_status_id,
                        int event_location_id) {
        this._ID = _ID;
        this.name = name;
        this.start_date = start_date;
        this.end_date = end_date;
        this.max_people = max_people;
        this.price = price;
        this.picture_url = picture_url;
        this.description = description;
        this.last_update = last_update;
        this.user_id = user_id;
        this.event_type_id = event_type_id;
        this.event_status_id = event_status_id;
        this.event_location_id = event_location_id;
    }

    public int get_ID() {
        return _ID;
    }

    public void set_ID(int _ID) {
        this._ID = _ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStart_date() {
        try {
            return stringDate.parse(start_date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getStart_dateString() {
        return stringDate.format(start_date);
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public Date getEnd_date() {
        try {
            return stringDate.parse(end_date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getEnd_dateString() {

        return stringDate.format(this.getEnd_date());
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public int getMax_people() {
        return max_people;
    }


    public void setMax_people(int max_people) {
        this.max_people = max_people;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPicture_url() {
        return picture_url;
    }

    public void setPicture_url(String picture_url) {
        this.picture_url = picture_url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLast_update() {
        try {
            return stringDate.parse(last_update);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setLast_update(String last_update) {
        this.last_update = last_update;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getEvent_type_id() {
        return event_type_id;
    }

    public void setEvent_type_id(int event_type_id) {
        this.event_type_id = event_type_id;
    }

    public int getEvent_status_id() {
        return event_status_id;
    }

    public void setEvent_status_id(int event_status_id) {
        this.event_status_id = event_status_id;
    }

    public int getEvent_location_id() {
        return event_location_id;
    }

    public void setEvent_location_id(int event_location_id) {
        this.event_location_id = event_location_id;
    }

    public String getPriceFormatted() {
        if (this.price == 0)
            return "GRATUIT";
        else
            return String.valueOf(this.getPrice()) + "€";
    }

    public String getStartDateFormatted() {
        return fDateEvent.format(this.getStart_date());
    }

    public String getEndDateFormatted() {
        return fDateEvent.format(this.getEnd_date());
    }

    public String getStartHour() {
        return fHourEvent.format(this.getStart_date());
    }

    public String getEndHour() {
        return fHourEvent.format(this.getEnd_date());
    }

    public String toString(){
        return this._ID + ": " + this.name + ", le " + this.start_date + " - " + this.description;
    }

    public String toJson(Context mContext) {
        return NetworkSingleton.getInstance(mContext).getGson().toJson(this);
    }

    public String getDescriptionFormatted() {
        return Html.fromHtml(getDescription()).toString();
    }
}
