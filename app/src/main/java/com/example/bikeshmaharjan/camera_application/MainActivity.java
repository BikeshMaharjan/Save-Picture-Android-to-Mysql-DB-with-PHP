package com.example.bikeshmaharjan.camera_application;

import android.Manifest;
        import android.accounts.Account;
        import android.accounts.AccountManager;
        import android.app.Activity;
        import android.app.AlertDialog;

        import android.content.ContentResolver;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.content.pm.PackageManager;

        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.net.Uri;
        import android.os.Bundle;
        import android.os.Environment;
        import android.provider.MediaStore;
        import android.provider.Settings;

        import android.support.v4.app.ActivityCompat;
        import android.support.v4.content.ContextCompat;
        import android.support.v4.content.FileProvider;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.widget.Toolbar;
        import android.util.Base64;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.Toast;

        import com.android.volley.AuthFailureError;
        import com.android.volley.NetworkError;
        import com.android.volley.NoConnectionError;
        import com.android.volley.ParseError;
        import com.android.volley.Request;
        import com.android.volley.RequestQueue;
        import com.android.volley.Response;
        import com.android.volley.ServerError;
        import com.android.volley.TimeoutError;
        import com.android.volley.VolleyError;
        import com.android.volley.toolbox.StringRequest;
        import com.android.volley.toolbox.Volley;

        import com.example.bikeshmaharjan.camera_application.Database.PictureDB;

        import java.io.ByteArrayOutputStream;
        import java.io.File;
        import java.io.IOException;
        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.Date;
        import java.util.HashMap;
        import java.util.Map;
        import java.util.Random;


        import io.realm.Realm;
        import io.realm.RealmConfiguration;
        import io.realm.RealmResults;



public class MainActivity extends AppCompatActivity {
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    public static final String ALLOW_KEY = "ALLOWED";
    public static final String CAMERA_PREF = "camera_pref";
    //for taking image
    static final int REQUEST_IMAGE_CAPTURE = 1;

    //photo path
    String mCurrentPhotoPath;

    //Volley Request
    RequestQueue queue;

    //upload url
    public static final String URL = "http://10.6.1.17/uploads/my.php";
    Button capture_button, cancel_button, sync_button, save_button; ImageView image_captured;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        capture_button = (Button) findViewById(R.id.capture_btn);
        cancel_button = (Button) findViewById(R.id.cancel_button);
        sync_button = (Button) findViewById(R.id.sync_btn);
        save_button = (Button) findViewById(R.id.save_button);
        image_captured = (ImageView) findViewById(R.id.captured_image);
        capture_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera_clicked();
            }
        });
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete_saved_photo();
            }
        });
        sync_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //sync_phone_data(view);
                upload_data();
            }
        });
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save_picture_database();
            }
        });

        queue = Volley.newRequestQueue(this);
    }

    /*
    Function to handle camera button tapped event.
    - Checks for ther permission for access of Camera for application.
    - If Permission is not granted for Camera for Applications, Asks for permission.
    - Otherwise opens up camera.
     */
    private void camera_clicked(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (getFromPref(this, ALLOW_KEY)) {
                showSettingsAlert();
            } else if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)

                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {
                    showAlert();
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                }
            }
        } else {
            openCamera();
        }
    }

    /*
    Function to save the access of Camera in the Preference to be checked next time.
     */
    public static void saveToPreferences(Context context, String key, Boolean allowed) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_PREF,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean(key, allowed);
        prefsEditor.commit();
    }

    /*
    Read the preference value from the Preference if the access for the camera is already given or not.
     */
    public static Boolean getFromPref(Context context, String key) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_PREF,
                Context.MODE_PRIVATE);
        return (myPrefs.getBoolean(key, false));
    }

    /*
    Show Alert: Choose to give access of camera or not.
     */
    private void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                });
        alertDialog.show();
    }

    /*
    Showing settings Alert to give access to Camera or not.
     */
    private void showSettingsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SETTINGS",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startInstalledAppDetailsActivity(MainActivity.this);
                    }
                });

        alertDialog.show();
    }

    /*
    Get option selected by user in alert and save it to preference.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                for (int i = 0, len = permissions.length; i < len; i++) {
                    String permission = permissions[i];

                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean
                                showRationale =
                                ActivityCompat.shouldShowRequestPermissionRationale(
                                        this, permission);

                        if (showRationale) {
                            showAlert();
                        } else if (!showRationale) {
                            // user denied flagging NEVER ASK AGAIN
                            // you can either enable some fall back,
                            // disable features of your app
                            // or open another dialog explaining
                            // again the permission and directing to
                            // the app setting
                            saveToPreferences(MainActivity.this, ALLOW_KEY, true);
                        }
                    }
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public static void startInstalledAppDetailsActivity(final Activity context) {
        if (context == null) {
            return;
        }

        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }

    /*
    Open up the camera.
     */
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d("BIKIZ", "exception while creating image file:" + ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    //get back the image from the camera app
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("BIKIZ", "returning from camera");
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d("BIKIZ", "onActivityResult");
            setPic();
        }
    }

    //Create image file with unique name containing timeStamp
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = image_captured.getWidth();
        int targetH = image_captured.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        image_captured.setImageBitmap(bitmap);
    }

    private void delete_saved_photo(){
        File fdelete = new File(mCurrentPhotoPath);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                Log.d("BIKIZ","file Deleted :" );
                Toast.makeText(this, "File Deleted.", Toast.LENGTH_SHORT).show();
                image_captured.setImageResource(android.R.color.transparent);
            } else {
                Log.d("BIKIZ","file not Deleted :" );
                Toast.makeText(this,"File can't be deleted.",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void save_picture_database(){
        Realm realm_object;
        Realm.init(this);
        try{
            realm_object = Realm.getDefaultInstance();
        }catch (Exception ex){
            RealmConfiguration config = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
            realm_object = Realm.getInstance(config);
        }
        Random r = new Random();
        int picture_id = r.nextInt(80 - 65) + 65;
        realm_object.beginTransaction();
        PictureDB picture_database = realm_object.createObject(PictureDB.class, picture_id);
        picture_database.set_picture_path(mCurrentPhotoPath);
        realm_object.commitTransaction();
        realm_object.close();
        Toast.makeText(this,"Picture Saved.",Toast.LENGTH_SHORT).show();
    }

    public ArrayList<String> read_picture_database(){
        Realm realm_object;
        Realm.init(this);
        try{
            realm_object = Realm.getDefaultInstance();
        }catch (Exception ex){
            RealmConfiguration config = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
            realm_object = Realm.getInstance(config);
        }
        realm_object.beginTransaction();

        RealmResults<PictureDB> pictures_list = realm_object.where(PictureDB.class).findAll();
        Log.d("BIKIZ", "Picure Size = " + String.valueOf(pictures_list.size()));
        ArrayList<String> path_array = new ArrayList<String>();
        for(int i = 0; i < pictures_list.size(); i++){
            PictureDB picture_path = pictures_list.get(i);
            path_array.add(picture_path.get_picture_path());
        }
        realm_object.commitTransaction();
        realm_object.close();
        return path_array;
    }

    public void upload_data(){

        final ArrayList<String> picture_paths = read_picture_database();
        StringRequest request = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("BIKIZ", "DONE" + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("BIKIZ", "" + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> values = new HashMap<>();
                //values.put("name", name.getText().toString());
                /*for(int i = 0; i < picture_paths.size();i++){
                    Bitmap bitmap = get_bitmap(picture_paths.get(i));
                    String image = null;
                    if (bitmap != null) {
                        image = BitMapToString(bitmap);
                    }
                    if (image != null){
                        values.put("image", image);
                    }
                }*/
                Bitmap bitmap = get_bitmap(picture_paths.get(0));

                String image = null;
                if (bitmap != null) {
                    image = BitMapToString(bitmap);
                }
                Log.d("BIKIZ", image);
                    values.put("name", "ImageName");
                    values.put("image", image);
                    values.put("id", "1001");

                return values;
            }
        };
        queue.add(request);
    }

    private Bitmap get_bitmap(String picture_path){
        Bitmap bitmap = BitmapFactory.decodeFile(picture_path);
        //File file = new File(mCurrentPhotoPath);
        return bitmap;
    }

    public String BitMapToString(Bitmap bitmap){
        Log.d("BIKIZ", "inside bitmaptostring");
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
}