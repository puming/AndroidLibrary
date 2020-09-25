package com.pm.hybridsdk.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.pm.hybridsdk.BuildConfig;
import com.pm.hybridsdk.R;
import com.pm.hybridsdk.core.HybridWebChromeClient;
import com.pm.hybridsdk.core.HybridWebViewClient;
import com.pm.hybridsdk.core.HybridConfig;
import com.pm.hybridsdk.core.HybridJsInterface;
import com.pm.hybridsdk.widget.HybridWebView;

import java.io.File;

/**
 *
 * @author pm
 * @date 16/6/5
 */

public class HybridBaseActivity extends AppCompatActivity {
    private static final String TAG = "HybridBaseActivity";

    protected Style mStyle = Style.LIGHT;
    protected HybridWebView mWebView;
    protected HybridWebViewClient mWebViewClient;
    protected HybridWebChromeClient mWebChromeClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.hybrid_webview_act);
        mWebView = (HybridWebView) findViewById(R.id.hybrid_webview);
        initConfig(mWebView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // without sdk version check
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
        }
    }

    /**
     * 需要设置webview的属性则重写此方法
     *
     * @param webView
     */
    protected void initConfig(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setAllowFileAccess(true);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setUserAgentString(settings.getUserAgentString() + " hybrid_1.0.0 ");
        mWebViewClient = new HybridWebViewClient(webView);
        webView.setWebViewClient(mWebViewClient);
        mWebChromeClient = new HybridWebChromeClient(this);
        webView.setWebChromeClient(mWebChromeClient);
        webView.addJavascriptInterface(new HybridJsInterface(), HybridConfig.JSInterface);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
    }

    protected void loadUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        if (!url.contains("http")) {
            builder.append("http").append(":").append("//").append(url);
        } else {
            builder.append(url);
        }
        mWebViewClient.setHostFilter(Uri.parse(builder.toString()).getHost());
        mWebView.loadUrl(builder.toString());
    }

    protected void setStyle(Style style) {
        mStyle = style;
    }

    public void updateNativeUI(Bitmap bitmap, String title) {
    }

    @Override
    public boolean isDestroyed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return super.isDestroyed();
        } else {
            return isFinishing();
        }
    }

    private String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_PHONE_STATE", "android.permission.WRITE_SETTINGS"};
    private static final int REQUEST_CODE = 0x11;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // save file
            } else {
                Toast.makeText(getApplicationContext(), "PERMISSION_DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onActivityResult: requestCode=" + requestCode + "resultCode=" + resultCode + "data=" + data);
        }
        if (requestCode == HybridWebChromeClient.REQUEST_SELECT_FILE) {
            result(resultCode, data);
        }
    }

    private void result(int resultCode, Intent data) {
        // Check that the response is a good one
        if (resultCode != Activity.RESULT_OK) {
            receiveValue(null);
            return;
        }

        long size = 0;
        String cameraPhotoPath = mWebChromeClient.getCameraPhotoPath();
        try {
            String filePath = cameraPhotoPath.replace("file:", "");
            File file = new File(filePath);
            size = file.length();
        } catch (Exception e) {
            Log.e("Error!", "Error while opening image file" + e.getLocalizedMessage());
        }

        if (data != null || cameraPhotoPath != null) {
            Integer count = 1;
            ClipData images = null;
            try {
                images = data.getClipData();
            } catch (Exception e) {
                Log.e("Error!", e.getLocalizedMessage());
            }

            if (images == null && data != null && data.getDataString() != null) {
                count = data.getDataString().length();
            } else if (images != null) {
                count = images.getItemCount();
            }
            Uri[] results = new Uri[count];

            if (size != 0) {
                // If there is not data, then we may have taken a photo
                if (cameraPhotoPath != null) {
                    results = new Uri[]{Uri.parse(cameraPhotoPath)};
                }
            } else if (data.getClipData() == null) {
                results = new Uri[]{Uri.parse(data.getDataString())};
            } else if (images != null) {
                for (int i = 0; i < images.getItemCount(); i++) {
                    results[i] = images.getItemAt(i).getUri();
                }
            }

            //receive
            if (results.length >= 1) {
                receiveValue(results);
            } else {
                receiveValue(null);
            }
        }
    }

    private void receiveValue(Uri[] uris) {
        try {
            mWebChromeClient.getValueCallbacks().onReceiveValue(uris);
        } catch (Exception e) {
            Log.e(TAG, "result: error", e);
        } finally {
            mWebChromeClient.clearValueCallbacks();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
        }
        mWebChromeClient.getValueCallbacks().onReceiveValue(results);
        mWebChromeClient.clearValueCallbacks();
    }

    protected enum Style {
        //亮色的导航栏
        LIGHT,
        //暗色的导航栏
        DARK,
        //金色的导航栏
        GOLDEN
    }
}
