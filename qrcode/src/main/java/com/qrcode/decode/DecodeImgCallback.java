package com.qrcode.decode;

import com.google.zxing.Result;

/**
 * 解析图片的回调
 *
 * @author pm
 * @date 2018/11/15
 * @email puming@zdsoft.cn
 *
 */
public interface DecodeImgCallback {
    public void onImageDecodeSuccess(Result result);

    public void onImageDecodeFailed();
}
