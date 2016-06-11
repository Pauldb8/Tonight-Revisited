package info.debuck.tonight;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

import info.debuck.tonight.EventClass.User;

public class SubscribedUsersActivty extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private Gson mGson;

    private ListView lvSubscribedUsers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribed_users_activty);

        /* Setting vars */
        mGson = NetworkSingleton.getInstance(this).getGson();

        /* getting the view */
        lvSubscribedUsers = (ListView) findViewById(R.id.list_subscribed_users);

        /* if we got the intent */
        if(getIntent().hasExtra(EventDescriptionActivity.TONIGHT_INTENT_USER_DETAILS)){
            User[] subscribedUsers;
            subscribedUsers = mGson.fromJson(getIntent().
                    getStringExtra((EventDescriptionActivity.TONIGHT_INTENT_USER_DETAILS)),
                    User[].class);

            ListAdapter mAdapter = new ListUsersAdapter(this, new ArrayList<User>(
                    Arrays.asList(subscribedUsers)));
            lvSubscribedUsers.setAdapter(mAdapter);

            lvSubscribedUsers.setOnItemClickListener(this);
        }
    }

    /**
     * This method will handle click on participants
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /* the user clicked on the name or avatar of a participant */
        Intent openDetail = new Intent(this, ProfileAndFriendsActivity.class);
        String serializedObject = mGson.toJson(parent.getAdapter().getItem(position));
        //Log.i("Test", serializedObject);
        openDetail.putExtra(ProfileAndFriendsActivity.SHOW_OTHER_USER_PROFILE, serializedObject);
        startActivityForResult(openDetail, 0);
    }
}
