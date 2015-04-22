package com.andrewkingmarshall.mobiquitycodechallenge.Utilities;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.andrewkingmarshall.mobiquitycodechallenge.MainActivity;
import com.andrewkingmarshall.mobiquitycodechallenge.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Data_Utility.java
 *
 * @author         :: Andrew K Marshall
 * Created On      :: 4/21/15
 * Revision By     :: N/A
 * Last Revised On :: N/A
 *
 * This class handles saving and restoring data.
 *
 * In our case we use this for saving the coordinates of a picture taken.
 *
 */
public class Data_Utility {

    /** Used for LogCat Tags */
    public final String tag = "general_LogCat_tag";

    /** Used to reference the MainActivity */
    private Context main_activity_context;
    private MainActivity main_activity;

    /** The HashMap that we will be storing our file names and coordinates in */
    private HashMap hash_map;

    /**
     * This constructor is used when we are using the MainActivity.
     *
     * @param activity_in The MainActivity
     * @param context_in The MainActivity context
     */
    public Data_Utility (MainActivity activity_in,  Context context_in) {

        main_activity = activity_in;
        main_activity_context = context_in;

        init();
    }

    /**
     * Handles the initialization.
     *
     * Used so we can add more constructors if need be.
     *
     */
    public void init() {

        hash_map = new HashMap();

        // Check to see if there is a HashMap file saved, if so we restore it
        load_hash_map();
    }

    /**
     * Checks if there is a hash map saved.
     *
     * If so it restores it.
     */
    public void load_hash_map() {
        File file = new File(main_activity.getDir("data", MainActivity.MODE_PRIVATE) , "map");
        if(file.exists()) {
            Log.d(tag, "A HashMap File Exists");

            ObjectInputStream inputStream = null;
            try {
                inputStream = new ObjectInputStream(new FileInputStream(file));
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(tag, "File reading failed: " + e.getLocalizedMessage());
            }

            try {
                Log.d(tag, "Getting saved Hash Map");

                // Get the saved hash map
                hash_map = (HashMap)inputStream.readObject();
                inputStream.close();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves the Hash Map into internal storage.
     */
    public void save_data() {

        if (hash_map != null) {
            File file = new File(main_activity.getDir("data", MainActivity.MODE_PRIVATE), "map");
            ObjectOutputStream outputStream = null;
            try {
                outputStream = new ObjectOutputStream(new FileOutputStream(file));
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(tag, "File writing failed(2): " + e.getLocalizedMessage());
            }
            try {
                outputStream.writeObject(hash_map);
                outputStream.flush();
                outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(tag, "File writing failed: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Puts an entry into the Data Utility.
     *
     * @param filename_in The name of the file.
     * @param latitude_in The latitude of the picture.
     * @param longitude_in The longitude of the picture.
     */
    public void put(String filename_in, double latitude_in, double longitude_in) {

        // Create an array that will hold the coordinates
        double coord[] = {latitude_in, longitude_in};

        hash_map.put(filename_in, coord);

        Log.i(tag, "New HashMap Key: " + filename_in);
        Log.i(tag, "New HashMap Value: " + Arrays.toString((double[]) hash_map.get(filename_in)));
    }

    /**
     * Remove an item from the Data.
     *
     * @param filename_in The name of the file you want to remove.
     * @return A boolean indicating if anything was removed.
     */
    public boolean remove(String filename_in) {

        if (hash_map.containsKey(filename_in)) {
            hash_map.remove(filename_in);
            return true;
        }

        return false;
    }

    /**
     * Goes through our Data Utility and removes any entry's that no longer are valid.
     *
     * It also will add any entries that were added, however, they will not contain coordinates.
     * Only pictures taken from the app will have coordinate data. //TODO: Need to figure out how to get this from Dropbox
     *
     * This is used when we refresh the Dropbox file list.
     *
     * @param filenames An ArrayList of all the filenames in our ListView
     * @return A boolean indicating if anything was changed.
     */
    public boolean clean_data(ArrayList<String> filenames) {

        boolean changed = false;

        for (int i = 0; i < filenames.size(); i++) {

            if (hash_map.containsKey(filenames.get(i))) {
                // This entry is already in our HashMap
            }
            else {
                // We need to add a new entry into the HaspMap with blank coordinates
                put(filenames.get(i), 0.0, 0.0);
                changed = true;
            }

        }

        // Create an iterator that will iterate through out entire hash map
        Iterator iterator = hash_map.entrySet().iterator();
        while (iterator.hasNext()) {

            // Get the next item in the HashMap
            HashMap.Entry pair = (HashMap.Entry)iterator.next();

            // If filenames does not contain this key, then it has been deleted and needs to be removed
            if (!filenames.contains(pair.getKey())) {

                iterator.remove();
                Log.d(tag, "Removed Key from HashMap: " + pair.getKey());
                changed = true;

            }
        }

        save_data();

        return changed;
    }

    /**
     * Displays all the data in the HashMap in LogCat.
     */
    public void display_data_in_log() {

        Iterator it = hash_map.entrySet().iterator();

        int i = 0;

        while (it.hasNext()) {

            // Get the next item in the HashMap
            HashMap.Entry pair = (HashMap.Entry) it.next();

            Log.d(tag, i + ": Key: " + pair.getKey());
            Log.d(tag, i++ + ": Value: " + Arrays.toString((double[]) pair.getValue()));

        }
    }

    /**
     * Displays textViews with the pictures coordinates if valid.
     *
     * @param filename_in The filename of the picture you want the coordinates for.
     */
    public void set_textViews_if_valid(String filename_in) {

        // Check to make sure our HashMap has the key
        if (hash_map.containsKey(filename_in)) {

            double temp[];

            temp = (double[])hash_map.get(filename_in);

            // Make sure the entry is valid.
            if (temp[0] != 0.0 && temp[0] != 200 && temp[1] != 0.0 && temp[1] != 200) {

                // The entry is valid and should be displayed.

                // Latitude
                main_activity.findViewById(R.id.textView_latitude).setVisibility(View.VISIBLE);
                ((TextView)main_activity.findViewById(R.id.textView_latitude)).setText("Latitude: " + temp[0]);

                // Longitude
                main_activity.findViewById(R.id.textView_longitude).setVisibility(View.VISIBLE);
                ((TextView)main_activity.findViewById(R.id.textView_longitude)).setText("Longitude: " + temp[1]);
            }
        }
    }

    /**
     * Returns the hash map.
     *
     * @return The Hash Map with all the data.
     */
    public HashMap get_hash_map() {
        return this.hash_map;
    }

}
