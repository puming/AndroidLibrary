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

public class HyBridWebChromeClient extends WebChromeClient {
    private static final String TAG = "HyBridWebChromeClient";
    private WeakReference<Activity> mActivitys;
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> mUploadMessages;
    public static final int REQUEST_SELECT_FILE = 100;
    private String mCameraPhotoPath;

    public HyBridWebChromeClient(Activity activity) {
        mActivitys = new WeakReference<>(activity);
    }


    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        Log.d(TAG, "onReceivedTitle: title=" + title);
        Activity activity = mActivitys.get();
        if (activity instanceof HybridBaseActivity) {
            HybridBaseActivity baseActivity = (HybridBaseActivity) activity;
            baseActivity.updateNativeUI(null,title);
        }
    }

    // For Lollipop 5.0+ Devices
    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        Log.d(TAG, "onShowFileChooser: webView=" + fileChooserParams.toString());
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
        showFileChooser();

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

    private boolean showFileChooser() {
        Activity activity = mActivitys.get();
        if (activity == null) {
            return false;
        }
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, "Unable to create Image File", ex);
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            } else {
                takePictureIntent = null;
            }
        }
        Log.d(TAG, "showFileChooser: mCameraPhotoPath=" + mCameraPhotoPath);

        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        contentSelectionIntent.setType("image/*");

        Intent[] intentArray;
        if (takePictureIntent != null) {
            intentArray = new Intent[]{takePictureIntent};
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

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
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
