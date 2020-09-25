package com.library.base;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;

/**
 * Created by puming on 2017/2/22.
 */

public class PermissionManger {
    private static final String TAG = "PermissionManger";
    private volatile static PermissionManger sPermissionManger;

    private PermissionListener mListener;
    private Activity mActivity;
    private boolean mIsShowSettingsDialog = false;

    public static final int HAS_PERMISSION = 2;

    /**
     * 申请多个所有危险权限
     */
    public static final int MULTIPLE_PERMISSION = 0x00;

    /**
     * group:android.permission-group.CONTACTS
     */
    public static final int WRITE_CONTACTS = 0x01;
    public static final int GET_ACCOUNTS = 0x02;
    public static final int READ_CONTACTS = 0x03;

    /**
     * group:android.permission-group.PHONE
     */
    public static final int READ_CALL_LOG = 0x04;
    public static final int READ_PHONE_STATE = 0x05;
    public static final int CALL_PHONE = 0x06;
    public static final int WRITE_CALL_LOG = 0x07;
    public static final int USE_SIP = 0x08;
    public static final int PROCESS_OUTGOING_CALL = 0x09;
    public static final int ADD_VOICEMAIL = 0x010;

    /**
     * group:android.permission-group.CALENDAR
     */
    public static final int READ_CALENDAR = 0x011;
    public static final int WRITE_CALENDAR = 0x012;

    /**
     * group:android.permission-group.CAMERA
     */
    public static final int CAMERA = 0x013;

    /**
     * group:android.permission-group.SENSORS
     */
    public static final int BODY_SENSORS = 0x014;

    /**
     * group:android.permission-group.LOCATION
     */
    public static final int ACCESS_FINE_LOCATION = 0x015;
    public static final int ACCESS_COARSE_LOCATIO = 0x016;

    /**
     * group:android.permission-group.STORAGE
     */
    public static final int READ_EXTERNAL_STORAGE = 0x017;
    public static final int WRITE_EXTERNAL_STORAG = 0x018;

    /**
     * group:android.permission-group.MICROPHONE
     */
    public static final int RECORD_AUDIO = 0x019;

    /**
     * group:android.permission-group.SMS
     */
    public static final int READ_SMS = 0x020;
    public static final int RECEIVE_WAP_PUSH = 0x021;
    public static final int RECEIVE_MMS = 0x022;
    public static final int RECEIVE_SMS = 0x023;
    public static final int SEND_SMS = 0x024;
    public static final int READ_CELL_BROADCASTS = 0x025;

    private PermissionManger(PermissionListener listener) {
        this.mListener = listener;
    }

    public static PermissionManger getInstance(@NonNull PermissionListener listener) {
        if (sPermissionManger == null) {
            synchronized (PermissionManger.class) {
                if (sPermissionManger == null) {
                    sPermissionManger = new PermissionManger(listener);
                }
            }
        }
        return sPermissionManger;
    }

    public void registerActivity(Activity activity) {
        this.mActivity = activity;
    }

    public void unRegisterActivity() {
        mActivity = null;
    }

    /**
     * 权限检查方法
     *
     * @param permissions
     * @return
     */
    private boolean hasPermission(String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(mActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 申请权限
     *
     * @param requestCode
     * @param permissions
     * @return
     */
    public int requestPermission(int requestCode, String... permissions) {
        if (hasPermission(permissions)) {
            mIsShowSettingsDialog = false;
            return HAS_PERMISSION;
        } else {
            boolean shouldShow = shouldShow(requestCode, permissions);
            if (!shouldShow) {
                ActivityCompat.requestPermissions(mActivity, permissions, requestCode);
            }
        }
        return -1;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAG:
                //读数据
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: permissions.length=" + permissions.length);
                    boolean isOk = permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE ||
                            permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE;
                    if (permissions.length == 1 && isOk) {
                        mListener.doStorage();
                    } else {
                        // TODO: 2017/2/22 申请权限错误
                    }
                } else {
                    showSettingDialog();
                }
                break;
            case CALL_PHONE:
                Log.d(TAG, "onRequestPermissionsResult: CALL_PHONE");
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (permissions[0] == Manifest.permission.CALL_PHONE) {
                        mListener.doPhone();
                    } else {
                        // TODO: 2017/2/22 申请权限错误
                    }
                } else {
                    // TODO: 2017/2/22 用户没有授权
                    showSettingDialog();
                }
                break;
            case MULTIPLE_PERMISSION:
                toDo(permissions, grantResults);
                break;
        }
    }

    private void toDo(@NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean isRejected = false;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

            } else {
                // TODO: 2017/2/22
                isRejected = true;
                break;
            }
        }

        if (isRejected) {
            showSettingDialog();
        } else {
            for (int i = 0; i < grantResults.length; i++) {
                switch (permissions[i]) {
                    case Manifest.permission.CALL_PHONE:
                        mListener.doPhone();
                        continue;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        mListener.doStorage();
                        continue;
                    case Manifest.permission.CAMERA:
                        mListener.doCamera();
                        continue;
                }
            }
        }
    }

    private boolean shouldShow(final int requestCode, final String... permissions) {
        SharedPreferences sharedPreferences = mActivity.getSharedPreferences("isRejected", Context.MODE_PRIVATE);
        boolean shouldShowSnackbar = false;
        for (int i = 0; i < permissions.length; i++) {
            boolean shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permissions[i]);
            Log.d(TAG, "shouldShow: shouldShowRationale=" + shouldShowRationale);
            if (shouldShowRationale) {
                //第一次申请权限被拒绝了，需要向用户阐述权限的用途
                shouldShowSnackbar = true;
                break;
            } else {
                shouldShowSnackbar = false;
            }
        }

        if (shouldShowSnackbar) {
            sharedPreferences.edit().putBoolean("isRejected", true).commit();
            Log.d(TAG, "shouldShow: showSnackbar");
            ActivityCompat.requestPermissions(mActivity, permissions, requestCode);
            return true;
        } else {
            //1.拒绝并要求不在提示 2.
            if (sharedPreferences.getBoolean("isRejected", false)) {
                mIsShowSettingsDialog = true;
            }
            return false;
        }
    }

    private void showSnackbar(final int requestCode, final String... permissions) {
        Snackbar.make(new View(mActivity), "这些权限是APP所必须权限",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(mActivity, permissions, requestCode);
                    }
                })
                .show();
//        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
//        builder.setMessage("这些权限是APP所必须权限").create().show();
    }

    public void showSettingDialog() {
        boolean shouldShowDialog = mIsShowSettingsDialog;
        if (!shouldShowDialog) {
            Log.d(TAG, "should not showSettingDialog: ");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setMessage("没有权限，不能正常使用程序，请前往设置授权");
        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.Settings");
                intent.setComponent(componentName);
                mActivity.startActivity(intent);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                mActivity.finish();
            }
        });
        builder.create().show();
    }

    public interface PermissionListener {
        /**
         * 拥有读写日历权限后需要执行的逻辑
         * <p>
         * 相关权限：
         * permission:android.permission.READ_CALENDAR
         * permission:android.permission.WRITE_CALENDAR
         */
        public void doCalendar();

        /**
         * 拥有调用系统相机权限后需要执行的逻辑
         * <p>
         * 相关权限：
         * permission:android.permission.CAMERA
         */
        public void doCamera();

        /**
         * 拥有读取联系人信息权限后需要执行的逻辑
         * <p>
         * 相关权限：
         * permission:android.permission.WRITE_CONTACTS
         * permission:android.permission.GET_ACCOUNTS
         * permission:android.permission.READ_CONTACTS
         */
        public void doContacts();

        /**
         * 拥有定位功能权限后需要执行的逻辑
         * <p>
         * 相关权限：
         * permission:android.permission.ACCESS_FINE_LOCATION
         * permission:android.permission.ACCESS_COARSE_LOCATIO
         */
        public void doLocation();

        /**
         * 拥有录音功能权限后需要执行的逻辑
         * <p>
         * 相关权限：
         * permission:android.permission.RECORD_AUDIO
         */
        public void doMicrophone();

        /**
         * 拥有调用系统电话功能相关权限后需要执行的逻辑
         * <p>
         * 相关权限：
         * permission:android.permission.READ_CALL_LOG
         * permission:android.permission.READ_PHONE_STATE
         * permission:android.permission.CALL_PHONE
         * permission:android.permission.WRITE_CALL_LOG
         * permission:android.permission.USE_SIP
         * permission:android.permission.PROCESS_OUTGOING_CALL
         * permission:com.android.voicemail.permission.ADD_VOI
         */
        public void doPhone();

        /**
         * 拥有传感器权限后需要执行的逻辑
         * <p>
         * 相关权限：
         * permission:android.permission.BODY_SENSORS
         */
        public void doSensors();

        /**
         * 拥有读取系统短信相关权限后需要执行的逻辑
         * <p>
         * 相关权限：
         * permission:android.permission.READ_SMS
         * permission:android.permission.RECEIVE_WAP_PUSH
         * permission:android.permission.RECEIVE_MMS
         * permission:android.permission.RECEIVE_SMS
         * permission:android.permission.SEND_SMS
         * permission:android.permission.READ_CELL_BROADCASTS
         */
        public void doSMS();

        /**
         * 拥有内部存储权限后需要执行的逻辑
         * <p>
         * 相关权限：
         * permission:android.permission.READ_EXTERNAL_STORAGE
         * permission:android.permission.WRITE_EXTERNAL_STORAG
         */
        public void doStorage();
    }
}
