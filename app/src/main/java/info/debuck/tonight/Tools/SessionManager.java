package info.debuck.tonight.Tools;

import android.content.Context;
import android.content.SharedPreferences;

import info.debuck.tonight.EventClass.User;
import info.debuck.tonight.NetworkSingleton;

/**
 * This class will handle the user session even after closing the application
 * thanks to the shared user prefs
 */
public class SessionManager {
    SharedPreferences pref;

    SharedPreferences.Editor editor;

    int PRIVATE_MODE = 0;

    Context mContext;
    private static final String PREF_NAME =" info.debuck.tonigh.prefs";
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_USER = "user";

    public SessionManager(Context context){
        mContext = context;
        pref = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create a login session, store the User class in serialized form as a String in the
     * shared preference under the KEY_USER
     */
    public void createLoginSession(User authUser){
        editor.putBoolean(IS_LOGIN, true);
        String serializedUser = NetworkSingleton.getInstance(mContext).getGson().toJson(authUser);
        editor.putString(KEY_USER, serializedUser);
        editor.commit();
    }

    /**
     * This method will return the user class if the user is connectd, else it returns null
     * @return: the connected User.class
     */
    public User getUser(){
        User connectedUser = null;
        if(isLoggedIn()) {
            connectedUser = NetworkSingleton.getInstance(mContext).getGson().fromJson(pref.getString(
                    KEY_USER, null), User.class);
        }
        return connectedUser;
    }

    /**
     * This disconnects the user, by removing the serialized User.class and IS_LOGIN
     * from the shared prefs
     */
    public void logoutUser(){
        editor.clear();
        editor.commit();
    }

    /**
     * Returns true if user if logged in, which means there is a serialized User in the shared prefs
     * and a true boolean in logged in shared prefs
     */
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }
}
