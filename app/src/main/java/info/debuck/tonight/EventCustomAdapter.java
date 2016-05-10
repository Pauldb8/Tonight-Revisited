package info.debuck.tonight;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import info.debuck.tonight.EventClass.TonightEvent;

/**
 * This is a multi use adapter, it will create an ArrayAdapter to use with a listView.
 * It will create separate view based on a parameter defining which events to query to the server.
 */
public class EventCustomAdapter extends ArrayAdapter<TonightEvent>{

    private Context mContext;
    private ImageLoader mImageLoader;

    public EventCustomAdapter(Context context, ArrayList<TonightEvent> events){
        super(context, 0, events);
        this.mContext = context;
        this.mImageLoader = NetworkSingleton.getInstance(this.mContext).getImageLoader();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        TonightEvent event = (TonightEvent) getItem(position);

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(R.layout.event_list, null);
        }

        // Lookup view for data population
        TextView evName = (TextView) v.findViewById(R.id.eventName);
        TextView evPrice = (TextView) v.findViewById(R.id.eventPrice);
        ImageView evIcCategory = (ImageView) v.findViewById(R.id.evIcCategory);
        //TextView evHour = (TextView) v.findViewById(R.id.eventHour);
        //Background picture for the current line
        NetworkImageView evPicture = (NetworkImageView) v.findViewById(R.id.eventPicture);

        // Populate the data into the template view using the data object
        evName.setText(event.getName());
        //evName.setShadowLayer((float) 20, 0, 0,   mContext.getResources().getColor(R.color.primary_text));
        //evDate.setText(event.getStartDateFormatted());
        //evMaxPeople.setText(String.valueOf(event.getMax_people()));
        evPicture.setImageUrl(event.getPicture_url(), mImageLoader);
        evPrice.setText(event.getPriceFormatted());
        //evHour.setText(event.getStartHour());

        /* Adapting icons depending on category */
        int category = event.getEvent_type_id();

        if(category == 1)
            evIcCategory.setImageResource(R.drawable.ic_nature_people);
        else if(category == 2)
            evIcCategory.setImageResource(R.drawable.ic_music_note);
        else if(category == 3)
            evIcCategory.setImageResource(R.drawable.ic_local_florist);
        else if(category == 4)
            evIcCategory.setImageResource(R.drawable.ic_local_bar);
        else if(category == 6)
            evIcCategory.setImageResource(R.drawable.ic_restaurant);
        else if(category == 7)
            evIcCategory.setImageResource(R.drawable.ic_local_activity);
        else if(category == 8)
            evIcCategory.setImageResource(R.drawable.ic_business_center);
        else if(category == 9)
            evIcCategory.setImageResource(R.drawable.ic_label);
        else
            evIcCategory.setImageResource(R.drawable.ic_label);




        return v;
    }
}
