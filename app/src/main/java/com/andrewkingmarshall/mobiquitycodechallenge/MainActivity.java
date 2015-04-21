package com.andrewkingmarshall.mobiquitycodechallenge;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.andrewkingmarshall.mobiquitycodechallenge.UI_Handlers.Button_Handler;
import com.andrewkingmarshall.mobiquitycodechallenge.UI_Handlers.ImageView_Handler;
import com.andrewkingmarshall.mobiquitycodechallenge.UI_Handlers.ListView_Handler;
import com.andrewkingmarshall.mobiquitycodechallenge.UI_Handlers.Swipe_to_Refresh_Handler;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;


/**
 * MainActivity.java
 *
 * @author         :: Andrew K Marshall
 * Created On      :: 4/20/15
 * Revision By     :: Andrew K Marshall
 * Last Revised On :: 4/21/15
 *
 * This class hanldes all the logic in the Main Activity.
 *
 */
public class MainActivity extends ActionBarActivity {

    /** Used for LogCat Tags */
    public final String tag = "general_LogCat_tag";

    /** Needed to connect to and use Dropbox */
    private static final String APP_KEY = "kxkc3c8pwmimf2b";
    private static final String APP_SECRET = "qhv4hace8y2r28v";

    /** Used for Dropbox Access */
    private DropboxAPI<AndroidAuthSession> mApi;

    /** Shared Preferences constants */
    private static final String ACCOUNT_PREFS_NAME = "mobiquity_challenge_prefs";
    private static final String ACCESS_KEY_NAME = "ACCESS_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";

    /** Used to manage all button pushes */
    public Button_Handler button_handler;

    /** Used to manage the ListView */
    public ListView_Handler listView_handler;

    /** Used to refresh the List of Files */
    public Swipe_to_Refresh_Handler refresh_handler;

    /** Used to manage the ImageViews displayed */
    public ImageView_Handler imageView_handler;


    /** Used to indicate if we are linked to a Dropbox account */
    private boolean logged_in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(tag, "MainActivity.onCreate()");

        setContentView(R.layout.activity_main);

        // Create a new AuthSession so we can use the Dropbox API
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);

        // Set up the button handler (creates button listeners)
        button_handler = new Button_Handler(this, this);

        // Set up the ListView handler
        listView_handler = new ListView_Handler(this, this);

        // Set up the refresh handler
        refresh_handler = new Swipe_to_Refresh_Handler(this, this);

        // Set up the ImageView handler
        imageView_handler = new ImageView_Handler(this, this);

        // Set the UI according to if we are logged in or not.
        set_logged_in(mApi.getSession().isLinked());
    }

    public void onResume() {
        super.onResume();
        Log.i(tag, "MainActivity.onResume()");

        AndroidAuthSession session = mApi.getSession();

        // This must be inserted in the onResume() method of the
        // activity from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
        if (session.authenticationSuccessful()) {
            try {
                // Mandatory call to complete the auth
                session.finishAuthentication();

                // Store it locally in our app for later use
                storeAuth(session);
                set_logged_in(true);

                logged_in = true;
            } catch (IllegalStateException e) {
                showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                Log.e(tag, "Error authenticating", e);
            }
        }
    }

    /**
     * This is what gets called on finishing a media piece to import.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(),
     *                    allowing you to identify who this result came from.
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Button_Handler.NEW_PICTURE) {
            button_handler.handle_new_picture(requestCode, resultCode, data);
        }
    }

    /**
     * Logs the user out of Dropbox, un-linking them
     */
    public void logOut() {
        // Remove credentials from the session
        mApi.getSession().unlink();

        // Clear our stored keys
        clearKeys();
        // Change UI state to display logged out version
        set_logged_in(false);
        logged_in = false;
    }

    /**
     * Gets the user's credentials from Shared Preferences.
     * This way we do not have to ask the user to re-authenticate each time.
     *
     * @param session The Session we want to authenticate.
     */
    private void loadAuth(AndroidAuthSession session) {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;

        if (key.equals("oauth2:")) {
            // If the key is set to "oauth2:", then we can assume the token is for OAuth 2.
            session.setOAuth2AccessToken(secret);
        } else {
            // Still support using old OAuth 1 tokens.
            session.setAccessTokenPair(new AccessTokenPair(key, secret));
        }
    }

    /**
     * Saves the credentials to Shared Preferences.
     *
     * @param session The session we want to use when we store the credentials.
     */
    private void storeAuth(AndroidAuthSession session) {
        // Store the OAuth 2 access token, if there is one.
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.commit();
            return;
        }
        // Store the OAuth 1 access token, if there is one.  This is only necessary if
        // you're still using OAuth 1.
        AccessTokenPair oauth1AccessToken = session.getAccessTokenPair();
        if (oauth1AccessToken != null) {
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, oauth1AccessToken.key);
            edit.putString(ACCESS_SECRET_NAME, oauth1AccessToken.secret);
            edit.commit();
            return;
        }
    }

    /**
     * Clears the Shared Preferences.
     * Clearing the User's saved credentials.
     */
    private void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    /**
     * Builds a new session.
     *
     * @return The Android Authentication Session
     */
    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }

    /**
     * Create a Toast and displays it.
     *
     * @param msg The message of the toast.
     */
    public void showToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    // ----- Getters and Setters -----

    /**
     * Sets the boolean indicating if we are logged in.
     * @param logged_in Boolean indicating if we are logged in.
     */
    public void set_logged_in(boolean logged_in) {
        this.logged_in = logged_in;

        if (logged_in) {
            button_handler.set_button_text(R.id.button_dropbox_link, "Unlink Dropbox"); //TODO String Resource
            refresh_handler.set_swipe_to_refresh_enabled(true);
            listView_handler.refresh_listView();
            findViewById(R.id.button_camera).setVisibility(View.VISIBLE);
        }

        else {
            button_handler.set_button_text(R.id.button_dropbox_link, "Link to Dropbox"); //TODO String Resource
            refresh_handler.stop_refreshing();
            refresh_handler.set_swipe_to_refresh_enabled(false);
            listView_handler.remove_all_items();
            findViewById(R.id.button_camera).setVisibility(View.GONE);
        }
    }

    /**
     * Override this so if the user pushed the back button when the Image is displayed,
     * the app won't exit.
     */
    @Override
    public void onBackPressed() {

        if (findViewById(R.id.imageView_main_canvas).getVisibility() == View.VISIBLE) {
            button_handler.go_back_from_image_view();
        }

        else {
            super.onBackPressed();
        }

    }

    /**
     * @return The API needed to log in.
     */
    public DropboxAPI<AndroidAuthSession> get_mApi() {
        return mApi;
    }

    /**
     * @return Boolean indicating if we are logged in.
     */
    public boolean is_logged_in() {
        return logged_in;
    }

    public void onPause() {
        super.onPause();
        Log.i(tag, "MainActivity.onPause()");
    }

    public void onStart() {
        super.onStart();
        Log.i(tag, "MainActivity.onStart()");
    }

    public void onRestart() {
        super.onRestart();
        Log.i(tag, "MainActivity.onRestart()");
    }

    public void onStop() {
        super.onStop();
        Log.i(tag, "MainActivity.onStop()");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.i(tag, "MainActivity.onDestroy()");
    }
}
