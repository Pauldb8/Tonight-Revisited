package info.debuck.tonight.EventClass;

/**
 * Created by onetec on 14-05-16.
 */

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import info.debuck.tonight.R;

/**
 * Created by onetec on 13-05-16.
 */
public class UserProfile {
    private Context mContext;

    private Map<String, String> profileName = new HashMap<String, String>();

    public UserProfile(Context ctx){
        this.mContext = ctx;
        profileName.put("0", mContext.getResources().getString(R.string.user_profile_anonymous));
        profileName.put("1", mContext.getResources().getString(R.string.user_profile_user));
        profileName.put("2", mContext.getResources().getString(R.string.user_profile_enterprise));
        profileName.put("3", mContext.getResources().getString(R.string.user_profile_administrator));
    }

    public String getUserProfile(String profileName) {
        return this.profileName.get(profileName);
    }

}