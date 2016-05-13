package info.debuck.tonight.EventClass;

/**
 * Created by onetec on 13-05-16.
 * This class serves the purpose of holding all the information of the foreign keys of an event
 */
public class TonightEventForeignKeys {
    private String status; /* Event status: programmed, canceld, finished... */
    private String address; /* Event address: Voie des gaumais 2/002, 1348 Louvain-La-Neuve... */
    private String category; /* Event category: nightlife, bar, workshop... */

    public TonightEventForeignKeys(String status, String address, String category){
        this.status = status;
        this.address = address;
        this.category = category;
    }

    /** Getters and setters */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
