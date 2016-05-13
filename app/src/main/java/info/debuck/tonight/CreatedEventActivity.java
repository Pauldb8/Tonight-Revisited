package info.debuck.tonight;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class CreatedEventActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView mainListView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_created_event);

        /* Getting the listview to where to show them */
        mainListView = (ListView) findViewById(R.id.mainListView);
        mainListView.setOnItemClickListener(this);
        mainListView.setEmptyView(findViewById(R.id.empty_view));

        /* Get subscribed event */
        getEventToView getEventAsyncTask = new getEventToView(this, mainListView,
                getEventToView.REQUEST_CREATED_EVENT);
        getEventAsyncTask.execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
