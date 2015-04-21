package com.andrewkingmarshall.mobiquitycodechallenge.UI_Handlers;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.andrewkingmarshall.mobiquitycodechallenge.MainActivity;
import com.andrewkingmarshall.mobiquitycodechallenge.R;
import com.andrewkingmarshall.mobiquitycodechallenge.Utilities.Download_File_List_Utility;

/**
 * ListView_Handler.java
 *
 * @author         :: Andrew K Marshall
 * Created On      :: 4/20/15
 * Revision By     :: N/A
 * Last Revised On :: N/A
 *
 * This class hanldes the ListView.
 *
 */
public class ListView_Handler implements AdapterView.OnItemClickListener {

    /** Used for LogCat Tags */
    public final String tag = "general_LogCat_tag";

    /** Used to keep a reference to our MainActivity */
    private MainActivity main_activity;
    private Context main_activity_context;

    /** MainActivity ListView */
    private ListView listView_dropbox_files;

    /**
     * This constructor is used when we are handling button pushed from the MainActivity.
     *
     * @param activity_in The MainActivity
     * @param context_in The MainActivity context
     */
    public ListView_Handler (MainActivity activity_in,  Context context_in) {

        main_activity = activity_in;
        main_activity_context = context_in;

        set_up_listView();

    }

    /**
     * This is currently unused, but could be used to handle button pushes from another class.
     *
     * @param activity_in The calling Activity
     * @param context_in The calling Activity's context
     */
    public ListView_Handler (Activity activity_in,  Context context_in) {

        // Unused right now.
    }

    /**
     * This will find the buttons by their ID and add a listener to them.
     */
    private void set_up_listView() {

        // Get a handle to the ListView
        listView_dropbox_files = (ListView) main_activity.findViewById(R.id.listView_dropbox_files);

        // Add the onItemClick listener
        listView_dropbox_files.setOnItemClickListener(this);

    }

    /**
     * Handles onItemClicked events.
     *
     * @param parent The AdapterView where the click happened.
     * @param view The view within the AdapterView that was clicked (this will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id The row id of the item that was clicked.
     */
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Log.i(tag, "OnItemClick:view: " + ((TextView)view).getText().toString());
        Log.i(tag, "OnItemClick:position: " + position);
        Log.i(tag, "OnItemClick:id: " + id);

        String filename_pressed = ((TextView)view).getText().toString();

        main_activity.imageView_handler.set_image_from_filename(filename_pressed);

    }

    /**
     * Refreshes the ListView.
     */
    public void refresh_listView() {

        Log.i(tag, "Refreshing ListView");

        Download_File_List_Utility refresh_file_list = new Download_File_List_Utility(main_activity,
                main_activity_context, main_activity.get_mApi(), "/", listView_dropbox_files);

        refresh_file_list.execute();
    }

    /**
     * Removes all the items from the ListView
     */
    public void remove_all_items() {
        listView_dropbox_files.setAdapter(null);
    }
}





























