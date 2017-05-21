package com.library.widght;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class WaveView extends View {

    private static final int MAX_DB = 90;

    private float mVoiceDb = 27;

    private double phase = 0;
    private boolean run = false;
    private int ratio;
    private int width;
    private int width_2;
    private int width_4;
    private int height;
    private int height_2;
    private float MAX;
    private float amplitude;
    private float speed;
    private int frequency;

    private final int ONE = 1;
    private final int TWO = 2;
    private final int THREE = 3;
    private final int FOUR = 4;
    private final int FIVE = 5;
    private final int SIX = 6;
    private final int SEVEN = 7;
    private final int EIGHT = 8;


    public WaveView(Context context) {
        super(context);
        init();
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.ratio = 1;
        this.width = this.ratio * getScreenWidth(getContext());
        this.height = this.width / 6;
        this.width_2 = this.width / 2;
        this.width_4 = this.width / 4;
        this.height_2 = this.height / 2;
        this.MAX = (this.height_2) - 4;
        this.amplitude = 1;
        this.speed = 0.15f;
        this.frequency = 6;
        start();
    }

    private int getScreenWidth(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        run = true;
        if (this.run == false)
            return;
        this.phase = (this.phase + Math.PI * this.speed) % (2f * Math.PI);
        Log.i("zeng", "phase:" + phase + "---getRateOfConversion()--->" + getRateOfConversion() + "---mVoiceDb--->" + mVoiceDb);
        this.clear(canvas);
        canvas.drawColor(getResources().getColor(android.R.color.black));
//        this._drawLine(canvas, -2, R.color.wave_color_5);
//        this._drawLine(canvas, -6, R.color.wave_color_4);
//        this._drawLine(canvas, 4, R.color.wave_color_3);
//        this._drawLine(canvas, 2, R.color.wave_color_2);
//        this._drawLine(canvas, 1, R.color.wave_color_1);
        drawStraightLine(canvas, android.R.color.white);
//        this._drawLine(canvas, 7, R.color.wave_color_1);
//        this._drawLine(canvas, 6, R.color.wave_color_1);
        this._drawLine(canvas, (float) (10.2 * getRateOfConversion()), android.R.color.white, EIGHT);
        this._drawLine(canvas, (float) (7.3 * getRateOfConversion()), android.R.color.white, SEVEN);
        this._drawLine(canvas, (float) (5.2 * getRateOfConversion()), android.R.color.white, SIX);
        this._drawLine(canvas, (float) (3.9 * getRateOfConversion()), android.R.color.white, FIVE);
        this._drawLine(canvas, (float) (2.8 * getRateOfConversion()), android.R.color.white, FOUR);
        this._drawLine(canvas, (float) (2 * getRateOfConversion()), android.R.color.white, THREE);
        this._drawLine(canvas, (float) (1.4 * getRateOfConversion()), android.R.color.white, TWO);
        this._drawLine(canvas, (float) (1 * getRateOfConversion()), android.R.color.holo_blue_light, ONE);
        postInvalidateDelayed(20);
    }

    public void drawStraightLine(Canvas canvas, int color) {
        Path path = new Path();
        path.moveTo(0, this.height);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.reset();
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);// 设置空心
        paint.setColor(getResources().getColor(color));
//        canvas.drawPath(path, paint);
        canvas.drawLine(0, this.height, this.width, this.height, paint);
    }

    public void _drawLine(Canvas canvas, float attenuation, int color, final int order) {
        Path path = new Path();
        path.moveTo(0, this.height);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.reset();
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);// 设置空心
//        if (attenuation == 1) {
//            paint.setStrokeWidth(2);
//        }
        paint.setColor(getResources().getColor(color));
        float i = -2f;
        while ((i += 0.01) <= 2f) {
            float y = this._ypos(i, attenuation, order);
            if (Math.abs(i) >= 1.90f)
                y = this.height;
            path.lineTo(this._xpos(i), y);
        }
        canvas.drawPath(path, paint);
    }

    ;

    public float _xpos(float i) {
        return this.width_2 + i * this.width_4;
//        return i * this.width_4;
    }

    ;

    public float _ypos(float i, float attenuation, int order) {
        float att = (this.MAX * this.amplitude) / attenuation;
        return (float) (this.height + this._globAttFunc(i) * att
                * Math.sin(this.frequency * i - this.phase - order * 0.15));
//        return (float) (att * Math.sin(this.frequency * i - this.phase));
    }

    ;

    public double _globAttFunc(float x) {
        return 2 * Math.pow(4 / (4 + Math.pow(x, 4)), 4);
    }

    ;

    public void clear(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(android.R.color.holo_blue_light));
        paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        canvas.drawPaint(paint);
    }

    ;

    public void start() {
        phase = 0;
        invalidate();
    }

    public float getRateOfConversion() {
        return (float) (MAX_DB / mVoiceDb);
    }

    public void setVoiceDb(float db) {
        mVoiceDb = db;
    }
}

