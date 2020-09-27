package com.pm.mediapicker;


import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pm.mediapicker.widget.AppBar;
import com.pm.mediapicker.adapter.MediaPickerAdapter;
import com.pm.mediapicker.data.MediaFile;
import com.pm.mediapicker.data.MediaFolder;
import com.pm.mediapicker.loader.LoaderDelegate;
import com.pm.mediapicker.loader.Scanner;
import com.pm.mediapicker.manager.ConfigManager;
import com.pm.mediapicker.manager.SelectionManager;
import com.pm.mediapicker.utils.DataUtil;
import com.pm.mediapicker.utils.GlideLoader;
import com.pm.mediapicker.utils.MediaFileUtil;
import com.pm.mediapicker.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author pm
 * @date 2019/6/13
 * @email puming@zdsoft.cn
 */
public class MediaPickerActivity extends AppCompatActivity implements View.OnClickListener, MediaPickerAdapter.OnItemClickListener {
    private static final String TAG = "MediaPickerActivity";

    /**
     * 启动参数
     */
    private String mTitle;
    private boolean isShowCamera;
    private boolean isShowImage;
    private boolean isShowVideo;
    private boolean isSingleType;
    private int mMaxCount;
    private List<String> mImagePaths;

    private AlbumPopupWindow mImageFolderPopupWindow;
    private AppBar mAppbar;
    private View mDivider;
    private RecyclerView mRecyclerView;
    /**
     * 预览
     */
    private Button mBtnPreview;
    /**
     * 上传
     */
    private Button mBtnUpload;
    private FrameLayout mCardView;
    /**
     * 全部
     */
    private TextView mTvPickerFolders;

    private String mFolderName;

    //表示屏幕亮暗
    private static final int LIGHT_OFF = 0;
    private static final int LIGHT_ON = 1;
    private LoaderDelegate mLoaderDelegate = new LoaderDelegate();
    //图片数据源
    private List<MediaFile> mMediaFileList;
    //文件夹数据源
    private List<MediaFolder> mMediaFolderList;
    private GridLayoutManager mGridLayoutManager;
    private MediaPickerAdapter mImagePickerAdapter;
    private static final int REQUEST_SELECT_IMAGES_CODE = 0x11;
    private static final int REQUEST_CODE_CAPTURE = 0x22;
    private MediaScannerConnection mScannerConnection;
    private TextView mTvImageTime;
    private boolean isShowTime;
    private Handler mHandler = new Handler();
    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideImageTime();
        }
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.bg_color, getTheme()));
        } else {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.bg_color));
        }
        setContentView(R.layout.activity_media_picker);
        initConfig();
        initView();
        mLoaderDelegate.create(this);
        ConfigManager.getInstance().setImageLoader(new GlideLoader());
        //列表相关
        setupAdapter();
    }

    private void setupAdapter() {
        mGridLayoutManager = new GridLayoutManager(this, 4);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        //注释说当知道Adapter内Item的改变不会影响RecyclerView宽高的时候，可以设置为true让RecyclerView避免重新计算大小。
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(60);

        mMediaFileList = new ArrayList<>();
        mImagePickerAdapter = new MediaPickerAdapter(this, mMediaFileList);
        mImagePickerAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mImagePickerAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                updateImageTime();
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                updateImageTime();
            }
        });
    }

    /**
     * 初始化配置
     */
    protected void initConfig() {
        mFolderName = getString(R.string.all_media);
        mTitle = ConfigManager.getInstance().getTitle();
        isShowCamera = ConfigManager.getInstance().isShowCamera();
        isShowImage = ConfigManager.getInstance().isShowImage();
        isShowVideo = ConfigManager.getInstance().isShowVideo();
        isSingleType = ConfigManager.getInstance().isSingleType();
        mMaxCount = ConfigManager.getInstance().getMaxCount();
        SelectionManager.getInstance().setMaxCount(mMaxCount);

        //载入历史选择记录
        mImagePaths = ConfigManager.getInstance().getImagePaths();
        if (mImagePaths != null && !mImagePaths.isEmpty()) {
            SelectionManager.getInstance().addImagePathsToSelectList(mImagePaths);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLoaderDelegate.startLoad(new Scanner.Result<List<MediaFolder>>() {
            @Override
            public void onValue(List<MediaFolder> mediaFolders) {
                //默认加载全部照片
                mMediaFileList.addAll(mediaFolders.get(0).getMediaFileList());
                mImagePickerAdapter.notifyDataSetChanged();
                //图片文件夹数据
                mMediaFolderList = new ArrayList<>(mediaFolders);
                setupPopupWindow(mediaFolders);
            }

            @Override
            public void onError() {

            }
        }, LoaderDelegate.Source.ALL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mImagePickerAdapter.notifyDataSetChanged();
        updateCommitButton();
    }

    private void setupPopupWindow(List<MediaFolder> folders) {
        ArrayList<MediaFolder> mMediaFolderList = new ArrayList<>(12);
        mImageFolderPopupWindow = new AlbumPopupWindow(MediaPickerActivity.this, folders);
        mImageFolderPopupWindow.setAnimationStyle(R.style.imageFolderAnimator);
        mImageFolderPopupWindow.getAdapter().setOnImageFolderChangeListener(this::imageFolderChange);
        mImageFolderPopupWindow.setOnDismissListener(() -> setLightMode(LIGHT_ON));
    }

    private void imageFolderChange(View view, int position) {
        MediaFolder mediaFolder = mMediaFolderList.get(position);
        //更新当前文件夹名
        String folderName = mediaFolder.getFolderName();
        if (!TextUtils.isEmpty(folderName)) {
            mAppbar.setAppbarTitle(folderName);
            mTvPickerFolders.setText(folderName);
        }
        //更新图片列表数据源
        mMediaFileList.clear();
        mMediaFileList.addAll(mediaFolder.getMediaFileList());
        mImagePickerAdapter.notifyDataSetChanged();

        mImageFolderPopupWindow.dismiss();
    }

    /**
     * 设置屏幕的亮度模式
     *
     * @param lightMode
     */
    private void setLightMode(int lightMode) {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        switch (lightMode) {
            case LIGHT_OFF:
                layoutParams.alpha = 0.7f;
                break;
            case LIGHT_ON:
                layoutParams.alpha = 1.0f;
                break;
            default:
                break;
        }
        getWindow().setAttributes(layoutParams);
    }

    /**
     * 隐藏时间
     */
    private void hideImageTime() {
        if (isShowTime) {
            isShowTime = false;
            ObjectAnimator.ofFloat(mTvImageTime, "alpha", 1, 0).setDuration(300).start();
        }
    }

    /**
     * 显示时间
     */
    private void showImageTime() {
        if (!isShowTime) {
            isShowTime = true;
            ObjectAnimator.ofFloat(mTvImageTime, "alpha", 0, 1).setDuration(300).start();
        }
    }

    /**
     * 更新时间
     */
    private void updateImageTime() {
        int position = mGridLayoutManager.findFirstVisibleItemPosition();
        MediaFile mediaFile = mImagePickerAdapter.getMediaFile(position);
        if (mediaFile != null) {
            if (mTvImageTime.getVisibility() != View.VISIBLE) {
                mTvImageTime.setVisibility(View.VISIBLE);
            }
            String time = Utils.getImageTime(mediaFile.getDateToken());
            mTvImageTime.setText(time);
            showImageTime();
            mHandler.removeCallbacks(mHideRunnable);
            mHandler.postDelayed(mHideRunnable, 1500);
        }
    }

    private void initView() {
        mAppbar = (AppBar) findViewById(R.id.appbar);
        mDivider = (View) findViewById(R.id.divider);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mTvImageTime = findViewById(R.id.tv_image_time);
        mBtnPreview = (Button) findViewById(R.id.btn_preview);
        mBtnPreview.setOnClickListener(this);
        mBtnUpload = (Button) findViewById(R.id.btn_preview);
        mBtnUpload.setOnClickListener(this);
        mCardView = (FrameLayout) findViewById(R.id.cardView);
        mTvPickerFolders = (TextView) findViewById(R.id.tv_picker_folders);
        mTvPickerFolders.setOnClickListener(this);
        mCardView.setOnClickListener(this);

        mTvPickerFolders.setText(mFolderName);
        //appbar
        mAppbar.setAppbarTitle(mFolderName);
        mAppbar.setAppbarMenuText("上传");
        mAppbar.getAppbarLeftContainer().setOnClickListener(v -> onBackPressed());
        mAppbar.getAppbarRightContainer().setOnClickListener(v -> commitSelection());
//        mAppbar.getAppbarRightContainer().getChildAt(0).setPadding(10,10,10,10);
//        mAppbar.getAppbarRightContainer().getChildAt(0).setBackgroundResource(R.drawable.folder_change_btn_shape);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_preview) {
            DataUtil.getInstance().setMediaData(SelectionManager.getInstance().getSelectMdedias());
            Intent intent = new Intent(this, ImagePreviewActivity.class);
            intent.putExtra(ImagePreviewActivity.IMAGE_POSITION, 0);
            startActivityForResult(intent, REQUEST_SELECT_IMAGES_CODE);

        } else if (i == R.id.tv_picker_folders) {
            if (mImageFolderPopupWindow != null) {
                setLightMode(LIGHT_OFF);
                mImageFolderPopupWindow.showAsDropDown(mCardView, 0, 0);
            }

        } else {
        }
    }

    /**
     * 选择图片完毕，返回
     */
    private void commitSelection() {
        ArrayList<String> list = new ArrayList<>(SelectionManager.getInstance().getSelectPaths());
        Intent intent = new Intent();
        intent.putStringArrayListExtra(MediaPicker.EXTRA_SELECT_IMAGES, list);
        setResult(RESULT_OK, intent);
        SelectionManager.getInstance().removeAll();//清空选中记录
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SELECT_IMAGES_CODE:
                    commitSelection();
                    break;
                case REQUEST_CODE_CAPTURE:
                    //通知媒体库刷新
                    /*Uri uri;
                    File fileToUri = new File(ImagePickerProvider.getAbsoluteImagePath());
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                        uri = FileProvider.getUriForFile(this, ImagePickerProvider.getFileProviderName(this), fileToUri);
                    } else {
                        uri = Uri.fromFile(fileToUri);
                    }
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));*/
                    mScannerConnection = new MediaScannerConnection(this, new MediaScannerConnection.MediaScannerConnectionClient() {
                        @Override
                        public void onMediaScannerConnected() {
                            mScannerConnection.scanFile(PickerFileProvider.getAbsoluteImagePath(), null);
                        }

                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            //添加到选中集合
                            SelectionManager.getInstance().addImageToSelectList(path);
                            ArrayList<String> list = new ArrayList<>(SelectionManager.getInstance().getSelectPaths());
                            Intent intent = new Intent();
                            intent.putStringArrayListExtra(MediaPicker.EXTRA_SELECT_IMAGES, list);
                            setResult(RESULT_OK, intent);
                            finish();
                            mScannerConnection.disconnect();
                        }
                    });
                    mScannerConnection.connect();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mHideRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLoaderDelegate.destroy();
    }

    @Override
    public void onMediaClick(View view, int position) {
        if (isShowCamera) {
            if (position == 0) {
                if (!SelectionManager.getInstance().isCanChoose()) {
                    Toast.makeText(this, String.format(getString(R.string.select_image_max), mMaxCount), Toast.LENGTH_SHORT).show();
                    return;
                }
                launchSysCamera();
                return;
            }
        }

        if (mMediaFileList != null) {
            DataUtil.getInstance().setMediaData(mMediaFileList);
            Intent intent = new Intent(this, ImagePreviewActivity.class);
            if (isShowCamera) {
                intent.putExtra(ImagePreviewActivity.IMAGE_POSITION, position - 1);
            } else {
                intent.putExtra(ImagePreviewActivity.IMAGE_POSITION, position);
            }
            startActivityForResult(intent, REQUEST_SELECT_IMAGES_CODE);
        }
    }

    @Override
    public void onMediaCheck(View view, int position) {
        if (isShowCamera) {
            if (position == 0) {
                if (!SelectionManager.getInstance().isCanChoose()) {
                    Toast.makeText(this, String.format(getString(R.string.select_image_max), mMaxCount), Toast.LENGTH_SHORT).show();
                    return;
                }
                launchSysCamera();
                // TODO: 2019/6/19 launch camera
                return;
            }
        }

        //执行选中/取消操作
        MediaFile mediaFile = mImagePickerAdapter.getMediaFile(position);
        if (mediaFile != null) {
            String imagePath = mediaFile.getPath();
            if (isSingleType) {
                //如果是单类型选取，判断添加类型是否满足（照片视频不能共存）
                ArrayList<String> selectPathList = SelectionManager.getInstance().getSelectPaths();
                if (!selectPathList.isEmpty()) {
                    //判断选中集合中第一项是否为视频
                    if (!SelectionManager.isCanAddSelectionPaths(imagePath, selectPathList.get(0))) {
                        //类型不同
                        Toast.makeText(this, getString(R.string.single_type_choose), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
            boolean addSuccess = SelectionManager.getInstance().addImageToSelectList(imagePath);
            SelectionManager.getInstance().addMediaToSelectList(mediaFile);
            if (addSuccess) {
                mImagePickerAdapter.notifyItemChanged(position);
            } else {
                Toast.makeText(this, String.format(getString(R.string.select_image_max), mMaxCount), Toast.LENGTH_SHORT).show();
            }
        }
        updateCommitButton();
    }

    /**
     * 更新确认按钮状态
     */
    private void updateCommitButton() {
        //改变确定按钮UI
        int selectCount = SelectionManager.getInstance().getSelectPaths().size();
        if (selectCount == 0) {
            mAppbar.getAppbarRightContainer().setEnabled(false);
            mAppbar.setAppbarMenuText(getString(R.string.confirm));
            mBtnPreview.setEnabled(false);
            mBtnPreview.setText(getString(R.string.preview));
            return;
        }
        if (selectCount < mMaxCount) {
            mAppbar.getAppbarRightContainer().setEnabled(true);
            mAppbar.setAppbarMenuText(String.format(getString(R.string.confirm_msg), selectCount, mMaxCount));
            mBtnPreview.setEnabled(true);
            mBtnPreview.setText(String.format(getString(R.string.preview_msg), selectCount));
            return;
        }
        if (selectCount == mMaxCount) {
            mAppbar.getAppbarRightContainer().setEnabled(true);
            mAppbar.setAppbarMenuText(String.format(getString(R.string.confirm_msg), selectCount, mMaxCount));
            mBtnPreview.setEnabled(true);
            mBtnPreview.setText(String.format(getString(R.string.preview_msg), selectCount));
            return;
        }
    }

    /**
     * 跳转相机拍照
     */
    private void launchSysCamera() {
        if (isSingleType) {
            //如果是单类型选取，判断添加类型是否满足（照片视频不能共存）
            ArrayList<String> selectPathList = SelectionManager.getInstance().getSelectPaths();
            if (!selectPathList.isEmpty()) {
                if (MediaFileUtil.isVideoFileType(selectPathList.get(0))) {
                    //如果存在视频，就不能拍照了
                    Toast.makeText(this, getString(R.string.single_type_choose), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri;
        File fileToUri = new File(PickerFileProvider.getAbsoluteImagePath());
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(this, PickerFileProvider.getFileProviderName(this), fileToUri);
        } else {
            uri = Uri.fromFile(fileToUri);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_CODE_CAPTURE);
    }
}
