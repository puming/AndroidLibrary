package com.pm.mediapicker;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pm.mediapicker.widget.AppBar;
import com.pm.mediapicker.adapter.ImagePreViewAdapter;
import com.pm.mediapicker.data.MediaFile;
import com.pm.mediapicker.manager.ConfigManager;
import com.pm.mediapicker.manager.SelectionManager;
import com.pm.mediapicker.utils.DataUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;

/**
 * @author pm
 * @date 2019/6/20
 * @email puming@zdsoft.cn
 */
public class ImagePreviewActivity extends AppCompatActivity implements View.OnClickListener {
    private AppBar mAppBar;
    private ViewPager mViewPager;
    private ImageView mIvItemCheck;
    private ImageView mIvPlayIcon;
    private LinearLayout mLlPreSelect;

    public static final String IMAGE_POSITION = "imagePosition";
    private List<MediaFile> mMediaFileList;
    private int mPosition = 0;

    private ImagePreViewAdapter mImagePreViewAdapter;
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.bg_color, getTheme()));
        } else {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.bg_color));
        }
        setContentView(R.layout.activity_image_preview);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mAppBar = (AppBar) findViewById(R.id.appBar);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mIvItemCheck = (ImageView) findViewById(R.id.iv_item_check);
        mIvPlayIcon = (ImageView) findViewById(R.id.iv_play_icon);
        mLlPreSelect = (LinearLayout) findViewById(R.id.ll_pre_select);
        mLlPreSelect.setOnClickListener(this);
        mIvPlayIcon.setOnClickListener(this);
    }

    protected void initListener() {
        mAppBar.getAppbarLeftContainer().setOnClickListener(v -> onBackPressed());
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mAppBar.setAppbarTitle(String.format("%d/%d", position + 1, mMediaFileList.size()));
                setIvPlayShow(mMediaFileList.get(position));
                updateSelectButton(mMediaFileList.get(position).getPath());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mLlPreSelect.setOnClickListener(v -> {
            //如果是单类型选取，判断添加类型是否满足（照片视频不能共存）
            if (ConfigManager.getInstance().isSingleType()) {
                ArrayList<String> selectPathList = SelectionManager.getInstance().getSelectPaths();
                if (!selectPathList.isEmpty()) {
                    //判断选中集合中第一项是否为视频
                    if (!SelectionManager.isCanAddSelectionPaths(mMediaFileList.get(mViewPager.getCurrentItem()).getPath(), selectPathList.get(0))) {
                        //类型不同
                        Toast.makeText(ImagePreviewActivity.this, getString(R.string.single_type_choose), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

            boolean addSuccess = SelectionManager.getInstance().addImageToSelectList(mMediaFileList.get(mViewPager.getCurrentItem()).getPath());
            if (addSuccess) {
                updateSelectButton(mMediaFileList.get(mViewPager.getCurrentItem()).getPath());
                updateCommitButton();
            } else {
                Toast.makeText(ImagePreviewActivity.this, String.format(getString(R.string.select_image_max), SelectionManager.getInstance().getMaxCount()), Toast.LENGTH_SHORT).show();
            }
        });

        mAppBar.getAppbarRightContainer().setOnClickListener(v -> {
            setResult(RESULT_OK, new Intent());
            finish();
        });
    }

    protected void initData() {
        mMediaFileList = DataUtil.getInstance().getMediaData();
        mPosition = getIntent().getIntExtra(IMAGE_POSITION, 0);
        mAppBar.setAppbarTitle(String.format("%d/%d", mPosition + 1, mMediaFileList.size()));
        mImagePreViewAdapter = new ImagePreViewAdapter(this, mMediaFileList);
        mViewPager.setAdapter(mImagePreViewAdapter);
        mViewPager.setCurrentItem(mPosition);
        //更新当前页面状态
        setIvPlayShow(mMediaFileList.get(mPosition));
        updateSelectButton(mMediaFileList.get(mPosition).getPath());
        updateCommitButton();
    }

    /**
     * 更新确认按钮状态
     */
    private void updateCommitButton() {

        int maxCount = SelectionManager.getInstance().getMaxCount();

        //改变确定按钮UI
        int selectCount = SelectionManager.getInstance().getSelectPaths().size();
        if (selectCount == 0) {
            mAppBar.getAppbarRightContainer().setEnabled(false);
            mAppBar.setAppbarMenuText(getString(R.string.confirm));
            return;
        }
        if (selectCount < maxCount) {
            mAppBar.getAppbarRightContainer().setEnabled(true);
            mAppBar.setAppbarMenuText(String.format(getString(R.string.confirm_msg), selectCount, maxCount));
            return;
        }
        if (selectCount == maxCount) {
            mAppBar.getAppbarRightContainer().setEnabled(true);
            mAppBar.setAppbarMenuText(String.format(getString(R.string.confirm_msg), selectCount, maxCount));
            return;
        }
    }

    /**
     * 更新选择按钮状态
     */
    private void updateSelectButton(String imagePath) {
        boolean isSelect = SelectionManager.getInstance().isImageSelect(imagePath);
        if (isSelect) {
            mIvItemCheck.setImageDrawable(getResources().getDrawable(R.drawable.ic_checkbox_checked));
        } else {
            mIvItemCheck.setImageDrawable(getResources().getDrawable(R.drawable.ic_checkbox_norm));
        }
    }

    /**
     * 设置是否显示视频播放按钮
     *
     * @param mediaFile
     */
    private void setIvPlayShow(MediaFile mediaFile) {
        if (mediaFile.getDuration() > 0) {
            mIvPlayIcon.setVisibility(View.VISIBLE);
        } else {
            mIvPlayIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ll_pre_select) {
            //如果是单类型选取，判断添加类型是否满足（照片视频不能共存）
            if (ConfigManager.getInstance().isSingleType()) {
                ArrayList<String> selectPathList = SelectionManager.getInstance().getSelectPaths();
                if (!selectPathList.isEmpty()) {
                    //判断选中集合中第一项是否为视频
                    if (!SelectionManager.isCanAddSelectionPaths(mMediaFileList.get(mViewPager.getCurrentItem()).getPath(), selectPathList.get(0))) {
                        //类型不同
                        Toast.makeText(ImagePreviewActivity.this, getString(R.string.single_type_choose), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

            boolean addSuccess = SelectionManager.getInstance().addImageToSelectList(mMediaFileList.get(mViewPager.getCurrentItem()).getPath());
            if (addSuccess) {
                updateSelectButton(mMediaFileList.get(mViewPager.getCurrentItem()).getPath());
                updateCommitButton();
            } else {
                Toast.makeText(ImagePreviewActivity.this, String.format(getString(R.string.select_image_max), SelectionManager.getInstance().getMaxCount()), Toast.LENGTH_SHORT).show();
            }

        } else if (i == R.id.iv_play_icon) {
            //实现播放视频的跳转逻辑(调用原生视频播放器)
            Intent intent = new Intent(Intent.ACTION_VIEW);
            File file = new File(mMediaFileList.get(mViewPager.getCurrentItem()).getPath());
            Uri uri = FileProvider.getUriForFile(ImagePreviewActivity.this, PickerFileProvider.getFileProviderName(ImagePreviewActivity.this), file);
            intent.setDataAndType(uri, "video/*");
            //给所有符合跳转条件的应用授权
            List<ResolveInfo> resInfoList = getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            startActivity(intent);

        } else {
        }
    }
}
