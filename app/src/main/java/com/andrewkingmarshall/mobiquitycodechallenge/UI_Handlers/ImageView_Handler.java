package com.andrewkingmarshall.mobiquitycodechallenge.UI_Handlers;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
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
 * Revision By     :: N/A
 * Last Revised On :: N/A
 *
 * This class hanldes the ImaveViews.
 *
 */
public class ImageView_Handler {

    /** Used for LogCat Tags */
    public final String tag = "general_LogCat_tag";

    /** Used to keep a reference to our MainActivity */
    private MainActivity main_activity;
    private Context main_activity_context;

    /** MainActivity Buttons */
    private ImageView image_view_main_canvas;

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
     * This is currently unused, but could be used to handle ImageViews from another class.
     *
     * @param activity_in The calling Activity
     * @param context_in The calling Activity's context
     */
    public ImageView_Handler (Activity activity_in,  Context context_in) {

        // Unused right now.
    }

    /**
     * This will find the buttons by their ID and add a listener to them.
     */
    private void set_up_imageViews() {

        // Get handles to the buttons
       image_view_main_canvas = (ImageView) main_activity.findViewById(R.id.imageView_main_canvas);
    }

    /**
     * Sets the ImageView from a filename located on Dropbox
     *
     * @param filename_in The name of the file.
     */
    public void set_image_from_filename(String filename_in) {

        Log.i(tag, "Attempting to get file from Dropbox: " + filename_in);
        Download_Image_Utility download_image = new Download_Image_Utility(main_activity, main_activity_context,
                main_activity.get_mApi(), "/" + filename_in, image_view_main_canvas);
        download_image.execute();


    }

    /**
     * Hides an ImageView.
     * @param imageView_id The ID of the ImageView you want to hide.
     */
    public void hide_imageView(int imageView_id) {
        main_activity.findViewById(imageView_id).setVisibility(View.GONE);
    }

    /**
     * Shows an ImageView.
     * @param imageView_id The ID of the ImageView you want to show.
     */
    public void show_imageView(int imageView_id) {
        main_activity.findViewById(imageView_id).setVisibility(View.VISIBLE);
    }
}
