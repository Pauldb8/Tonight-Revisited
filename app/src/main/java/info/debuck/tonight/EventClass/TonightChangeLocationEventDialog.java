package info.debuck.tonight.EventClass;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
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
import com.google.gson.Gson;

import info.debuck.tonight.EditEventActivity;
import info.debuck.tonight.ManageEventActivity;
import info.debuck.tonight.NetworkSingleton;
import info.debuck.tonight.R;
import info.debuck.tonight.Tools.GsonRequest;
import info.debuck.tonight.Tools.PlaceAutoCompleteAdapter;

/**
 * Created by onetec on 11-06-16.
 */
public class TonightChangeLocationEventDialog extends DialogFragment implements GoogleApiClient.OnConnectionFailedListener {

    /* View */
    private AutoCompleteTextView evLocation;

    /* Vars */
    private TonightEvent mEvent;
    private TonightEventLocation mEventLocation;
    private Gson mGson;
    private RequestQueue mRequestQueue;
    private String newTitle;

    /* Google API Vars for location autocomplete */
    protected GoogleApiClient mGoogleApiClient;
    private PlaceAutoCompleteAdapter mAdapter;
    private AutoCompleteTextView mAutocompleteView;
    private String newAdress;
    private String newAddressName;
    private LatLng newAddressLtdLgt;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        mGson = NetworkSingleton.getInstance(getActivity()).getGson();
        mRequestQueue = NetworkSingleton.getInstance(getActivity()).getRequestQueue();

        /* Getting the event in argument */
        mEvent = mGson.fromJson(
                (String)getArguments().get(ManageEventActivity.TONIGHT_INTENT_EXTRA_EVENT),
                TonightEvent.class);
        mEventLocation = mGson.fromJson((String)getArguments().get(ManageEventActivity.TONIGHT_INTENT_EXTRA_DESC_FK),
                TonightEventLocation.class);
        if(mEvent == null)
            this.dismiss();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.activity_event_change_location_dialog, null);

        /* Setting views */
        evLocation = (AutoCompleteTextView) view.findViewById(R.id.evLocation);
        evLocation.setText(mEventLocation.getAddress());

        /* Setting up the autocompletetextview */
        /** Construct GoogleApiClient
         * All of the following lines are for the Google Maps Autocomplete API
         */
        mGoogleApiClient = new GoogleApiClient.Builder(getContext()).enableAutoManage(getActivity(), 0, this)
                .addApi(Places.GEO_DATA_API).build();
        mAutocompleteView = (AutoCompleteTextView) view.findViewById(R.id.evLocation);
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickLister);
        /* Here we can change the third argument with some Bounds to precise the location */
        mAdapter = new PlaceAutoCompleteAdapter(getContext(), mGoogleApiClient, null, null);
        mAutocompleteView.setAdapter(mAdapter);

        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.dialog_change_event_button_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                /* Setting new status */
                                mEvent.setName(evLocation.getText().toString());
                                /* Uploading event */
                                updateEvent(mEvent, getActivity());
                            }
                        })
                .setNegativeButton(R.string.dialog_change_event_button_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                TonightChangeLocationEventDialog.this.getDialog().cancel();
                            }
                        });

        return builder.create();
    }


    /**
     * This method will send the Event object to the server in order to update it in the database
     * @param mEvent
     */
    private void updateEvent(TonightEvent mEvent, final Context mContext) {
        /* Preparing the Volley Request for TonightEvent */
        String uploadEventURL = mContext.getString(R.string.updateEventURL);
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
                        Log.i("statusReturn", "" + response.getStatusCode());
                        if (statusReturn) {
                            Toast.makeText(mContext,
                                    mContext.getString(R.string.tonight_event_change_confirmed),
                                    Toast.LENGTH_SHORT).show();
                                    /* Updating view in calling activity */
                            ((EditEventActivity) mContext).updateViews();
                        } else {
                            Toast.makeText(mContext,
                                    response.getStatusMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mContext,
                                mContext.getString(R.string.add_event_error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                getActivity());
        mRequestQueue.add(updatedEvent);
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
             read the place ID and evTitle.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            //Log.i("AddEventActivity", "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            //Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
            //Toast.LENGTH_SHORT).show();
            //Log.i("AddEventActivity", "Called getPlaceById to get Place details for " + placeId);
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
            evLocation.setText(place.getAddress());
            evLocation.setSelected(false);
            evLocation.clearFocus();
            newAdress = place.getAddress().toString();
            newAddressLtdLgt = place.getLatLng();
            newAddressName = place.getName().toString();
            Log.i("AddEventActivity", "Place details received: " + place.getName());

            places.release();
        }
    };


    /**
     * Called when the Activity could not connect to Google Play services and the auto manager
     * could resolve the error automatically.
     * In this case the API is not available and notify the mUser.
     *
     * @param connectionResult can be inspected to determine the cause of the failure
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("AddEventActivity", "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the mUser of error state and resolution.
        Toast.makeText(getActivity(),
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }
}
