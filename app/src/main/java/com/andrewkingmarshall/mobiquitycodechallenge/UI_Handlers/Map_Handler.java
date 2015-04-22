package com.andrewkingmarshall.mobiquitycodechallenge.UI_Handlers;

import android.app.FragmentTransaction;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;

import com.andrewkingmarshall.mobiquitycodechallenge.MainActivity;
import com.andrewkingmarshall.mobiquitycodechallenge.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Map_Handler.java
 *
 * @author         :: Andrew K Marshall
 * Created On      :: 4/21/15
 * Revision By     :: N/A
 * Last Revised On :: N/A
 *
 * This class manipulates the Google Map.
 *
 */
public class Map_Handler implements OnMapReadyCallback {

    /** Used for LogCat Tags */
    public final String tag = "general_LogCat_tag";

    /** Used to keep a reference to our MainActivity */
    private MainActivity main_activity;
    private Context main_activity_context;

    /** The Map Fragment */
    MapFragment map_fragment;

    /**
     * This constructor is used when we are handling maps from the MainActivity.
     *
     * @param activity_in The MainActivity
     * @param context_in The MainActivity context
     */
    public Map_Handler (MainActivity activity_in,  Context context_in) {

        main_activity = activity_in;
        main_activity_context = context_in;

    }


    /**
     * Adds the map and displays it.
     */
    public void add_map() {

        // Adjust the UI
        main_activity.refresh_handler.set_swipe_to_refresh_enabled(false);
        main_activity.findViewById(R.id.map).setVisibility(View.VISIBLE);
        main_activity.findViewById(R.id.button_map_back).setVisibility(View.VISIBLE);
        main_activity.findViewById(R.id.button_show_map).setVisibility(View.GONE);
        main_activity.findViewById(R.id.button_camera).setVisibility(View.GONE);
        // These need to go away so we don't accidentally push them and cause problems
        main_activity.findViewById(R.id.button_dropbox_link).setVisibility(View.GONE);
        main_activity.findViewById(R.id.listView_dropbox_files).setVisibility(View.INVISIBLE);


        map_fragment = MapFragment.newInstance();

        FragmentTransaction fragmentTransaction =
                main_activity.getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map, map_fragment);
        fragmentTransaction.commit();

        map_fragment.getMapAsync(this);
    }

    /**
     * Called when the map is ready to display.
     *
     * @param map_in The Google Map that is ready.
     */
    public void onMapReady(GoogleMap map_in) {


        Location loc = new Location(LocationManager.GPS_PROVIDER);
        loc.setLatitude(29.648591);
        loc.setLongitude(-82.331251);

        map_in.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 6.0f));

        // Set the location of my hopefully soon to be workplace :)
        map_in.addMarker(new MarkerOptions()
                .position(new LatLng(29.648591, -82.331251))
                .title("Mobiquity"));

        add_markers(map_in);
    }

    /**
     * Adds markers for each picture.
     *
     * @param map_in The map we are adding the markers to.
     */
    public void add_markers(GoogleMap map_in) {

        HashMap hash = main_activity.data_utility.get_hash_map();

        Iterator it = hash.entrySet().iterator();

        double temp[];

        while (it.hasNext()) {

            // Get the next item in the HashMap
            HashMap.Entry pair = (HashMap.Entry) it.next();

            temp = (double[])pair.getValue();

            // Make sure the entry is valid.
            if (temp[0] != 0.0 && temp[0] != 200 && temp[1] != 0.0 && temp[1] != 200) {
                map_in.addMarker(new MarkerOptions()
                        .position(new LatLng(temp[0], temp[1]))
                        .title((String) pair.getKey()));

                Log.d(tag, "Adding new point to map");
                Log.d(tag, "Key: " + pair.getKey());
                Log.d(tag, "Value: " + Arrays.toString((double[]) pair.getValue()));
            }

            else {
                Log.w(tag, "Invalid point skipped.");
            }


        }

    }

    /**
     * Removes the Map from the screen and destroys the fragment.
     */
    public void kill_map() {

        // Adjust the UI
        main_activity.refresh_handler.set_swipe_to_refresh_enabled(true);
        main_activity.findViewById(R.id.button_map_back).setVisibility(View.GONE);
        main_activity.findViewById(R.id.button_show_map).setVisibility(View.VISIBLE);
        main_activity.findViewById(R.id.button_camera).setVisibility(View.VISIBLE);
        main_activity.findViewById(R.id.button_dropbox_link).setVisibility(View.VISIBLE);
        main_activity.findViewById(R.id.listView_dropbox_files).setVisibility(View.VISIBLE);

        if (map_fragment != null) {
            main_activity.getFragmentManager().beginTransaction().remove(map_fragment).commit();

            main_activity.findViewById(R.id.map).setVisibility(View.GONE);
        }

    }
}
