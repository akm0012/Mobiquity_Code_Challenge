package com.andrewkingmarshall.mobiquitycodechallenge.Utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import java.util.ArrayList;


/**
 * Download_File_List_Utility.java
 *
 * @author         :: Andrew K Marshall
 * Created On      :: 4/20/15
 * Revision By     :: N/A
 * Last Revised On :: N/A
 *
 * This class handles the downloading from DropBox
 *
 */
public class Download_File_List_Utility extends AsyncTask<Void, Long, Boolean> {

    /** Used for LogCat Tags */
    public final String tag = "general_LogCat_tag";

    /** Used to keep a reference to our MainActivity */
    private Context main_activity_context;

    /** Used for Dropbox Access */
    private DropboxAPI<?> mApi;

    /** The path to the file on Dropbox */
    private String mPath;

    /** The error message that will be displayed if anything goes wrong. */
    private String mErrorMsg;

    /** The ListView that will hold the file names */
    private ListView listView_dropbox_files;

    /** The Adapter that will be used to populate our ListView. */
    ArrayAdapter<String> arrayAdapter;

    /**
     * //TODO: Fill this in
     *
     * @param context
     * @param api
     * @param dropboxPath
     * @param listView_in
     */
    public Download_File_List_Utility(Context context, DropboxAPI<?> api,
                                      String dropboxPath, ListView listView_in) {
        // We set the context this way so we don't accidentally leak activities
        main_activity_context = context.getApplicationContext();

        mApi = api;
        mPath = dropboxPath;
        listView_dropbox_files = listView_in;
    }

    //TODO : Used for testing only!
    private void get_names_of_files() {

        ArrayList<String> filename = new ArrayList<String>();
        String mPath = "/";
        DropboxAPI.Entry dirent = null;
        try {
            dirent = mApi.metadata(mPath, 1000, null, true, null);
        } catch (DropboxException e) {
            System.out.println("Error :  " + e.getMessage());
        }
        for (DropboxAPI.Entry ent : dirent.contents) {
            if (ent.isDir) {
                filename.add(ent.fileName());
                Log.d(tag, "File Name: " + ent.fileName());
            }
//            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_list_item_1, filename );
//            mListView.setAdapter(arrayAdapter);


        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {

            // An ArrayList that holds all the filenames
            ArrayList<String> filenames = new ArrayList<String>();

            // Get the metadata for a directory
            DropboxAPI.Entry dirent = mApi.metadata(mPath, 1000, null, true, null);

            if (!dirent.isDir || dirent.contents == null) {
                // It's not a directory, or there's nothing in it
                mErrorMsg = "File or empty directory";
                return false;
            }

            // Make a list of everything in it that we can get a thumbnail for
            ArrayList<DropboxAPI.Entry> thumbs = new ArrayList<DropboxAPI.Entry>();
            for (DropboxAPI.Entry ent: dirent.contents) {

                Log.d(tag, "ent.fileName: " + ent.fileName());
                filenames.add(ent.fileName());
            }

            // Make an ArrayAdapter that will be used to update the ListView
            arrayAdapter = new ArrayAdapter<String>(main_activity_context,
                    android.R.layout.simple_list_item_1, filenames);

            return true;

        } catch (DropboxUnlinkedException e) {
            // The AuthSession wasn't properly authenticated or user unlinked.
        } catch (DropboxPartialFileException e) {
            // We canceled the operation
            mErrorMsg = "Download canceled";
        } catch (DropboxServerException e) {
            // Server-side exception.  These are examples of what could happen,
            // but we don't do anything special with them here.
            if (e.error == DropboxServerException._304_NOT_MODIFIED) {
                // won't happen since we don't pass in revision with metadata
            } else if (e.error == DropboxServerException._401_UNAUTHORIZED) {
                // Unauthorized, so we should unlink them.  You may want to
                // automatically log the user out in this case.
            } else if (e.error == DropboxServerException._403_FORBIDDEN) {
                // Not allowed to access this
            } else if (e.error == DropboxServerException._404_NOT_FOUND) {
                // path not found (or if it was the thumbnail, can't be
                // thumbnailed)
            } else if (e.error == DropboxServerException._406_NOT_ACCEPTABLE) {
                // too many entries to return
            } else if (e.error == DropboxServerException._415_UNSUPPORTED_MEDIA) {
                // can't be thumbnailed
            } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
                // user is over quota
            } else {
                // Something else
            }
            // This gets the Dropbox error, translated into the user's language
            mErrorMsg = e.body.userError;
            if (mErrorMsg == null) {
                mErrorMsg = e.body.error;
            }
        } catch (DropboxIOException e) {
            // Happens all the time, probably want to retry automatically.
            mErrorMsg = "Network error.  Try again.";
        } catch (DropboxParseException e) {
            // Probably due to Dropbox server restarting, should retry
            mErrorMsg = "Dropbox error.  Try again.";
        } catch (DropboxException e) {
            // Unknown error
            mErrorMsg = "Unknown error.  Try again.";
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
    }

    @Override
    protected void onPostExecute(Boolean result) {
//        mDialog.dismiss();
        if (result) {

            // Set the Adapter to the ListView.
            listView_dropbox_files.setAdapter(arrayAdapter);

        } else {
            // Couldn't download file list, so show an error
            showToast(mErrorMsg);
        }
    }

    /**
     * Create a Toast and displays it.
     *
     * @param msg The message of the toast.
     */
    private void showToast(String msg) {
        Toast error = Toast.makeText(main_activity_context, msg, Toast.LENGTH_LONG);
        error.show();
    }

}
