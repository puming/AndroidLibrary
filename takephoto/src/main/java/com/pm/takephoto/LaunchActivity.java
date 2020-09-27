package com.pm.takephoto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author pm
 * @date 2019/6/25
 * @email puming@zdsoft.cn
 */
public class LaunchActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 手持身份证
     */
    private Button mTvShouchi;
    /**
     * 身份证正面
     */
    private Button mTvFront;
    /**
     * 身份证反面
     */
    private Button mTvBack;
    /**
     * 银行卡
     */
    private Button mTvBank;
    /**
     * 通用模式
     */
    private Button mTvNone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        initView();
    }

    private void initView() {
        mTvShouchi = (Button) findViewById(R.id.tvShouchi);
        mTvShouchi.setOnClickListener(this);
        mTvFront = (Button) findViewById(R.id.tvFront);
        mTvFront.setOnClickListener(this);
        mTvBack = (Button) findViewById(R.id.tvBack);
        mTvBack.setOnClickListener(this);
        mTvBank = (Button) findViewById(R.id.tvBank);
        mTvBank.setOnClickListener(this);
        mTvNone = (Button) findViewById(R.id.tvNone);
        mTvNone.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, TakePhotoActivity.class);
        int i = v.getId();
        if (i == R.id.tvShouchi) {
            intent.putExtra(TakePhotoActivity.KEY_CONTENT_TYPE, TakePhotoActivity.CONTENT_TYPE_HANDHELD);
            intent.putExtra(TakePhotoActivity.KEY_OUTPUT_FILE_PATH, PictureFileProvider.getAbsoluteImagePath());
            startActivityForResult(intent, 1000);

        } else if (i == R.id.tvFront) {
            intent.putExtra(TakePhotoActivity.KEY_CONTENT_TYPE, TakePhotoActivity.CONTENT_TYPE_ID_CARD_FRONT);
            intent.putExtra(TakePhotoActivity.KEY_OUTPUT_FILE_PATH, PictureFileProvider.getAbsoluteImagePath());
            startActivityForResult(intent, 1001);

        } else if (i == R.id.tvBack) {
            intent.putExtra(TakePhotoActivity.KEY_CONTENT_TYPE, TakePhotoActivity.CONTENT_TYPE_ID_CARD_BACK);
            intent.putExtra(TakePhotoActivity.KEY_OUTPUT_FILE_PATH, PictureFileProvider.getAbsoluteImagePath());
            startActivityForResult(intent, 1002);

        } else if (i == R.id.tvBank) {
            intent.putExtra(TakePhotoActivity.KEY_CONTENT_TYPE, TakePhotoActivity.CONTENT_TYPE_BANK_CARD);
            intent.putExtra(TakePhotoActivity.KEY_OUTPUT_FILE_PATH, PictureFileProvider.getAbsoluteImagePath());
            startActivityForResult(intent, 1003);

        } else if (i == R.id.tvNone) {
            intent.putExtra(TakePhotoActivity.KEY_CONTENT_TYPE, TakePhotoActivity.CONTENT_TYPE_GENERAL);
            intent.putExtra(TakePhotoActivity.KEY_OUTPUT_FILE_PATH, PictureFileProvider.getAbsoluteImagePath());
            startActivityForResult(intent, 1004);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
