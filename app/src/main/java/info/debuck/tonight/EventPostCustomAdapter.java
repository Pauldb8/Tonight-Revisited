package info.debuck.tonight;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import info.debuck.tonight.EventClass.TonightEventPost;
import info.debuck.tonight.EventClass.User;
import info.debuck.tonight.EventClass.UserAvatar;
import info.debuck.tonight.Tools.GsonRequest;

/**
 * This is a multi use adapter, it will create an ArrayAdapter to use with a listView.
 * It will create separate view based on a parameter defining which events to query to the server.
 */
public class EventPostCustomAdapter extends ArrayAdapter<TonightEventPost>{

    private Context mContext;
    private ImageLoader mImageLoader;
    private RequestQueue mRequestQueue;
    private SimpleDateFormat fDateAndTimeEvent = new SimpleDateFormat("EEE d MMM yyyy à HH:mm", Locale.FRENCH);
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRENCH);
    private UserAvatar userAvatar;
    private ArrayList<User> mUserList;


    public EventPostCustomAdapter(Context context, ArrayList<TonightEventPost> events){
        super(context, 0, events);
        this.mContext = context;
        this.mImageLoader = NetworkSingleton.getInstance(this.mContext).getImageLoader();
        this.mRequestQueue = NetworkSingleton.getInstance(this.mContext).getRequestQueue();
        this.mUserList = new ArrayList<>();
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent){
        TonightEventPost post = getItem(position);

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(R.layout.event_post_list, null);
        }

        TextView postDateTime = (TextView) v.findViewById(R.id.event_post_datetime);
        TextView postMessage = (TextView) v.findViewById(R.id.event_post_message);

        /* This will create a unique view */
        ViewHolder holder = (ViewHolder) v.getTag(R.id.viewHolder);
        if(holder == null){
            holder = new ViewHolder(v);
            v.setTag(R.id.viewHolder, holder);
        }

        /* Setting it to null first so it is in the right order, then we will assign it the user */
        mUserList.add(null);

        final String url = mContext.getString(R.string.get_user_url) + "?user_id=" + post.getCreator_id();
        /* Getting asynchronously user information */
        final ViewHolder finalHolder = holder;
        GsonRequest<User> getUserPost = new GsonRequest<>(
                url,
                User.class,
                new Response.Listener<User>() {
                    @Override
                    public void onResponse(User response) {
                        //Log.i("EventPostCustomAdapter", "url: " + url);
                        mUserList.set(position, response);
                        finalHolder.creator.setText(response.getFullName());
                        finalHolder.image.setImageUrl(response.getPicture_url(), mImageLoader);

                        /* Adding listener on click for the name and avatar */
                        finalHolder.creator.setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ((ListView) parent).performItemClick(v, position, 0);
                                    }
                                }
                        );
                        finalHolder.image.setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ((ListView) parent).performItemClick(v, position, 0);
                                    }
                                }
                        );

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

    public User getmUser(int position) {
        return mUserList.get(position);
    }


    /**
     * This class allow each view to be differentiated
     */
    private class ViewHolder {
        UserAvatar image;
        TextView creator;

        public ViewHolder(View v){
            image = (UserAvatar) v.findViewById(R.id.uaProfilePicture);
            creator = (TextView) v.findViewById(R.id.event_post_full_name);

            v.setTag(this);
        }
    }
}
