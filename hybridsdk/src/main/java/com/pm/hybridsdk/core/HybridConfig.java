package com.pm.hybridsdk.core;

import com.pm.hybridsdk.R;
import com.pm.hybridsdk.action.HybridActionAjaxGet;
import com.pm.hybridsdk.action.HybridActionAjaxPost;
import com.pm.hybridsdk.action.HybridActionBack;
import com.pm.hybridsdk.action.HybridActionForward;
import com.pm.hybridsdk.action.HybridActionShowHeader;
import com.pm.hybridsdk.action.HybridActionShowLoading;
import com.pm.hybridsdk.action.HybridActionUpdateHeader;

import java.util.HashMap;

/**
 * Created by vane on 16/6/2.
 */

public class HybridConfig {

    public static final String SCHEME = "hybrid";
    public static final String ACTIONPRE = "medlinker.hybrid.";//配置是intent-filter中的action的前缀
//    public static final String VERSION_HOST = "http://yexiaochai.github.io/Hybrid/webapp/hybrid_ver.json";
    public static final String VERSION_HOST = "http://h5.qa.medlinker.com";
    public static final String FILE_HYBRID_DATA_VERSION = "hybrid_data_version";
    public static final String FILE_HYBRID_DATA_PATH = "hybrid_webapp";
    public static final String JSInterface = "HybridJSInterface";


    public static class TagnameMapping {
        private static HashMap<String, Class> mMap;

        static {
            mMap = new HashMap<>();
            mMap.put("forward", HybridActionForward.class);
            mMap.put("showheader", HybridActionShowHeader.class);
            mMap.put("updateheader", HybridActionUpdateHeader.class);
            mMap.put("back", HybridActionBack.class);
            mMap.put("showloading", HybridActionShowLoading.class);
            mMap.put("get", HybridActionAjaxGet.class);
            mMap.put("post", HybridActionAjaxPost.class);
        }

        public static Class mapping(String method) {
            return mMap.get(method);
        }
    }

    public static class IconMapping {
        private static HashMap<String, Integer> mMap;

        static {
            mMap = new HashMap<>();
            mMap.put("back", R.drawable.ic_back);
        }

        public static int mapping(String icon) {
            boolean has = mMap.containsKey(icon);
            if (!has) return -1;
            return mMap.get(icon);
        }
    }
}
