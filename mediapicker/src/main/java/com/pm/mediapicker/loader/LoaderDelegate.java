package com.pm.mediapicker.loader;

import android.content.Context;
import android.util.Log;

import com.pm.mediapicker.data.MediaFile;
import com.pm.mediapicker.data.MediaFolder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author pm
 * @date 2019/6/18
 * @email puming@zdsoft.cn
 */
public class LoaderDelegate {
    private Context mContext;
    ImageScanner mImageScanner;
    VideoScanner mVideoScanner;

    private List<MediaFile> mImages;
    private List<MediaFile> mVideos;

    private LinkedList<String> mLinkedList;

    public void create(Context context) {
        mContext = context;
        mImageScanner = new ImageScanner(context);
        mVideoScanner = new VideoScanner(context);

        mImages = new ArrayList<>(12);
        mVideos = new ArrayList<>(12);

        mLinkedList = new LinkedList<String>();
    }

    public void startLoad(Scanner.Result<List<MediaFolder>> result, Source source) {
        switch (source) {
            case IMAGE:
                loadImage(result, source);
                break;
            case VIDEO:
                loadVideo(result, source);
                break;
            case ALL:
                loadVideo(result, source);
                loadImage(result, source);
                break;
            default:
                loadImage(result, Source.ALL);
                loadVideo(result, Source.ALL);
                break;
        }
    }

    private void loadImage(Scanner.Result<List<MediaFolder>> result, Source source) {
        mImageScanner.load(new Scanner.Result<List<MediaFile>>() {
            @Override
            public void onValue(List<MediaFile> mediaFiles) {
                Log.d("puming loadImage", "onValue: ");
                if (mediaFiles != null) {
                    mImages = mediaFiles;
                }
                mLinkedList.addLast(mImageScanner.toString());
                if (source != Source.ALL) {
                    result.onValue(MediaHandler.getImageFolder(mContext, (ArrayList<MediaFile>) mImages));
                } else {
                    loadImageAndVideo(result);
                }
            }

            @Override
            public void onError() {

            }
        });
    }

    private void loadVideo(Scanner.Result<List<MediaFolder>> result, Source source) {
        mVideoScanner.load(new Scanner.Result<List<MediaFile>>() {
            @Override
            public void onValue(List<MediaFile> mediaFiles) {
                Log.d("puming loadVideo", "onValue: ");
                if (mediaFiles != null) {
                    mVideos = mediaFiles;
                }
                mLinkedList.addLast(mVideoScanner.toString());
                if (source != Source.ALL) {
                    result.onValue(MediaHandler.getVideoFolder(mContext, (ArrayList<MediaFile>) mVideos));
                } else {
                    loadImageAndVideo(result);
                }
            }

            @Override
            public void onError() {

            }
        });
    }

    private void loadImageAndVideo(Scanner.Result<List<MediaFolder>> result) {
        if (mLinkedList.size() >= 2) {
            result.onValue(MediaHandler.getMediaFolder(mContext, (ArrayList<MediaFile>) mImages, (ArrayList<MediaFile>) mVideos));
        }
    }

    public void destroy() {

    }

    public enum Source {
        IMAGE,
        VIDEO,
        ALL,
    }
}
