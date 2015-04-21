package com.andrewkingmarshall.mobiquitycodechallenge.UI_Handlers;

import android.content.Context;
import android.util.Log;
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
public class ImageView_Handler {

    /** Used for LogCat Tags */
    public final String tag = "general_LogCat_tag";

    /** Used to keep a reference to our MainActivity */
    private MainActivity main_activity;
    private Context main_activity_context;

    /** MainActivity ImageViews */
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
     * This will find the ImageViews by their ID.
     */
    private void set_up_imageViews() {

        // Get handles to the buttons
       image_view_main_canvas = (ImageView) main_activity.findViewById(R.id.imageView_main_canvas);
    }

    /**
     * Sets the ImageView from a filename located on Dropbox.
     *
     * @param filename_in The name of the file.
     */
    public void set_image_from_filename(String filename_in) {

        Log.i(tag, "Attempting to get file from Dropbox: " + filename_in);
        Download_Image_Utility download_image = new Download_Image_Utility(main_activity, main_activity_context,
                main_activity.get_mApi(), "/" + filename_in, image_view_main_canvas);
        download_image.execute();
    }
}
