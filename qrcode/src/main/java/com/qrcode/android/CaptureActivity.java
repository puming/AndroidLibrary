package com.qrcode.android;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.google.zxing.Result;
import com.qrcode.R;
import com.qrcode.RouterPath;
import com.qrcode.bean.ZxingConfig;
import com.qrcode.camera.CameraManager;
import com.qrcode.common.Constant;
import com.qrcode.decode.DecodeImgCallback;
import com.qrcode.decode.DecodeImgThread;
import com.qrcode.decode.ImageUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author pm
 * @date 2018/11/15
 * @email puming@zdsoft.cn
 */
@Route(path = RouterPath.QRCODE_MAIN_CAPTUREACTIVITY)
public class CaptureActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {
    private static final int REQ_PERM_CODE = 0x100;

    private static final String TAG = CaptureActivity.class.getSimpleName();

    public ZxingConfig config;
    private SurfaceView previewView;
    private ViewfinderView viewfinderView;
    private AppCompatImageView flashLightIv;
    private TextView flashLightTv;
    private LinearLayoutCompat flashLightLayout;
    private boolean hasSurface;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private SurfaceHolder surfaceHolder;
    private Toolbar mToolBar;


    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }


    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 保持Activity处于唤醒状态
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /*先获取配置信息*/
        try {
            config = (ZxingConfig) getIntent().getExtras().get(Constant.INTENT_ZXING_CONFIG);
        } catch (Exception e) {
            Log.i("config", e.toString());
        }

        if (config == null) {
            config = new ZxingConfig();
        }
        setContentView(R.layout.qr_activity_capture);
        initView();

        hasSurface = false;

        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        beepManager.setPlayBeep(config.isPlayBeep());
        beepManager.setVibrate(config.isShake());
        ArrayList<String> permissions = new ArrayList<>(2);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            permissions.add(Manifest.permission.CAMERA);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (!permissions.isEmpty()) {
            String[] array = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(CaptureActivity.this, array, REQ_PERM_CODE);
        }

    }


    private void initView() {
        previewView = findViewById(R.id.preview_view);
        previewView.setOnClickListener(this);

        viewfinderView = findViewById(R.id.viewfinder_view);
        viewfinderView.setZxingConfig(config);

        flashLightIv = findViewById(R.id.flashLightIv);
        flashLightTv = findViewById(R.id.flashLightTv);

        flashLightLayout = findViewById(R.id.flashLightLayout);
        flashLightLayout.setOnClickListener(this);


        switchVisibility(flashLightLayout, config.isShowFlashLight());


        /*有闪光灯就显示手电筒按钮  否则不显示*/
        if (isSupportCameraLedFlash(getPackageManager())) {
            flashLightLayout.setVisibility(View.VISIBLE);
        } else {
            flashLightLayout.setVisibility(View.GONE);
        }

        mToolBar = (Toolbar) findViewById(R.id.toolBar);
        //setTitle必须在setSupportActionBar之前设置
//        mToolBar.setTitle("");
        setSupportActionBar(mToolBar);
        //隐藏标题
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolBar.setNavigationIcon(R.drawable.qr_ic_back);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mToolBar.inflateMenu(R.menu.qr_menu_capture);
        mToolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.open_photo) {
                    /*打开相册*/
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, Constant.REQUEST_IMAGE);
                }
                return true;
            }
        });
    }


    /**
     * @param pm
     * @return 是否有闪光灯
     */
    public static boolean isSupportCameraLedFlash(PackageManager pm) {
        if (pm != null) {
            FeatureInfo[] features = pm.getSystemAvailableFeatures();
            if (features != null) {
                for (FeatureInfo f : features) {
                    if (f != null && PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param flashState 切换闪光灯图片
     */
    public void switchFlashImg(int flashState) {

        if (flashState == Constant.FLASH_OPEN) {
            flashLightIv.setImageResource(R.drawable.qr_ic_open);
            flashLightTv.setText("关闭闪光灯");
        } else {
            flashLightIv.setImageResource(R.drawable.qr_ic_close);
            flashLightTv.setText("打开闪光灯");
        }

    }

    /**
     * @param rawResult 返回的扫描结果
     */
    public void handleDecode(Result rawResult) {

        inactivityTimer.onActivity();

        beepManager.playBeepSoundAndVibrate();

        Intent intent = getIntent();
        intent.putExtra(Constant.CODED_CONTENT, rawResult.getText());
        setResult(RESULT_OK, intent);
        this.finish();
    }


    private void switchVisibility(View view, boolean b) {
        if (b) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        cameraManager = new CameraManager(getApplication(), config);

        viewfinderView.setCameraManager(cameraManager);
        handler = null;

        surfaceHolder = previewView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            // 重置callback，等待surfaceCreated()来初始化camera
            surfaceHolder.addCallback(this);
        }

        beepManager.updatePrefs();
        inactivityTimer.onResume();

    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            return;
        }
        try {
            // 打开Camera硬件设备
            cameraManager.openDriver(surfaceHolder);
            // 创建一个handler来打开预览，并抛出一个运行时异常
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("扫一扫");
        builder.setMessage(getString(R.string.qr_msg_camera_framework_bug));
        builder.setPositiveButton(R.string.qr_button_ok, new FinishListener(this));
        builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        beepManager.close();
        cameraManager.closeDriver();

        if (!hasSurface) {

            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        boolean hasPermission = ContextCompat.checkSelfPermission(CaptureActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        if (!hasSurface) {
            hasSurface = true;
            if (hasPermission) {
                initCamera(holder);
            }
        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void onClick(View view) {

        int id = view.getId();
        if (id == R.id.flashLightLayout) {
            /*切换闪光灯*/
            cameraManager.switchFlashLight(handler);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constant.REQUEST_IMAGE && resultCode == RESULT_OK) {
            String path = ImageUtil.getImageAbsolutePath(this, data.getData());

            new DecodeImgThread(path, new DecodeImgCallback() {
                @Override
                public void onImageDecodeSuccess(Result result) {
                    handleDecode(result);
                }

                @Override
                public void onImageDecodeFailed() {
                    Toast.makeText(CaptureActivity.this, "抱歉，解析失败,换个图片试试.", Toast.LENGTH_SHORT).show();
                }
            }).run();


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERM_CODE) {
            boolean allGranted = true;
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                }
            }

            if (allGranted) {
            } else {
                Toast.makeText(this, "请打开相机权限和存取权限", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.qr_menu_capture, menu);
        return true;
    }
}
