package info.debuck.tonight.EventClass;

/**
 * This class contains all the information about the location of an event
 */
public class TonightEventLocation {

    private int id;
    private String name;
    private double latitude;
    private double longitude;
    private String address;
    private int cities_id;

    public TonightEventLocation(int id, String name, double latitude, double longitude, String address,
                                int cities_id){
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.cities_id = cities_id;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCities_id() {
        return cities_id;
    }

    public void setCities_id(int cities_id) {
        this.cities_id = cities_id;
    }
}
