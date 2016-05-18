package info.debuck.tonight;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import info.debuck.tonight.EventClass.IsSubscribedRequest;
import info.debuck.tonight.EventClass.SubscriptionRequest;
import info.debuck.tonight.EventClass.TonightEvent;
import info.debuck.tonight.EventClass.TonightEventForeignKeys;
import info.debuck.tonight.EventClass.TonightEventPost;
import info.debuck.tonight.EventClass.TonightRequest;
import info.debuck.tonight.EventClass.User;
import info.debuck.tonight.Tools.GsonRequest;
import info.debuck.tonight.Tools.SessionManager;

public class EventDescriptionActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

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
    private LinearLayout llWritePost;
    private ViewSwitcher viewSwitcher;
    private TextView sendWritePost;
    private ListView lvEventListPost;

    /* TonightEvent properties */
    private Gson gson;
    private String serializedObject;
    private TonightEvent event;
    private TonightEventForeignKeys eventFK;
    private User mUser;

    /* Network Singleton information */
    private ImageLoader mImageLoader;
    private RequestQueue mRequestQueue;
    SessionManager sessionManager;

    //Pour formater la date en Sam. 21 Jan 2015 à 21:05
    private SimpleDateFormat fDateAndTimeEvent = new SimpleDateFormat("EEE d MMM yyyy à hh:mm");
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
        llWritePost = (LinearLayout) findViewById(R.id.fifthRow);
        sendWritePost = (TextView) findViewById(R.id.sendWritePost);
        lvEventListPost = (ListView) findViewById(R.id.event_post_list);

        /* Getting the serialized TonightEvent object from the intent extra and creating an instance
        * of TonightEvent from it */
        gson = NetworkSingleton.getInstance(this).getGson();
        serializedObject = getIntent().getStringExtra(MainActivity.TONIGHT_INTENT_EXTRA_DESC);
        event = gson.fromJson(serializedObject, TonightEvent.class);
        /* Get the foreign keys of the event thanks to a volley request */
        GsonRequest<TonightEventForeignKeys> fkrequest = new GsonRequest<TonightEventForeignKeys>(
                getString(R.string.url_event_foreign_keys) + "?id=" + event.get_ID(),
                TonightEventForeignKeys.class,
                new Response.Listener<TonightEventForeignKeys>() {
                    @Override
                    public void onResponse(TonightEventForeignKeys response) {
                        eventFK = response;
                        evLocation.setText(eventFK.getAddress());
                        evCategory.setText(NetworkSingleton.getInstance(getApplicationContext())
                                .getEventCategory().getCategoryName(eventFK.getCategory()));
                        Log.i("EDA", "received FKeys");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("EDA", "Did NOT receive FKeys" + error.getMessage());
                    }
                }, this);


        /* Getting the loader information from Network Singleton */
        mImageLoader = NetworkSingleton.getInstance(this.getApplicationContext()).getImageLoader();
        mRequestQueue = NetworkSingleton.getInstance(this).getRequestQueue();

        /* Adding the request to the request queue */
        mRequestQueue.add(fkrequest);

        /* getting user information */
        mUser = NetworkSingleton.getInstance(this).getConnectedUSer();

        /* Associating information from the event object to its respective views */
        evDescPicture.setImageUrl(event.getPicture_url(), mImageLoader);
        evTitle.setText(event.getName());
        evStartDate.setText(event.getStartDateFormatted());
        evPrice.setText(event.getPriceFormatted());
        evStartTime.setText(event.getStartHour());
        //todo: evLocation.setText(event.get);
        evDescription.setText(Html.fromHtml(refactorText(event.getDescription())));

        /* Adding click listeners */
        evSubscribe.setOnClickListener(this);
        evUnsubscribe.setOnClickListener(this);
        sendWritePost.setOnClickListener(this);
        evLocation.setOnClickListener(this);
        lvEventListPost.setOnItemClickListener(this);

        /* We get the post the this event and download and show them asynchronously */
        getPostEventToView getEventPostAsyncTask = new getPostEventToView(this, lvEventListPost,
                getPostEventToView.REQUEST_PARENT_POST, event.get_ID());
        getEventPostAsyncTask.execute();

        showSubscribeOrUnsubscribe();
        showWritePost();

        /* Don't show the keyboard at first */
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
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
                    Toast.makeText(this, R.string.event_detail_not_auth,
                            Toast.LENGTH_LONG).show();
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
                    Toast.makeText(this, R.string.event_detail_not_auth,
                            Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.evLocation:
                // Search for restaurants in San Francisco
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + eventFK.getAddress());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                startActivity(mapIntent);
                    break;
            case R.id.sendWritePost:
                trySendPost();
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

    /**
     * This method will show the box to write a post to the event, only if we are authenticated
     */
    public void showWritePost(){
        /* Si on est connecté */
        if(sessionManager.isLoggedIn()){
            llWritePost.setVisibility(View.VISIBLE);
        }else {
            llWritePost.setVisibility(View.GONE);
        }
    }

    /**
     * This method will check that the user entered information to send and then will send it to
     * the post of this event
     */
    public void trySendPost(){
        EditText etWritePost = ((EditText) findViewById(R.id.etWritePost));
        /* First let's make sure the post isn't empty */
        String post = etWritePost.getText().toString();
        if(post.isEmpty()){
            etWritePost.setError(getString(R.string.add_event_post_empty));
            etWritePost.requestFocus();
        }else{
            etWritePost.setError(null);
            /* Post isn't empty, we send it to the database */
            /* Creating the post object in order to send it */
            Calendar cal = Calendar.getInstance();
            String date = dateFormat.format(cal.getTime()); //2014/08/06 16:00:22
            TonightEventPost directPost = new TonightEventPost(event.get_ID(), mUser.getId(), date, post);
            Log.i("EDA", "TEP = " + directPost.toString());

            /* Create the volley request to send the object */
            String urlToAddPost = getString(R.string.add_event_post_url);
            GsonRequest<TonightRequest> addDirectPost = new GsonRequest<TonightRequest>(
                    Request.Method.POST,
                    urlToAddPost,
                    TonightRequest.class,
                    directPost,
                    new Response.Listener<TonightRequest>() {
                        @Override
                        public void onResponse(TonightRequest response) {
                            if (response.isStatusReturn()) { /* correctly added to the database */
                                recreate();
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.event_detail_post_added),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        response.getStatusMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.add_event_error_network),
                                    Toast.LENGTH_LONG).show();
                        }
            }, this);

            /* Adding to the request queue */
            mRequestQueue.add(addDirectPost);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
