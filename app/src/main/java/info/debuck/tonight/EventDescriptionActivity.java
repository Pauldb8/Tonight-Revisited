package info.debuck.tonight;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;

import info.debuck.tonight.EventClass.IsSubscribedRequest;
import info.debuck.tonight.EventClass.SubscriptionRequest;
import info.debuck.tonight.EventClass.TonightEvent;
import info.debuck.tonight.EventClass.TonightRequest;
import info.debuck.tonight.Tools.GsonRequest;
import info.debuck.tonight.Tools.SessionManager;

public class EventDescriptionActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int TONIGHT_SUBSCRIBE = 2;
    private static final int TONIGHT_UNSUBSCRIBE = 3;
    /* Layout properties */
    private NetworkImageView evDescPicture;
    private TextView evTitle;
    private TextView evStartDate;
    private TextView evPrice;
    private TextView evStartTime;
    private TextView evLocation;
    private TextView evDescription;
    private TextView evSubscribe;
    private TextView evUnsubscribe;
    private TextView evCategory;
    private ViewSwitcher viewSwitcher;

    /* TonightEvent properties */
    private Gson gson;
    private String serializedObject;
    private TonightEvent event;

    /* Network Singleton information */
    private ImageLoader mImageLoader;
    private RequestQueue mRequestQueue;
    SessionManager sessionManager;

    /** We will load the received extra intent and associate it with the views */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_description_activity);

        sessionManager = new SessionManager(this);

        /* getting the views */
        evDescPicture = (NetworkImageView) findViewById(R.id.evDescPicture);
        evTitle = (TextView) findViewById(R.id.evTitle);
        evStartDate = (TextView) findViewById(R.id.evStartDate);
        evPrice = (TextView) findViewById(R.id.evPrice);
        evStartTime = (TextView) findViewById(R.id.evStartTime);
        evLocation = (TextView) findViewById(R.id.evLocation);
        evDescription = (TextView) findViewById(R.id.evDescription);
        evSubscribe = (TextView) findViewById(R.id.subscribe);
        evUnsubscribe = (TextView) findViewById(R.id.unsubscribe);
        viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        evCategory = (TextView) findViewById(R.id.evCategory);

        /* Getting the serialized TonightEvent object from the intent extra and creating an instance
        * of TonightEvent from it */
        gson = NetworkSingleton.getInstance(this).getGson();
        serializedObject = getIntent().getStringExtra(MainActivity.TONIGHT_INTENT_EXTRA_DESC);
        event = gson.fromJson(serializedObject, TonightEvent.class);

        /* Getting the loader information from Network Singleton */
        mImageLoader = NetworkSingleton.getInstance(this.getApplicationContext()).getImageLoader();
        mRequestQueue = NetworkSingleton.getInstance(this).getRequestQueue();

        /* Associating information from the event object to its respective views */
        evDescPicture.setImageUrl(event.getPicture_url(), mImageLoader);
        evTitle.setText(event.getName());
        evStartDate.setText(event.getStartDateFormatted());
        evPrice.setText(event.getPriceFormatted());
        evStartTime.setText(event.getStartHour());
        //todo: evLocation.setText(event.get);
        evDescription.setText(Html.fromHtml(refactorText(event.getDescription())));
        evLocation.setText(event.getEvent_location_id());
        evCategory.setText(event.getEvent_type_id());

        /* Adding click listeners */
        evSubscribe.setOnClickListener(this);
        evUnsubscribe.setOnClickListener(this);
        showSubscribeOrUnsubscribe();
    }

    public String refactorText(String originalText){
        String newText;
        newText = originalText;

        newText = newText.replace("\u0027", "'");

        return newText;
    }

    /** This method will handle the click in the views */
    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch(id){
            case R.id.subscribe:
                if(sessionManager.isLoggedIn()){
                    Boolean subscription_status = false;
                    String subscribeUrl = getResources().getString(R.string.subscribe_event_url);

                    /* Create the SubscriptionRequest */
                    SubscriptionRequest subscribeToEventRequestObject = new SubscriptionRequest(0,
                            sessionManager.getUser().getId(), event.get_ID(), TONIGHT_SUBSCRIBE);

                    /* Let's call the request to register and see what we got in return */
                    GsonRequest<TonightRequest> subscribeRequest = new GsonRequest<TonightRequest>(
                            GsonRequest.Method.POST,
                            subscribeUrl,
                            TonightRequest.class,
                            subscribeToEventRequestObject,
                            new Response.Listener<TonightRequest>() {
                                @Override
                                public void onResponse(TonightRequest response) {
                                    if(response.isStatusReturn())
                                        show_unsubscribe();
                                    else
                                        Log.e("subscribeEvent", response.getStatusMessage());
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getApplicationContext(),
                                            R.string.event_detail_error_network,
                                            Toast.LENGTH_SHORT).show();
                                }
                            },
                            this);

                    /* Adding the request to the queue */
                    mRequestQueue.add(subscribeRequest);
                }
                else{
                    Toast.makeText(getApplicationContext(), R.string.event_detail_not_auth,
                            Toast.LENGTH_LONG);
                }
                break;

            case R.id.unsubscribe:
                if(sessionManager.isLoggedIn()){
                    Boolean subscription_status = false;
                    String subscribeUrl = getResources().getString(R.string.subscribe_event_url);

                    /* Create the SubscriptionRequest */
                    SubscriptionRequest subscribeToEventRequestObject = new SubscriptionRequest(0,
                            sessionManager.getUser().getId(), event.get_ID(), TONIGHT_UNSUBSCRIBE);

                    /* Let's call the request to register and see what we got in return */
                    GsonRequest<TonightRequest> subscribeRequest = new GsonRequest<TonightRequest>(
                            GsonRequest.Method.POST,
                            subscribeUrl,
                            TonightRequest.class,
                            subscribeToEventRequestObject,
                            new Response.Listener<TonightRequest>() {
                                @Override
                                public void onResponse(TonightRequest response) {
                                    if(response.isStatusReturn())
                                        show_subscribe();
                                    else
                                        Log.e("subscribeEvent", response.getStatusMessage());
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getApplicationContext(),
                                            R.string.event_detail_error_network,
                                            Toast.LENGTH_SHORT).show();
                                }
                            },
                            this);

                    /* Adding the request to the queue */
                    mRequestQueue.add(subscribeRequest);
                }
                else{
                    Toast.makeText(getApplicationContext(), R.string.event_detail_not_auth,
                            Toast.LENGTH_LONG);
                }
                break;
        }
    }


    /** This function hide the subscribe button and show the unsubscribe button */
    public void show_unsubscribe(){
        if(viewSwitcher.getCurrentView() == evSubscribe)
            viewSwitcher.showNext();
    }
    public void show_subscribe(){
        if(viewSwitcher.getCurrentView() == evUnsubscribe)
            viewSwitcher.showNext();
    }

    /** this method checks whether the user is already registered to the event and show the right
     * button accordingly
     */
    public void showSubscribeOrUnsubscribe(){
        if(sessionManager.isLoggedIn()){
            String isSubscribeUrl = getResources().getString(R.string.is_subscribe_event_url);
            /* Let's call the request to register and see what we got in return */
            GsonRequest<TonightRequest> isSubscribeRequest = new GsonRequest<TonightRequest>(
                    GsonRequest.Method.POST,
                    isSubscribeUrl,
                    TonightRequest.class,
                    new IsSubscribedRequest(sessionManager.getUser().getId(), event.get_ID()),
                    new Response.Listener<TonightRequest>() {
                        @Override
                        public void onResponse(TonightRequest response) {
                            if(response.isStatusReturn())
                                show_unsubscribe();
                            else
                                show_subscribe();
                            Log.i("isSubscribed", response.getStatusMessage());
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            show_unsubscribe();
                        }
                    },
                    this);

                    /* Adding the request to the queue */
            mRequestQueue.add(isSubscribeRequest);
        }
    }
}
