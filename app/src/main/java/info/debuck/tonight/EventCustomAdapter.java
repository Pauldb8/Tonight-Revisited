package info.debuck.tonight;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import info.debuck.tonight.EventClass.TonightEvent;

/**
 * This is a multi use adapter, it will create an ArrayAdapter to use with a listView.
 * It will create separate view based on a parameter defining which events to query to the server.
 */
public class EventCustomAdapter extends ArrayAdapter<TonightEvent> implements Filterable {

    private Context mContext;
    private ImageLoader mImageLoader;
    private SimpleDateFormat fDateDay = new SimpleDateFormat("d");
    private SimpleDateFormat fDateMonth= new SimpleDateFormat("MMM");
    private ArrayList<TonightEvent> mOrigData;
    private ArrayList<TonightEvent> mEvents;

    public EventCustomAdapter(Context context, ArrayList<TonightEvent> events){
        super(context, 0, events);
        this.mContext = context;
        this.mImageLoader = NetworkSingleton.getInstance(this.mContext).getImageLoader();
        this.mEvents = events;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        TonightEvent event = getItem(position);

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(R.layout.event_list, null);
        }

        // Lookup view for data population
        TextView evName = (TextView) v.findViewById(R.id.eventName);
        TextView evPrice = (TextView) v.findViewById(R.id.eventPrice);
        TextView evDay = (TextView) v.findViewById(R.id.eventDay);
        TextView evMonth = (TextView) v.findViewById(R.id.eventMonth);
        TextView evDescription = (TextView) v.findViewById(R.id.eventDescription);
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
        evDay.setText(fDateDay.format(event.getStart_date()));
        evMonth.setText(fDateMonth.format(event.getStart_date()).toUpperCase());
        evDescription.setText(Html.fromHtml(event.getDescription()));
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


    /**
     * This method filter the events based on their name
     * @return a list of the events title containing the param
     */
    @Override
    public Filter getFilter(){
        return new Filter(){
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                constraint = constraint.toString().toLowerCase();
                Log.i("ECA", "Searching for: " + constraint);
                FilterResults results = new FilterResults();

                if(mOrigData == null)
                    mOrigData = mEvents;

                if(constraint != null && constraint.toString().length() > 0){

                    ArrayList<TonightEvent> found = new ArrayList<>();
                    for(TonightEvent item : mOrigData){
                        Log.i("ECA", "   comparing " + constraint + " with " + item.getName().toLowerCase());
                        if(item.getName().toLowerCase().contains(constraint)){
                            found.add(item);
                        }
                    }

                    results.values = found;
                    results.count = found.size();
                }else {
                    results.values = mOrigData;
                    results.count = mOrigData.size();
                }

                return  results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                /*clear();
                for(TonightEvent item: (ArrayList<TonightEvent>) results.values){
                    add(item);
                }*/

                mEvents = (ArrayList<TonightEvent>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mEvents.size();
    }

    @Override
    public TonightEvent getItem(int position) {
        return mEvents.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
