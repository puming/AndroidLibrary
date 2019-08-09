package com.pm.hybridsdk.action;

import android.webkit.WebView;

import com.pm.hybridsdk.param.HybridParamShowHeader;

import de.greenrobot.event.EventBus;

/**
 * Created by vane on 16/6/6.
 */

public class HybridActionShowHeader extends HybridAction {

    @Override
    public void onAction(WebView webView, String params, String jsmethod) {
        HybridParamShowHeader hybridParam = mGson.fromJson(params, HybridParamShowHeader.class);
        EventBus.getDefault().post(hybridParam);
    }
}
