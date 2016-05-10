package info.debuck.tonight;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import info.debuck.tonight.EventClass.TonightEvent;

public class SubscribedEventActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView mainListView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribed_event);

        /* Getting the listview to where to show them */
        mainListView = (ListView) findViewById(R.id.mainListView);
        mainListView.setOnItemClickListener(this);

        /* Get subscribed event */
        getEventToView getEventAsyncTask = new getEventToView(this, mainListView,
                getEventToView.REQUEST_ALL_EVENT);
        getEventAsyncTask.execute();
    }

    /* This method will handle clicks */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TonightEvent clickedEvent = (TonightEvent)parent.getAdapter().getItem(position);
        Intent openDetail = new Intent(this, EventDescriptionActivity.class);
        String serializedObject = NetworkSingleton.getInstance(this).getGson().toJson(clickedEvent);
        //Log.i("Test", serializedObject);
        openDetail.putExtra(MainActivity.TONIGHT_INTENT_EXTRA_DESC, serializedObject);
        getEventToView getEventAsyncTask = new getEventToView(this, mainListView,
                getEventToView.REQUEST_SUBSCRIBED_EVENT);
        getEventAsyncTask.execute();
        //startActivity(openDetail);
    }
}
