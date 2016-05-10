package info.debuck.tonight;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import info.debuck.tonight.EventClass.User;

public class ProfileActivity extends AppCompatActivity {

    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /** Setting the views */
        mUser = NetworkSingleton.getInstance(this).getConnectedUSer();
        setupViews(mUser);

    }

    /**
     * This methods places the information from the user in their corresponding position on the view
     * @param mUser: the connected user class
     */
    private void setupViews(User mUser) {
        TextView tvName = (TextView) findViewById(R.id.profile_name);
        TextView tvEmail = (TextView) findViewById(R.id.profile_email);

        tvName.setText(mUser.getName());
        tvEmail.setText(mUser.getEmail());
     }

}
