package info.debuck.tonight;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import info.debuck.tonight.EventClass.TonightRequest;
import info.debuck.tonight.EventClass.User;
import info.debuck.tonight.EventClass.UserAvatar;
import info.debuck.tonight.Tools.GsonRequest;
import info.debuck.tonight.Tools.SessionManager;

public class ChangeProfilePictureActivity extends AppCompatActivity implements View.OnClickListener {

    /* views */
    private ImageView ivChangeGallery;
    private ImageView ivChangeCamera;
    private Button btValidate;
    private UserAvatar civProfilePicture;
    private ProgressBar changeProgressBar;

    /* vars */
    private String upload_url = "";
    private Bitmap bitmap;
    private User mUser;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    public final int PICK_IMAGE_REQUEST = 1;
    public final String KEY_IMAGE = "image";
    public final String KEY_NAME = "name";
    public final String KEY_USER_ID = "user_id";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile_picture);

        /* getting the views */
        ivChangeGallery = (ImageView) findViewById(R.id.changeGallery);
        ivChangeCamera = (ImageView) findViewById(R.id.changeCamera);
        btValidate = (Button) findViewById(R.id.validate);
        civProfilePicture = (UserAvatar) findViewById(R.id.profile_picture);
        changeProgressBar = (ProgressBar) findViewById(R.id.changeProgressBar);

        /* setting vars */
        mUser = NetworkSingleton.getInstance(this).getConnectedUSer();
        mRequestQueue = NetworkSingleton.getInstance(this).getRequestQueue();
        mImageLoader = NetworkSingleton.getInstance(this).getImageLoader();
        upload_url = getString(R.string.change_profile_picture_upload_url);

        /* Setting views */
        civProfilePicture.setImageUrl(mUser.getPicture_url(), mImageLoader);

        /* adding the listeners */
        ivChangeCamera.setOnClickListener(this);
        ivChangeGallery.setOnClickListener(this);
        btValidate.setOnClickListener(this);
    }


    /** This method will handle click event on our views */
    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch(id){
            case R.id.changeGallery:
                showFileChooser();
                break;

            case R.id.validate:
                uploadImage();
                break;
        }
    }


    /**
     * This method will launch a file chooser for the user to choose a picture
     */
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                getString(R.string.change_profile_picture_choose_picture)), PICK_IMAGE_REQUEST);
    }

    /**
     * This method will choose what to do with the picture chosen by the user
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Setting the Bitmap to ImageView
                civProfilePicture.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method will convert a bitmap to a base64 string
     * @param bmp
     * @return
     */
    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }


    private void uploadImage(){
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this,
                getString(R.string.change_profile_picture_loading_title),
                getString(R.string.change_profile_picture_loading_subtitle),
                false,
                false);

        /* prep var to send */
        //Converting bitmap to string
        final String image = getStringImage(bitmap);
        String name = "profile_picture_" + mUser.getId() + "_" + new Date().getTime();
        int user_id = mUser.getId();

        Map<String, String> params = new Hashtable<String,String>();
        params.put(KEY_IMAGE, image);
        params.put(KEY_NAME, name);
        params.put(KEY_USER_ID, "" + user_id);

        final GsonRequest<TonightRequest> uploadPicture = new GsonRequest<TonightRequest>(
                Request.Method.POST,
                upload_url,
                TonightRequest.class,
                params,
                new Response.Listener<TonightRequest>() {
                    @Override
                    public void onResponse(TonightRequest response) {
                        if (response.isStatusReturn()) {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.change_profile_picture_update),
                                    Toast.LENGTH_LONG).show();
                            /* We update the user picture_url */
                            User temp = NetworkSingleton.getInstance(getApplicationContext()).getConnectedUSer();
                            temp.setPicture_url(response.getStatusMessage());
                            NetworkSingleton.getInstance(getApplicationContext()).setConnectedUSer(temp);
                            (new SessionManager(getApplicationContext()))
                                    .createLoginSession(NetworkSingleton.getInstance(
                                            getApplicationContext()).getConnectedUSer());
                            /* We return the picture as a intent */
                            // Create intent to deliver some kind of result data
                            setResult(Activity.RESULT_OK);
                            loading.dismiss();
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.change_profile_picture_upload_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("ChangeProfilePicture", error.getMessage());
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.change_profile_picture_upload_error),
                                Toast.LENGTH_LONG).show();
                    }
                },
                this);

        /* adding to the request queue */
        mRequestQueue.add(uploadPicture);
    }
}
