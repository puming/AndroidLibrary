package com.pm.hybridsdk.action;

import android.webkit.WebView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pm.hybridsdk.param.HybridParamAnimation;
import com.pm.hybridsdk.param.HybridParamType;

/**
 * Created by vane on 16/6/2.
 */

public abstract class HybridAction {
    public static Gson mGson;

    static {
        mGson = new GsonBuilder()
                .registerTypeAdapter(HybridParamAnimation.class, new HybridParamAnimation.TypeDeserializer())
                .registerTypeAdapter(HybridParamType.class, new HybridParamType.TypeDeserializer())
                .create();
    }

    public abstract void onAction(WebView webView, String params, String jsmethod);

}
