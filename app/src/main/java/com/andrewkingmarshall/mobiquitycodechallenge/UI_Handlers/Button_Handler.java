package com.andrewkingmarshall.mobiquitycodechallenge.UI_Handlers;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.andrewkingmarshall.mobiquitycodechallenge.MainActivity;
import com.andrewkingmarshall.mobiquitycodechallenge.R;

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
    private Button button_refresh;

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
        button_refresh = (Button) main_activity.findViewById(R.id.button_refresh);

        // Add the button listeners
        button_dropbox_link.setOnClickListener(this);
        button_refresh.setOnClickListener(this);

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

            case R.id.button_refresh:
                Log.i(tag, "Refresh Button Pushed");

                if (main_activity.is_logged_in()) {
                    main_activity.listView_handler.refresh_listView();
                }


                break;
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
