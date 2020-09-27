package com.pm.mediapicker.loader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

public abstract class Scanner<T> {

    /**
     * 查询URI
     *
     * @return
     */
    protected abstract Uri getScanUri();

    /**
     * 查询列名
     *
     * @return
     */
    protected abstract String[] getProjection();

    /**
     * 查询条件
     *
     * @return
     */
    protected abstract String getSelection();

    /**
     * 查询条件值
     *
     * @return
     */
    protected abstract String[] getSelectionArgs();

    /**
     * 查询排序
     *
     * @return
     */
    protected abstract String getOrder();

    /**
     * 对外暴露游标，让开发者灵活构建对象
     *
     * @param cursor
     * @return
     */
    protected abstract T parse(Cursor cursor);

    /**
     * loader 的 id
     *
     * @return
     */
    protected abstract int getLoaderId();


    private Context mContext;

    public Scanner(Context context) {
        this.mContext = context;
    }

    /**
     * 根据查询条件进行媒体库查询，隐藏查询细节，让开发者更专注业务
     *
     * @return
     */
    public ArrayList<T> queryMedia() {
        ArrayList<T> list = new ArrayList<>();
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = contentResolver.query(getScanUri(), getProjection(), getSelection(), getSelectionArgs(), getOrder());
        if (cursor != null) {
            while (cursor.moveToNext()) {
                T t = parse(cursor);
                list.add(t);
            }
            cursor.close();
        }
        return list;
    }

    public void load(Result<List<T>> result) {
        AppCompatActivity activity = (AppCompatActivity) mContext;
        LoaderManager.getInstance(activity).restartLoader(getLoaderId(), null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @NonNull
            @Override
            public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
                return new CursorLoader(mContext, getScanUri(), getProjection(), getSelection(), getSelectionArgs(), getOrder());
            }

            @Override
            public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
                ArrayList<T> list = new ArrayList<>();
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        T t = parse(cursor);
                        list.add(t);
                    }
                    cursor.close();
                }
                result.onValue(list);
                Log.d("puming", "onLoadFinished: " + list.size());
            }

            @Override
            public void onLoaderReset(@NonNull Loader<Cursor> loader) {

            }
        });
    }


    public interface Result<T> {
        void onValue(T t);

        void onError();
    }
}
