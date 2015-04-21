package com.andrewkingmarshall.mobiquitycodechallenge;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.andrewkingmarshall.mobiquitycodechallenge.UI_Handlers.Button_Handler;
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
 * Revision By     :: N/A
 * Last Revised On :: N/A
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

        //TODO: SetLoggedIn - mApi.getSession().isLinked() - indicates if we are logged in. Alter UI Accordingly
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
                //TODO: setLoggedIn(true);

                logged_in = true;
            } catch (IllegalStateException e) {
                showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                Log.e(tag, "Error authenticating", e);
            }
        }
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

    /**
     * Logs the user out of Dropbox, un-linking them
     */
    public void logOut() {
        // Remove credentials from the session
        mApi.getSession().unlink();

        // Clear our stored keys
        clearKeys();
        // Change UI state to display logged out version
        //TODO: setLoggedIn(false);
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

    /**
     * Sets the boolean indicating if we are logged in.
     * @param logged_in Boolean indicating if we are logged in.
     */
    public void set_logged_in(boolean logged_in) {
        this.logged_in = logged_in;
    }

}

/* Do not need any overflow menus yet. */

//@Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
