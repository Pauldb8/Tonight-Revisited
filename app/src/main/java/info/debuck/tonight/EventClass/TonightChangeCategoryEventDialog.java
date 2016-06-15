package info.debuck.tonight.EventClass;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import info.debuck.tonight.EditEventActivity;
import info.debuck.tonight.ManageEventActivity;
import info.debuck.tonight.NetworkSingleton;
import info.debuck.tonight.R;
import info.debuck.tonight.Tools.GsonRequest;

/**
 * Created by onetec on 11-06-16.
 */
public class TonightChangeCategoryEventDialog extends DialogFragment implements AdapterView.OnItemSelectedListener {

    /* View */
    private Spinner changeCategory;
    private TextView infoOnChangeStatus;

    /* Vars */
    private TonightEvent mEvent;
    private Gson mGson;
    private RequestQueue mRequestQueue;
    private int newStatus;

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
        if(mEvent == null)
            this.dismiss();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.activity_event_change_category_dialog, null);
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.dialog_change_event_button_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                /* Setting new status */
                                mEvent.setEvent_type_id(newStatus);
                                /* Uploading event */
                                updateEvent(mEvent, getActivity());
                            }
                        })
                .setNegativeButton(R.string.dialog_change_event_button_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                TonightChangeCategoryEventDialog.this.getDialog().cancel();
                            }
                        });

        /* Setting views */
        changeCategory = (Spinner) view.findViewById(R.id.spinner);
        changeCategory.setOnItemSelectedListener(this);
        setDefaultSelected(changeCategory);

        return builder.create();
    }


    /**
     * This method sets the default chosen category
     * @param changeCategory
     */
    private void setDefaultSelected(Spinner changeCategory) {
        int status = mEvent.getEvent_type_id();
        int position = 0;
        if(status == 1)
            position = 0;
        if(status == 2)
            position = 1;
        if(status == 3)
            position = 2;
        if(status == 4)
            position = 3;
        if(status == 6)
            position = 4;
        if(status == 7)
            position = 5;
        if(status == 8)
            position = 6;
        if(status == 9)
            position = 7;
        changeCategory.setSelection(position);
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
                                    Toast.LENGTH_LONG).show();
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(position == 0)
            newStatus = 1;
        if(position == 1)
            newStatus = 2;
        if(position == 2)
            newStatus = 3;
        if(position == 3)
            newStatus = 4;
        if(position == 4)
            newStatus = 6;
        if(position == 5)
            newStatus = 7;
        if(position == 6)
            newStatus = 8;
        if(position == 7)
            newStatus = 9;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
