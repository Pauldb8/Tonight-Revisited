package info.debuck.tonight;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import info.debuck.tonight.EventClass.TonightChangeCategoryEventDialog;
import info.debuck.tonight.EventClass.TonightChangeDescriptionEventDialog;
import info.debuck.tonight.EventClass.TonightChangeLocationEventDialog;
import info.debuck.tonight.EventClass.TonightChangeTitleEventDialog;
import info.debuck.tonight.EventClass.TonightEvent;
import info.debuck.tonight.EventClass.TonightEventLocation;
import info.debuck.tonight.EventClass.TonightRequest;
import info.debuck.tonight.Tools.GsonRequest;

public class EditEventActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, NumberPicker.OnValueChangeListener {


    /* Event vars */
    private TonightEvent mEvent;
    private TonightEventLocation mEventLocation;
    private double newPrice = 0;

    /* Views vars */
    private NetworkImageView evPicture;
    private TextView evTitle;
    private TextView evStartDate;
    private TextView evStartTime;
    private TextView evEndTime;
    private TextView evPrice;
    private TextView evLocation;
    private TextView evCategory;
    private TextView evMaxPeople;
    private TextView evDescription;
    private LinearLayout lineTitle;
    private LinearLayout firstRow;
    private LinearLayout secondRow;
    private LinearLayout thirdRow;
    private LinearLayout fourthRow;
    private LinearLayout fifthRow;
    private LinearLayout sixthRow;
    private LinearLayout eightRow;
    private LinearLayout ninthRow;
    private Dialog d;


    private Gson mGson;
    private ImageLoader mImageLoader;
    private RequestQueue mRequestQueue;
    private SimpleDateFormat year = new SimpleDateFormat("yyyy");
    private SimpleDateFormat month = new SimpleDateFormat("MM");
    private SimpleDateFormat day = new SimpleDateFormat("d");
    private SimpleDateFormat hour = new SimpleDateFormat("HH");
    private SimpleDateFormat minutes = new SimpleDateFormat("mm");
    private DateFormat dateSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        if(getIntent().hasExtra(MainActivity.TONIGHT_INTENT_EXTRA_DESC)
                && getIntent().hasExtra(ManageEventActivity.TONIGHT_INTENT_EXTRA_DESC_FK)){
            mGson = NetworkSingleton.getInstance(this).getGson();
            /* deserialize objects */
            mEvent = mGson.fromJson(
                    getIntent().getStringExtra(MainActivity.TONIGHT_INTENT_EXTRA_DESC),
                    TonightEvent.class);
            mEventLocation = mGson.fromJson(getIntent().getStringExtra(ManageEventActivity.TONIGHT_INTENT_EXTRA_DESC_FK),
                    TonightEventLocation.class);
            mImageLoader = NetworkSingleton.getInstance(this).getImageLoader();
            mRequestQueue = NetworkSingleton.getInstance(this).getRequestQueue();
            newPrice = mEvent.getPrice();

            fillDetails();
        }
    }

    /* This method will set the details in their position */
    private void fillDetails() {
        /* Assigning view to theirs vars */
        evPicture = (NetworkImageView) findViewById(R.id.evDescPicture);
        evTitle = (TextView) findViewById(R.id.evTitle);
        evStartDate = (TextView) findViewById(R.id.evStartDate);
        evStartTime = (TextView) findViewById(R.id.evStartTime);
        evEndTime = (TextView) findViewById(R.id.evEndTime);
        evPrice = (TextView) findViewById(R.id.evPrice);
        evCategory = (TextView) findViewById(R.id.evLabel);
        evLocation = (TextView) findViewById(R.id.evLocalisation);
        evDescription = (TextView) findViewById(R.id.evDescription);
        evMaxPeople = (TextView) findViewById(R.id.evParticipants);

        lineTitle = (LinearLayout) findViewById(R.id.lineTitle);
        firstRow = (LinearLayout) findViewById(R.id.firstRow);
        secondRow = (LinearLayout) findViewById(R.id.secondRow);
        thirdRow = (LinearLayout) findViewById(R.id.thirdRow);
        fourthRow = (LinearLayout) findViewById(R.id.fourthRow);
        fifthRow = (LinearLayout) findViewById(R.id.fifthRow);
        sixthRow = (LinearLayout) findViewById(R.id.sixthRow);
        eightRow = (LinearLayout) findViewById(R.id.eightRow);
        ninthRow = (LinearLayout) findViewById(R.id.ninthRow);

        /* Setting the text in the views */
        evPicture.setImageUrl(mEvent.getPicture_url(), mImageLoader);
        evTitle.setText(mEvent.getName());
        evStartDate.setText(mEvent.getStartDateFormatted());
        evStartTime.setText(mEvent.getStartHour());
        evEndTime.setText(mEvent.getEndHour());
        evPrice.setText(mEvent.getPriceFormatted());
        evCategory.setText(NetworkSingleton.getInstance(getApplicationContext())
                .getEventCategory().getCategoryName(mEvent.getEvent_type_id()));
        evLocation.setText(mEventLocation.getAddress());
        evDescription.setText(mEvent.getDescriptionFormatted());
        evMaxPeople.setText(String.valueOf(mEvent.getMax_people()));

        /* adding listeners */
        evPicture.setOnClickListener(this);
        lineTitle.setOnClickListener(this);
        firstRow.setOnClickListener(this);
        secondRow.setOnClickListener(this);
        //thirdRow.setOnClickListener(this);
        fourthRow.setOnClickListener(this);
        fifthRow.setOnClickListener(this);
        sixthRow.setOnClickListener(this);
        eightRow.setOnClickListener(this);
        ninthRow.setOnClickListener(this);
    }

    /* This get the category id and returns its place in the arraylist with the names */
    private int getCategory(int event_type_id) {
        if(event_type_id == 1)
            return 0;
        if(event_type_id == 2)
            return 1;
        if(event_type_id == 3)
            return 2;
        if(event_type_id == 4)
            return 3;
        if(event_type_id == 6)
            return 4;
        if(event_type_id == 7)
            return 5;
        if(event_type_id == 8)
            return 6;
        if(event_type_id == 9)
            return 7;
        return event_type_id;
    }


    /**
     * This function handles click event
     * @param v
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch(id){
            case R.id.evDescPicture:

                break;
            case R.id.lineTitle:
                TonightChangeTitleEventDialog dialog = new TonightChangeTitleEventDialog();
                Bundle args = new Bundle();
                args.putString(ManageEventActivity.TONIGHT_INTENT_EXTRA_EVENT, mGson.toJson(mEvent));
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "TonightChangeTitleEventDialog");
                break;
            case R.id.firstRow:
                /* We launch the DatePickerDialog */
                new DatePickerDialog(this, this, Integer.valueOf(year.format(mEvent.getStart_date())),
                        (Integer.valueOf(month.format(mEvent.getStart_date())))-1,
                        Integer.valueOf(day.format(mEvent.getStart_date()))).show();
                break;
            case R.id.secondRow:
                /* We change the time */
                new TimePickerDialog(this, this, Integer.valueOf(hour.format(mEvent.getStart_date())),
                        Integer.valueOf(minutes.format(mEvent.getStart_date())), true).show();
                break;
            case R.id.thirdRow:

                break;
            case R.id.fourthRow:
                showPriceDialog();
                break;
            case R.id.fifthRow:
                TonightChangeCategoryEventDialog chageCategoryDialog = new TonightChangeCategoryEventDialog();
                Bundle args2 = new Bundle();
                args2.putString(ManageEventActivity.TONIGHT_INTENT_EXTRA_EVENT, mGson.toJson(mEvent));
                chageCategoryDialog.setArguments(args2);
                chageCategoryDialog.show(getSupportFragmentManager(), "TonightChangeCategoryEventDialog");
                break;
            case R.id.sixthRow:
                showLocationDialog();
                break;
            case R.id.eightRow:

                break;
            case R.id.ninthRow:
                TonightChangeDescriptionEventDialog descDialog = new TonightChangeDescriptionEventDialog();
                Bundle args3 = new Bundle();
                args3.putString(ManageEventActivity.TONIGHT_INTENT_EXTRA_EVENT, mGson.toJson(mEvent));
                descDialog.setArguments(args3);
                descDialog.show(getSupportFragmentManager(), "TonightChangeTitleEventDialog");
                break;
            /* for the price changing button */
            case R.id.buttonCancel:
                d.dismiss();
                break;
            case R.id.buttonSet:
                mEvent.setPrice(newPrice);
                d.dismiss();
                updateEvent(mEvent);
                break;
        }

    }

    /**
     * This will send the event and eventlocation to the change location dialog
     */
    private void showLocationDialog() {
        TonightChangeLocationEventDialog dialog = new TonightChangeLocationEventDialog();
        Bundle args = new Bundle();
        args.putString(ManageEventActivity.TONIGHT_INTENT_EXTRA_EVENT, mGson.toJson(mEvent));
        args.putString(ManageEventActivity.TONIGHT_INTENT_EXTRA_DESC_FK, mGson.toJson(mEventLocation));
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "TonightChangeTitleEventDialog");
    }


    /**
     * This method will show the price dialog
     */
    private void showPriceDialog() {
        //We show a dialog that allows mUser to choose the evPrice ranging from 0 to 100
        d = new Dialog(this);
        d.setTitle(getResources().getString(R.string.add_choose_price));
        d.setContentView(R.layout.add_event_price_dialog);
        Button b1 = (Button) d.findViewById(R.id.buttonSet);
        Button b2 = (Button) d.findViewById(R.id.buttonCancel);
        NumberPicker np = (NumberPicker) d.findViewById(R.id.npPrice);
        np.setMaxValue(100);
        np.setMinValue(0);
        np.setValue((int)mEvent.getPrice());
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(this);
        b1.setText(getResources().getString(R.string.dialog_change_event_button_confirm));
        b2.setText(getResources().getString(R.string.dialog_change_event_button_cancel));
        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
        d.show();
    }


    /**
     * This method handles the change in the date of the event
     * It also checks that it doesn't take place in the past
     * @param view
     * @param year
     * @param monthOfYear
     * @param dayOfMonth
     */
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar today = new GregorianCalendar();
        int thisyear = today.get(Calendar.YEAR);  // 2012
        int thismonth = today.get(Calendar.MONTH);  // 9 - October!!!
        int thisday = today.get(Calendar.DAY_OF_MONTH);

        /* For comparison we do not use the hour */
        Calendar newDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);

        if(today.before(newDate)){ /* The date is today or in the future */
            /* When saving we get the hour */
            newDate = new GregorianCalendar(year, monthOfYear, dayOfMonth,
                    Integer.valueOf(hour.format(mEvent.getStart_date())),
                    Integer.valueOf(minutes.format(mEvent.getStart_date())));
            mEvent.setStart_date(dateSql.format(newDate.getTime()));
            updateEvent(mEvent);
        }else { /* La date se trouve dans le passé */
            Toast.makeText(this, getString(R.string.edit_event_error_date_update_too_soon), Toast.LENGTH_LONG).show();
        }
    }


    /**
     * This method will handle when the user change the time, there is no restriction about the time
     * @param view
     * @param hourOfDay
     * @param minute
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar newDate = new GregorianCalendar(Integer.valueOf(year.format(mEvent.getStart_date())),
                (Integer.valueOf(month.format(mEvent.getStart_date())))-1,
                Integer.valueOf(day.format(mEvent.getStart_date())),
                hourOfDay,
                minute,
                0);
        /* Updating the event with the new time but same date */
        mEvent.setStart_date(dateSql.format(newDate.getTime()));
        /* Send to the server */
        updateEvent(mEvent);
    }


    /**
     * This method handles the changing price
     * @param picker
     * @param oldVal
     * @param newVal
     */
    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        newPrice = newVal;
    }


    /**
     * This method will send the Event object to the server in order to update it in the database
     * @param mEvent
     */
    private void updateEvent(TonightEvent mEvent) {
        /* Preparing the Volley Request for TonightEvent */
        String uploadEventURL = getString(R.string.updateEventURL);
        //Log.i("ok", mGson.toJson(mEvent));
        GsonRequest<TonightRequest> updatedEvent = new GsonRequest<TonightRequest>(
                GsonRequest.Method.POST,
                uploadEventURL,
                TonightRequest.class,
                mEvent,
                new Response.Listener<TonightRequest>() {
                    @Override
                    public void onResponse(TonightRequest response) {
                        boolean statusReturn = response.isStatusReturn();
                        //Log.i("statusReturn", "" + response.getStatusCode());
                        if (statusReturn) {
                            Toast.makeText(getApplicationContext(),
                                    getApplicationContext().getString(R.string.tonight_event_change_confirmed),
                                    Toast.LENGTH_SHORT).show();
                            /* Updating view in calling activity */
                            updateViews();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    response.getStatusMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),
                                getApplicationContext().getString(R.string.add_event_error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                this);
        mRequestQueue.add(updatedEvent);
    }


    /**
     * This method will get called by the child dialog once an updated has taken place
     */
    public void updateViews() {
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
                        evPicture.setImageUrl(mEvent.getPicture_url(), mImageLoader);
                        evTitle.setText(mEvent.getName());
                        evStartDate.setText(mEvent.getStartDateFormatted());
                        evStartTime.setText(mEvent.getStartHour());
                        evEndTime.setText(mEvent.getEndHour());
                        evPrice.setText(mEvent.getPriceFormatted());
                        evCategory.setText(NetworkSingleton.getInstance(getApplicationContext())
                                .getEventCategory().getCategoryName(mEvent.getEvent_type_id()));
                        evDescription.setText(mEvent.getDescriptionFormatted());
                        evMaxPeople.setText(String.valueOf(mEvent.getMax_people()));
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
