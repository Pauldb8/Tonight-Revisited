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
    private String[] categoryNum = new String[9];

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

        categoryNum[1] = mContext.getResources().getString(R.string.event_category_cultural);
        categoryNum[2] = mContext.getResources().getString(R.string.event_category_music);
        categoryNum[3] = mContext.getResources().getString(R.string.event_category_workshop);
        categoryNum[4] = mContext.getResources().getString(R.string.event_category_nightlife);
        categoryNum[5] = mContext.getResources().getString(R.string.event_category_bar);
        categoryNum[6] = mContext.getResources().getString(R.string.event_category_festival);
        categoryNum[7] = mContext.getResources().getString(R.string.event_category_meeting);
        categoryNum[8] = mContext.getResources().getString(R.string.event_category_other);
    }

    public String getCategoryName(String category) {
        return categoryName.get(category);
    }

    public String getCategoryName(int numCategory){
        return categoryNum[numCategory];
    }



}
