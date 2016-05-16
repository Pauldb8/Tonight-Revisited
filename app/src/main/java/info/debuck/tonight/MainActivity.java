package info.debuck.tonight;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.google.gson.Gson;
import com.pkmmte.view.CircularImageView;

import info.debuck.tonight.EventClass.TonightEvent;
import info.debuck.tonight.EventClass.User;
import info.debuck.tonight.Tools.SessionManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AdapterView.OnItemClickListener, View.OnClickListener {


    public static final String TONIGHT_INTENT_EXTRA_DESC = "tonight.intent.extra_desc";
    public static final String TONIGHT_INTENT_EXTRA_LOGIN = "tonight.intent.extra_login";
    static final int TONIGHT_AUTH_REQUEST = 8020;  // The request code
    private ListView mainListView = null;
    private User mUser;
    private DrawerLayout mDrawer;
    private Gson mGson;
    private NavigationView navigationView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.main_include)
                .findViewById(R.id.fab);
        fab.setOnClickListener(this);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /* Verifiy if the user has login credential from previous use of application, in which case
         * we can log him in right away.
         */
        sessionManager = new SessionManager(this);
        if(sessionManager.isLoggedIn()){
            NetworkSingleton.getInstance(this).setConnectedUSer(sessionManager.getUser());
        }

        /* See if we need to update the drawer when the user is connected for this session */
        if(NetworkSingleton.getInstance(this).getConnectedUSer() != null) {
            mUser = NetworkSingleton.getInstance(this).getConnectedUSer();
            updateDrawerInfo(mUser);
            Log.i("MainActivity", mUser.toString());
        }

        /* Starting the NetworkSingleton for unique use with Volley downloader */
        RequestQueue requestQueue = NetworkSingleton.getInstance(this)
                .getRequestQueue();
        requestQueue.start();

        /* Unique GSON from singleton */
        mGson = NetworkSingleton.getInstance(this).getGson(); /* getting the Gson */


        /* Gettint the ListView and calling function which will aynchronously download all event,
        and set the Adapter to the view one download is done. We also add the onClick listener
         */
        mainListView = (ListView) findViewById(R.id.mainListView);
        mainListView.setOnItemClickListener(this);
        getEventToView getEventAsyncTask = new getEventToView(this, mainListView,
                getEventToView.REQUEST_ALL_EVENT);
        getEventAsyncTask.execute();

    }

    /** This method will update the texts on the drawer with the user information
     * It will erase the previous navigation view and recreate a new one with the online menu for the
     * user.
     * @param user
     */
    private void updateDrawerInfo(User user) {
        if(user != null) {
            /* We delete previous header */
            navigationView.removeHeaderView(navigationView.getHeaderView(0));
            navigationView.inflateHeaderView(R.layout.nav_header_main);

            /* we delete previous offline menu and replace with online one */
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.activity_main_drawer_online);

            View header = navigationView.getHeaderView(0);

            TextView a = ((TextView) header.findViewById(R.id.user_name));
            a.setText(user.getFullName());
            a.setOnClickListener(this);
            TextView b = ((TextView) header.findViewById(R.id.user_email));
            b.setText(user.getEmail());
            b.setOnClickListener(this);
            CircularImageView c = (CircularImageView) header.findViewById(R.id.user_picture);
            c.setOnClickListener(this);
            //Toast.makeText(this, user.toString(), Toast.LENGTH_LONG).show();
        }
        else { //User is signing out
            /* We delete previous header */
            navigationView.removeHeaderView(navigationView.getHeaderView(0));
            navigationView.inflateHeaderView(R.layout.nav_header_main);

            /* we delete previous offline menu and replace with online one */
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.activity_main_drawer_offline);
            //Toast.makeText(this, user.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** for handling click from the drawer menu
     *
     * @param item: clicked item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        /* Connect button, start LoginActivity */
        switch(id){
            case R.id.nav_connect:
                startActivityForResult(new Intent(this, LoginActivity.class), TONIGHT_AUTH_REQUEST);
                break;
            case R.id.nav_disconnect:
                disconnect_user();
                break;
            case R.id.nav_profile:
                startActivity(new Intent(this, ProfileAndFriendsActivity.class));
                break;
            case R.id.nav_register:
                startActivity(new Intent(this, SubscribeActivity.class));
                break;
            case R.id.nav_event:
                startActivity(new Intent(this, SubscribedEventActivity.class));
                break;
            case R.id.nav_event_created:
                startActivity(new Intent(this, CreatedEventActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     * When clicking an item in the list, we get the TonightEvent associated to it, we then serialize it using
     * Gson, and we call the EventDescriptionActivty intent with the serialized object as an extra
     * to the intent.
     * @param parent: the ArrayAdapter<TonightEvent> associated with the ListView
     * @param position: the position of the clicked object
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TonightEvent clickedEvent = (TonightEvent)parent.getAdapter().getItem(position);
        Intent openDetail = new Intent(this, EventDescriptionActivity.class);
        String serializedObject = mGson.toJson(clickedEvent);
        //Log.i("Test", serializedObject);
        openDetail.putExtra(TONIGHT_INTENT_EXTRA_DESC, serializedObject);
        startActivity(openDetail);
    }

    /**
     * For handling a click from the FLoating Action Button
     * @param v: button clicked.
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();

        /* Clicked the FAB button, check wheter user connected */
        if(id == R.id.fab){
            /* user is connected we start the activity for creating a new event */
            if(NetworkSingleton.getInstance(this).getConnectedUSer() != null){
                startActivity(new Intent(this, AddEventActivity.class));
            }
            /* user is not connected show error message */
            else {
                Toast.makeText(this, getString(R.string.main_error_not_connected), Toast.LENGTH_LONG)
                        .show();
            }
        }

        /* We clicked on our profile name email or picture, we send the user to the profile page */
        if(id == R.id.user_name || id == R.id.user_email || id == R.id.user_picture){
            startActivity(new Intent(this, ProfileAndFriendsActivity.class));
        }
    }

    /** When we got a result back from a subsequent activity */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to

        /* Here we received the intent from the LoginActivity:
        we either received a User class so we put it in the Singleton and update the drawer
         */
        if (requestCode == TONIGHT_AUTH_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                mUser = mGson.fromJson(data.getStringExtra(TONIGHT_INTENT_EXTRA_LOGIN), User.class);
                updateDrawerInfo(mUser);
                NetworkSingleton.getInstance(this).setConnectedUSer(mUser);
            }
        }
    }

    /**
     * This method disconnects the user, it remove the current user from the Singleton and remove
     * the shared pref that allowed him to be connected after the application had close. So next time
     * the user will have to reconnect.
     */
    private void disconnect_user() {
        sessionManager = new SessionManager(this);
        if(sessionManager.isLoggedIn()){
            sessionManager.logoutUser();
            NetworkSingleton.getInstance(this).setConnectedUSer(null);
            //updateDrawerInfo(null);
            Toast.makeText(getApplicationContext(), getString(R.string.auth_disconnect_success),
                    Toast.LENGTH_LONG).show();
            recreate();
        }
    }


}
