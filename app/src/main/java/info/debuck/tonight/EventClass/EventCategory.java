package info.debuck.tonight.EventClass;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import info.debuck.tonight.R;

/**
 * Created by onetec on 13-05-16.
 */
public class EventCategory {
    private Context mContext;

    private Map<String, String> categoryName = new HashMap<String, String>();

    public EventCategory(Context ctx){
        this.mContext = ctx;
        categoryName.put("cultural", mContext.getResources().getString(R.string.event_category_cultural));
        categoryName.put("music", mContext.getResources().getString(R.string.event_category_music));
        categoryName.put("workshop", mContext.getResources().getString(R.string.event_category_workshop));
        categoryName.put("nightlife", mContext.getResources().getString(R.string.event_category_nightlife));
        categoryName.put("bar", mContext.getResources().getString(R.string.event_category_bar));
        categoryName.put("festival", mContext.getResources().getString(R.string.event_category_festival));
        categoryName.put("meeting", mContext.getResources().getString(R.string.event_category_meeting));
        categoryName.put("other", mContext.getResources().getString(R.string.event_category_other));
    }

    public String getCategoryName(String category) {
        return categoryName.get(category);
    }

}
