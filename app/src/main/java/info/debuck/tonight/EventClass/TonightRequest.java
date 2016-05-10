package info.debuck.tonight.EventClass;

/**
 * Created by onetec on 24-04-16.
 */
public class TonightRequest {

    private int statusCode;
    private String statusMessage;
    private boolean statusReturn;

    public TonightRequest(int a, String b, boolean c){
        this.statusCode = a;
        this.statusMessage = b;
        this.statusReturn = c;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isStatusReturn() {
        return statusReturn;
    }

    public void setStatusReturn(boolean statusReturn) {
        this.statusReturn = statusReturn;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
