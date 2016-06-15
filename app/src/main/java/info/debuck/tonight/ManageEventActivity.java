package info.debuck.tonight;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;

import info.debuck.tonight.EventClass.TonightChangeStatusEventDialog;
import info.debuck.tonight.EventClass.TonightEvent;
import info.debuck.tonight.EventClass.TonightEventLocation;
import info.debuck.tonight.EventClass.User;
import info.debuck.tonight.Tools.GsonRequest;

public class ManageEventActivity extends AppCompatActivity implements View.OnClickListener {

    /* Event vars */
    private TonightEvent mEvent;
    private TonightEventLocation mEventLocation;
    private Gson mGson;
    private ImageLoader mImageLoader;
    private RequestQueue mRequestQueue;
    private User[] mParticipants;

    /* Views */
    private NetworkImageView evDescPicture;
    private TextView evTitle;
    private TextView evStatus;
    private LinearLayout firstRowPartcipant;
    private LinearLayout secondRowStatus;
    private LinearLayout thirdRowEditEvent;

    public static final String TONIGHT_INTENT_EXTRA_DESC_FK = "tonight_intent_event_fk";
    public static final String TONIGHT_INTENT_EXTRA_EVENT = "tonight_event_extra";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_event);

        /* We verify that we got the event as extra */
        if(getIntent().hasExtra(MainActivity.TONIGHT_INTENT_EXTRA_DESC)
                && getIntent().hasExtra(TONIGHT_INTENT_EXTRA_DESC_FK)) {
            mGson = NetworkSingleton.getInstance(this).getGson();
            /* deserialize objects */
            mEvent = mGson.fromJson(
                    getIntent().getStringExtra(MainActivity.TONIGHT_INTENT_EXTRA_DESC),
                    TonightEvent.class);
            mEventLocation = mGson.fromJson(getIntent().getStringExtra(TONIGHT_INTENT_EXTRA_DESC_FK),
                    TonightEventLocation.class);
            mImageLoader = NetworkSingleton.getInstance(this).getImageLoader();
            mRequestQueue = NetworkSingleton.getInstance(this).getRequestQueue();

            associateViews();
            fillViews();
        }
    }


    /**
     * This method will associate views with the vars
     */
    private void associateViews() {
        evDescPicture = (NetworkImageView) findViewById(R.id.evDescPicture);
        evTitle = (TextView) findViewById(R.id.evTitle);
        evStatus = (TextView) findViewById(R.id.evStatus);
        firstRowPartcipant = (LinearLayout) findViewById(R.id.firstRow);
        secondRowStatus = (LinearLayout) findViewById(R.id.secondRow);
        thirdRowEditEvent = (LinearLayout) findViewById(R.id.thirdRow);
    }

    /**
     * This method will fill the views with corresponding elements
     */
    private void fillViews() {
        evDescPicture.setImageUrl(mEvent.getPicture_url(), mImageLoader);
        evTitle.setText(mEvent.getName());
        evStatus.setText((getResources().getStringArray(R.array.status_events))[mEvent.getEvent_status_id()]);
        firstRowPartcipant.setOnClickListener(this);
        secondRowStatus.setOnClickListener(this);
        thirdRowEditEvent.setOnClickListener(this);
    }


    /**
     * This method take action when clicking on a line
     * @param v
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch(id){
            case R.id.firstRow:
                startParticipantIntent();
                break;
            case R.id.secondRow:
                startChangeStatusDialog();
                break;
            case R.id.thirdRow:
                startEditEventActivity();
                break;
            default:
                break;
        }

    }


    /**
     * This method will show the dialog for changing the status
     */
    private void startChangeStatusDialog() {
        TonightChangeStatusEventDialog dialog = new TonightChangeStatusEventDialog();
        Bundle args = new Bundle();
        args.putString(TONIGHT_INTENT_EXTRA_EVENT, mGson.toJson(mEvent));
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "TonightChangeStatusDialog");
    }


    /**
     * This method will show the participant activity
     */
    private void startParticipantIntent() {
        /* Getting the subscribed users */
        String getUserUrl = getString(R.string.get_user_subscribed_url);

        GsonRequest<User[]> getSubscridedUser = new GsonRequest<User[]>(
                getUserUrl + "?event_id=" + mEvent.get_ID(),
                User[].class,
                new Response.Listener<User[]>() {
                    @Override
                    public void onResponse(User[] response) {
                        mParticipants = response;
                        /* Creating the JSONIFIED object of user array */
                        Intent viewUsersDetail = new Intent(getApplicationContext(),
                                SubscribedUsersActivty.class);
                        String usersArray = NetworkSingleton.getInstance(getApplicationContext())
                                .getGson().toJson(mParticipants);
                        viewUsersDetail.putExtra(EventDescriptionActivity.TONIGHT_INTENT_USER_DETAILS,
                                usersArray);
                        startActivityForResult(viewUsersDetail, 3 );
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
     * This method will start the edit event activity
     */
    private void startEditEventActivity() {
        Intent openDetail = new Intent(this, EditEventActivity.class);
        String serializedObject = mGson.toJson(mEvent);
        String serializedObjectFK = mGson.toJson(mEventLocation);
        openDetail.putExtra(MainActivity.TONIGHT_INTENT_EXTRA_DESC, serializedObject);
        openDetail.putExtra(ManageEventActivity.TONIGHT_INTENT_EXTRA_DESC_FK, serializedObjectFK);
        startActivity(openDetail);
    }


    /**
     * This method should be called by the dialog, when called it will retrieve the info from this
     * event from the internet, and refresh the views
     */
    public void updateViews(){
        /* First getting the update event */
        String getEventURL = getString(R.string.url_event_by_id);

        GsonRequest<TonightEvent> getEvent = new GsonRequest<>(
                getEventURL + "?event_id=" + mEvent.get_ID(),
                TonightEvent.class,
                new Response.Listener<TonightEvent>() {
                    @Override
                    public void onResponse(TonightEvent response) {
                        mEvent = response;
                        /* Actually updating the view */
                        evStatus.setText((getResources().getStringArray(R.array.status_events))
                                [mEvent.getEvent_status_id()]);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("EDA", "Couldn't get the new event updated.");
                    }
                },
                this);

        mRequestQueue.add(getEvent);
    }
}
