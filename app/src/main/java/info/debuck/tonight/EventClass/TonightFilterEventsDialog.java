package info.debuck.tonight.EventClass;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.google.gson.Gson;

import info.debuck.tonight.NetworkSingleton;
import info.debuck.tonight.R;

/**
 * Created by onetec on 11-06-16.
 */
public class TonightFilterEventsDialog extends DialogFragment implements AdapterView.OnItemSelectedListener, SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    public static final int TONIGHT_DEFAULT_PRICE = 100;
    public static final int TONIGHT_DEFAULT_DISTANCE = 30;
    public static final int TONIGHT_DEFAULT_CATEGORY = 0;

    /* View */
    private Spinner changeCategory;
    private TextView tvPrefPrice;
    private SeekBar sbPrefPrice;
    private TextView tvPrefDistance;
    private SeekBar sbPrefDistance;
    private Spinner sCategory;
    private TextView tvDefault;

    /* Vars */
    private TonightEvent mEvent;
    private Gson mGson;
    private RequestQueue mRequestQueue;
    private int newStatus;
    private int pref_distance;
    private int pref_price;
    private int pref_category;
    private int new_distance;
    private int new_price;
    private int new_category;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    int PRIVATE_MODE = 0;

    public static final String PREF_NAME =" info.debuck.tonigh.prefs";
    public static final String PREF_DISTANCE = "info.debuck.filter.distance";
    public static final String PREF_PRICE = "info.debuck.filter.price";
    public static final String PREF_CATEGORY = "info.debuck.filter.category";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        mGson = NetworkSingleton.getInstance(getActivity()).getGson();
        mRequestQueue = NetworkSingleton.getInstance(getActivity()).getRequestQueue();

        /* Getting user prefs */
        pref = getContext().getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        pref_distance = pref.getInt(PREF_DISTANCE, TONIGHT_DEFAULT_DISTANCE);
        pref_price = pref.getInt(PREF_PRICE, TONIGHT_DEFAULT_PRICE);
        pref_category = pref.getInt(PREF_CATEGORY, TONIGHT_DEFAULT_CATEGORY);
        new_distance = pref_distance;
        new_price = pref_price;
        new_category = pref_category;

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.activity_event_filter_dialog, null);

        /* Setting the views */
        tvPrefPrice = (TextView) view.findViewById(R.id.filter_chosen_price);
        sbPrefPrice = (SeekBar) view.findViewById(R.id.seekBarPrice);
        sbPrefPrice.setOnSeekBarChangeListener(this);
        tvPrefDistance = (TextView) view.findViewById(R.id.filter_chosen_distance);
        sbPrefDistance = (SeekBar) view.findViewById(R.id.seekBarDistance);
        sbPrefDistance.setOnSeekBarChangeListener(this);
        sCategory = (Spinner) view.findViewById(R.id.spinner);
        sCategory.setOnItemSelectedListener(this);
        tvDefault = (TextView) view.findViewById(R.id.button_default);
        tvDefault.setOnClickListener(this);

        /* Setting info views */
        updateViews(pref_price, pref_distance, pref_category);

        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.dialog_change_event_button_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                /* changing prefs */
                                editor.putInt(PREF_DISTANCE, new_distance);
                                editor.putInt(PREF_PRICE, new_price);
                                editor.putInt(PREF_CATEGORY, new_category);
                                editor.commit();
                                Toast.makeText(getContext(), getString(R.string.dialog_filter_updated),
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                .setNegativeButton(R.string.dialog_change_event_button_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                TonightFilterEventsDialog.this.getDialog().cancel();
                            }
                        });


        return builder.create();
    }


    /**
     * This method updates the views accordingly to the parameters
     * @param pref_price
     * @param pref_distance
     * @param pref_category
     */
    private void updateViews(int pref_price, int pref_distance, int pref_category) {
        tvPrefPrice.setText("" + pref_price);
        sbPrefPrice.setProgress(pref_price);
        tvPrefDistance.setText("" + pref_distance);
        sbPrefDistance.setProgress(pref_distance);
        setDefaultSelected(sCategory, pref_category);
    }


    /**
     * This method sets the default chosen category
     * @param changeCategory
     */
    private void setDefaultSelected(Spinner changeCategory, int status) {
        int position = 0;
        if(status == 0)
            position = 0;
        if(status == 1)
            position = 1;
        if(status == 2)
            position = 2;
        if(status == 3)
            position = 3;
        if(status == 4)
            position = 4;
        if(status == 6)
            position = 5;
        if(status == 7)
            position = 6;
        if(status == 8)
            position = 7;
        if(status == 9)
            position = 8;
        changeCategory.setSelection(position);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(position == 0)
            new_category = 0;
        if(position == 1)
            new_category = 1;
        if(position == 2)
            new_category = 2;
        if(position == 3)
            new_category = 3;
        if(position == 4)
            new_category = 4;
        if(position == 5)
            new_category = 6;
        if(position == 6)
            new_category = 7;
        if(position == 7)
            new_category = 8;
        if(position == 8)
            new_category = 9;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int id = seekBar.getId();
        switch(id){
            case R.id.seekBarPrice:
                tvPrefPrice.setText("" + progress);
                sbPrefPrice.setProgress(progress);
                new_price = progress;
                break;
            case R.id.seekBarDistance:
                tvPrefDistance.setText("" + progress);
                sbPrefDistance.setProgress(progress);
                new_distance = progress;
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    /**
     * This will handle clicks
     * @param v
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.button_default){
            updateViews(TONIGHT_DEFAULT_PRICE, TONIGHT_DEFAULT_DISTANCE, TONIGHT_DEFAULT_CATEGORY);
        }
    }
}
