package info.debuck.tonight;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

import info.debuck.tonight.EventClass.TonightChangeFriendStatusDialog;
import info.debuck.tonight.EventClass.TonightRequest;
import info.debuck.tonight.EventClass.User;
import info.debuck.tonight.EventClass.UserAvatar;
import info.debuck.tonight.EventClass.UserProfile;
import info.debuck.tonight.Tools.GsonRequest;

public class ProfileAndFriendsActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String SHOW_OTHER_USER_PROFILE = "tonight_show_other_user_profile";
    public static final String TONIGHT_INTENT_EXTRA_USER = "tonight_intent_user_extra";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private User mUser;
    private RequestQueue mRequestQueue;
    static final int TONIGHT_CHANGE_PICTURE = 8030;  // The request code
    private TextView tvEmail;
    private TextView tvProfileStatus;
    private FloatingActionButton fab;
    private String serializedObject;
    private boolean isMyProfile = false;
    private int friendshiptStatus;
    TabLayout tabLayout;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_and_friends);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRequestQueue = NetworkSingleton.getInstance(this).getRequestQueue();


        /* Let's get the user we are going to display the info about */
        if(getIntent().hasExtra(SHOW_OTHER_USER_PROFILE)) {
            serializedObject = getIntent().getStringExtra(SHOW_OTHER_USER_PROFILE);
            mUser = NetworkSingleton.getInstance(this).getGson().fromJson(serializedObject, User.class);
            /* If we are connected, it might be our profile we are showing */
            if (NetworkSingleton.getInstance(this).getConnectedUSer() != null) {
                isMyProfile = (mUser.getId() ==
                        NetworkSingleton.getInstance(this).getConnectedUSer().getId()) ? true : false;
                friendshiptStatus = User.FRIENDSHIP_STATUS_MYSELF; /* myself */
            } else {
                isMyProfile = false;
            }
        }else { /* we got no extra, then showing this user infor */
            mUser = NetworkSingleton.getInstance(this).getConnectedUSer();
            isMyProfile = true;
            friendshiptStatus = User.FRIENDSHIP_STATUS_MYSELF; /* myself */
        }


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), mUser,
                isMyProfile);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        if(!isMyProfile){
            if(NetworkSingleton.getInstance(this).getConnectedUSer() != null) { /* connected user */
                 /* we get the friendship status */
                GsonRequest<TonightRequest> getFriendshipStatus = new GsonRequest<TonightRequest>(
                        getString(R.string.url_get_friendship_status) + "?user_id=" +
                                NetworkSingleton.getInstance(this).getConnectedUSer().getId() +
                                "&friend_id=" + mUser.getId(),
                        TonightRequest.class,
                        new Response.Listener<TonightRequest>() {
                            @Override
                            public void onResponse(TonightRequest response) {
                                friendshiptStatus = response.getStatusCode();

                                tvProfileStatus = (TextView) ((Fragment) mSectionsPagerAdapter.
                                        instantiateItem(mViewPager, mViewPager.getCurrentItem()))
                                        .getView().findViewById(R.id.profile_profile);

                                if (friendshiptStatus == User.FRIENDSHIP_STATUS_FRIEND) {
                                    tvProfileStatus.setText(R.string.profile_is_my_friend);
                                    fab.hide();
                                } else if (friendshiptStatus == User.FRIENDSHIP_STATUS_NOT_FRIEND) {
                                    tvProfileStatus.setText(R.string.profile_is_not_my_friend);
                                    fab.show();
                                } else if (friendshiptStatus == User.FRIENDSHIP_STATUS_PENDING) {
                                    tvProfileStatus.setText(R.string.profile_friend_request_sent);
                                    fab.hide();
                                }

                                tvProfileStatus.setVisibility(View.VISIBLE);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("PAFA", "Error while retrieving friendship status: "
                                        + error.getMessage());
                            }
                        },
                        this);
                mRequestQueue.add(getFriendshipStatus);
            }
            else { /* Not connected user */
                friendshiptStatus = 3;
                //fab.hide();
            }
        }
        else
            fab.hide(); /*Hidden on default */


        final boolean finalIsMyProfile = isMyProfile;
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == 0){
                    /* In my profile we don't show the add button in the Profile section */
                    if(finalIsMyProfile)
                        fab.hide();
                    else if(!finalIsMyProfile && friendshiptStatus != User.FRIENDSHIP_STATUS_FRIEND
                            && friendshiptStatus != User.FRIENDSHIP_STATUS_PENDING) {
                        /* We show only if we are viewing another user's profile */
                        fab.show();
                    }
                    else {
                        fab.hide();
                    }

                    mViewPager.setCurrentItem(tab.getPosition());
                }
                else {
                    /* if it is our profile we show the add friends button */
                    if(finalIsMyProfile)
                        fab.show();
                    else /* if it is another user's friends page, we don't show the button */
                        fab.hide();

                    mViewPager.setCurrentItem(tab.getPosition());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


       @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile_and_friends, menu);
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

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.fab){ /* we clicked the fab button, what do we want ? */
            if(!isMyProfile) { /* We are seeing another user's profile */
                if(friendshiptStatus != User.FRIENDSHIP_STATUS_FRIEND && mUser != null) { /* We are not already friends */
                    if(NetworkSingleton.getInstance(this).getConnectedUSer() != null) {
                         /* we add as friend */
                        GsonRequest<TonightRequest> addFriend = new GsonRequest<TonightRequest>(
                                getString(R.string.url_add_friend) + "?user_id=" +
                                        NetworkSingleton.getInstance(this).getConnectedUSer().getId() +
                                        "&friend_id=" + mUser.getId(),
                                TonightRequest.class,
                                new Response.Listener<TonightRequest>() {
                                    @Override
                                    public void onResponse(TonightRequest response) {
                                        if (response.isStatusReturn()) { /* Friend request correctly sent */
                                            tvProfileStatus.setText(getString(R.string.profile_friend_request_sent));
                                            Toast.makeText(getApplicationContext(),
                                                    getString(R.string.profile_friend_request_sent_correctly),
                                                    Toast.LENGTH_LONG).show();
                                            friendshiptStatus = User.FRIENDSHIP_STATUS_PENDING;
                                            fab.hide();
                                        } else {
                                            Toast.makeText(getApplicationContext(),
                                                    getString(R.string.profile_friend_request_send_error),
                                                    Toast.LENGTH_LONG).show();
                                        }

                                        tvProfileStatus.setVisibility(View.VISIBLE);
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("PAFA", "Error while retrieving friendship status: "
                                                + error.getMessage());
                                        Toast.makeText(getApplicationContext(),
                                                getString(R.string.profile_friend_request_send_error),
                                                Toast.LENGTH_LONG).show();
                                    }
                                },
                                this);
                        mRequestQueue.add(addFriend);
                    }
                    else { /* not connected */
                        Toast.makeText(this, getString(R.string.event_detail_not_auth),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }
    }


    /* update view */
    public void updateViews() {
        recreate();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_IS_MY_PROFILE = "tonight_is_my_profile";
        private static final String ARG_USER_INFO = "tonight_user_info";
        private User mUser;
        private UserProfile mUserProfile;
        private ImageLoader mImageLoader;
        private RequestQueue mRequestQueue;
        private Gson mGson;

        /* Views */
        /* Profile Fragment views */
        private TextView tvProfileName;
        private TextView tvProfileEmail;
        private TextView tvProfileProfile;
        private ImageView ivEditProfilePicture;
        private RelativeLayout rlFriendSwitcher;
        private ViewSwitcher vsFriendButton;
        private TextView tvAddAsFriend;
        private TextView tvRemoveAsFriend;
        private UserAvatar uaProfilePicture;
        /* Friend list Fragment views */
        private ListView lvFriendList;
        private LinearLayout lvEmptyList;

        /* vars */
        private int chosenView;
        private boolean isMyProfile;


        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, String user, boolean isMyProfile) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString(ARG_USER_INFO, user);
            args.putBoolean(ARG_IS_MY_PROFILE, isMyProfile);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            chosenView = getArguments().getInt(ARG_SECTION_NUMBER);
            isMyProfile = getArguments().getBoolean(ARG_IS_MY_PROFILE);
            mUser = NetworkSingleton.getInstance(getActivity().getApplicationContext()).getGson()
                .fromJson(getArguments().getString(ARG_USER_INFO), User.class);
            mGson = NetworkSingleton.getInstance(getActivity().getApplicationContext()).getGson();

            View rootView;

            switch(chosenView){
                case 1:
                    rootView = inflater.inflate(R.layout.content_profile, container, false);
                    /** Setting the views */
                    mImageLoader = NetworkSingleton.getInstance(getActivity().getApplicationContext())
                            .getImageLoader();
                    setupViews(rootView, isMyProfile);
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.content_friends, container, false);
                    mImageLoader = NetworkSingleton.getInstance(getActivity().getApplicationContext())
                            .getImageLoader();
                    mRequestQueue = NetworkSingleton.getInstance(getActivity().getApplicationContext())
                            .getRequestQueue();
                    setupFriendView(rootView);
                    break;
                default:
                    rootView = inflater.inflate(R.layout.content_profile, container, false);
            }

            return rootView;
        }

        /**
         * This methods places the information from the user in their corresponding position on the view
         */
        public void setupViews(View rootView, boolean isMyProfile) {
            tvProfileName = (TextView) rootView.findViewById(R.id.profile_name);
            tvProfileEmail = (TextView) rootView.findViewById(R.id.profile_email);
            tvProfileProfile = (TextView) rootView.findViewById(R.id.profile_profile);
            ivEditProfilePicture = (ImageView) rootView.findViewById(R.id.editProfile);
            uaProfilePicture = (UserAvatar) rootView.findViewById(R.id.profile_picture);
            tvProfileName.setText(mUser.getFullName());
            tvProfileEmail.setText(mUser.getEmail());

            /* Only if this is our profile */
            if(isMyProfile){
                ivEditProfilePicture.setVisibility(View.VISIBLE);
                ivEditProfilePicture.setOnClickListener(this);
            }else{
                tvProfileProfile.setVisibility(View.VISIBLE);
                tvProfileProfile.setText(getString(R.string.profile_is_not_my_friend));

                /* Adding the friend button */
                showAndSetupFriendButton(rootView);
            }
            uaProfilePicture.setImageUrl(mUser.getPicture_url(), mImageLoader);
        }


        /**
         * This method shows the friend/unfriend button and reacts to it push
         */
        private void showAndSetupFriendButton(View rootView) {
            /* We show the button */

        }

        /**
         * This method places the information from the user's friends in the right fragment
         * @param rootView this is the view we are populating
         */
        private void setupFriendView(View rootView){
            User[] friendList = null;
            lvFriendList = (ListView) rootView.findViewById(R.id.list_user_friends);
            lvEmptyList = (LinearLayout) rootView.findViewById(R.id.empty_friend_view);

            String request_friends_url;
            if(isMyProfile) {
                request_friends_url = getString(R.string.url_user_friends);
            }
            else{
                request_friends_url = getString(R.string.url_other_user_friends);
            }
            request_friends_url += "?user_id=" + mUser.getId();

            /* GsonRequest to get the friend list and set them in the adapter view */
            GsonRequest<User[]> getFriends = new GsonRequest<User[]>(request_friends_url,
                    User[].class,
                    new Response.Listener<User[]>() {
                        @Override
                        public void onResponse(User[] friendList) {
                            lvFriendList.setEmptyView(lvEmptyList);
                            /* Si il a effectivement des amis */
                            if(friendList != null) {
                                ListAdapter mAdapter =
                                        new ListUsersAdapter(getActivity().getApplicationContext(),
                                                new ArrayList<User>(Arrays.asList(friendList)));
                                lvFriendList.setAdapter(mAdapter);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("PAFA", "Error getting friend list: " + error.getMessage());
                        }
                    },
                    getActivity().getApplicationContext());

            mRequestQueue.add(getFriends);

            /* OnItemClickListener for when clicking a username */
            lvFriendList.setOnItemClickListener(this);
            lvFriendList.setOnItemLongClickListener(this);
        }

        /* This will handle click inside the fragment picture */
        @Override
        public void onClick(View v) {
            int id = v.getId();

            switch(id) {
                case R.id.editProfile:
                    startActivityForResult(new Intent(getActivity().getApplicationContext(),
                            ChangeProfilePictureActivity.class),
                            TONIGHT_CHANGE_PICTURE);
                    break;
            }
        }

        @Override
        public void onResume(){
            super.onResume();
            Log.i("ProfileAndFriends", "resumed");
        }

        /** When we got a result back from a subsequent activity */
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            // Check which request we're responding to

        /* Here we received the intent from the LoginActivity:
        we either received a User class so we put it in the Singleton and update the drawer
         */
            Log.i("PAFA", "updated picture received");
            if (requestCode == TONIGHT_CHANGE_PICTURE) {
                // Make sure the request was successful
                if (resultCode == RESULT_OK) {
                    /* Getting new information from the user */
                    mUser = NetworkSingleton.getInstance(getActivity().getApplicationContext())
                            .getConnectedUSer();
                    /* Changing the profile picture */
                    uaProfilePicture.setImageUrl(mUser.getPicture_url(), mImageLoader);

                }else{
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }else{
                super.onActivityResult(requestCode, resultCode, data);
            }
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /* the user clicked on the name or avatar of a participant */
            Intent openDetail = new Intent(getActivity(), ProfileAndFriendsActivity.class);
            String serializedObject = mGson.toJson(parent.getAdapter().getItem(position));
            //Log.i("Test", serializedObject);
            openDetail.putExtra(ProfileAndFriendsActivity.SHOW_OTHER_USER_PROFILE, serializedObject);
            startActivityForResult(openDetail, 0);
        }


        /**
         * We want to add or remove from friends
         * @param parent
         * @param view
         * @param position
         * @param id
         * @return
         */
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            User userClicked = (User) parent.getAdapter().getItem(position);
            TonightChangeFriendStatusDialog dialog = new TonightChangeFriendStatusDialog();
            Bundle args3 = new Bundle();
            args3.putString(ProfileAndFriendsActivity.TONIGHT_INTENT_EXTRA_USER, mGson.toJson(userClicked));
            dialog.setArguments(args3);
            dialog.show(getActivity().getSupportFragmentManager(), "PAFA");
            return true;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private User mUser;
        private String mUserJsonified;
        private boolean isMyProfile = true;

        public SectionsPagerAdapter(FragmentManager fm, User user, boolean isMyProfile) {
            super(fm);
            this.mUser = user;
            this.mUserJsonified = NetworkSingleton.getInstance(getApplicationContext()).getGson()
                    .toJson(user);
            this.isMyProfile = isMyProfile;
        }


        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1, mUserJsonified, isMyProfile);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    if(isMyProfile)
                        return getResources().getString(R.string.profile_tab_my_profile);
                    else
                        return getResources().getString(R.string.profile_tab_profile);
                case 1:
                    if(isMyProfile)
                        return getResources().getString(R.string.profile_tab_my_friends);
                    else
                        return getResources().getString(R.string.profile_tab_friends);
            }
            return null;
        }
    }
}
