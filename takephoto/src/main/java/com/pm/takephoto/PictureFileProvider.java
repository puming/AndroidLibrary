package com.pm.takephoto;

import android.content.Context;
import android.os.Environment;

import java.io.File;

import androidx.core.content.FileProvider;


/**
 * @author pm
 * @date 2019/6/20
 * @email puming@zdsoft.cn
 */
public class PictureFileProvider extends FileProvider {

    public static String getFileProviderName(Context context) {
        return context.getPackageName() + ".pictureprovider";
    }

    public static String getAbsoluteImagePath() {
        File fileDir = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_PICTURES );
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        return fileDir.getAbsolutePath() + "/IMG_" + System.currentTimeMillis() + ".jpg";
    }

}
