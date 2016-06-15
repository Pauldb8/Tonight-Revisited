package info.debuck.tonight;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
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
import info.debuck.tonight.EventClass.TonightReportDialog;
import info.debuck.tonight.EventClass.TonightEvent;
import info.debuck.tonight.EventClass.TonightEventForeignKeys;
import info.debuck.tonight.EventClass.TonightEventPost;
import info.debuck.tonight.EventClass.TonightRequest;
import info.debuck.tonight.EventClass.User;
import info.debuck.tonight.EventClass.UserAvatar;
import info.debuck.tonight.Tools.GsonRequest;
import info.debuck.tonight.Tools.SessionManager;

public class EventDescriptionActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final int TONIGHT_SUBSCRIBE = 2;
    private static final int TONIGHT_UNSUBSCRIBE = 3;
    public static String TONIGHT_INTENT_USER_DETAILS = "tonight_intent_user_details";
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
    private LinearLayout llEventParticipants;
    private LinearLayout llFourthRow;
    private UserAvatar userAvatar;

    /* TonightEvent properties */
    private Gson mGson;
    private String serializedObject;
    private TonightEvent event;
    private TonightEventForeignKeys eventFK;
    private User mUser;
    private User[] mParticipants;

    /* Network Singleton information */
    private ImageLoader mImageLoader;
    private RequestQueue mRequestQueue;
    SessionManager sessionManager;

    //Pour formater la date en Sam. 21 Jan 2015 à 21:05
    private SimpleDateFormat fDateAndTimeEvent = new SimpleDateFormat("EEE d MMM yyyy à HH:mm");
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** We will load the received extra intent and associate it with the views */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_description_activity);

        /* Setting transparent toolbar */
        if (((Toolbar) findViewById(R.id.toolbar)) != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ((Toolbar) findViewById(R.id.toolbar)).setBackground(getDrawable(R.drawable.background_toolbar_translucent));
            }
        }

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
        llWritePost = (LinearLayout) findViewById(R.id.SixthRow);
        llFourthRow = (LinearLayout) findViewById(R.id.FourthRow);
        llEventParticipants = (LinearLayout) findViewById(R.id.event_participant);
        sendWritePost = (TextView) findViewById(R.id.sendWritePost);
        lvEventListPost = (ListView) findViewById(R.id.event_post_list);
        userAvatar = (UserAvatar) findViewById(R.id.uaProfilePicture);

        /* Getting the serialized TonightEvent object from the intent extra and creating an instance
        * of TonightEvent from it */
        mGson = NetworkSingleton.getInstance(this).getGson();

        /* Getting the loader information from Network Singleton */
        mImageLoader = NetworkSingleton.getInstance(this.getApplicationContext()).getImageLoader();
        mRequestQueue = NetworkSingleton.getInstance(this).getRequestQueue();

        /* Getting the event to show, wheter by dejsonified extra or by downloading with the id */
        if(getIntent().hasExtra(MainActivity.TONIGHT_INTENT_EXTRA_DESC)) {
            serializedObject = getIntent().getStringExtra(MainActivity.TONIGHT_INTENT_EXTRA_DESC);
            event = mGson.fromJson(serializedObject, TonightEvent.class);
            fillDetails();
        }

        /* If we only got the id from the event, we proceed to download it */
        else if(getIntent().hasExtra(MainActivity.TONIGHT_INTENT_EXTRA_DESC_ID)){
            int id = getIntent().getIntExtra(MainActivity.TONIGHT_INTENT_EXTRA_DESC_ID, 0);
            GsonRequest<TonightEvent> getEvent = new GsonRequest<>(
                    getString(R.string.url_event_by_id) + "?event_id=" + id,
                    TonightEvent.class,
                    new Response.Listener<TonightEvent>() {
                        @Override
                        public void onResponse(TonightEvent response) {
                            /* Getting the event back */
                            event = response;
                            /* filling the views with the event informaiton */
                            fillDetails();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("EDA", "Did NOT receive event" + error.getMessage());
                        }
                    },
                    this);
            mRequestQueue.add(getEvent);
        }



        /* Don't show the keyboard at first */
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    /** We inflate the menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        /* If user is creator, then show modifiy button, else don't */
        if(sessionManager.isLoggedIn() && sessionManager.getUser().getId() == event.getUser_id()) {
            getMenuInflater().inflate(R.menu.event_description_menu_author, menu);
        }else{
            getMenuInflater().inflate(R.menu.event_description_menu, menu);
        }
        return true;
    }
    /**
     * Once we got the information from a specific event, we fill the views
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.event_description_report) {
            TonightReportDialog dialog = new TonightReportDialog();
            dialog.show(getSupportFragmentManager(), "EventDescriptionActivity");
        }
        else if (id == R.id.event_description_modify){
            /* We'll create an intent with the event and event location as extra */
            if(event != null && eventFK != null) {
                Intent openDetail = new Intent(this, ManageEventActivity.class);
                String serializedObject = mGson.toJson(event);
                String serializedObjectFK = mGson.toJson(eventFK);
                //Log.i("Test", serializedObject);
                openDetail.putExtra(MainActivity.TONIGHT_INTENT_EXTRA_DESC, serializedObject);
                openDetail.putExtra(ManageEventActivity.TONIGHT_INTENT_EXTRA_DESC_FK, serializedObjectFK);
                startActivity(openDetail);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void fillDetails() {
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

        /* Adding the request to the request queue */
        mRequestQueue.add(fkrequest);

        mUser = NetworkSingleton.getInstance(this).getConnectedUSer();
        /* getting user information, only if user is connected */
        if(sessionManager.isLoggedIn()){
            NetworkSingleton.getInstance(this).setConnectedUSer(sessionManager.getUser());
            userAvatar.setImageUrl(mUser.getPicture_url(), mImageLoader);
            showWritePost();
            showSubscribeOrUnsubscribe();
        }


        /* Associating information from the event object to its respective views */
        evDescPicture.setImageUrl(event.getPicture_url(), mImageLoader);
        evTitle.setText(event.getName());
        evStartDate.setText(event.getStartDateFormatted());
        evPrice.setText(event.getPriceFormatted());
        evStartTime.setText(event.getStartHour());
        evDescription.setText(Html.fromHtml(refactorText(event.getDescription())));

        setParticipantView(llEventParticipants);

        /* Adding click listeners */
        evSubscribe.setOnClickListener(this);
        evUnsubscribe.setOnClickListener(this);
        sendWritePost.setOnClickListener(this);
        evLocation.setOnClickListener(this);
        lvEventListPost.setOnItemClickListener(this);
        llFourthRow.setOnClickListener(this);

        /* We get the post the this event and download and show them asynchronously */
        getPostEventToView getEventPostAsyncTask = new getPostEventToView(this, lvEventListPost,
                getPostEventToView.REQUEST_PARENT_POST, event.get_ID());
        getEventPostAsyncTask.execute();
        lvEventListPost.setOnItemClickListener(this);
    }


    /**
     * This method will get the users subscribed to this event and put them in their view
     * @param llEventParticipants
     */
    private void setParticipantView(LinearLayout llEventParticipants) {
        /* Getting the subscribed users */
        final User[] subscribedUser = null;
        String getUserUrl = getString(R.string.get_user_subscribed_url);

        GsonRequest<User[]> getSubscridedUser = new GsonRequest<User[]>(
                getUserUrl + "?event_id=" + event.get_ID(),
                User[].class,
                new Response.Listener<User[]>() {
                    @Override
                    public void onResponse(User[] response) {
                        mParticipants = response;
                        fillViewWithParticipants(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("EDA", "Couldn't get subscribed users.");
                    }
                },
                this);

        mRequestQueue.add(getSubscridedUser);
    }


    /**
     * This method will place the retrieved user in the view where they belong
     * @param subscribedUsers
     */
    private void fillViewWithParticipants(User[] subscribedUsers) {
        /* if there is at least 1 user, the response is not null */
        if(subscribedUsers != null){
            int maxUsers = 5; /* How many partcipants are we going to display */
            /* First hide the no participants view */
            ((TextView) findViewById(R.id.event_no_participants)).setVisibility(View.GONE);

            /* We create the view that are going  to add */
            for(int i = 0; i < Math.min(subscribedUsers.length, maxUsers); i++){
                User currentUser = subscribedUsers[i];

                UserAvatar picture = new UserAvatar(this);
                picture.setImageUrl(currentUser.getPicture_url(), mImageLoader);
                picture.setLayoutParams(new ActionBar.LayoutParams(100, 100));

                ((LinearLayout) findViewById(R.id.event_participant)).addView(picture);
            }

            /* this show that there are more partcipants */
            if(subscribedUsers.length > maxUsers){
                TextView moreUsers = new TextView(this);
                if(subscribedUsers.length-maxUsers > 1)
                    moreUsers.setText(String.format(getString(R.string.event_detail_more_users),
                        subscribedUsers.length-maxUsers) + "s");
                else
                    moreUsers.setText(String.format(getString(R.string.event_detail_more_users),
                            subscribedUsers.length-maxUsers));
                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                params.setMargins(32, 0, 0, 0);

                moreUsers.setLayoutParams(params);
                moreUsers.setTextColor(getResources().getColor(R.color.primary_text));
                ((LinearLayout) findViewById(R.id.event_participant)).addView(moreUsers);
            }
        }
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
            case R.id.FourthRow:
                /* Creating the JSONIFIED object of user array */
                Intent viewUsersDetail = new Intent(this, SubscribedUsersActivty.class);
                String usersArray = NetworkSingleton.getInstance(this).getGson().
                        toJson(mParticipants);
                viewUsersDetail.putExtra(EventDescriptionActivity.TONIGHT_INTENT_USER_DETAILS,
                        usersArray);
                startActivityForResult(viewUsersDetail, 3 );
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
        if(sessionManager.isLoggedIn() || mUser != null){
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
        int viewId = view.getId();

        /* The user clicked the name or avatar of a participant's post */
        if(viewId == R.id.event_post_full_name || viewId == R.id.uaProfilePicture){
            /* the user clicked on the name or avatar of a participant */
            Intent openDetail = new Intent(this, ProfileAndFriendsActivity.class);

            /* Getting the user info */
            String serializedObject = mGson.toJson(((EventPostCustomAdapter)parent.getAdapter())
                    .getmUser(position));
            //Log.i("Test", serializedObject);
            openDetail.putExtra(ProfileAndFriendsActivity.SHOW_OTHER_USER_PROFILE, serializedObject);
            startActivityForResult(openDetail, 0);
        }
    }
}
