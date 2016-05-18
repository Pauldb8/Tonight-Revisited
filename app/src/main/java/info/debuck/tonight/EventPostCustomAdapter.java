package info.debuck.tonight;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import info.debuck.tonight.EventClass.TonightEventPost;
import info.debuck.tonight.EventClass.User;
import info.debuck.tonight.Tools.GsonRequest;

/**
 * This is a multi use adapter, it will create an ArrayAdapter to use with a listView.
 * It will create separate view based on a parameter defining which events to query to the server.
 */
public class EventPostCustomAdapter extends ArrayAdapter<TonightEventPost>{

    private Context mContext;
    private ImageLoader mImageLoader;
    private RequestQueue mRequestQueue;
    private SimpleDateFormat fDateAndTimeEvent = new SimpleDateFormat("EEE d MMM yyyy à hh:mm");
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public EventPostCustomAdapter(Context context, ArrayList<TonightEventPost> events){
        super(context, 0, events);
        this.mContext = context;
        //this.mImageLoader = NetworkSingleton.getInstance(this.mContext).getImageLoader();
        this.mRequestQueue = NetworkSingleton.getInstance(this.mContext).getRequestQueue();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        TonightEventPost post = (TonightEventPost) getItem(position);

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(R.layout.event_post_list, null);
        }

        TextView postDateTime = (TextView) v.findViewById(R.id.event_post_datetime);
        TextView postMessage = (TextView) v.findViewById(R.id.event_post_message);
        final TextView postCreator = (TextView) v.findViewById(R.id.event_post_full_name);

        final String url = mContext.getString(R.string.get_user_url) + "?user_id=" + post.getCreator_id();
        /* Getting asynchronously user information */
        GsonRequest<User> getUserPost = new GsonRequest<User>(
                url,
                User.class,
                new Response.Listener<User>() {
                    @Override
                    public void onResponse(User response) {
                        //Log.i("EventPostCustomAdapter", "url: " + url);
                        postCreator.setText(response.getFullName());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }, mContext);
        mRequestQueue.add(getUserPost);

        /* Putting datetime info */
        try {
            postDateTime.setText(fDateAndTimeEvent.format(dateFormat.parse(post.getDate())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        postMessage.setText(post.getMessage());

        return v;
    }
}
