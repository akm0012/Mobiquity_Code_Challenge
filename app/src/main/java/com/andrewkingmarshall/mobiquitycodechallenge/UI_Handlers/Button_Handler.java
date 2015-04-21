package com.andrewkingmarshall.mobiquitycodechallenge.UI_Handlers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Button_Handler.java
 *
 * @author         :: Andrew K Marshall
 * Created On      :: 4/20/15
 * Revision By     :: N/A
 * Last Revised On :: N/A
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
     * This is currently unused, but could be used to handle button pushes from another class.
     *
     * @param activity_in The calling Activity
     * @param context_in The calling Activity's context
     */
    public Button_Handler (Activity activity_in,  Context context_in) {

        // Unused right now.
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

                // Hide the ImageView while showing everything else
                main_activity.findViewById(R.id.button_dropbox_link).setVisibility(View.VISIBLE);
                main_activity.findViewById(R.id.listView_dropbox_files).setVisibility(View.VISIBLE);
                main_activity.findViewById(R.id.imageView_main_canvas).setVisibility(View.GONE);
                main_activity.findViewById(R.id.button_back).setVisibility(View.GONE);
                main_activity.refresh_handler.set_swipe_to_refresh_enabled(true);

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

                String newPicFile = df.format(date) + ".jpg";
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
                if (uri == null && mCameraFileName != null) {
                    uri = Uri.fromFile(new File(mCameraFileName));
                }
                File file = new File(mCameraFileName);

                if (uri != null) {
                    Upload_Image_Utility upload = new Upload_Image_Utility(main_activity_context,
                            main_activity.get_mApi(), "/", file);
                    upload.execute();
                }
            } else {
                Log.e(tag, "Unknown Activity Result from mediaImport: "
                        + resultCode);
            }
        }
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
