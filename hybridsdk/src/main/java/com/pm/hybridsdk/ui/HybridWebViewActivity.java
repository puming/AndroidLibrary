package com.pm.hybridsdk.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.webkit.ValueCallback;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.pm.hybridsdk.R;
import com.pm.hybridsdk.core.HybridAjaxService;
import com.pm.hybridsdk.core.HybridConfig;
import com.pm.hybridsdk.core.HybridConstant;
import com.pm.hybridsdk.param.HybridParamAjax;
import com.pm.hybridsdk.param.HybridParamAnimation;
import com.pm.hybridsdk.param.HybridParamBack;
import com.pm.hybridsdk.param.HybridParamCallback;
import com.pm.hybridsdk.param.HybridParamShowHeader;
import com.pm.hybridsdk.param.HybridParamShowLoading;
import com.pm.hybridsdk.param.HybridParamUpdateHeader;
import com.pm.hybridsdk.widget.NavgationView;
import com.qrcode.LaunchDelegate;
import com.qrcode.common.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by vane on 16/6/3.
 */

public class HybridWebViewActivity extends HybridBaseActivity {

    private NavgationView mNavgationView;
    private ProgressBar mProgessbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mNavgationView = (NavgationView) findViewById(R.id.hybrid_navgation);
        mProgessbar = (ProgressBar) findViewById(R.id.hybrid_progressbar);
        boolean navgation = getIntent().getBooleanExtra(HybridConstant.INTENT_EXTRA_KEY_HASNAVGATION, true);
        if (navgation) {
            int leftIcon;
            int rightIcon;
            switch (mStyle) {
                case LIGHT:
                    leftIcon = R.drawable.ic_back_vector;
                    rightIcon = R.drawable.ic_scan_vector;
                    mNavgationView.setTextColor(Color.BLACK);
                    mNavgationView.setBackgroundColor(getResources().getColor(R.color.primary_light));
                    break;
                case DARK:
                    leftIcon = R.drawable.ic_back;
                    rightIcon = R.drawable.ic_scan;
                    mNavgationView.setTextColor(Color.WHITE);
                    mNavgationView.setBackgroundColor(getResources().getColor(R.color.primary_dark));
                    break;
                case GOLDEN:
                    leftIcon = R.drawable.ic_back;
                    rightIcon = R.drawable.ic_scan;
                    mNavgationView.setTextColor(Color.WHITE);
                    mNavgationView.setBackgroundColor(getResources().getColor(R.color.primary_golden));
                    break;
                default:
                    leftIcon = R.drawable.ic_back_vector;
                    rightIcon = R.drawable.ic_scan_vector;
                    mNavgationView.setTextColor(Color.BLACK);
                    mNavgationView.setBackgroundColor(getResources().getColor(R.color.primary_light));
                    break;
            }
            mNavgationView.appendNavgation(NavgationView.Direct.LEFT, "", leftIcon, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mWebView.canGoBack()) {
                        mWebView.goBack();
                    } else {
                        finish();
                    }
                }
            }).setVisibility(View.VISIBLE);
            mNavgationView.appendNavgation(NavgationView.Direct.RIGHT, "", rightIcon, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LaunchDelegate.toActivity(HybridWebViewActivity.this);
                }
            }).setVisibility(View.VISIBLE);
        } else {
            mNavgationView.setVisibility(View.GONE);
        }
        String url = getUrl();
        if (!TextUtils.isEmpty(url)) {
            loadUrl(url);
        }
    }

    @Override
    public void updateNativeUI(Bitmap bitmap, String title) {
        super.updateNativeUI(bitmap, title);
        if (title == null) {
            title = String.valueOf(getPackageManager().getApplicationLabel(getApplicationInfo()));
        }
        mNavgationView.setTitle(title, "", "", "", null);
    }

    protected String getUrl() {
        Uri data = getIntent().getData();
        String url = null;
        if (null == data) {
            url = getIntent().getStringExtra(HybridConstant.INTENT_EXTRA_KEY_TOPAGE);
        } else {
            url = data.toString();
        }
        return url;
    }

    @Override
    public void onBackPressed() {
        Log.e("vane", "webview cangoback= " + mWebView.canGoBack());
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        super.finish();
        HybridParamAnimation animation = (HybridParamAnimation) getIntent().getSerializableExtra(HybridConstant.INTENT_EXTRA_KEY_ANIMATION);
        if (null == animation || animation.equals(HybridParamAnimation.PUSH)) {
            overridePendingTransition(R.anim.hybrid_left_in, R.anim.hybrid_right_out);
        } else if (animation.equals(HybridParamAnimation.POP)) {
            overridePendingTransition(R.anim.hybrid_right_in, R.anim.hybrid_left_out);
        } else if (animation.equals(HybridParamAnimation.PRESENT)) {
            overridePendingTransition(R.anim.hybrid_top_in, R.anim.hybrid_bottom_out);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Log.e("vane", "HybridWebViewActivity onCreate");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LaunchDelegate.REQ_QR_CODE:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Bundle bundle = data.getExtras();
                        if (bundle == null) {
                            return;
                        }
                        String result = bundle.getString(Constant.CODED_CONTENT);
                        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 返回通知
     *
     * @param msg
     */
    @Subscribe
    public void onEventMainThread(HybridParamBack msg) {
        if (null == msg) return;
        onBackPressed();
    }


    /**
     * 显示、隐藏loading
     *
     * @param msg
     */
    @Subscribe
    public void onEventMainThread(HybridParamShowLoading msg) {
        if (null == msg) return;
        mProgessbar.setVisibility(msg.display ? View.VISIBLE : View.GONE);
    }

    /**
     * 显示、隐藏header通知
     *
     * @param msg
     */
    @Subscribe
    public void onEventMainThread(HybridParamShowHeader msg) {
        if (null == msg) return;
        mNavgationView.setVisibility(msg.display ? View.VISIBLE : View.GONE);
        if (msg.animate)
            mNavgationView.startAnimation(AnimationUtils.loadAnimation(this, msg.display ? R.anim.hybrid_top_in : R.anim.hybrid_top_out));
    }

    /**
     * ajax请求
     *
     * @param msg
     */
    @Subscribe
    public void onEventMainThread(final HybridParamAjax msg) {
        if (TextUtils.isEmpty(msg.url)) return;
        Uri uri = Uri.parse(msg.url);
        HybridAjaxService.IApiService service = HybridAjaxService.getService(uri);
        Set<String> queryParameterNames = uri.getQueryParameterNames();
        HashMap<String, String> map = new HashMap<>();
        if (null != queryParameterNames && !queryParameterNames.isEmpty()) {
            Iterator<String> iterator = queryParameterNames.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                map.put(next, uri.getQueryParameter(next));
            }
        }
        String path = uri.getPath();
        Call<String> call = msg.tagname.equals(HybridParamAjax.ACTION.POST) ?
                service.post(path, map) : service.get(path, map);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (TextUtils.isEmpty(msg.callback)) return;
                HybridParamCallback hybridParamCallback = new HybridParamCallback();
                hybridParamCallback.callback = msg.callback;
                hybridParamCallback.data = response.body();
                handleHybridCallback(hybridParamCallback);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
            }
        });
    }

    /**
     * 更新header通知
     *
     * @param msg
     */
    @Subscribe
    public void onEventMainThread(HybridParamUpdateHeader msg) {
        if (null == msg) return;
        if (msg.id != mWebView.hashCode()) return;
        mNavgationView.cleanNavgation();
        // left
        ArrayList<HybridParamUpdateHeader.NavgationButtonParam> left = msg.left;
        if (null != left && !left.isEmpty()) {
            int size = left.size();
            for (int i = 0; i < size; i++) {
                final HybridParamUpdateHeader.NavgationButtonParam param = left.get(i);
                if (TextUtils.isEmpty(param.icon)) {
                    mNavgationView.appendNavgation(NavgationView.Direct.LEFT, param.value, HybridConfig.IconMapping.mapping(param.tagname), new H5UpdateHeaderClickListener(param));
                } else {
                    mNavgationView.appendNavgation(NavgationView.Direct.LEFT, param.value, param.icon, new H5UpdateHeaderClickListener(param));
                }
            }
        }
        //right
        ArrayList<HybridParamUpdateHeader.NavgationButtonParam> right = msg.right;
        if (null != right && !right.isEmpty()) {
            int size = right.size();
            for (int i = 0; i < size; i++) {
                final HybridParamUpdateHeader.NavgationButtonParam param = right.get(i);
                if (TextUtils.isEmpty(param.icon)) {
                    mNavgationView.appendNavgation(NavgationView.Direct.RIGHT, param.value, HybridConfig.IconMapping.mapping(param.tagname), new H5UpdateHeaderClickListener(param));
                } else {
                    mNavgationView.appendNavgation(NavgationView.Direct.RIGHT, param.value, param.icon, new H5UpdateHeaderClickListener(param));
                }
            }
        }
        //title
        HybridParamUpdateHeader.NavgationTitleParam title = msg.title;
        mNavgationView.setTitle(title.title, title.subtitle, title.lefticon, title.righticon, new H5UpdateHeaderClickListener(title));
    }

    private final class H5UpdateHeaderClickListener implements View.OnClickListener {

        private HybridParamCallback param;

        public H5UpdateHeaderClickListener(HybridParamCallback param) {
            this.param = param;
        }

        @Override
        public void onClick(View v) {
            handleHybridCallback(param);
        }
    }

    private void handleHybridCallback(final HybridParamCallback param) {
        if (isDestroyed()) return;
        if (!TextUtils.isEmpty(param.callback)) {
//            new H5RequestEntity(param.callback, null);
            String script = "Hybrid.callback(" + new H5RequestEntity(param.callback, param.data).toString() + ")";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mWebView.evaluateJavascript(script, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        if (!"true".equals(value) && "back".equals(param.tagname))
                            onBackPressed();
                    }
                });
            } else {
                mWebView.loadUrl("javascript:Hybrid.callback(" + new H5RequestEntity(param.callback, param.data).toString() + ")");
//                String js = "javascript:(function(){var result= Hybrid.callback(" + new H5RequestEntity(param.callback, param.data).toString() + ");window." + HybridConfig.JSInterface + ".stringByEvaluatingJavaScriptFromString(\"" + param.tagname + "\",result);})()";
//                Log.e("vane","js="+js);
//                mWebView.loadUrl(js);
            }

        } else if ("back".equals(param.tagname)) {
            onBackPressed();
        }
    }

    public static final class H5RequestEntity {

        public H5RequestEntity(String callback, String data) {
            this.data = data;
            this.callback = callback;
        }

        public String callback;
        public String data;

        @Override
        public String toString() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("callback", callback);
                jsonObject.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject.toString();
        }
    }
}
