package info.debuck.tonight;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.google.gson.Gson;

import java.util.ArrayList;

import info.debuck.tonight.EventClass.User;
import info.debuck.tonight.EventClass.UserAvatar;

/**
 * Created by onetec on 26-05-16.
 */
public class ListUsersAdapter extends ArrayAdapter<User> {
    private Context mContext;
    private ArrayList<User> listUsers;
    private ImageLoader mImageLoader;
    private Gson mGson;
    private User mUser;

    public ListUsersAdapter(Context ctx, ArrayList<User> listUsers) {
        super(ctx, 0, listUsers);
        this.mContext = ctx;
        this.listUsers = listUsers;
        this.mImageLoader = NetworkSingleton.getInstance(ctx).getImageLoader();
        this.mGson = NetworkSingleton.getInstance(ctx).getGson();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mUser = (User) getItem(position);

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(R.layout.user_list, null);
        }

        UserAvatar avatar = (UserAvatar) v.findViewById(R.id.user_list_avatar);
        TextView name = (TextView) v.findViewById(R.id.user_list_name);

        avatar.setImageUrl(mUser.getPicture_url(), mImageLoader);
        name.setText(mUser.getFullName());

        /* User friend status is pending, we show it as is */
        if(mUser.getFriend_status() == User.FRIENDSHIP_STATUS_PENDING){
            ((LinearLayout) v.findViewById(R.id.mainLine)).setBackgroundColor(
                    mContext.getResources().getColor(R.color.colorAccentLight));
            ((TextView) v.findViewById(R.id.user_pending_friend_request)).setVisibility(View.VISIBLE);
        }

        return v;
    }

}