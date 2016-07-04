package info.debuck.tonight.Tools;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.HashMap;
import java.util.Map;

import info.debuck.tonight.EventClass.User;
import info.debuck.tonight.NetworkSingleton;
import info.debuck.tonight.R;

/**
 * Created by onetec on 23-04-16.
 */
public class RegisterUserTask extends AsyncTask {

    private String firstName;
    private String lastName;
    private int id;
    private String email;
    private String password;
    private Context mContext;

    public RegisterUserTask(String firstName, String lastName, String email, String password,
                            Context context) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.mContext = context;
    }

    /** this method will try to create the user with the given credits
     * it will call a HTTP page with POST params, the page should return a JSON serialized User.class
     * of the user created, which we get back and use to directly login the user
     * @param params
     * @return
     */
    @Override
    protected Object doInBackground(Object[] params) {
        RequestQueue requestQueue = NetworkSingleton.getInstance(mContext).getRequestQueue();
        String finalRequestURL = mContext.getString(R.string.subscribe_url);

        Map<String, String> postParams = new HashMap<String, String>();
        postParams.put("firstName", firstName);
        postParams.put("lastName", lastName);
        postParams.put("email", email);
        postParams.put("password", password);

        GsonRequest<User> myRequest = new GsonRequest<>(GsonRequest.Method.POST,
                finalRequestURL,
                User.class,
                postParams, new Response.Listener<User>() {
            @Override
            public void onResponse(User response) {
                Log.i("RegisterUserTask", "Received: " + response.toString());

                /*  We received the user so we log him in */
                SessionManager sessionManager = new SessionManager(mContext);
                if(!sessionManager.isLoggedIn()){
                    sessionManager.createLoginSession(response);
                    NetworkSingleton.getInstance(mContext).setConnectedUSer(response);
                    //updateDrawerInfo(null);
                    Toast.makeText(mContext, mContext.getString(R.string.auth_subscribe_success),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("RegisterUserTask", "Received: ERROR");
            }
        }, mContext);

        /* Filling resquest queue */
        requestQueue.add(myRequest);

        return null;
    }
}
