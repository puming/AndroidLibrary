package com.pm.mediapicker;

import android.app.Activity;
import android.content.Intent;


import com.pm.mediapicker.manager.ConfigManager;
import com.pm.mediapicker.utils.ImageLoader;

import java.util.ArrayList;
/**
 * @author pm
 * @date 2019/6/19
 * @email puming@zdsoft.cn
 *
 */
public class MediaPicker {

    public static final String EXTRA_SELECT_IMAGES = "selectItems";

    private static volatile MediaPicker mImagePicker;

    private MediaPicker() {
    }

    /**
     * 创建对象
     *
     * @return
     */
    public static MediaPicker getInstance() {
        if (mImagePicker == null) {
            synchronized (MediaPicker.class) {
                if (mImagePicker == null) {
                    mImagePicker = new MediaPicker();
                }
            }
        }
        return mImagePicker;
    }


    /**
     * 设置标题
     *
     * @param title
     * @return
     */
    public MediaPicker setTitle(String title) {
        ConfigManager.getInstance().setTitle(title);
        return mImagePicker;
    }

    /**
     * 是否支持相机
     *
     * @param showCamera
     * @return
     */
    public MediaPicker showCamera(boolean showCamera) {
        ConfigManager.getInstance().setShowCamera(showCamera);
        return mImagePicker;
    }

    /**
     * 是否展示图片
     *
     * @param showImage
     * @return
     */
    public MediaPicker showImage(boolean showImage) {
        ConfigManager.getInstance().setShowImage(showImage);
        return mImagePicker;
    }

    /**
     * 是否展示视频
     *
     * @param showVideo
     * @return
     */
    public MediaPicker showVideo(boolean showVideo) {
        ConfigManager.getInstance().setShowVideo(showVideo);
        return mImagePicker;
    }


    /**
     * 图片最大选择数
     *
     * @param maxCount
     * @return
     */
    public MediaPicker setMaxCount(int maxCount) {
        ConfigManager.getInstance().setMaxCount(maxCount);
        return mImagePicker;
    }

    /**
     * 设置单类型选择（只能选图片或者视频）
     *
     * @param isSingleType
     * @return
     */
    public MediaPicker setSingleType(boolean isSingleType) {
        ConfigManager.getInstance().setSingleType(isSingleType);
        return mImagePicker;
    }


    /**
     * 设置图片加载器
     *
     * @param imageLoader
     * @return
     */
    public MediaPicker setImageLoader(ImageLoader imageLoader) {
        ConfigManager.getInstance().setImageLoader(imageLoader);
        return mImagePicker;
    }

    /**
     * 设置图片选择历史记录
     *
     * @param imagePaths
     * @return
     */
    public MediaPicker setImagePaths(ArrayList<String> imagePaths) {
        ConfigManager.getInstance().setImagePaths(imagePaths);
        return mImagePicker;
    }

    /**
     * 启动
     *
     * @param activity
     */
    public void start(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, MediaPickerActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

}
