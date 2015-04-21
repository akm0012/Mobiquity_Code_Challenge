package com.andrewkingmarshall.mobiquitycodechallenge.UI_Handlers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.andrewkingmarshall.mobiquitycodechallenge.MainActivity;
import com.andrewkingmarshall.mobiquitycodechallenge.R;
import com.andrewkingmarshall.mobiquitycodechallenge.Utilities.Upload_Image_Utility;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Button_Handler.java
 *
 * @author         :: Andrew K Marshall
 * Created On      :: 4/20/15
 * Revision By     :: Andrew K Marshall
 * Last Revised On :: 4/21/15
 *
 * This class hanldes all button pushes.
 *
 */
public class Button_Handler implements View.OnClickListener {

    /** Used for LogCat Tags */
    public final String tag = "general_LogCat_tag";

    /** Used to keep a reference to our MainActivity */
    private MainActivity main_activity;
    private Context main_activity_context;

    /** MainActivity Buttons */
    private Button button_dropbox_link;
    private Button button_back;
    private Button button_camera;

    /** Used for taking a picture */
    public static final int NEW_PICTURE = 1;
    private String mCameraFileName;

    /**
     * This constructor is used when we are handling button pushed from the MainActivity.
     *
     * @param activity_in The MainActivity
     * @param context_in The MainActivity context
     */
    public Button_Handler (MainActivity activity_in,  Context context_in) {

        main_activity = activity_in;
        main_activity_context = context_in;

        set_up_buttons();

    }

    /**
     * This will find the buttons by their ID and add a listener to them.
     */
    private void set_up_buttons() {

        // Get handles to the buttons
        button_dropbox_link = (Button) main_activity.findViewById(R.id.button_dropbox_link);
        button_back = (Button) main_activity.findViewById(R.id.button_back);
        button_camera = (Button) main_activity.findViewById(R.id.button_camera);

        // Add the button listeners
        button_dropbox_link.setOnClickListener(this);
        button_back.setOnClickListener(this);
        button_camera.setOnClickListener(this);

    }

    /**
     * This is where all the button logic goes.
     *
     * @param v The Button (View) we pushed.
     */
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.button_dropbox_link:
                Log.i(tag, "Dropbox link Button Pushed");

                if (main_activity.is_logged_in()) {
                    main_activity.logOut();
                }

                else {
                    main_activity.get_mApi().getSession().startOAuth2Authentication(main_activity_context);
                }

                break;

            case R.id.button_back:
                Log.i(tag, "Dropbox Back Button Pushed");

                go_back_from_image_view();

                break;

            case R.id.button_camera:
                Log.i(tag, "Dropbox Back Button Pushed");

                Intent intent = new Intent();
                // Picture from camera
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

                // This is not the right way to do this, but for some reason, having
                // it store it in
                // MediaStore.Images.Media.EXTERNAL_CONTENT_URI isn't working right.

                Date date = new Date();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss", Locale.US);

                String current_city = main_activity.location_utility.get_current_city();

                String newPicFile;

                if (!current_city.equalsIgnoreCase("n/a")) {

                    newPicFile = current_city + "_" + df.format(date) + ".jpg";

                }

                else {
                    newPicFile = df.format(date) + ".jpg";
                }

                String outPath = new File(Environment.getExternalStorageDirectory(), newPicFile).getPath();
                File outFile = new File(outPath);

                mCameraFileName = outFile.toString();
                Uri outuri = Uri.fromFile(outFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
                Log.i(tag, "Importing New Picture: " + mCameraFileName);
                try {
                    main_activity.startActivityForResult(intent, NEW_PICTURE); // See MainActivity for onActivityResult()
                } catch (ActivityNotFoundException e) {
                    main_activity.showToast("There doesn't seem to be a camera.");
                }

        }
    }

    /**
     * Goes back to the main screen.
     */
    public void go_back_from_image_view() {
        // Hide the ImageView while showing everything else
        main_activity.findViewById(R.id.button_dropbox_link).setVisibility(View.VISIBLE);
        main_activity.findViewById(R.id.listView_dropbox_files).setVisibility(View.VISIBLE);
        main_activity.findViewById(R.id.imageView_main_canvas).setVisibility(View.GONE);
        main_activity.findViewById(R.id.button_back).setVisibility(View.GONE);
        main_activity.findViewById(R.id.button_camera).setVisibility(View.VISIBLE);
        main_activity.findViewById(R.id.imageView_globe).setVisibility(View.VISIBLE);
        main_activity.findViewById(R.id.textView_latitude).setVisibility(View.GONE);
        main_activity.findViewById(R.id.textView_longitude).setVisibility(View.GONE);
        main_activity.refresh_handler.set_swipe_to_refresh_enabled(true);
    }

    /**
     * Handles a new picture being created from the intent created in the MainActivity.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(),
     *                    allowing you to identify who this result came from.
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    public void handle_new_picture(int requestCode, int resultCode, Intent data) {

        if (requestCode == NEW_PICTURE) {
            // return from file upload
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = null;
                if (data != null) {
                    uri = data.getData();
                }

                File file = null;

                if (uri == null && mCameraFileName != null) {
                    uri = Uri.fromFile(new File(mCameraFileName));
                    file = new File(mCameraFileName);
                }

                if (uri != null && file != null) {

                    // TODO - Testing
                    Log.d(tag, "" + ReadExif(file.getAbsolutePath()));

                    Upload_Image_Utility upload = new Upload_Image_Utility(main_activity, main_activity_context,
                            main_activity.get_mApi(), "/", file, main_activity.location_utility.get_latitude(),
                            main_activity.location_utility.get_longitude());
                    upload.execute();
                }

                else {
                    main_activity.showToast("Error. Try Again.");
                }

            }

            else {
                Log.e(tag, "Unknown Activity Result from mediaImport: "
                        + resultCode);
            }
        }
    }

    /**
     * This is not used, but can be used to get the Exif .jpeg data.
     *
     * This didn't work once the picture was uploaded then downloaded from Dropbox.
     *
     * Source: http://android-coding.blogspot.com/2011/10/read-exif-of-jpg-file-using.html
     *
     * @param file The .jpeg file with the Exif data
     * @return A String with all the Exif data.
     */
    public String ReadExif(String file) {
        String exif="Exif: " + file;
        try {
            ExifInterface exifInterface = new ExifInterface(file);

            exif += "\nIMAGE_LENGTH: " + exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
            exif += "\nIMAGE_WIDTH: " + exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
            exif += "\n DATETIME: " + exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            exif += "\n TAG_MAKE: " + exifInterface.getAttribute(ExifInterface.TAG_MAKE);
            exif += "\n TAG_MODEL: " + exifInterface.getAttribute(ExifInterface.TAG_MODEL);
            exif += "\n TAG_ORIENTATION: " + exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
            exif += "\n TAG_WHITE_BALANCE: " + exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE);
            exif += "\n TAG_FOCAL_LENGTH: " + exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
            exif += "\n TAG_FLASH: " + exifInterface.getAttribute(ExifInterface.TAG_FLASH);
            exif += "\nGPS related:";
            exif += "\n TAG_GPS_DATESTAMP: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
            exif += "\n TAG_GPS_TIMESTAMP: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
            exif += "\n TAG_GPS_LATITUDE: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            exif += "\n TAG_GPS_LATITUDE_REF: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            exif += "\n TAG_GPS_LONGITUDE: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            exif += "\n TAG_GPS_LONGITUDE_REF: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
            exif += "\n TAG_GPS_PROCESSING_METHOD: " + exifInterface.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return exif;
    }

    /** Sets the text of a button.
     *
     * @param button_id The ID of the button you want to change.
     * @param text The new text of that button.
     */
    public void set_button_text(int button_id, String text) {

        ((Button) main_activity.findViewById(button_id)).setText("" + text);

    }



}
