package info.debuck.tonight;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import info.debuck.tonight.EventClass.TonightEventPost;
import info.debuck.tonight.Tools.GsonRequest;

/**
 * Asynchronous task to download the events based on parameters and insert it in the appropriate
 * ListView based also on parameters.
 */
public class getPostEventToView extends AsyncTask<Object, Void, Void>{

    /* Public request for the event to download */
    public final static int REQUEST_PARENT_POST = 1;
    public final static int REQUEST_CHILD_POST = 2;

    /* Necessary variables */
    private ArrayList<TonightEventPost> postArray;
    private EventPostCustomAdapter mAdapter;
    private ListView mView;
    private int mRequest;
    private int id_request;
    private String finalRequestURL;
    private Context mContext;
    private JSONObject JSONResponse;

    public getPostEventToView(Context context, ListView myView, int request, int id_request){
        this.mContext = context;
        this.mView = myView;
        this.mRequest = request;
        this.id_request = id_request;
    }

    /**
     * We get the URL correct before calling starting the task
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        finalRequestURL = mContext.getResources().getString(R.string.webserver);

        switch(mRequest){
            case REQUEST_PARENT_POST:
                finalRequestURL += "tonight/getEventPost.php?event_id="
                        + id_request;
                break;
            case REQUEST_CHILD_POST:
                finalRequestURL += "tonight/getSubscribedEvents.php?id="
                        + NetworkSingleton.getInstance(mContext).getConnectedUSer().getId();
                break;
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

        GsonRequest<TonightEventPost[]> myRequest = new GsonRequest<>(finalRequestURL,
                TonightEventPost[].class, new Response.Listener<TonightEventPost[]>() {
            @Override
            public void onResponse(TonightEventPost[] response) {
                if(response != null) {
                    Log.i("getEventPostToView", "Received: " + response[response.length - 1].toString());
                    postArray = new ArrayList<TonightEventPost>(Arrays.asList(response));
                    /* we have received the events so we set the adapter to the view */
                    mAdapter = new EventPostCustomAdapter(mContext, postArray);
                    mView.setAdapter(mAdapter);
                    setListViewHeightBasedOnChildren(mView);
                }
                else {
                    Log.i("getEventPostToView", "Received: null array, showing empty");
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("getEventPostToView", "Received: ERROR");
            }
        }, mContext);

        /* Filling resquest queue */;
        requestQueue.add(myRequest);

        return null;
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
            totalHeight -= 16 + 10;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight ;//+ (listView.getDividerHeight()  * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

}
