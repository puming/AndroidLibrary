/*
 * Copyright 2011 woozzu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.library.widght.indexlistview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
/**
 * 绘制索引条和预览框
 * @author puming
 *
 */
public class IndexScroller {
	
	private float mIndexbarWidth;//索引条的宽度
	private float mIndexbarMargin;//索引条距离右边的宽度
	private float mPreviewPadding;//索引文本的内边距
	private float mDensity;// 当前屏幕密度除以160
	private float mScaledDensity;// 当前屏幕密度除以160（设置字体尺寸）
	private float mAlphaRate;//透明度（用于显示和隐藏）
	
	private int mState = STATE_HIDDEN;//索引条的状态
	private int mListViewWidth;
	private int mListViewHeight;
	private int mCurrentSection = -1;
	private boolean mIsIndexing = false;
	private ListView mListView = null;
	private SectionIndexer mIndexer = null;
	private String[] mSections = null;//索引条字符串数组
	private RectF mIndexbarRect;//索引条
	
	
	/**
	 * 索引条的四个状态
	 */
	private static final int STATE_HIDDEN = 0;
	private static final int STATE_SHOWING = 1;
	private static final int STATE_SHOWN = 2;
	private static final int STATE_HIDING = 3;
	
	
	/**
	 * 索引条初始化与尺寸本地化
	 * @param context
	 * @param listView
	 */
	public IndexScroller(Context context, ListView listView) {
		
		//获取屏幕密度
		mDensity = context.getResources().getDisplayMetrics().density;
		mScaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
		
		mListView = listView;
		setAdapter(mListView.getAdapter());
		
		
		//根据屏幕密度计算索引条的宽度（单位啊；像素）
		mIndexbarWidth = 20 * mDensity;
		mIndexbarMargin = 10 * mDensity;
		mPreviewPadding = 5 * mDensity;
	}

	
	/**
	 * 绘制索引条和预览文本
	 * @param canvas
	 */
	public void draw(Canvas canvas) {
		//work1.绘制索引条，包括索引条背景和文本
		//work2.绘制预览文本和背景
		
		if (mState == STATE_HIDDEN){//如果索引条隐藏，不进行绘制
			return;
		}
		//设置索引条背景的绘制属性
		Paint indexbarPaint = new Paint();
		indexbarPaint.setColor(Color.BLACK);
		indexbarPaint.setAlpha((int) (64 * mAlphaRate));
		indexbarPaint.setAntiAlias(true);
		canvas.drawRoundRect(mIndexbarRect, 5 * mDensity, 5 * mDensity, indexbarPaint);//画索引条
		
		//绘制Sections
		if (mSections != null && mSections.length > 0) {
			
			if (mCurrentSection >= 0) {//如果。。。。绘制预览文本和背景
				//设置预览文本背景属性
				Paint previewPaint = new Paint();
				previewPaint.setColor(Color.BLACK);
				previewPaint.setAlpha(96);
				previewPaint.setAntiAlias(true);
				previewPaint.setShadowLayer(3, 0, 0, Color.argb(64, 0, 0, 0));
				//设置预览文本属性
				Paint previewTextPaint = new Paint();
				previewTextPaint.setColor(Color.WHITE);
				previewTextPaint.setAntiAlias(true);
				previewTextPaint.setTextSize(50 * mScaledDensity);
				
				//预览文本尺寸
				float previewTextWidth = previewTextPaint.measureText(mSections[mCurrentSection]);
				//预览框尺寸
				float previewSize = 2 * mPreviewPadding + previewTextPaint.descent() - previewTextPaint.ascent();
				
				//预览文本背景区域（正方形）
				RectF previewRect = new RectF((mListViewWidth - previewSize) / 2
						, (mListViewHeight - previewSize) / 2
						, (mListViewWidth - previewSize) / 2 + previewSize
						, (mListViewHeight - previewSize) / 2 + previewSize);
				canvas.drawRoundRect(previewRect, 5 * mDensity, 5 * mDensity, previewPaint);//画预览文本背景
				canvas.drawText(mSections[mCurrentSection], 
						previewRect.left + (previewSize - previewTextWidth) / 2 - 1,
						previewRect.top + mPreviewPadding - previewTextPaint.ascent() + 1, 
						previewTextPaint);//画预览文本
			}
			
			//设置索引文本的属性
			Paint indexPaint = new Paint();
			indexPaint.setColor(Color.WHITE);
			indexPaint.setAlpha((int) (255 * mAlphaRate));
			indexPaint.setAntiAlias(true);
			indexPaint.setTextSize(12 * mScaledDensity);
			
			float sectionHeight = (mIndexbarRect.height() - 2 * mIndexbarMargin) / mSections.length;
			float paddingTop = (sectionHeight - (indexPaint.descent() - indexPaint.ascent())) / 2;
			for (int i = 0; i < mSections.length; i++) {
				float paddingLeft = (mIndexbarWidth - indexPaint.measureText(mSections[i])) / 2;
				canvas.drawText(mSections[i], 
						mIndexbarRect.left + paddingLeft, 
						mIndexbarRect.top + mIndexbarMargin + sectionHeight * i + paddingTop - indexPaint.ascent(), 
						indexPaint);//画索引条上的文本（ABCD...）
			}
		}
	}
	
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// If down event occurs inside index bar region, start indexing
			if (mState != STATE_HIDDEN && contains(ev.getX(), ev.getY())) {
				setState(STATE_SHOWN);
				
				// It demonstrates that the motion event started from index bar
				mIsIndexing = true;
				// Determine which section the point is in, and move the list to that section
				mCurrentSection = getSectionByPoint(ev.getY());
				mListView.setSelection(mIndexer.getPositionForSection(mCurrentSection));
				return true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mIsIndexing) {
				// If this event moves inside index bar
				if (contains(ev.getX(), ev.getY())) {
					// Determine which section the point is in, and move the list to that section
					mCurrentSection = getSectionByPoint(ev.getY());
					mListView.setSelection(mIndexer.getPositionForSection(mCurrentSection));
				}
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mIsIndexing) {
				mIsIndexing = false;
				mCurrentSection = -1;
			}
			if (mState == STATE_SHOWN)
				setState(STATE_HIDING);
			break;
		}
		return false;
	}
	
	/**
	 * 
	 * @param w
	 * @param h
	 * @param oldw
	 * @param oldh
	 */
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		mListViewWidth = w;
		mListViewHeight = h;
		mIndexbarRect = new RectF(w - mIndexbarMargin - mIndexbarWidth
				, mIndexbarMargin
				, w - mIndexbarMargin
				, h - mIndexbarMargin);
	}
	
	public void show() {
		if (mState == STATE_HIDDEN)
			setState(STATE_SHOWING);
		else if (mState == STATE_HIDING)
			setState(STATE_HIDING);
	}
	
	public void hide() {
		if (mState == STATE_SHOWN)
			setState(STATE_HIDING);
	}
	
	public void setAdapter(Adapter adapter) {
		if (adapter instanceof SectionIndexer) {
			mIndexer = (SectionIndexer) adapter;
			mSections = (String[]) mIndexer.getSections();//27个字符
		}
	}
	
	/**
	 * 设置状态
	 * @param state
	 */
	private void setState(int state) {
		if (state < STATE_HIDDEN || state > STATE_HIDING)
			return;
		
		mState = state;
		switch (mState) {
		case STATE_HIDDEN:
			// Cancel any fade effect
			mHandler.removeMessages(0);
			break;
		case STATE_SHOWING:
			// Start to fade in
			mAlphaRate = 0;
			fade(0);
			break;
		case STATE_SHOWN:
			// Cancel any fade effect
			mHandler.removeMessages(0);
			break;
		case STATE_HIDING:
			// Start to fade out after three seconds
			mAlphaRate = 1;
			fade(3000);
			break;
		}
	}
	
	public boolean contains(float x, float y) {
		// Determine if the point is in index bar region, which includes the right margin of the bar
		return (x >= mIndexbarRect.left && y >= mIndexbarRect.top && y <= mIndexbarRect.top + mIndexbarRect.height());
	}
	
	private int getSectionByPoint(float y) {
		if (mSections == null || mSections.length == 0)
			return 0;
		if (y < mIndexbarRect.top + mIndexbarMargin)
			return 0;
		if (y >= mIndexbarRect.top + mIndexbarRect.height() - mIndexbarMargin)
			return mSections.length - 1;
		return (int) ((y - mIndexbarRect.top - mIndexbarMargin) / ((mIndexbarRect.height() - 2 * mIndexbarMargin) / mSections.length));
	}
	
	/**
	 * 发送消息
	 * @param delay
	 */
	private void fade(long delay) {
		mHandler.removeMessages(0);
		mHandler.sendEmptyMessageAtTime(0, SystemClock.uptimeMillis() + delay);
	}
	
	//
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			switch (mState) {
			case STATE_SHOWING:
				// Fade in effect
				mAlphaRate += (1 - mAlphaRate) * 0.2;
				if (mAlphaRate > 0.9) {
					mAlphaRate = 1;
					setState(STATE_SHOWN);
				}
				
				mListView.invalidate();
				fade(10);
				break;
			case STATE_SHOWN:
				// If no action, hide automatically
				setState(STATE_HIDING);
				break;
			case STATE_HIDING:
				// Fade out effect
				mAlphaRate -= mAlphaRate * 0.2;
				if (mAlphaRate < 0.1) {
					mAlphaRate = 0;
					setState(STATE_HIDDEN);
				}
				
				mListView.invalidate();
				fade(10);
				break;
			}
		}
		
	};
}
