package com.library.widght.ptrlistview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.library.R;

/**
 * 支持下拉刷新，头部显示圆形进度条
 */
public class PTRListView extends ListView implements AbsListView.OnScrollListener {

    //头部
    private RelativeLayout mHeaderLayout;
    private TextView mTvHeaderTitle;
    private TextView mTvHeaderProgress;

    // 是否有下拉刷新
    private boolean mIsPTRStyle = true;
    // 触发刷新的偏移量
    private int mOffsetHeight;

    //当前滑动的状态
    private int mCurrentScrollState;

    public static final int STATE_NORNAL = 1;
    public static final int STATE_PULL_TO_REFRESH = 2;
    public static final int STATE_RELEASE_TO_REFRESH = 3;
    public static final int STATE_REFRESHING = 4;

    //头部状态
    private int mCurrentHeaderState = STATE_NORNAL;

    private int mHeaderProgress = 0;

    private float mLastDownPointY;
    private float headerPaddingTopRate = 1.5f;

    private PTRListViewHeader mViewProgress;

    public PTRListView(Context context) {
        this(context, null);
    }

    public PTRListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PTRListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        if(mIsPTRStyle) {
            setSelection(1);
        }

        super.setOnScrollListener(this);
    }

    private void init() {
        initHeader();
    }

    private void initHeader() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mHeaderLayout = (RelativeLayout) inflater.inflate(R.layout.ptrlistview_header, this, false);
        mTvHeaderTitle = (TextView) mHeaderLayout.findViewById(R.id.tv_header);
        mTvHeaderProgress = (TextView) mHeaderLayout.findViewById(R.id.tv_progress);
        mViewProgress = (PTRListViewHeader) mHeaderLayout.findViewById(R.id.view_progress);

        addHeaderView(mHeaderLayout);
        measureHeaderlayout(mHeaderLayout);

        mTvHeaderProgress.setText(mHeaderProgress + "%");
    }

    private void measureHeaderlayout(View child){
        ViewGroup.LayoutParams lp = child.getLayoutParams();
        mOffsetHeight = lp.height;
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(mIsPTRStyle){
            mCurrentScrollState = scrollState;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(mIsPTRStyle){
            if(mCurrentScrollState == SCROLL_STATE_TOUCH_SCROLL && mCurrentHeaderState != STATE_REFRESHING){
                int currentOffset = mHeaderLayout.getBottom();
                mHeaderProgress = (currentOffset*100)/mOffsetHeight;
                mTvHeaderProgress.setText(mHeaderProgress + "%");
                mViewProgress.setProgress(mHeaderProgress);
                if(mHeaderLayout.getBottom() >= mOffsetHeight){
                    mTvHeaderTitle.setText(getContext().getString(R.string.release_to_refresh));
                    mCurrentHeaderState = STATE_RELEASE_TO_REFRESH;
                }else{
                    mTvHeaderTitle.setText(getContext().getString(R.string.pull_to_refresh));
                    mCurrentHeaderState = STATE_PULL_TO_REFRESH;
                }
            }else if (mCurrentScrollState == SCROLL_STATE_FLING && getFirstVisiblePosition() == 0){
                setPositionToHideHeader();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_UP:
                if (!isVerticalScrollBarEnabled()) {
                    setVerticalScrollBarEnabled(true);
                }

                if(getFirstVisiblePosition() == 0) {
                    if (mCurrentHeaderState == STATE_RELEASE_TO_REFRESH && mCurrentHeaderState != STATE_REFRESHING) {
                        Toast.makeText(getContext(), "放开开始刷新", Toast.LENGTH_SHORT).show();
                        onHeaderRefresh();
                    } else if (mCurrentHeaderState == STATE_PULL_TO_REFRESH && mCurrentHeaderState != STATE_REFRESHING) {
                        Toast.makeText(getContext(), "放开不刷新，收回去", Toast.LENGTH_SHORT).show();
                        setPositionToHideHeader();
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                mLastDownPointY = ev.getY();
//                ev.setLocation(ev.getX(), ev.getY()/2);
                break;
            case MotionEvent.ACTION_MOVE:
//                setHeaderPosition(ev);
//                ev.setLocation(ev.getX(), ev.getY()/2);
                break;
        }

        return super.onTouchEvent(ev);
    }

    public void setHeaderRefreshListener(OnClickListener listener){
        if(listener != null){
            mTvHeaderTitle.setOnClickListener(listener);
        }
    }

    private void setPositionToHideHeader(){
        if(getAdapter() != null && getAdapter().getCount() > 0 && getFirstVisiblePosition() == 0){
            setSelection(1);
        }
    }

    private void onHeaderRefresh(){
        mCurrentHeaderState = STATE_REFRESHING;
        //TODO 开始刷新回调
        mTvHeaderTitle.performClick();
    }

    public void headerRefreshComplete(){
        mCurrentHeaderState = STATE_NORNAL;
        setPositionToHideHeader();
    }

    private void setHeaderPosition(MotionEvent ev){
        int pointerCount = ev.getHistorySize();

        if (isVerticalFadingEdgeEnabled()) {
            setVerticalScrollBarEnabled(false);
        }

        for (int i = 0; i < pointerCount; i++) {
            int after = (int) (((ev.getHistoricalY(i) - mLastDownPointY) - mOffsetHeight) / headerPaddingTopRate);
            mHeaderLayout.setPadding(mHeaderLayout.getPaddingLeft(), after,
                    mHeaderLayout.getPaddingRight(), mHeaderLayout.getPaddingBottom());
            Log.d("debug", "history = " + ev.getHistoricalY(i) + ", after = " + after);
        }
    }
}
