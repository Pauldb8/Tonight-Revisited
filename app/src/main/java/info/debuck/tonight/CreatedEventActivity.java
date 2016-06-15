package info.debuck.tonight;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import info.debuck.tonight.EventClass.TonightEvent;

public class CreatedEventActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView mainListView = null;
    private ProgressBar mLoader = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_created_event);

        /* Getting the listview to where to show them */
        mainListView = (ListView) findViewById(R.id.mainListView);
        mainListView.setOnItemClickListener(this);
        mainListView.setEmptyView(findViewById(R.id.empty_view));

        /* Getting and setting the loader */
        mLoader = (ProgressBar) findViewById(R.id.loading);

        /* Get subscribed event */
        getEventToView getEventAsyncTask = new getEventToView(this, mainListView, mLoader,
                getEventToView.REQUEST_CREATED_EVENT);
        getEventAsyncTask.execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TonightEvent clickedEvent = (TonightEvent)parent.getAdapter().getItem(position);
        Intent openDetail = new Intent(this, EventDescriptionActivity.class);
        String serializedObject = NetworkSingleton.getInstance(this).getGson().toJson(clickedEvent);
        Log.i("Test", serializedObject);
        openDetail.putExtra(MainActivity.TONIGHT_INTENT_EXTRA_DESC, serializedObject);
        startActivity(openDetail);
    }
}
