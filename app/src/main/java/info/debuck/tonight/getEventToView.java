package info.debuck.tonight;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import info.debuck.tonight.EventClass.TonightEvent;
import info.debuck.tonight.Tools.GsonRequest;

/**
 * Asynchronous task to download the events based on parameters and insert it in the appropriate
 * ListView based also on parameters.
 */
public class getEventToView extends AsyncTask<Object, Void, Void>{

    /* Public request for the event to download */
    public final static int REQUEST_ALL_EVENT = 1;
    public final static int REQUEST_CREATED_EVENT = 2;
    public final static int REQUEST_SUBSCRIBED_EVENT = 3;

    /* Necessary variables */
    private ArrayList<TonightEvent> eventArray;
    private EventCustomAdapter mAdapter;
    private ListView mView;
    private int mRequest;
    private String finalRequestURL;
    private Context mContext;
    private JSONObject JSONResponse;

    public getEventToView(Context context, ListView myView, int request){
        this.mContext = context;
        this.mView = myView;
        this.mRequest = request;
    }

    /**
     * We get the URL correct before calling starting the task
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        finalRequestURL = mContext.getResources().getString(R.string.webserver);

        switch(mRequest){
            case REQUEST_ALL_EVENT:
                finalRequestURL += "tonight/getEvents.php";
                break;
            case REQUEST_SUBSCRIBED_EVENT:
                finalRequestURL += "tonight/getSubscribedEvent.php?id="
                + NetworkSingleton.getInstance(mContext).getConnectedUSer().getId();
            default:
                finalRequestURL += "tonight/getEvents.php";
                break;
        }
    }


    /**
     * This function calls an URL which will return a JSONObject with all the events
     * @param parameters
     * @return
     */
    @Override
    protected Void doInBackground(Object[] parameters) {
        // Instantiate the RequestQueue.
        RequestQueue requestQueue = NetworkSingleton.getInstance(mContext).getRequestQueue();

        GsonRequest<TonightEvent[]> myRequest = new GsonRequest<>(finalRequestURL,
                TonightEvent[].class, new Response.Listener<TonightEvent[]>() {
            @Override
            public void onResponse(TonightEvent[] response) {
                Log.i("getEventToView", "Received: " + response[response.length-1].toString());

                /* we have received the events so we set the adapter to the view */
                eventArray = new ArrayList<TonightEvent>(Arrays.asList(response));
                mAdapter = new EventCustomAdapter(mContext, eventArray);
                mView.setAdapter(mAdapter);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("getEventToView", "Received: ERROR");
            }
        }, mContext);

        /* Filling resquest queue */;
        requestQueue.add(myRequest);

        return null;
    }

}
