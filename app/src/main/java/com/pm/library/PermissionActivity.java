package com.pm.library;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.library.base.PermissionManger;

public class PermissionActivity extends AppCompatActivity{

    private static final String TAG = "PermissionActivity";
    private PermissionManger mPermissionManger;

    PermissionManger.PermissionListener mListener=new PermissionManger.PermissionListener() {
        @Override
        public void doCalendar() {

        }

        @Override
        public void doCamera() {

        }

        @Override
        public void doContacts() {

        }

        @Override
        public void doLocation() {

        }

        @Override
        public void doMicrophone() {

        }

        @Override
        public void doPhone() {
            todoPhone();
        }

        @Override
        public void doSensors() {

        }

        @Override
        public void doSMS() {

        }

        @Override
        public void doStorage() {
            Log.d(TAG, "doStorage: ");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        Log.d(TAG, "onCreate: ");
        mPermissionManger = PermissionManger.getInstance(mListener);
        mPermissionManger.registerActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPermissionManger.unRegisterActivity();
    }

    public void phone(View view) {
        int requestResult = mPermissionManger.requestPermission(PermissionManger.CALL_PHONE, Manifest.permission.CALL_PHONE);
        if (requestResult==PermissionManger.HAS_PERMISSION) {
            mListener.doPhone();
        }else {
            // TODO: 2017/2/23
            Log.d(TAG, "phone: false");
        }
    }

    public void Reade(View view) {

    }

    public void Multiple(View view) {
        int requestResult = mPermissionManger.requestPermission(PermissionManger.MULTIPLE_PERMISSION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CALL_PHONE);
        if(requestResult==PermissionManger.HAS_PERMISSION){
            mListener.doStorage();
            mListener.doPhone();
        }else {
            // TODO: 2017/2/23
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionManger.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }
    
    public void todoPhone() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:"+"10086"));
        startActivity(intent);
    }


}
