package info.debuck.tonight;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import info.debuck.tonight.EventClass.TonightRequest;
import info.debuck.tonight.EventClass.User;
import info.debuck.tonight.EventClass.UserAvatar;
import info.debuck.tonight.Tools.GsonRequest;
import info.debuck.tonight.Tools.SessionManager;

public class ChangeProfilePictureActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int ADD_EV_OPEN_CAMERA = 8030;
    /* views */
    private ImageView ivChangeGallery;
    private ImageView ivChangeCamera;
    private Button btValidate;
    private UserAvatar civProfilePicture;
    private ProgressBar changeProgressBar;
    private ImageView rotatePicture;

    /* vars */
    private String upload_url = "";
    private Bitmap bitmap;
    private User mUser;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private Uri imageToUpload;

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
        rotatePicture = (ImageView) findViewById(R.id.pictureRotate);

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
        rotatePicture.setOnClickListener(this);
    }


    /** This method will handle click event on our views */
    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch(id){
            case R.id.changeGallery:
                showFileChooser();
                break;

            case R.id.changeCamera:
                takePicture();
                break;

            case R.id.validate:
                uploadImage();
                break;

            case R.id.pictureRotate:
                if(bitmap  != null) {
                    bitmap = rotateImage(bitmap, 90);
                    civProfilePicture.setImageBitmap(bitmap);
                }
                break;
        }
    }


    /**
     * this method will start an activity taking a picture and load it
     */
    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(Environment.getExternalStorageDirectory(), "event_picture.jpg");
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        imageToUpload = Uri.fromFile(f);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, ADD_EV_OPEN_CAMERA);
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
                //bitmap = rotateIfnecessary(bitmap, filePath);
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Setting the Bitmap to ImageView
                civProfilePicture.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //We receive data from the CAMERA so we get back a picture
        if (requestCode == ADD_EV_OPEN_CAMERA && resultCode == RESULT_OK) {
            if(imageToUpload != null){
                Uri selectedImage = imageToUpload;
                getContentResolver().notifyChange(selectedImage, null);
                Bitmap reducedSizeBitmap = getBitmap(imageToUpload.getPath());
                if(reducedSizeBitmap != null){
                    //bitmap = rotateIfnecessary(bitmap, imageToUpload);
                    bitmap = reducedSizeBitmap;
                    civProfilePicture.setImageBitmap(bitmap);
                }
            }
        }
    }


    /**
     * This method checks the EXIF data of the picture and rotate it if necessary
     * @param bitmap
     * @param selectedImage
     * @return
     */
    private Bitmap rotateIfnecessary(Bitmap bitmap, Uri selectedImage) {
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(getRealPathFromURI(this, selectedImage));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(ei != null) {
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch(orientation){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap = rotateImage(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap = rotateImage(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmap = rotateImage(bitmap, 270);
                    break;
            }

            return bitmap;
        }else {
            return bitmap;
        }

    }

    /**
     * This methods returns the real path from a URI
     * @param contentUri
     * @return
     */
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    /**
     * this method rotate the bitmap to the degrees asked
     * @param source picture
     * @param angle of rotation
     * @return
     */
    private Bitmap rotateImage(Bitmap source, int angle) {
        Bitmap retVal;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        return retVal;
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


    /**
     * This method will transfomr the bitmap to a BASE64 string and upload it to the server
     * associated with the user ID
     */
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

    /**
     * This method get a bitmap from a URI and downsize it to 1.2MP and returns it
     * @param path
     * @return
     */
    private Bitmap getBitmap(String path) {

        Uri uri = Uri.fromFile(new File(path));
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
            in = getContentResolver().openInputStream(uri);

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();


            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
                    IMAGE_MAX_SIZE) {
                scale++;
            }
            Log.d("", "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight);

            Bitmap b = null;
            in = getContentResolver().openInputStream(uri);
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                b = BitmapFactory.decodeStream(in, null, o);

                // resize to desired dimensions
                int height = b.getHeight();
                int width = b.getWidth();
                Log.d("", "1th scale operation dimenions - width: " + width + ", height: " + height);

                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x,
                        (int) y, true);
                b.recycle();
                b = scaledBitmap;

                System.gc();
            } else {
                b = BitmapFactory.decodeStream(in);
            }
            in.close();

            Log.d("", "bitmap size - width: " + b.getWidth() + ", height: " +
                    b.getHeight());
            return b;
        } catch (IOException e) {
            Log.e("", e.getMessage(), e);
            return null;
        }
    }

}
