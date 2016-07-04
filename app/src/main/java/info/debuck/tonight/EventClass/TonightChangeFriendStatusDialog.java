package info.debuck.tonight.EventClass;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.gson.Gson;

import info.debuck.tonight.NetworkSingleton;
import info.debuck.tonight.ProfileAndFriendsActivity;
import info.debuck.tonight.R;
import info.debuck.tonight.Tools.GsonRequest;

/**
 * Created by onetec on 11-06-16.
 */
public class TonightChangeFriendStatusDialog extends DialogFragment implements View.OnClickListener {

    /* View */
    private TextView userName;
    private UserAvatar userAvatar;
    private LinearLayout acceptRequest;
    private LinearLayout refuseRequest;

    /* Vars */
    private User mUser;
    private Gson mGson;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private String newTitle;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                android.R.style.Theme_Translucent_NoTitleBar);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        mGson = NetworkSingleton.getInstance(getActivity()).getGson();
        mRequestQueue = NetworkSingleton.getInstance(getActivity()).getRequestQueue();
        mImageLoader = NetworkSingleton.getInstance(getActivity()).getImageLoader();

        /* Getting the event in argument */
        mUser = mGson.fromJson(
                (String)getArguments().get(ProfileAndFriendsActivity.TONIGHT_INTENT_EXTRA_USER),
                User.class);
        if(mUser == null)
            this.dismiss();

        // Inflate and set the layout for the dialog
        View view;
        if(mUser.getFriend_status() == 1) {
            view = inflater.inflate(R.layout.activity_event_change_my_friend_status_dialog, null);
            refuseRequest = (LinearLayout) view.findViewById(R.id.user_friend_refuse);
            refuseRequest.setOnClickListener(this);
        }else{
            view = inflater.inflate(R.layout.activity_event_change_friend_status_dialog, null);
            acceptRequest = (LinearLayout) view.findViewById(R.id.user_friend_accept);
            refuseRequest = (LinearLayout) view.findViewById(R.id.user_friend_refuse);
            refuseRequest.setOnClickListener(this);
            acceptRequest.setOnClickListener(this);
        }

        /* Setting views */
        userName = (TextView) view.findViewById(R.id.user_list_name);
        userAvatar = (UserAvatar) view.findViewById(R.id.user_list_avatar);
        userName.setText(mUser.getFullName());
        userAvatar.setImageUrl(mUser.getPicture_url(), mImageLoader);

        builder.setView(view);

        Dialog returned = builder.create();
        returned.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        returned.getWindow().setGravity(Gravity.BOTTOM);
        returned.setTitle(null);

        return returned;
    }

    /**
     * This method will handle clicks
     * @param v
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){
            case R.id.user_friend_accept:
                    changeStatusFriend(
                            NetworkSingleton.getInstance(getContext()).getConnectedUSer().getId(),
                            mUser.getId(),
                            1); /* changeStatusFriend(me, friend, 1); 1 = friend, 0 = request, 3 = blocked */

                break;

            case R.id.user_friend_refuse:
                changeStatusFriend(
                        NetworkSingleton.getInstance(getContext()).getConnectedUSer().getId(),
                        mUser.getId(),
                        3);
                break;
        }
    }


    /**
     * This method will update the friendship status to @friendStatus
     * @param me
     * @param friend
     * @param friendshipStatus
     */
    private void changeStatusFriend(int me, int friend, final int friendshipStatus) {
        String url = getActivity().getString(R.string.url_change_friendship);
        url = url + "?user_id=" + me;
        url = url + "&friend_id=" + friend;
        url = url + "&status_id=" + friendshipStatus;

        GsonRequest<TonightRequest> changeFriendship = new GsonRequest<TonightRequest>(url,
                TonightRequest.class,
                new Response.Listener<TonightRequest>() {
                    @Override
                    public void onResponse(TonightRequest response) {
                        if (response.isStatusReturn()) {
                            if (friendshipStatus == 1) {/* Friend request */
                                Toast.makeText(getContext(), getString(R.string.dialog_change_friend_accepted),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), getString(R.string.dialog_friend_refused),
                                        Toast.LENGTH_LONG).show();
                            }

                        } else {
                            Toast.makeText(getActivity(), getString(R.string.add_event_error_network),
                                    Toast.LENGTH_LONG).show();
                        }
                        TonightChangeFriendStatusDialog.this.getDialog().cancel();
                        ((ProfileAndFriendsActivity) getActivity()).updateViews();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        TonightChangeFriendStatusDialog.this.getDialog().cancel();
                    }
                },
        getActivity());

        mRequestQueue.add(changeFriendship);
    }


}
