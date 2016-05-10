package info.debuck.tonight.Tools;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import info.debuck.tonight.NetworkSingleton;

/**
 * Volley GET request which parses JSON server response into Java object.
 */
public class GsonRequest<T> extends Request<T> {
    private Gson gson;
    private final Class<T> clazz;
    private final Map<String, String> headers;
    private final Map<String, String> params;
    private final Response.Listener<T> listener;
    private final Context mCtx;
    private JSONObject paramObject = null;

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param url URL of the request to make
     * @param clazz Relevant class object, for Gson's reflection
     */
    public GsonRequest(String url, Class<T> clazz, Response.Listener<T> listener,
                       Response.ErrorListener errorListener, Context context) {
        super(Method.GET, url, errorListener);
        this.clazz = clazz;
        this.headers = new HashMap<String, String>();
        this.listener = listener;
        this.mCtx = context;
        this.params = null;
        //Log.i("GsonRequest", "Started");

        gson = NetworkSingleton.getInstance(mCtx).getGson();
    }

    /**
     * Make a POST request and return a parsed object from JSON.
     *
     * @param url URL of the request to make
     * @param clazz Relevant class object, for Gson's reflection
     */
    public GsonRequest(int method, String url, Class<T> clazz, Map<String, String> params,
                       Response.Listener<T> listener, Response.ErrorListener errorListener,
                       Context context) {

        super(method, url, errorListener);
        this.clazz = clazz;
        this.params = params;
        this.listener = listener;
        this.mCtx = context;
        this.headers = null;
        gson = NetworkSingleton.getInstance(mCtx).getGson();
    }

    /**
     * Make a POST request and return a parsed object from JSON.
     *
     * @param url URL of the request to make
     * @param clazz Relevant class object, for Gson's reflection
     * @param params The JSONified object to attach in the POST
     */
    public GsonRequest(int method, String url, Class<T> clazz, JSONObject params,
                       Response.Listener<T> listener, Response.ErrorListener errorListener,
                       Context context) {

        super(method, url, errorListener);
        this.clazz = clazz;
        this.paramObject = params;
        this.listener = listener;
        this.mCtx = context;
        this.headers = null;
        gson = NetworkSingleton.getInstance(mCtx).getGson();

        //We will transform the JSONObject to Map<String, String> in order to pass it
        Type stringStringMap = new TypeToken<Map<String, String>>(){}.getType();
        this.params = gson.fromJson(params.toString(), stringStringMap);
    }

    /**
     * Make a POST request and return a parsed object from JSON.
     *
     * @param url URL of the request to make
     * @param clazz Relevant class object, for Gson's reflection
     * @param params An object, that can be JSONified with GSON
     */
    public GsonRequest(int method, String url, Class<T> clazz, Object params,
                       Response.Listener<T> listener, Response.ErrorListener errorListener,
                       Context context) {

        super(method, url, errorListener);
        this.clazz = clazz;
        this.mCtx = context;
        /* Let's try to JSONifie the object */
        JSONObject eventJSON = null;
        try {
            eventJSON = new JSONObject(NetworkSingleton.getInstance(this.mCtx).getGson().
                    toJson(params));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.paramObject = eventJSON;
        this.listener = listener;
        this.headers = null;
        gson = NetworkSingleton.getInstance(mCtx).getGson();

        //We will transform the JSONObject to Map<String, String> in order to pass it
        Type stringStringMap = new TypeToken<Map<String, String>>(){}.getType();
        this.params = gson.fromJson(this.paramObject.toString(), stringStringMap);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    public Map<String, String> getParams() throws AuthFailureError{
        return params;
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            //Log.i("GsonRequest", "Received JSONString: " + json);
            return Response.success(
                    gson.fromJson(json, clazz),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }
}