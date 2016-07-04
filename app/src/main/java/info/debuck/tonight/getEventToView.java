package info.debuck.tonight;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import info.debuck.tonight.EventClass.TonightEvent;
import info.debuck.tonight.EventClass.TonightFilterEventsDialog;
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
    public final static int REQUEST_EVENT_FROM_CITY = 4;
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private int pref_distance;
    private int pref_price;
    private int pref_category;

    /* Necessary variables */
    private ArrayList<TonightEvent> eventArray;
    private EventCustomAdapter mAdapter;
    private ListView mView;
    private ProgressBar mLoader;
    private int mRequest;
    private String finalRequestURL;
    private Context mContext;
    private JSONObject JSONResponse;
    private int cities_id;
    int PRIVATE_MODE = 0;

    public getEventToView(Context context, ListView myView, ProgressBar loader, int request){
        this.mContext = context;
        this.mView = myView;
        this.mLoader = loader;
        this.mRequest = request;
        /* Getting user prefs */
        pref = mContext.getSharedPreferences(TonightFilterEventsDialog.PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        pref_distance = pref.getInt(TonightFilterEventsDialog.PREF_DISTANCE,
                TonightFilterEventsDialog.TONIGHT_DEFAULT_DISTANCE);
        pref_price = pref.getInt(TonightFilterEventsDialog.PREF_PRICE,
                TonightFilterEventsDialog.TONIGHT_DEFAULT_PRICE);
        pref_category = pref.getInt(TonightFilterEventsDialog.PREF_CATEGORY,
                TonightFilterEventsDialog.TONIGHT_DEFAULT_CATEGORY);
    }

    /* This constructor handles the city */
    public getEventToView(Context context, ListView myView, ProgressBar loader, int request, int cities_id){
        this(context, myView, loader, request);
        this.cities_id = cities_id;
    }

    /**
     * We get the URL correct before calling starting the task
     * we hide the listview, and show the loader
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mView.setVisibility(ListView.GONE);
        mLoader.setVisibility(View.VISIBLE);

        finalRequestURL = mContext.getResources().getString(R.string.webserver);

        switch(mRequest){
            case REQUEST_ALL_EVENT:
                finalRequestURL += "tonight/getEvents.php";
                break;
            case REQUEST_SUBSCRIBED_EVENT:
                finalRequestURL += "tonight/getSubscribedEvents.php?id="
                + NetworkSingleton.getInstance(mContext).getConnectedUSer().getId();
                break;
            case REQUEST_CREATED_EVENT:
                finalRequestURL += "tonight/getCreatedEvents.php?id="
                        + NetworkSingleton.getInstance(mContext).getConnectedUSer().getId();
                break;
            case REQUEST_EVENT_FROM_CITY:
                finalRequestURL += "tonight/getEventFromCity.php?cities_id="
                        + this.cities_id
                        + "&price=" + pref_price
                        + "&distance=" + pref_distance
                        + "&category=" + pref_category;
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
                if(response != null) {
                    Log.i("getEventToView", "Received: " + response[response.length - 1].toString());
                    eventArray = new ArrayList<TonightEvent>(Arrays.asList(response));
                    /* we have received the events so we set the adapter to the view */
                    mAdapter = new EventCustomAdapter(mContext, eventArray);
                    mView.setAdapter(mAdapter);
                }
                else {
                    Log.i("getEventToView", "Received: null array, showing empty");
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("getEventToView", "Received: ERROR");
            }
        }, mContext);

        /* Filling resquest queue */
        requestQueue.add(myRequest);

        return null;
    }


    @Override
    protected void onPostExecute(Void v){
        mView.setVisibility(ListView.VISIBLE);
        mLoader.setVisibility(View.GONE);
    }

}
