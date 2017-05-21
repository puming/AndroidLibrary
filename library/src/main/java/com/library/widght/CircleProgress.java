package com.library.widght;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.library.R;

/**
 * 圆形进度条，圆心同步显示百分数
 */
public class CircleProgress extends View {

    private Paint mPaintBackCircle;
    private Paint mPaintFrontCircle;
    private Paint mPaintText;

    private float mStrokeWidth = 50;
    private float mhalfStrokeWidth = mStrokeWidth/2;

    private float mX = 200 + mhalfStrokeWidth;
    private float mY = 200 + mhalfStrokeWidth;

    private float mRadius = 200;

    private RectF mRectF;

    private int mProgress = 0;

    private int mTargetProgress = 70;

    private int mMax = 100;

    //宽度
    private int mWidth;
    //高度
    private int mHeight;

    //默认半径
    private static final int DEFALUT_RADIUS = 200;
    private static final int DEFALUT_STROKE_WIDTH = 50;

    public CircleProgress(Context context) {
        this(context, null);
    }

    public CircleProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public CircleProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if(attrs != null){
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CircleProgress);

            mRadius = array.getDimensionPixelSize(R.styleable.CircleProgress_radius,DEFALUT_RADIUS);
            mStrokeWidth = array.getDimensionPixelSize(R.styleable.CircleProgress_stroke_width, DEFALUT_STROKE_WIDTH);

            array.recycle();
        }
        init();
    }

    private void initRect(){
        if(mRectF == null) {
            mRectF = new RectF();
            int viewSize = (int)(mRadius*2);

            int left = (mWidth - viewSize) / 2;
            int top = (mHeight - viewSize) / 2;
            int right = left + viewSize;
            int bottom = top + viewSize;
            mRectF.set(left, top, right, bottom);
        }
    }

    private void init(){
        mPaintBackCircle = new Paint();
        mPaintBackCircle.setColor(Color.WHITE);
        mPaintBackCircle.setAntiAlias(true);
        mPaintBackCircle.setStyle(Paint.Style.STROKE);
        mPaintBackCircle.setStrokeWidth(mStrokeWidth);

        mPaintFrontCircle = new Paint();
        mPaintFrontCircle.setColor(0xFF66C796);
        mPaintFrontCircle.setAntiAlias(true);
        mPaintFrontCircle.setStyle(Paint.Style.STROKE);
        mPaintFrontCircle.setStrokeWidth(mStrokeWidth);

        mPaintText = new Paint();
        mPaintText.setColor(0xFF66C796);
        mPaintText.setAntiAlias(true);
        mPaintText.setTextSize(50);
        mPaintText.setTextAlign(Paint.Align.CENTER);

//        mRectF = new RectF(mhalfStrokeWidth, mhalfStrokeWidth, mRadius*2+mhalfStrokeWidth, mRadius*2+mhalfStrokeWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);

        initRect();

        float angle = mProgress/(float)mMax * 360;

        canvas.drawCircle(mWidth/2, mHeight/2, mRadius, mPaintBackCircle);
        canvas.drawArc(mRectF, -90, angle, false, mPaintFrontCircle);
        canvas.drawText(mProgress+"%", mWidth/2, mHeight/2, mPaintText);

        if(mProgress < mTargetProgress) {
            mProgress += 2;
            invalidate();
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = getRealSize(widthMeasureSpec);
        mHeight = getRealSize(heightMeasureSpec);

        setMeasuredDimension(mWidth, mHeight);
    }

    public int getRealSize(int measureSpec){
        int result = -1;

        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        if(mode == MeasureSpec.AT_MOST || mode == MeasureSpec.UNSPECIFIED){
         //自己计算
            result = (int) (mRadius*2 + mStrokeWidth);
        }else{
            result = size;
        }

        return result;
    }
}
