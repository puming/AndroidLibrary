package com.qrcode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.qrcode.android.CaptureActivity;

/**
 * @author pm
 * @date 2019/8/12
 * @email puming@zdsoft.cn
 */
public class LaunchDelegate {
    public static final int REQ_QR_CODE = 0x200;

    public static void toActivity(Activity activity) {
        activity.startActivityForResult(new Intent(activity, CaptureActivity.class),REQ_QR_CODE);
    }

    public static void toSampleActivity(Context context) {
        context.startActivity(new Intent(context, SampleActivity.class));
    }
}
