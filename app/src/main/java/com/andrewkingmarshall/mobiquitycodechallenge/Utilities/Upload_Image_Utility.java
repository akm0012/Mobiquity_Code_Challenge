package com.andrewkingmarshall.mobiquitycodechallenge.Utilities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.widget.Toast;

import com.andrewkingmarshall.mobiquitycodechallenge.MainActivity;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Upload_Image_Utility.java
 *
 * @author         :: Andrew K Marshall
 * Created On      :: 4/21/15
 * Revision By     :: N/A
 * Last Revised On :: N/A
 *
 * This class handles uploading image files to Dropbox.
 *
 */
public class Upload_Image_Utility extends AsyncTask<Void, Long, Boolean> {

    /** Used for LogCat Tags */
    public final String tag = "general_LogCat_tag";

    /** Used for Dropbox Access */
    private DropboxAPI<?> mApi;

    /** The destination path */
    private String mPath;

    /** The File to upload */
    private File mFile;

    /** File length */
    private long mFileLen;

    /** Dropbox Upload Request */
    private DropboxAPI.UploadRequest mRequest;

    /** A progress dialog indicating upload progress */
    private final ProgressDialog mDialog;

    /** The error message that will be displayed if anything goes wrong. */
    private String mErrorMsg;

    /** Main Activity References */
    MainActivity main_activity;
    private Context mContext;


    /**
     * Creates an Upload Image Utility.
     *
     * @param main_activity_in The Main Activity
     * @param context The Context of the app
     * @param api The Dropbox API
     * @param dropboxPath The path to where you want the filenames (should be "/")
     * @param file The file to upload
     */
    public Upload_Image_Utility(MainActivity main_activity_in, Context context, DropboxAPI<?> api, String dropboxPath,
                         File file) {
        // We set the context this way so we don't accidentally leak activities
        mContext = context.getApplicationContext();

        main_activity = main_activity_in;

        mFileLen = file.length();
        mApi = api;
        mPath = dropboxPath;
        mFile = file;

        //TODO: Cleanup
//        Log.d(tag, "Old File Name: " + mFile.getName());

//        File new_path = new File(mFile.getAbsolutePath());

//        Log.d(tag, "new_path File Name: " + new_path.getName());

//        boolean name_change = mFile.renameTo(new_path);

//        Log.d(tag, "File Name Changed: " + name_change);

//        Log.d(tag, "New File Name: " + mFile.getName());

        mDialog = new ProgressDialog(context);
        mDialog.setMax(100);
        // Makes it so you have to push the cancel button to stop the upload
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setMessage("Uploading " + file.getName());
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setProgress(0);
        mDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                try {
                    // This will cancel the putFile operation
                    mRequest.abort();
                } catch (NetworkOnMainThreadException e) {
                    Log.e(tag, "Error: " + e.getMessage());
                }

            }
        });
        mDialog.show();
    }

    /**
     * The background task that occurs on a separate thread.
     *
     * Do not access UI Thread here.
     */
    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            // By creating a request, we get a handle to the putFile operation,
            // so we can cancel it later if we want to
            FileInputStream fis = new FileInputStream(mFile);
            String path = mPath + mFile.getName();
            mRequest = mApi.putFileOverwriteRequest(path, fis, mFile.length(),
                    new ProgressListener() {
                        @Override
                        public long progressInterval() {
                            // Update the progress bar every half-second or so
                            return 500;
                        }

                        @Override
                        public void onProgress(long bytes, long total) {
                            publishProgress(bytes);
                        }
                    });

            if (mRequest != null) {
                mRequest.upload();
                return true;
            }

        } catch (DropboxUnlinkedException e) {
            // This session wasn't authenticated properly or user unlinked
            mErrorMsg = "This app wasn't authenticated properly.";
        } catch (DropboxFileSizeException e) {
            // File size too big to upload via the API
            mErrorMsg = "This file is too big to upload";
        } catch (DropboxPartialFileException e) {
            // We canceled the operation
            mErrorMsg = "Upload canceled";
        } catch (DropboxServerException e) {
            // Server-side exception.  These are examples of what could happen,
            // but we don't do anything special with them here.
            if (e.error == DropboxServerException._401_UNAUTHORIZED) {
                // Unauthorized, so we should unlink them.  You may want to
                // automatically log the user out in this case.
                main_activity.logOut();
                mErrorMsg = "Unauthorized! Logging Off";
            } else if (e.error == DropboxServerException._403_FORBIDDEN) {
                // Not allowed to access this
                mErrorMsg = "Not allowed to access this";
            } else if (e.error == DropboxServerException._404_NOT_FOUND) {
                // path not found (or if it was the thumbnail, can't be
                // thumbnailed)
                mErrorMsg = "Path not found";
            } else if (e.error == DropboxServerException._406_NOT_ACCEPTABLE) {
                // too many entries to return
                mErrorMsg = "Too many entries";
            } else if (e.error == DropboxServerException._415_UNSUPPORTED_MEDIA) {
                // can't be thumbnailed
                mErrorMsg = "Unsupported media";
            } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
                // user is over quota
                mErrorMsg = "Not enough storage";
            } else {
                // Something else
                mErrorMsg = "Something went wrong, try again";
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
        } catch (FileNotFoundException e) {
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        int percent = (int)(100.0*(double)progress[0]/mFileLen + 0.5);
        mDialog.setProgress(percent);
    }

    /**
     * This is called when the task is completed.
     *
     * Only edit UI stuff here.
     */
    @Override
    protected void onPostExecute(Boolean result) {
        mDialog.dismiss();
        if (result) {
            showToast("Image successfully uploaded");
            main_activity.refresh_handler.force_refresh();

        } else {
            showToast(mErrorMsg);
        }

    }

    /**
     * Create a Toast and displays it.
     *
     * @param msg The message of the toast.
     */
    private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }
}
