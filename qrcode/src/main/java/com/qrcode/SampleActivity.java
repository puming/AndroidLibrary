package com.qrcode;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.google.zxing.WriterException;
import com.qrcode.android.CaptureActivity;
import com.qrcode.common.Constant;
import com.qrcode.encode.CodeCreator;

/**
 * @author pm
 * @date 2018/11/15
 * @email puming@zdsoft.cn
 */
@Route(path = RouterPath.QRCODE_MAIN_SAMPLEACTIVITY)
public class SampleActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQ_PERM_CAMERA = 0x100;
    private static final int REQ_QR_CODE = 0x200;

    private ImageView mImageViewScan;
    private ImageView mImageViewQrCode;
    private ImageView mImageViewGenQr;
    private ConstraintLayout mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_activity_sample);
        initView();
        ARouter.init(getApplication());
    }

    private void initView() {
        mImageViewScan = (ImageView) findViewById(R.id.iv_scan);
        mImageViewScan.setOnClickListener(this);
        mImageViewQrCode = (ImageView) findViewById(R.id.iv_qr_code);
        mImageViewGenQr = (ImageView) findViewById(R.id.iv_gen_qr);
        mImageViewGenQr.setOnClickListener(this);
        mLayout = (ConstraintLayout) findViewById(R.id.cl);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_scan) {
            scanQrCode();
        } else if (id == R.id.iv_gen_qr) {
            generateQrCode();
        }
    }

    private void generateQrCode() {
        Bitmap bitmap = null;
        try {
            Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.zd);
            bitmap = CodeCreator.createQRCode("二维码", 400, 400, logo);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        if (bitmap != null) {
            mImageViewQrCode.setImageBitmap(bitmap);
        }
    }

    private void scanQrCode() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(SampleActivity.this, new String[]{Manifest.permission.CAMERA}, REQ_PERM_CAMERA);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(SampleActivity.this, CaptureActivity.class);
        startActivityForResult(intent, REQ_QR_CODE);
        ARouter.getInstance().build(RouterPath.QRCODE_MAIN_CAPTUREACTIVITY).navigation(this, REQ_QR_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_QR_CODE:
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERM_CAMERA:
                // 摄像头权限申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    scanQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(SampleActivity.this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }
}
