package com.andrewkingmarshall.mobiquitycodechallenge.Utilities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.andrewkingmarshall.mobiquitycodechallenge.MainActivity;
import com.andrewkingmarshall.mobiquitycodechallenge.R;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Download_Image_Utility.java
 *
 * @author         :: Andrew K Marshall
 * Created On      :: 4/20/15
 * Revision By     :: Andrew K Marshall
 * Last Revised On :: 4/21/15
 *
 * This class handles downloading image files from Dropbox.
 *
 */
public class Download_Image_Utility extends AsyncTask<Void, Long, Boolean> {

    /** Used for LogCat Tags */
    public final String tag = "general_LogCat_tag";

    /** Used to reference the MainActivity */
    private Context mContext;
    private MainActivity main_activity;

    /** Used to indicate progress of the download */
    private final ProgressDialog mDialog;

    /** Needed to interact with Dropbox */
    private DropboxAPI<?> mApi;
    private String mPath;

    /** Used for displaying the image we are downloading */
    private ImageView mView;
    private Drawable mDrawable;
    private Long mFileLen;

    /** Used for saving in the temp cache */
    private FileOutputStream mFos;

    /** Used to indicate if we cancel a download */
    private boolean mCanceled;

    /** Used to store any error messages that may arise */
    private String mErrorMsg;



    private final static String IMAGE_FILE_NAME = "temp.png";

    /**
     * Creates a Download Image Utility
     *
     * @param main_activity_in The Main Activity
     * @param context The Context of the app
     * @param api The Dropbox API
     * @param dropboxPath The path to where you want the filenames (should be "/")
     * @param view The ImageView we're updating
     */
    public Download_Image_Utility(MainActivity main_activity_in, Context context, DropboxAPI<?> api,
                                 String dropboxPath, ImageView view) {

        main_activity = main_activity_in;

        // We set the context this way so we don't accidentally leak activities
        mContext = context.getApplicationContext();

        mApi = api;
        mPath = dropboxPath;
        mView = view;

        mDialog = new ProgressDialog(context);
        mDialog.setMessage("Downloading Image");
        mDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mCanceled = true;
                mErrorMsg = "Canceled";

                // This will cancel the getThumbnail operation by closing
                // its stream
                if (mFos != null) {
                    try {
                        mFos.close();
                    } catch (IOException e) {
                    }
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
            if (mCanceled) {
                return false;
            }

            String cachePath = mContext.getCacheDir().getAbsolutePath() + "/" + IMAGE_FILE_NAME;
            try {
                mFos = new FileOutputStream(cachePath);
            } catch (FileNotFoundException e) {
                mErrorMsg = "Couldn't create a local file to store the image";
                return false;
            }

            // This downloads a smaller, thumbnail version of the file.
            mApi.getThumbnail(mPath, mFos, DropboxAPI.ThumbSize.BESTFIT_960x640,
                    DropboxAPI.ThumbFormat.JPEG, null);
            if (mCanceled) {
                return false;
            }

            mDrawable = Drawable.createFromPath(cachePath);
            // We must have a legitimate picture
            return true;

        } catch (DropboxUnlinkedException e) {
            // The AuthSession wasn't properly authenticated or user unlinked.
            mErrorMsg = "Not authenticated";
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
            // Set the image now that we have it
            mView.setImageDrawable(mDrawable);

            // Display the Image while hiding everything else
            main_activity.findViewById(R.id.button_dropbox_link).setVisibility(View.GONE);
            main_activity.findViewById(R.id.listView_dropbox_files).setVisibility(View.GONE);
            main_activity.findViewById(R.id.imageView_main_canvas).setVisibility(View.VISIBLE);
            main_activity.findViewById(R.id.button_back).setVisibility(View.VISIBLE);
            main_activity.findViewById(R.id.button_camera).setVisibility(View.GONE);
            main_activity.findViewById(R.id.imageView_globe).setVisibility(View.GONE);
            main_activity.refresh_handler.set_swipe_to_refresh_enabled(false);

        } else {
            // Couldn't download it, so show an error
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
