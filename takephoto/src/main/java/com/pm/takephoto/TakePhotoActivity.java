package com.pm.takephoto;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.RenderScript;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.pm.takephoto.widget.MaskView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

/**
 * @author pm
 * @date 2019/6/24
 * @email puming@zdsoft.cn
 */
public class TakePhotoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "TakePhotoActivity";
    public static final String KEY_RESULT_FILE_PATH = "result";
    public static final String KEY_OUTPUT_FILE_PATH = "outputFilePath";
    public static final String KEY_CONTENT_TYPE = "contentType";

    public static final String CONTENT_TYPE_GENERAL = "general";
    public static final String CONTENT_TYPE_ID_CARD_FRONT = "IDCardFront";
    public static final String CONTENT_TYPE_ID_CARD_BACK = "IDCardBack";
    public static final String CONTENT_TYPE_HANDHELD = "handheld";
    public static final String CONTENT_TYPE_BANK_CARD = "bankCard";

    private static final int PERMISSIONS_REQUEST_CAMERA = 800;
    private static final int PERMISSIONS_EXTERNAL_STORAGE = 801;
    private CameraView mCameraView;
    private ImageView mTakePhotoButton;
    private ConstraintLayout mTakePictureContainer;
    private AppCompatImageView mDisplayImageView;
    private ImageView mCancelButton;
    private ImageView mConfirmButton;
    private ConstraintLayout mConfirmResultContainer;

    private File outputFile;
    private String contentType;
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_take_photo);
        initView();
        mCameraView.getCameraControl().setPermissionCallback(new PermissionCallback() {
            @Override
            public boolean onRequestPermission() {
                ActivityCompat.requestPermissions(TakePhotoActivity.this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_CAMERA);
                return false;
            }
        });
        initParams();
        setOrientation(getResources().getConfiguration());
    }

    private void initView() {
        mCameraView = (CameraView) findViewById(R.id.cameraView);
        mTakePhotoButton = (ImageView) findViewById(R.id.take_photo_button);
        mTakePhotoButton.setOnClickListener(this);
        mTakePictureContainer = (ConstraintLayout) findViewById(R.id.take_picture_container);
        mDisplayImageView = (AppCompatImageView) findViewById(R.id.display_image_view);
        mCancelButton = (ImageView) findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(this);
        mConfirmButton = (ImageView) findViewById(R.id.confirm_button);
        mConfirmButton.setOnClickListener(this);
        mConfirmResultContainer = (ConstraintLayout) findViewById(R.id.confirm_result_container);
    }

    private void initParams() {
        String outputPath = getIntent().getStringExtra(KEY_OUTPUT_FILE_PATH);
        if (outputPath != null) {
            outputFile = new File(outputPath);
        } else {
            outputFile = new File(PictureFileProvider.getAbsoluteImagePath());
        }
        contentType = getIntent().getStringExtra(KEY_CONTENT_TYPE);
        if (contentType == null) {
            contentType = CONTENT_TYPE_GENERAL;
        }
        int maskType;
        switch (contentType) {
            case CONTENT_TYPE_ID_CARD_FRONT:
                maskType = MaskView.MASK_TYPE_ID_CARD_FRONT;
//                overlayView.setVisibility(View.INVISIBLE);
                break;
            case CONTENT_TYPE_ID_CARD_BACK:
                maskType = MaskView.MASK_TYPE_ID_CARD_BACK;
//                overlayView.setVisibility(View.INVISIBLE);
                break;
            case CONTENT_TYPE_BANK_CARD:
                maskType = MaskView.MASK_TYPE_BANK_CARD;
//                overlayView.setVisibility(View.INVISIBLE);
                break;
            case CONTENT_TYPE_HANDHELD:
                maskType = MaskView.MASK_TYPE_HANDHELD;
                break;
            case CONTENT_TYPE_GENERAL:
            default:
                maskType = MaskView.MASK_TYPE_NONE;
//                cropMaskView.setVisibility(View.INVISIBLE);
                break;
        }
        mCameraView.setMaskType(maskType);
//        cropMaskView.setMaskType(maskType);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCameraView.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCameraView.stop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCameraView.getCameraControl().refreshPermission();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.camera_permission_required, Toast.LENGTH_LONG)
                            .show();
                }
                break;
            }
            case PERMISSIONS_EXTERNAL_STORAGE:
            default:
                break;
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setOrientation(newConfig);
    }

    private void setOrientation(Configuration newConfig) {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int orientation;
        int cameraViewOrientation = CameraView.ORIENTATION_PORTRAIT;
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                cameraViewOrientation = CameraView.ORIENTATION_PORTRAIT;
//                orientation = OCRCameraLayout.ORIENTATION_PORTRAIT;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
//                orientation = OCRCameraLayout.ORIENTATION_HORIZONTAL;
                if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
                    cameraViewOrientation = CameraView.ORIENTATION_HORIZONTAL;
                } else {
                    cameraViewOrientation = CameraView.ORIENTATION_INVERT;
                }
                break;
            default:
//                orientation = OCRCameraLayout.ORIENTATION_PORTRAIT;
                mCameraView.setOrientation(CameraView.ORIENTATION_PORTRAIT);
                break;
        }
//        takePictureContainer.setOrientation(orientation);
        mCameraView.setOrientation(cameraViewOrientation);
//        cropContainer.setOrientation(orientation);
//        confirmResultContainer.setOrientation(orientation);
    }

    private void showTakePicture() {
        mCameraView.getCameraControl().resume();
//        updateFlashMode();
        mTakePictureContainer.setVisibility(View.VISIBLE);
        mConfirmResultContainer.setVisibility(View.INVISIBLE);
//        cropContainer.setVisibility(View.INVISIBLE);
    }

    private void showCrop() {
        mCameraView.getCameraControl().pause();
//        updateFlashMode();
        mTakePictureContainer.setVisibility(View.INVISIBLE);
        mConfirmResultContainer.setVisibility(View.INVISIBLE);
//        cropContainer.setVisibility(View.VISIBLE);
    }

    private void showResultConfirm() {
        mCameraView.getCameraControl().pause();
//        updateFlashMode();
        mTakePictureContainer.setVisibility(View.INVISIBLE);
        mConfirmResultContainer.setVisibility(View.VISIBLE);
//        cropContainer.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.take_photo_button) {
            mCameraView.takePicture(outputFile, new CameraView.OnTakePictureCallback() {
                @Override
                public void onPictureTaken(final Bitmap bitmap) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: bitmap=" + bitmap);
//                            Glide.with(TakePhotoActivity.this).asBitmap().load(bitmap).into(mDisplayImageView);
                            /*Canvas canvas = new Canvas(bitmap);
                            Paint paint = new Paint();
                            paint.setShader(new BitmapShader(bitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
                            paint.setAntiAlias(true);
                            RectF rectF = new RectF(0f, 0f, bitmap.getWidth(), bitmap.getHeight());
                            canvas.drawRoundRect(rectF, 10, 10, paint);*/
                            int width = mCameraView.getMaskRect().right- mCameraView.getMaskRect().left;
                            int height = mCameraView.getMaskRect().bottom- mCameraView.getMaskRect().top;
                            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) mDisplayImageView.getLayoutParams();
                            layoutParams.width = width;
                            layoutParams.height = height;
                            mDisplayImageView.requestLayout();
                            mDisplayImageView.setImageBitmap(bitmap);
                            showResultConfirm();
                        }
                    });
                }
            });
        } else if (i == R.id.cancel_button) {
            mDisplayImageView.setImageBitmap(null);
            showTakePicture();

        } else if (i == R.id.confirm_button) {
            doConfirmResult();
        }
    }

    private void doConfirmResult() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Log.d(TAG, "run: outputFile="+outputFile);
                    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                    Bitmap bitmap = ((BitmapDrawable) mDisplayImageView.getDrawable()).getBitmap();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent();
                intent.putExtra(TakePhotoActivity.KEY_RESULT_FILE_PATH, outputFile.getAbsolutePath());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }.start();
    }
}
