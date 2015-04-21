package com.andrewkingmarshall.mobiquitycodechallenge.Utilities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
 * Revision By     :: N/A
 * Last Revised On :: N/A
 *
 * This class handles downloading image files from Dropbox.
 *
 */
public class Download_Image_Utility extends AsyncTask<Void, Long, Boolean> {

    /** Used for LogCat Tags */
    public final String tag = "general_LogCat_tag";

    private Context mContext;
    private final ProgressDialog mDialog;
    private DropboxAPI<?> mApi;
    private String mPath;
    private ImageView mView;
    private Drawable mDrawable;

    private FileOutputStream mFos;

    private boolean mCanceled;
    private Long mFileLen;
    private String mErrorMsg;

    private MainActivity main_activity;

    private final static String IMAGE_FILE_NAME = "temp.png";

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

            // This downloads a smaller, thumbnail version of the file.  The
            // API to download the actual file is roughly the same.
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
        int percent = (int)(100.0*(double)progress[0]/mFileLen + 0.5);
        mDialog.setProgress(percent);
    }

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
            main_activity.refresh_handler.set_swipe_to_refresh_enabled(false);

        } else {
            // Couldn't download it, so show an error
            showToast(mErrorMsg);
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }
}
