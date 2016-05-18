package info.debuck.tonight;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.HashMap;
import java.util.Map;

import info.debuck.tonight.EventClass.User;
import info.debuck.tonight.Tools.GsonRequest;
import info.debuck.tonight.Tools.SessionManager;

public class SubscribeActivity extends AppCompatActivity{

    private TextView tvAppNameLabel;
    private EditText tvFirstName;
    private EditText tvLastName;
    private EditText tvEmail;
    private EditText tvPassword;
    private View mProgressView;
    private View mLoginFormView;
    private Button btRegister;
    private Toolbar toolbar;
    private RegisterUserTask mAuthTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);

        tvAppNameLabel = (TextView) findViewById(R.id.subscribe_app_title);
        Typeface septemberFont = Typeface.createFromAsset(getAssets(),"fonts/September Mornings.ttf");
        tvAppNameLabel.setTypeface(septemberFont);

        tvFirstName = (EditText) findViewById(R.id.subscribe_tv_firstName);
        tvLastName = (EditText) findViewById(R.id.subscribe_tv_lastNAme);
        tvEmail = (EditText) findViewById(R.id.subscribe_tv_email);
        tvPassword = (EditText) findViewById(R.id.subscribe_tv_password);

        mProgressView = (ProgressBar) findViewById(R.id.login_progress);
        mLoginFormView = (LinearLayout) findViewById(R.id.login_form);

        btRegister = (Button) findViewById(R.id.subscribe_button);
        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        /* Don't show the keyboard at first */
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        tvEmail.setError(null);
        tvPassword.setError(null);
        tvFirstName.setError(null);
        tvLastName.setError(null);

        // Store values at the time of the login attempt.
        String email = tvEmail.getText().toString();
        String password = tvPassword.getText().toString();
        String firstName = tvFirstName.getText().toString();
        String lastName = tvLastName.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            tvPassword.setError(getString(R.string.error_invalid_password));
            focusView = tvPassword;
            cancel = true;
        }

        //Check for not empty first name
        if(TextUtils.isEmpty(firstName)){
            tvFirstName.setError(getString(R.string.subscribe_error_empty));
            focusView = tvFirstName;
            cancel = true;
        }

        //Check for not empty last name
        if(TextUtils.isEmpty(lastName)){
            tvLastName.setError(getString(R.string.subscribe_error_empty));
            focusView = tvLastName;
            cancel = true;
        }


        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            tvEmail.setError(getString(R.string.error_field_required));
            focusView = tvEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            tvEmail.setError(getString(R.string.error_invalid_email));
            focusView = tvEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new RegisterUserTask(firstName, lastName, email, password, this);
            mAuthTask.execute((Void) null);
        }
    }


    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 8;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous registration task used to authenticate
     * the user.
     */
    public class RegisterUserTask extends AsyncTask<Void, Void, Boolean>  {

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
        protected Boolean doInBackground(Void... params) {
            RequestQueue requestQueue = NetworkSingleton.getInstance(getApplicationContext()).getRequestQueue();
            String finalRequestURL = getApplicationContext().getString(R.string.subscribe_url);

            Map<String, String> postParams = new HashMap<String, String>();
            postParams.put("firstName", firstName);
            postParams.put("lastName", lastName);
            postParams.put("email", email);
            postParams.put("password", password);

            final GsonRequest<User> myRequest = new GsonRequest<>(GsonRequest.Method.POST,
                    finalRequestURL,
                    User.class,
                    postParams, new Response.Listener<User>() {
                @Override
                public void onResponse(User response) {
                    if(response != null) {
                        Log.i("RegisterUserTask", "Received: " + response.toString());

                    /*  We received the user so we log him in */
                        SessionManager sessionManager = new SessionManager(getApplicationContext());
                        sessionManager.createLoginSession(response);
                        NetworkSingleton.getInstance(getApplicationContext()).setConnectedUSer(response);
                        //updateDrawerInfo(null);
                        Toast.makeText(getApplicationContext(), getApplicationContext()
                                        .getString(R.string.auth_subscribe_success),
                                Toast.LENGTH_LONG).show();
                        //We redirect him to his profile page
                        finish();
                        startActivity(new Intent(getApplicationContext(), ProfileAndFriendsActivity.class));
                    }else {
                        Log.i("RegisterUserTask", "Received: ERROR");
                        tvEmail.setError(getString(R.string.register_user_email_taken));
                        tvEmail.requestFocus();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i("RegisterUserTask", "Received: ERROR");
                    tvEmail.setError(getString(R.string.register_user_email_taken));
                    tvEmail.requestFocus();
                }
            }, getApplicationContext());

        /* Filling resquest queue */;
            requestQueue.add(myRequest);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
