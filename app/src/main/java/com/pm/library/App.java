package com.pm.library;

import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.util.Log;

/**
 * Created by puming on 2016/11/29.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        int pid = Process.myPid();
        Log.d("App", "onCreate: pid="+pid);
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }
}
