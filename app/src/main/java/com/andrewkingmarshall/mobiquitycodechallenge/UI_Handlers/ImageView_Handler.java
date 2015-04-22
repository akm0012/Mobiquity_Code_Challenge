package com.andrewkingmarshall.mobiquitycodechallenge.UI_Handlers;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.andrewkingmarshall.mobiquitycodechallenge.MainActivity;
import com.andrewkingmarshall.mobiquitycodechallenge.R;
import com.andrewkingmarshall.mobiquitycodechallenge.Utilities.Download_Image_Utility;

/**
 * ImageView_Handler.java
 *
 * @author         :: Andrew K Marshall
 * Created On      :: 4/20/15
 * Revision By     :: Andrew K Marshall
 * Last Revised On :: 4/21/15
 *
 * This class hanldes the ImaveViews.
 *
 */
public class ImageView_Handler implements View.OnTouchListener{

    /** Used for LogCat Tags */
    public final String tag = "general_LogCat_tag";

    /** Used to keep a reference to our MainActivity */
    private MainActivity main_activity;
    private Context main_activity_context;

    /** MainActivity ImageViews */
    private ImageView image_view_main_canvas;
    private ImageView imageView_globe;

    /** Used to monitor Globe status */
    private int globe_color;

    /** Globe status' */
    private final int GREEN = 1;
    private final int RED = 2;
    private final int GREY = 0;

    /**
     * This constructor is used when we are handling ImageViews from the MainActivity.
     *
     * @param activity_in The MainActivity
     * @param context_in The MainActivity context
     */
    public ImageView_Handler (MainActivity activity_in,  Context context_in) {

        main_activity = activity_in;
        main_activity_context = context_in;

        set_up_imageViews();
    }

    /**
     * This will find the ImageViews by their ID.
     */
    private void set_up_imageViews() {

        // Get handles to the buttons
        image_view_main_canvas = (ImageView) main_activity.findViewById(R.id.imageView_main_canvas);
        imageView_globe = (ImageView) main_activity.findViewById(R.id.imageView_globe);

        imageView_globe.setOnTouchListener(this);

        globe_color = RED;
    }

    /**
     * Sets the ImageView from a filename located on Dropbox.
     *
     * @param filename_in The name of the file.
     */
    public void set_image_from_filename(String filename_in) {

        Log.i(tag, "Attempting to get file from Dropbox: " + filename_in);
        Download_Image_Utility download_image = new Download_Image_Utility(main_activity, main_activity_context,
                main_activity.get_mApi(), filename_in, image_view_main_canvas);
        download_image.execute();
    }

    /**
     * Changes the globe image to green.
     */
    public void change_globe_green()
    {
        imageView_globe.setImageResource(R.drawable.green_globe);
        globe_color = GREEN;
    }

    /**
     * Changes the globe image to red.
     */
    public void change_globe_red()
    {
        imageView_globe.setImageResource(R.drawable.red_globe);
        globe_color = RED;
    }

    /**
     * Changes the globe image to grey.
     */
    public void change_globe_grey()
    {
        imageView_globe.setImageResource(R.drawable.grey_globe);
        globe_color = GREY;
    }

    /**
     * Used to indicate what the globe means.
     */
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Log.i(tag, "Globe Touched Down ");

            if (globe_color == GREEN) {

                main_activity.showToast("GPS Link Established");

            } else if (globe_color == RED) {

                main_activity.showToast("GPS Searching");

            } else if (globe_color == GREY) {

                main_activity.showToast("GPS Disabled");

            }
        }

        return false;
    }
}
