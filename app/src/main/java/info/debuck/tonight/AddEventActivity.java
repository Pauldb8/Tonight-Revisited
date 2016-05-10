package info.debuck.tonight;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import info.debuck.tonight.EventClass.TonightEvent;
import info.debuck.tonight.EventClass.TonightEventLocation;
import info.debuck.tonight.EventClass.TonightRequest;
import info.debuck.tonight.EventClass.User;
import info.debuck.tonight.Tools.GsonRequest;
import info.debuck.tonight.Tools.PlaceAutoCompleteAdapter;

//import com.google.android.gms.location.places.Place;
//import com.google.android.gms.location.places.ui.PlacePicker;


/**
 * This activity let's the user create an event and add it to the database
 */
public class AddEventActivity extends ActionBarActivity implements View.OnClickListener,
        TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener,
        NumberPicker.OnValueChangeListener, GoogleApiClient.OnConnectionFailedListener {

    private static final int TONIGHT_ADD_EVENT_ERROR_LOCATION = 8020;
    private static final int TONIGHT_ADD_EVENT_ERROR_NETWORK = 8021;
    private static final int TONIGHT_ADD_EVENT_FILL_ALL = 8022;

    //The views
    private TextView tvStartTime;
    private TextView tvEndTime;
    private Switch swAllday;
    private TextView tvStartDate;
    private TextView tvPrice;
    private TextView tvPosition;
    private TextView tvCreateEvent;
    private TextView tvMaxPeople;
    private EditText tvEditMaxPeople;
    private EditText tvTitle;
    private EditText tvDescription;
    private ImageView ivAddCamera;
    private ImageView ivAddGallery;
    private Spinner spCategory;
    private LinearLayout llAddPicture;
    NavigationView mNavigationView;
    DrawerLayout mDrawerLayout;
    private User userInfo;
    private Dialog d;


    //The user provided information about the event
    private int startHourChosen;
    private int startMinuteChosen;
    private int endHourChosen;
    private int endMinuteChosen;
    private int startDateDay;
    private int startDateMonth;
    private int startDateYear;
    private boolean choosingStartTime = false;
    private boolean allDay = false;
    private boolean dateChosen = false;
    private boolean timeChosen = false;
    private boolean timeEndChosen = false;

    //Needed final var for creating the event
    private Date startDateFull;
    private double evPrice;
    private String priceString;
    private Bitmap pictureTaken = null;
    private String title;
    private String description;
    private String evAddress;
    private LatLng evAddressLatLng;
    private String evAddressName;
    private int evAddressCitiesId;
    private int evLocationId;
    private int max_people;
    private int category;
    private String dateAndTimeStart;
    private String dateAndTimeFinish;
    private User user;

    private NumberPicker np;


    //Pour formater la date en Sam. 21 Jan 2015
    private SimpleDateFormat fDateEvent = new SimpleDateFormat("EEE d MMM yyyy");
    private SimpleDateFormat fHourEvent = new SimpleDateFormat("hh:mm");
    private SimpleDateFormat fDateOnly = new SimpleDateFormat("d/MM/yyyy");

    //Necessary vars
    private static final int ADD_EV_CHOOSE_TIME = 2010;
    private static final int ADD_EV_CHOOSE_END_TIME = 2017;
    private static final int ADD_EV_CHOOSE_DATE = 2011;
    private static final int ADD_EV_CHOOSE_POSITION = 2012;
    private static final int ADD_EV_CHOOSE_PRICE = 2016;
    private static final int PLACE_PICKER_REQUEST = 2013;
    private static final int ADD_EV_OPEN_CAMERA = 2014;
    private static final int ADD_EV_FROM_GALLERY = 2015;
    private static final int GET_SIGNIN_INFO = 8020;  // The request code

    /* Google API Vars for location autocomplete */
    protected GoogleApiClient mGoogleApiClient;
    private PlaceAutoCompleteAdapter mAdapter;
    private AutoCompleteTextView mAutocompleteView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        //Adding listeners to the TextViews
        tvTitle = (EditText) findViewById(R.id.addTitle);
        tvTitle.requestFocus();
        tvDescription = (EditText) findViewById(R.id.addDescription);

        tvStartTime = (TextView) findViewById(R.id.addStartTime);
        tvStartTime.setOnClickListener(this);

        tvEndTime = (TextView) findViewById(R.id.addEndTime);
        tvEndTime.setOnClickListener(this);

        swAllday = (Switch) findViewById(R.id.addAllDay);
        swAllday.setOnClickListener(this);

        tvStartDate = (TextView) findViewById(R.id.addStartDate);
        tvStartDate.setOnClickListener(this);

        tvPrice = (TextView) findViewById(R.id.addPrice);
        tvPrice.setOnClickListener(this);

        tvPosition = (TextView) findViewById(R.id.addPosition);
        tvPosition.setOnClickListener(this);

        ivAddCamera = (ImageView) findViewById(R.id.evAddFromCamera);
        ivAddCamera.setOnClickListener(this);

        ivAddGallery = (ImageView) findViewById(R.id.evAddFromLibrary);
        ivAddGallery.setOnClickListener(this);

        tvCreateEvent = (TextView) findViewById(R.id.addButton);
        tvCreateEvent.setOnClickListener(this);

        tvMaxPeople = (TextView) findViewById(R.id.add_ev_tvMaxPeople);
        tvMaxPeople.setOnClickListener(this);

        /* This handle the max people selection */
        tvEditMaxPeople = (EditText) findViewById(R.id.max_people);
        max_people = 0;
        tvEditMaxPeople.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){ /* when losing focus, check if inserted correct number > 0 */
                    if(!((EditText) v).getText().toString().isEmpty()) { /* Not null */
                        max_people = Integer.decode(((EditText) v).getText().toString());
                        if (max_people == 0) {
                            showUnlimitedPeople();
                        }
                    }else {
                        showUnlimitedPeople();
                    }
                }
            }
        });

        /* This handles the category selection */
        category = 9;
        spCategory = (Spinner) findViewById(R.id.spinner);
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "pos:" + position, Toast.LENGTH_LONG).show();
                if(position == 0)
                    category = 1;
                if(position == 1)
                    category = 2;
                if(position == 2)
                    category = 3;
                if(position == 3)
                    category = 4;
                if(position == 4)
                    category = 6;
                if(position == 5)
                    category = 7;
                if(position == 6)
                    category = 8;
                if(position == 7)
                    category = 9;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                category = 9;
            }
        });


        llAddPicture = (LinearLayout) findViewById(R.id.evAddPicture);

        //If we had already taken a picture, then we show it
        if(savedInstanceState != null){
            pictureTaken = (Bitmap) savedInstanceState.getParcelable("pictureTaken");
            if(pictureTaken != null)
                alternateTakeOrSeePicture("see");
            else
                alternateTakeOrSeePicture("take");
        }
        else{
            alternateTakeOrSeePicture("take");
        }

        user = NetworkSingleton.getInstance(this).getConnectedUSer();

        /* Don't show the keyboard at first */
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        /** Construct GoogleApiClient
         * All of the following lines are for the Google Maps Autocomplete API
          */
        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, 0, this)
                .addApi(Places.GEO_DATA_API).build();
        mAutocompleteView = (AutoCompleteTextView) findViewById(R.id.addPosition);
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickLister);
        /* Here we can change the third argument with some Bounds to precise the location */
        mAdapter = new PlaceAutoCompleteAdapter(this, mGoogleApiClient, null, null);
        mAutocompleteView.setAdapter(mAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_add_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    /**
     * This is for handling click events
     * @param v
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){
            case R.id.addStartTime:
                showMyDialog(ADD_EV_CHOOSE_TIME);
                break;
            case R.id.addEndTime:
                showMyDialog(ADD_EV_CHOOSE_END_TIME);
                break;
            case R.id.addAllDay:
                toggleAllDay();
                break;
            case R.id.addStartDate:
                showMyDialog(ADD_EV_CHOOSE_DATE);
                break;
            case R.id.addPrice:
                showMyDialog(ADD_EV_CHOOSE_PRICE);
                break;
            case R.id.evAddFromCamera:
                //The user wants to take a picture
                dispatchTakePictureIntent();
                break;
            case R.id.evAddFromLibrary:
                dispatchGetFromGalleryIntent();
                break;
            case R.id.evAddPicture:
                //We clicked on the picture, the user want to change the one he chose/took
                alternateTakeOrSeePicture("take");
                break;
            case R.id.add_ev_tvMaxPeople:
                    showLimitedPeople();
                break;
            case R.id.buttonSet:
                if((this.evPrice != 0)) {
                    tvPrice.setText(priceString);
                    tvPrice.setTypeface(tvPrice.getTypeface(), Typeface.BOLD);
                }
                else {
                    tvPrice.setText(getResources().getString(R.string.ev_detail_free));
                    tvPrice.setTypeface(tvPrice.getTypeface(), Typeface.BOLD);
                }
                d.dismiss();
                break;
            case R.id.buttonCancel:
                d.dismiss();
                evPrice = 0;
                break;
            case R.id.addButton: //We pushed the create
                verifyAndSendForm();
                break;
        }
    }

    /**
     * This method will checker wheter the form has been filled correctly, it returns true if so
     * @return: wether the form is complete
     */
    private void verifyAndSendForm() {
        //Verifying
        if (evAddressName != null) {
            String isLocationRightUrl = getResources().getString(R.string.isLocationRightURL);
            Map<String, String> params = new HashMap<String, String>();

            String fullAddress = mAutocompleteView.getText().toString();
            String[] splitAddress = fullAddress.split(",");
            String zipAddress = splitAddress[1];

            params.put("location", zipAddress.trim());
            Log.i("AEAzipAddress", zipAddress);
            /* position is in one of the authorized city */
            GsonRequest<TonightRequest> isLocationRight = new GsonRequest<TonightRequest>(
                    GsonRequest.Method.POST,
                    isLocationRightUrl,
                    TonightRequest.class,
                    params,
                    new Response.Listener<TonightRequest>() {
                        @Override
                        public void onResponse(TonightRequest response) {
                            boolean statusReturn = response.isStatusReturn();
                            //Log.i("statusReturn", "" + response.getStatusCode());
                            if (statusReturn) {
                                evAddressCitiesId = Integer.decode(response.getStatusMessage());
                                //Log.i("statusReturnCitiesID", "" + response.getStatusMessage());
                                sendInformation();
                            } else {
                                Log.i("VolleyRespErr", "errorlocation");
                                showMyDialog(TONIGHT_ADD_EVENT_ERROR_LOCATION);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("VolleyErrLis", "errornetwork");
                            showMyDialog(TONIGHT_ADD_EVENT_ERROR_NETWORK);
                        }
                    }, this);
            NetworkSingleton.getInstance(this).getRequestQueue().add(isLocationRight);
        }else {
            showMyDialog(TONIGHT_ADD_EVENT_FILL_ALL);
        }
    }


    /** This method will create the TonightEventLocation and the TonightEvent class and will upload
     * them to the server without verifying, it supposes the information has already been checked
     */
    public void sendInformation(){
        /* Creating the TonightEventLocation class */

        TonightEventLocation location = new TonightEventLocation(0,
                evAddressName,
                evAddressLatLng.latitude,
                evAddressLatLng.longitude,
                mAutocompleteView.getText().toString(),
                evAddressCitiesId);


        /* Preparing the Volley Request for TonightEventLocation */
        String uploadEventLocation = getResources().getString(R.string.uploadEventLocationURL);
        JSONObject eventLocationJSON = null;
        try {
            eventLocationJSON = new JSONObject(NetworkSingleton.getInstance(this).getGson()
                    .toJson(location));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("adding", eventLocationJSON.toString());


        /* position is in one of the authorized city */
        GsonRequest<TonightRequest> addLocation = new GsonRequest<TonightRequest>(
                GsonRequest.Method.POST,
                uploadEventLocation,
                TonightRequest.class,
                eventLocationJSON,
                new Response.Listener<TonightRequest>() {
                    @Override
                    public void onResponse(TonightRequest response) {
                        boolean statusReturn = response.isStatusReturn();
                        //Log.i("statusReturn", "" + response.getStatusCode());
                        if (statusReturn) {
                            evLocationId = Integer.decode(response.getStatusMessage());
                            /* The EventLocation is correct and we received validation from server */
                            /* We can now add the TonightEvent to the queue and wait for a response*/
                            sendAddEvent();
                            //Log.i("addedevlocat", "" + evLocationId);
                        } else {
                            Log.i("addevlocat", "errorlocation");
                            showMyDialog(TONIGHT_ADD_EVENT_ERROR_LOCATION);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("addevlocat", error.getMessage());
                        showMyDialog(TONIGHT_ADD_EVENT_ERROR_NETWORK);
                    }
                }, this);


        /* Adding the create EventLocation request queue */
        NetworkSingleton.getInstance(this).getRequestQueue().add(addLocation);
    }


    /** this method will get the id of the event location created, add it to the event
     * and then try to execute the volley request to add the event to the DB
     */
    public void sendAddEvent(){
        /* Getting the parameters to create the TonightEvent class */
        String start_date = startDateYear + "-" + startDateMonth + "-" + startDateDay + " " +
                startHourChosen + ":" + startMinuteChosen + ":00";
        String end_date = startDateYear + "-" + startDateMonth + "-" + startDateDay + " " +
                endHourChosen + ":" + endMinuteChosen + ":00";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        String today = dateFormat.format(cal.getTime()); //2014/08/06 16:00:22

        /* Creating the TonightEvent */
        TonightEvent event = new TonightEvent(0,
                tvTitle.getText().toString(),
                start_date,
                end_date,
                max_people,
                evPrice,
                "https://placeholdit.imgix.net/~text?txtsize=56&txt=600%C3%97300&w=600&h=300",
                tvDescription.getText().toString(),
                today,
                NetworkSingleton.getInstance(this).getConnectedUSer().getId(),
                category, /*Style: 1 = cultural, 2 = nightlife etc... */
                1, /* 1 = open, 2 = private, 3 = canceled etc.. */
                evLocationId);//The id returned from the previous request

        Log.i("EventObject", NetworkSingleton.getInstance(this).getGson().toJson(event).toString());

        /* Preparing the Volley Request for TonightEvent */
        String uploadEvent = getResources().getString(R.string.uploadEventURL);

        /* Creating the GSONified object to send */
        JSONObject eventJSON = null;
        try {
            eventJSON = new JSONObject(NetworkSingleton.getInstance(this).getGson().
                    toJson(event));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        /* This will create the event, we will have to add it to the queue */
         GsonRequest<TonightRequest> addEvent = new GsonRequest<TonightRequest>(
                GsonRequest.Method.POST,
                uploadEvent,
                TonightRequest.class,
                eventJSON,
                new Response.Listener<TonightRequest>() {
                    @Override
                    public void onResponse(TonightRequest response) {
                        boolean statusReturn = response.isStatusReturn();
                        //Log.i("statusReturn", "" + response.getStatusCode());
                        if (statusReturn) {
                            Toast.makeText(getApplicationContext(), NetworkSingleton.getInstance(
                                    getApplicationContext()).getGson().toJson(response).toString(),
                                    Toast.LENGTH_LONG).show();
                            Log.i("addEvent", NetworkSingleton.getInstance(
                                    getApplicationContext()).getGson().toJson(response).toString());
                        }
                        else{
                            Toast.makeText(getApplicationContext(), NetworkSingleton.getInstance(
                                    getApplicationContext()).getGson().toJson(response).toString(),
                                    Toast.LENGTH_LONG).show();
                            Log.i("addEvent", NetworkSingleton.getInstance(
                                    getApplicationContext()).getGson().toJson(response).toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("addEventError", error.getMessage());
                    }
                },
                this);

        /* Adding the request to the queue */
        NetworkSingleton.getInstance(getApplicationContext()).getRequestQueue().add(addEvent);
    }


    /* This method show the EditText for inserting number of people */
    public void showLimitedPeople(){
        this.tvMaxPeople.setVisibility(View.GONE);
        this.tvEditMaxPeople.setVisibility(View.VISIBLE);
    }

    /* This methods does the exact opposite of the previous */
    public void showUnlimitedPeople(){
        this.tvMaxPeople.setVisibility(View.VISIBLE);
        this.tvEditMaxPeople.setVisibility(View.GONE);
        max_people = 0;
    }
    /**
     * This method show a dialog for inserting the details
     * @param type
     */
    public void showMyDialog(int type){
        switch(type){
            case ADD_EV_CHOOSE_TIME:
                choosingStartTime = true;
                new TimePickerDialog(this, this, Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                        Calendar.getInstance().get(Calendar.MINUTE), true).show();
                break;
            case ADD_EV_CHOOSE_END_TIME:
                choosingStartTime = false;
                new TimePickerDialog(this, this, Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                        Calendar.getInstance().get(Calendar.MINUTE), true).show();

                break;
            case ADD_EV_CHOOSE_DATE:
                new DatePickerDialog(this, this, Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).show();
                break;
            case ADD_EV_CHOOSE_PRICE:
                //We show a dialog that allows user to choose the evPrice ranging from 0 to 100
                d = new Dialog(this);
                d.setTitle(getResources().getString(R.string.add_choose_price));
                d.setContentView(R.layout.add_event_price_dialog);
                Button b1 = (Button) d.findViewById(R.id.buttonSet);
                Button b2 = (Button) d.findViewById(R.id.buttonCancel);
                np = (NumberPicker) d.findViewById(R.id.npPrice);
                np.setMaxValue(100);
                np.setMinValue(0);
                np.setWrapSelectorWheel(false);
                np.setOnValueChangedListener(this);
                b1.setText(getResources().getString(R.string.add_choose_price_confirm));
                b2.setText(getResources().getString(R.string.add_choose_price_cancel));
                b1.setOnClickListener(this);
                b2.setOnClickListener(this);
                d.show();
                break;
            case TONIGHT_ADD_EVENT_ERROR_LOCATION:
                Toast.makeText(this, getString(R.string.add_event_error_location),
                        Toast.LENGTH_LONG).show();
                break;
            case TONIGHT_ADD_EVENT_ERROR_NETWORK:
                Toast.makeText(this, getString(R.string.add_event_error_network),
                        Toast.LENGTH_SHORT).show();
                break;
            case TONIGHT_ADD_EVENT_FILL_ALL:
                Toast.makeText(this, getString(R.string.add_event_error_fill_all),
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * This method launches an Intent to take a picture from the camera.
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, ADD_EV_OPEN_CAMERA);
        }


    }/**
     * This method launches an Intent to take a picture from the gallery.
     */
    private void dispatchGetFromGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Show only images, no videos or anything else
        intent.setType("image/*");
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.add_choose_pic))
                , ADD_EV_FROM_GALLERY);
    }

    /**
     * This method receives results from Intents launched
     * It will take the appropriate action depending on the requestCode from the Intent
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //We receive data from the GOOGLE MAPS API so we get back the position
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                /*Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                createdEvent = new EventLocation(place.getId(), place.getName().toString(),
                        place.getLatLng().latitude, place.getLatLng().longitude,
                        place.getAddress().toString());
                Log.i("AddEvent:", "Position choosed : " + createdEvent.toString());
                this.tvPosition.setTypeface(tvPosition.getTypeface(), Typeface.BOLD);
                this.tvPosition.setText(createdEvent.getAddress());*/
            }
        }

        //We receive data from the CAMERA so we get back a picture
        if (requestCode == ADD_EV_OPEN_CAMERA && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            //Converting bitmap to drawable
            pictureTaken = (Bitmap) extras.get("data");
            alternateTakeOrSeePicture("see");
        }

        //We receive data from the GALLERY APP so we get back a picture
        if (requestCode == ADD_EV_FROM_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();

            //Resizing the picture
            String[] projection = { MediaStore.MediaColumns.DATA };
            CursorLoader cursorLoader = new CursorLoader(this,selectedImageUri, projection, null, null,
                    null);
            Cursor cursor = cursorLoader.loadInBackground();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            String selectedImagePath = cursor.getString(column_index);
            Bitmap bm;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(selectedImagePath, options);
            final int REQUIRED_SIZE = 800;
            int scale = 1;
            while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                    && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;
            options.inSampleSize = scale;
            options.inJustDecodeBounds = false;
            bm = BitmapFactory.decodeFile(selectedImagePath, options);

            pictureTaken = bm;
            alternateTakeOrSeePicture("see");
            // Log.d(TAG, String.valueOf(bitmap));
        }
    }

    /**
     * This method does two things
     * see: removes the icons to take picture, and show the selected picture instead
     * take: removes the picture and allows user to take a new one.
     * @param action
     */
    private void alternateTakeOrSeePicture(String action) {
        //The user took a picture so we show it and remove the icons to take a picture
        if(action.equals("see")){
            //Remove the icon Take Picture and From Gallery
            ivAddCamera.setVisibility(View.GONE);
            ivAddGallery.setVisibility(View.GONE);
            //Set the background the picture taken
            llAddPicture.setBackground(new BitmapDrawable(getResources(), pictureTaken));
            llAddPicture.setOnClickListener(this);
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200,
                    getResources().getDisplayMetrics());
            llAddPicture.getLayoutParams().height = height;
        }
        if(action.equals("take")){
            //Remove the icon Take Picture and From Gallery
            ivAddCamera.setVisibility(View.VISIBLE);
            ivAddGallery.setVisibility(View.VISIBLE);
            //Set the background the picture taken
            llAddPicture.setBackground(ContextCompat.getDrawable(this,
                    R.drawable.activity_profile_background));
            llAddPicture.setOnClickListener(null);
            //We make the picture bigger
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150,
                    getResources().getDisplayMetrics());
            llAddPicture.getLayoutParams().height = height;
            //Remove the take picture
            pictureTaken = null;
        }
    }

    /**
     * This function disable the check if the all day switch is checked, in which case it disable
     * the chose time views, else it activates them
     */
    public void toggleAllDay(){
        if(swAllday.isChecked()) {
            this.tvStartTime.setEnabled(false);
            this.tvEndTime.setEnabled(false);
            this.tvStartTime.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryLightest));
            this.tvEndTime.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryLightest));
            ((ImageView) findViewById(R.id.evIcTime)).setColorFilter(ContextCompat.getColor(this,
                    R.color.colorPrimaryLightest));
            ((ImageView) findViewById(R.id.evIcTimeEnd)).setColorFilter(ContextCompat.getColor(this,
                    R.color.colorPrimaryLightest));
            allDay = true;
        } else {
            this.tvStartTime.setEnabled(true);
            this.tvEndTime.setEnabled(true);
            this.tvStartTime.setTextColor(ContextCompat.getColor(this, R.color.primary_text));
            this.tvEndTime.setTextColor(ContextCompat.getColor(this, R.color.primary_text));
            ((ImageView) findViewById(R.id.evIcTime)).setColorFilter(ContextCompat.getColor(this,
                    R.color.primary_text));
            ((ImageView) findViewById(R.id.evIcTimeEnd)).setColorFilter(ContextCompat.getColor(this,
                    R.color.primary_text));
            allDay = false;
        }
    }

    /**
     * We update the textview when the time is chosen
     * @param view
     * @param hourOfDay
     * @param minute
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if(choosingStartTime) {
            this.startHourChosen = hourOfDay;
            this.startMinuteChosen = minute;
            this.timeChosen = true;

            this.tvStartTime.setText(String.format("%02d:%02d", this.startHourChosen,
                    this.startMinuteChosen));
            this.tvStartTime.setTypeface(tvStartTime.getTypeface(), Typeface.BOLD);
        }
        else{
                this.endHourChosen = hourOfDay;
                this.endMinuteChosen = minute;
                this.timeEndChosen = true;

                this.tvEndTime.setText(String.format("%02d:%02d", this.endHourChosen,
                        this.endMinuteChosen));
                this.tvEndTime.setTypeface(tvEndTime.getTypeface(), Typeface.BOLD);
        }
    }

    /**
     * We update the textview when the user chooses the date
     * @param view
     * @param year
     * @param monthOfYear
     * @param dayOfMonth
     */
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        this.startDateDay = dayOfMonth;
        this.startDateMonth = monthOfYear + 1; //Because it returns month from 0 to 11
        this.startDateYear = year;
        this.dateChosen = true;
        Log.i("AddEvent", "Chosen date : " + this.startDateDay + "/" + this.startDateMonth
                + "/" + this.startDateYear);

        //Get the day from this date
        try {
            startDateFull = fDateOnly.parse(this.startDateDay + "/" + this.startDateMonth
                    + "/" + this.startDateYear);
        } catch (ParseException e) {
            Log.e("AddEvent", "Could not parse date");
            e.printStackTrace();
        }

        tvStartDate.setText(fDateEvent.format(startDateFull));
        tvStartDate.setTypeface(tvStartTime.getTypeface(), Typeface.BOLD);
    }

    /**
     * When the user makes changes to the NumberPicker for choosing the evPrice
     * We save the value entered and update the UI accordingly
     * @param picker
     * @param oldVal
     * @param newVal
     */
    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        this.evPrice = newVal;
        priceString = NumberFormat.getCurrencyInstance().format(evPrice);
    }


    /**
     * This allows to retain the picture take from the roll or camera even after restarting the
     * activity (on changing screen orientation for exmample)
     * @param savedInstanceState
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        //We save the picture we took if it's not empty
        if(pictureTaken != null) {
            savedInstanceState.putParcelable("pictureTaken", pictureTaken);
        }
    }


    /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data API
     * to retrieve more details about the place.
     *
     * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById(com.google.android.gms.common.api.GoogleApiClient,
     * String...)
     */
    private AdapterView.OnItemClickListener mAutocompleteClickLister = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
             /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            Log.i("AddEventActivity", "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            //Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    //Toast.LENGTH_SHORT).show();
            Log.i("AddEventActivity", "Called getPlaceById to get Place details for " + placeId);
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e("AddEventActivity", "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);

            // Format details of the place for display and show it in a TextView.
            tvPosition.setText(place.getAddress());
            evAddress = place.getAddress().toString();
            evAddressLatLng = place.getLatLng();
            evAddressName = place.getName().toString();
            Log.i("AddEventActivity", "Place details received: " + place.getName());

            places.release();
        }
    };


    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.e("AddEventActivity", res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));

    }


    /**
     * Called when the Activity could not connect to Google Play services and the auto manager
     * could resolve the error automatically.
     * In this case the API is not available and notify the user.
     *
     * @param connectionResult can be inspected to determine the cause of the failure
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("AddEventActivity", "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }
}
