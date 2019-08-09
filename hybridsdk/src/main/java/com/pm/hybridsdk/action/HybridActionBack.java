package com.pm.hybridsdk.action;

import android.webkit.WebView;

import com.pm.hybridsdk.param.HybridParamBack;

import de.greenrobot.event.EventBus;

/**
 * Created by vane on 16/6/2.
 */

public class HybridActionBack extends HybridAction {

    @Override
    public void onAction(WebView webView, String params, String jsmethod) {
        EventBus.getDefault().post(new HybridParamBack());
    }
}
