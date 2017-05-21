
package com.library.common.commonadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * 通用适配器
 * @param <T>
 */

public abstract class CommonAdapter<T> extends BaseAdapter {
    protected Context mContext;
    protected List<T> mDatas;
    protected LayoutInflater mInflater;
    protected int mLayoutId;

    public CommonAdapter(Context context, List<T> datas, int layoutId) {
        this.mContext = context;
        this.mDatas = datas;
        this.mLayoutId = layoutId;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public T getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = ViewHolder.getHolder(mContext,position,convertView, parent, mLayoutId);
        convert(holder, getItem(position));
        return holder.getConvertView();
    }

    /**
     * 设置控件
     * @param holder
     * @param t
     */
    public abstract void convert(ViewHolder holder, T t);
}
