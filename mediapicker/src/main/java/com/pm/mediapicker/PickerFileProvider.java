package com.pm.mediapicker;

import android.content.Context;
import android.os.Environment;

import java.io.File;

import androidx.core.content.FileProvider;

/**
 * @author pm
 * @date 2019/6/20
 * @email puming@zdsoft.cn
 */
public class PickerFileProvider extends FileProvider {

    public static String getFileProviderName(Context context) {
        return context.getPackageName() + ".mediaprovider";
    }

    public static String getAbsoluteImagePath() {
        File fileDir = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DCIM + File.separator + "Camera");
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        return fileDir.getAbsolutePath() + "/IMG_" + System.currentTimeMillis() + ".jpg";
    }

}
