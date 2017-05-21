
package com.library.common.commonadapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.URI;


public class ViewHolder {
    private SparseArray<View> mViews;
    private int mPosition;
    private View mConvertView;

    public View getConvertView() {
        return mConvertView;
    }

    public ViewHolder(Context context, int position, ViewGroup parent, int layoutId) {
        this.mViews = new SparseArray<View>();
        this.mPosition = position;
        this.mConvertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
        this.mConvertView.setTag(this);
    }

    /**
     * 得到ViewHolder
     *
     * @param context
     * @param position
     * @param convertView
     * @param parent
     * @param layoutId
     * @return holder
     */
    public static ViewHolder getHolder(Context context, int position, View convertView, ViewGroup parent, int layoutId) {
        if (null == convertView) {
            return new ViewHolder(context, position, parent, layoutId);
        } else {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.mPosition = position;
            return holder;
        }
    }

    /**
     * 得到View
     *
     * @param viewId
     * @param <T>
     * @return view
     */
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (null == view) {
            view = mConvertView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * @param viewId
     * @param text
     * @return
     */
    public ViewHolder setText(int viewId, String text) {
        TextView tv = getView(viewId);
        tv.setText(text);
        return this;
    }

    public ViewHolder setImagView(int viewId, int resId) {
        ImageView imageView = getView(viewId);
        imageView.setImageResource(resId);
        return this;
    }

    public ViewHolder setImagView(int viewId, Bitmap bitmap) {
        ImageView imageView = getView(viewId);
        imageView.setImageBitmap(bitmap);
        return this;
    }

    public ViewHolder setImagView(int viewId, Drawable drawable) {
        ImageView imageView = getView(viewId);
        imageView.setImageDrawable(drawable);
        return this;
    }

    public ViewHolder setImagView(int viewId, Uri uri) {
        ImageView imageView = getView(viewId);
        imageView.setImageURI(uri);
        return this;
    }
}
