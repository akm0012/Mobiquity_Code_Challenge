package com.andrewkingmarshall.mobiquitycodechallenge.UI_Handlers;

import android.content.Context;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import com.andrewkingmarshall.mobiquitycodechallenge.MainActivity;
import com.andrewkingmarshall.mobiquitycodechallenge.R;

/**
 * Button_Handler.java
 *
 * @author         :: Andrew K Marshall
 * Created On      :: 4/20/15
 * Revision By     :: 4/21/15
 * Last Revised On :: Andrew K Marshall
 *
 * This class hanldes the Swipe to Refresh widget.
 *
 */
public class Swipe_to_Refresh_Handler implements SwipeRefreshLayout.OnRefreshListener {

    /** Used for LogCat Tags */
    public final String tag = "general_LogCat_tag";

    /** Handles to MainActivity */
    private MainActivity main_activity;
    private Context main_activity_context;

    /** MainActivity SwipeRefreshLayout container */
    public SwipeRefreshLayout swipe_refresh_layout;

    /**
     * This constructor is used when we are handling vertical refresh swipes.
     *
     * @param activity_in The MainActivity
     * @param context_in  The MainActivity context
     */
    public Swipe_to_Refresh_Handler(MainActivity activity_in, Context context_in) {

        main_activity = activity_in;
        main_activity_context = context_in;

        set_up_layout();

    }

    /**
     * This will find the layout by its ID and add required listeners.
     */
    private void set_up_layout() {

        // Get a handle on the layout
        swipe_refresh_layout = (SwipeRefreshLayout) main_activity.findViewById(R.id.swipe_container);

        // Add the Refresh listeners
        swipe_refresh_layout.setOnRefreshListener(this);

        // Set the icon's color scheme
        swipe_refresh_layout.setColorSchemeResources(R.color.green,
                R.color.blue, R.color.orange);

    }

    /**
     * Called when you pull down to refresh the ListView
     */
    public void onRefresh() {

        // Refresh the ListView
        main_activity.listView_handler.refresh_listView();

    }

    /**
     * Stops the refresh animation.
     */
    public void stop_refreshing() {

        swipe_refresh_layout.setRefreshing(false);
    }

    /**
     * Sets whether or not the refresh widget is enabled.
     *
     * @param enabled A boolean indicating if the widget is enabled.
     */
    public void set_swipe_to_refresh_enabled(boolean enabled) {
        swipe_refresh_layout.setEnabled(enabled);
    }

    public void force_refresh() {
        swipe_refresh_layout.setRefreshing(true);
        // Refresh the ListView
        main_activity.listView_handler.refresh_listView();
    }
}
