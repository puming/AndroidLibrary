package com.pm.hybridsdk.core;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.pm.hybridsdk.ui.HybridBaseActivity;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author pm
 */
public class HybridWebChromeClient extends WebChromeClient {
    private static final String TAG = "HybridWebChromeClient";
    private static final String ACCEPT_TYPE_VIDEO = "video";
    private static final String ACCEPT_TYPE_IMAGE = "image";
    private WeakReference<Activity> mActivitys;
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> mUploadMessages;
    public static final int REQUEST_SELECT_FILE = 100;
    private String mCameraPhotoPath;

    public HybridWebChromeClient(Activity activity) {
        mActivitys = new WeakReference<>(activity);
    }


    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        Log.d(TAG, "onReceivedTitle: title=" + title);
        Activity activity = mActivitys.get();
        if (activity instanceof HybridBaseActivity) {
            HybridBaseActivity baseActivity = (HybridBaseActivity) activity;
            baseActivity.updateNativeUI(null, title);
        }
    }

    // For Lollipop 5.0+ Devices
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        String[] acceptTypes = fileChooserParams.getAcceptTypes();
        if (mUploadMessages != null) {
            mUploadMessages.onReceiveValue(null);
            mUploadMessages = null;
            try {
            } catch (Exception e) {
                Log.e(TAG, "onShowFileChooser: error", e);
            } finally {
                clearValueCallbacks();
            }
        }
        mUploadMessages = filePathCallback;
        for (String str : acceptTypes) {
            if (str.contains(ACCEPT_TYPE_VIDEO)) {
                showFileChooser(ACCEPT_TYPE_VIDEO);
                break;
            } else if (str.contains(ACCEPT_TYPE_IMAGE)) {
                showFileChooser(ACCEPT_TYPE_IMAGE);
                break;
            }
        }

        return true;
    }

    private boolean fileChooser(ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        if (mUploadMessages != null) {
            mUploadMessages.onReceiveValue(null);
            mUploadMessages = null;
        }
        mUploadMessages = filePathCallback;

        Intent intent = fileChooserParams.createIntent();
        boolean launched = launchActivity(intent);
        if (!launched) {
            mUploadMessages = null;
        }
        return launched;
    }

    private boolean showFileChooser(String type) {
        Activity activity = mActivitys.get();
        if (activity == null) {
            return false;
        }

        Intent launchSysCameraIntent;
        if (type.contains(ACCEPT_TYPE_VIDEO)) {
            launchSysCameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        } else {
            launchSysCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        if (launchSysCameraIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(type);
                launchSysCameraIntent.putExtra("PhotoPath", mCameraPhotoPath);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, "Unable to create Image File", ex);
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                launchSysCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            } else {
                launchSysCameraIntent = null;
            }
        }
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        if (type.equals(ACCEPT_TYPE_VIDEO)) {
            contentSelectionIntent.setType("video/*");
        } else if (type.equals(ACCEPT_TYPE_IMAGE)) {
            contentSelectionIntent.setType("image/*");
        }

        Intent intentPickImage = new Intent();
        intentPickImage.setAction(Intent.ACTION_PICK);

        if (type.equals(ACCEPT_TYPE_VIDEO)) {
            intentPickImage.setType("video/*");
        } else if (type.equals(ACCEPT_TYPE_IMAGE)) {
            intentPickImage.setType("image/*");
        }

        Intent[] intentArray;
        if (launchSysCameraIntent != null) {
            intentArray = new Intent[]{launchSysCameraIntent};
        } else {
            intentArray = new Intent[2];
        }

        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
//        chooserIntent.putExtra(Intent.EXTRA_TITLE, "选择操作");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
        try {
            activity.startActivityForResult(Intent.createChooser(chooserIntent, "Select images"), REQUEST_SELECT_FILE);
        } catch (ActivityNotFoundException e) {
            clearValueCallbacks();
            Toast.makeText(activity.getBaseContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
            return false;
        } catch (Exception e) {
            Log.e(TAG, "showFileChooser: error:", e);
            clearValueCallbacks();
        }

        return true;
    }

    private File createImageFile(String type) throws IOException {
        // Create an image file name
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
        String timeStamp = dateFormat.format(new Date());
        if (type.equals(ACCEPT_TYPE_VIDEO)) {
            String imageFileName = "Video_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MOVIES);
            File imageFile = File.createTempFile(
                    /* prefix */
                    imageFileName,
                    /* suffix */
                    ".mp4",
                    /* directory */
                    storageDir
            );
            return imageFile;
        } else {
            String imageFileName = "Image_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            File imageFile = File.createTempFile(
                    /* prefix */
                    imageFileName,
                    /* suffix */
                    ".jpg",
                    /* directory */
                    storageDir
            );
            return imageFile;
        }
    }

    //For Android 4.1 only
    protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        mUploadMessage = uploadMsg;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        launchActivity(intent);
    }

    private boolean launchActivity(Intent intent) {
        Activity activity = mActivitys.get();
        if (activity == null) {
            return false;
        }

        try {
            activity.startActivityForResult(Intent.createChooser(intent, "File Chooser"), REQUEST_SELECT_FILE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity.getBaseContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        launchActivity(i);
    }

    protected void openFileChooser(ValueCallback uploadMsg, String acceptType) {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        launchActivity(i);
    }

    public ValueCallback<Uri[]> getValueCallbacks() {
        return mUploadMessages;
    }

    public void clearValueCallbacks() {
        mUploadMessages = null;
    }

    public ValueCallback<Uri> getValueCallback() {
        return mUploadMessage;
    }

    public String getCameraPhotoPath() {
        return mCameraPhotoPath;
    }

    public void clearValueCallback() {
        mUploadMessage = null;
    }
}
