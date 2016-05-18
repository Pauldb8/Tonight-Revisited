package info.debuck.tonight;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import info.debuck.tonight.EventClass.EventCategory;
import info.debuck.tonight.EventClass.User;
import info.debuck.tonight.EventClass.UserProfile;

/**
 * This singleton creates a unique RequestQueue for use with Volley downloading
 */
public class NetworkSingleton {
    private static NetworkSingleton ourInstance;
    private RequestQueue mRequestQueue; //Ou Singleton RequestQueue for Volley queuing
    private Gson mGson;
    private ImageLoader mImageLoader;
    private static Context mCtx;
    private DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); /* for formatting */
    private User connectedUSer;

    private EventCategory eventCategory;
    private UserProfile userProfile;

    private NetworkSingleton(Context context){
        this.mCtx = context;
        mRequestQueue = getRequestQueue(); /* Assigning the queue */
        mGson = getGson(); /* Assigning the Gson */


        /* Creating a cache String - Bitmap of 20 allocation */
        mImageLoader = getImageLoader();

        /* Caching the eventcategory for whole program accessibility */
        eventCategory = new EventCategory(mCtx);
        /* Caching the userPRofile for whole program accessibility */
        userProfile = new UserProfile(mCtx);
    }

    public static synchronized NetworkSingleton getInstance(Context context) {
        if(ourInstance == null)
            ourInstance = new NetworkSingleton(context);
        return ourInstance;
    }

    public RequestQueue getRequestQueue(){
        if(mRequestQueue == null){
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public Gson getGson(){
        if(mGson == null){
            /* The Gson is not existing, let's create one with the ability to serialize/unserialize
            Date class, we'll use the Gson builder
             */
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(Date.class, new DateTimeSerializer());
            builder.registerTypeAdapter(Date.class, new DateTimeDeserializer());
            //gson = builder.create();
            mGson = builder.create();
        }
        return mGson;
    }

    public User getConnectedUSer(){
        return connectedUSer;
    }

    public void setConnectedUSer(User user){
        this.connectedUSer = user;
    }

    public <T> void addToRequestQueue(Request<T> req){
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader(){
        if(mImageLoader == null){
            mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
                private final LruCache<String, Bitmap>
                        cache = new LruCache<String, Bitmap>(20);

                @Override
                public Bitmap getBitmap(String url){
                    return cache.get(url);
                }

                @Override
                public void putBitmap(String url, Bitmap bitmap){
                    cache.put(url, bitmap);
                }
            });
        }
        return mImageLoader;
    }

    /** Serializer/Deserializer for the Date class in Gson */
    private class DateTimeSerializer implements JsonSerializer<Date> {
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(fmt.format(src));
            //return new JsonPrimitive("2016-01-15 20:00:00");
        }
    }
    private class DateTimeDeserializer implements JsonDeserializer<Date> {
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return parseDateTime(json.getAsJsonPrimitive().getAsString());
            //return parseDateTime("2016-01-15 20:00:00");
        }
    }

    /**
     * This fonction returns a Date var from a String formatted as "2015-12-25 13:15:17"
     * @param dateString
     * @return Date variable with the date from the string
     */
    private Date parseDateTime(String dateString) {
        if (dateString == null) return null;

        try {
            return fmt.parse(dateString);
        }
        catch (ParseException e) {
            Log.e("ERROR", "Could not parse datetime: " + dateString);
            return null;
        }
    }

    /* This function returns the Eventategory class accessible */
    public EventCategory getEventCategory() {
        return eventCategory;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }
}
