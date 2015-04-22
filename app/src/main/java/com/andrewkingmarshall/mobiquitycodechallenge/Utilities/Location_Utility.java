package com.andrewkingmarshall.mobiquitycodechallenge.Utilities;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.andrewkingmarshall.mobiquitycodechallenge.MainActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Location_Utility.java
 *
 * @author         :: Andrew K Marshall
 * Created On      :: 4/21/15
 * Revision By     :: N/A
 * Last Revised On :: N/A
 *
 * This class handles getting location data.
 *
 */
public class Location_Utility implements LocationListener, GpsStatus.Listener{

    /** Used for LogCat Tags */
    public final String tag = "general_LogCat_tag";

    /** Used to reference the MainActivity */
    private Context main_activity_context;
    private MainActivity main_activity;

    /** The current longitude of the device */
    private double longitude;

    /** The current latitude of the device */
    private double latitude;

    /** The Location Manager */
    LocationManager location_manager;

    /** The time in milliseconds in which we want to update out position */
    private final int milli_update = 5000; // 5 seconds

    /** The distance we must travel in meters in which we want to update out position */
    private final int meters_distance = 0; // 0 meter

    /**
     * This constructor is used when we are using the MainActivity.
     *
     * @param activity_in The MainActivity
     * @param context_in The MainActivity context
     */
    public Location_Utility (MainActivity activity_in,  Context context_in) {

        main_activity = activity_in;
        main_activity_context = context_in;

        init();

    }

    /**
     * Initial set up of the Location Utility.
     */
    private void init() {

        // Get the location manager
        location_manager = (LocationManager) main_activity.getSystemService(Context.LOCATION_SERVICE);

        // Register GPSStatus listener for events
        location_manager.addGpsStatusListener(this);

        // Set the Listener to update the location when we move
        location_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, milli_update,
                meters_distance, this);

    }

    /**
     * Used to monitor the GPS Status.
     *
     * Currently just being used for LogCat.
     *
     * @param event The GPS Status event.
     */
    public void onGpsStatusChanged(int event) {

        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                Log.i(tag, "GPS - Searching");
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                Log.i(tag, "GPS - Stopped Searching");
                break;

            // Called when we get a satellite fix
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                Log.i(tag, "GPS - Locked");

                // Removing the GPS status listener once GPS is locked
                location_manager.removeGpsStatusListener(this);

                break;
        }
    }

    /**
     * Listener that updates whenever our location changes.
     *
     * Triggered by time or distance.
     *
     * @param location The new location object.
     */
    public void onLocationChanged(Location location) {
        Log.i(tag, "Updating Location.");

        if (location != null) {

            if (location.getLongitude() != 0 || location.getLatitude() != 0) {
                this.latitude = location.getLatitude();
                this.longitude = location.getLongitude();

                Log.d(tag, "Lat: " + latitude);
                Log.d(tag, "Long: " + longitude);

                main_activity.imageView_handler.change_globe_green();
            } else {
                Log.d(tag, "GPS not receiving valid data");
                main_activity.imageView_handler.change_globe_red();
            }

        } else {
            Log.d(tag, "GPS not receiving valid data");
            main_activity.imageView_handler.change_globe_red();
        }

    }

    /**
     * Gets the live latitude if possible.
     *
     * If not, uses last known location.
     *
     * @return The latitude if available or 200 (invalid) if not.
     */
    public double get_latitude() {

        // Create temp variables
        double temp_lat = 200;

        // Used to check if we are receiving live location data yet
        if (longitude != 0 || latitude != 0) {

            temp_lat = this.latitude;

        }

        else {

            Location location = location_manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location.getLatitude() != 0) {
                temp_lat = location.getLatitude();
            }

        }

        return temp_lat;

    }

    /**
     * Gets the live longitude if possible.
     *
     * If not, uses last known location.
     *
     * @return The longitude if available or 200 (invalid) if not.
     */
    public double get_longitude() {

        // Create temp variables
        double temp_lot = 200;

        // Used to check if we are receiving live location data yet
        if (longitude != 0 || latitude != 0) {

            temp_lot = this.longitude;

        }

        else {

            Location location = location_manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location.getLongitude() != 0) {
                temp_lot = location.getLongitude();
            }

        }

        return temp_lot;
    }

    /**
     * Gets the live longitude and latitude if possible.
     *
     * If not, uses last known location.
     *
     * Uses this information to find what city you are in.
     *
     * @return The city you are in.  Or "n/a" if not available.
     */
    public String get_current_city() {

        // Create temp variables
        double temp_lat = 200;
        double temp_long = 200;

        // Used to check if we are receiving live location data yet
        if (longitude != 0 || latitude != 0) {

            temp_lat = this.latitude;
            temp_long = this.longitude;

        }

        else {

            Location location = location_manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location.getLongitude() != 0 || location.getLatitude() != 0) {
                temp_lat = location.getLatitude();
                temp_long = location.getLongitude();
            }

            else {
                return "n/a";
            }
        }

        Geocoder geocoder = new Geocoder(main_activity_context, Locale.getDefault());
        List<Address> addresses = null;
                try {

                    addresses = geocoder.getFromLocation(temp_lat, temp_long, 1);

                    if (addresses.size() > 0) {
                        Log.d(tag, "City: " + addresses.get(0).getLocality());
                        return addresses.get(0).getLocality();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
        return "n/a";
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    /**
     * Called when Location is turned on by the user.
     */
    public void onProviderEnabled(String provider) {
        Log.i(tag, "Provider Enabled");
        main_activity.imageView_handler.change_globe_red();
    }

    /**
     * Called when Location is turned off by the user.
     */
    public void onProviderDisabled(String provider) {
        Log.i(tag, "Provider disabled");
        main_activity.imageView_handler.change_globe_grey();

    }

}
