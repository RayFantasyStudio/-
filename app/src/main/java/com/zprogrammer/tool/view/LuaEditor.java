/*
 * Copyright 2015. Alex Zhang aka. ztc1997
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
 *//*


package com.zprogrammer.tool.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.Scroller;

import com.zprogrammer.tool.bean.ZZ;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class zLuaEditor extends EditText {

    private Editable spannable;
    private int blockStart;
    private int blockEnd;
    private int startLine;
    private int endLine;
    private String blockText;
    private boolean isOnLayout;
    private int maxChars = 1000000;
    private Paint mPaint = new Paint();

    private Scroller mScroller;
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private VelocityTracker mVelocityTracker;
    private float mLastMotionY;
    private float mLastMotionX;

    private int maxWidth;
    private int flingOrientation = 0;
    private int VERTICAL = 1;
    private int HORIZONTAL = -1;
    private int FLINGED = 0;

    private int mLastScrollX;

    private int mLastScrollY;
    //private int isH;


    public LuaEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LuaEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LuaEditor(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public LuaEditor(Context context) {
        super(context);
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int lineHeight = getLineHeight();
//		int topPadding = getPaddingTop();
        int leftPadding = getPaddingLeft();
//		int y = topPadding + lineHeight + 1;
        int x = getScrollX();
        int r = x + getRight() - 4;
        int t = getScrollY();
        int b = getHeight() + t;
        mPaint.setTextSize(30);
        Layout layout = getLayout();
        int SelectionLine = layout.getLineForOffset(getSelectionEnd());
//		int SelectionLineBottom=SelectionLine * lineHeight + lineHeight;
        int SelectionLineBottom = layout.getLineBottom(SelectionLine);
        canvas.drawLine(x, SelectionLineBottom, r, SelectionLineBottom, mPaint);
        canvas.drawLine(x + leftPadding - 4, t, x + leftPadding - 4, b, mPaint);
        int topLine = layout.getLineForVertical(t);
        int bottomLine = layout.getLineForVertical(b);
        for (int i = topLine; i <= bottomLine + 1; i++) {
            canvas.drawText(String.valueOf(i), x + 2, i * lineHeight, mPaint);
        }
        canvas.translate(0, 0);
        super.onDraw(canvas);
    }


    public void setLineColor(int color) {
        mPaint.setColor(color);
        invalidate();
    }


    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        spannable = getText();
        if (count > maxChars || !isOnLayout) {
            return;
        }

        if (count > 32) {
            refreshHightlight();
        } else {
            refreshHightlight(start, start + count);
        }
    }


    public void refreshHightlight() {
        getOnScreenText();
        if (blockStart >= blockEnd)
            return;
        clear(0, spannable.length());
        blockText = blockText.replaceAll("\\\\\"|\\\\\'", "  ");

        highlight(ZZ.COLOR_BLUE, ZZ.BLUE);
        highlight(ZZ.COLOR_OTHER, ZZ.OTHER);
        highlight(ZZ.COLOR_RED, ZZ.RED);
        highlight(ZZ.COLOR_GREEN, ZZ.GREEN);
    }


    public void refreshHightlight(int start, int end) {
        getBlockText(start, end);

        if (blockStart >= blockEnd)
            return;
        clear(blockStart, blockEnd);
        blockText = blockText.replaceAll("\\\\\"|\\\\\'", "  ");

        highlight(ZZ.COLOR_BLUE, ZZ.BLUE);
        highlight(ZZ.COLOR_OTHER, ZZ.OTHER);
        highlight(ZZ.COLOR_RED, ZZ.RED);
        highlight(ZZ.COLOR_GREEN, ZZ.GREEN);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // TODO: Implement this method
        spannable = getText();
        isOnLayout = true;
        refreshHightlight();
        super.onLayout(changed, left, top, right, bottom);
    }

    private void highlight(int clr, String target) {
        CharacterStyle span;
        Pattern p = Pattern.compile(target);
        Matcher m = p.matcher(blockText);
        while (m.find()) {
            span = new ForegroundColorSpan(clr);
            spannable.setSpan(span, blockStart + m.start(), blockStart + m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void clear(int start, int end) {

        ForegroundColorSpan[] spans = spannable.getSpans(start, end, ForegroundColorSpan.class);
        for (ForegroundColorSpan span : spans) spannable.removeSpan(span);

    }


    public boolean gotoLine(int line) {
        line--;
        if (line > getLineCount())
            return false;
        Layout layout = getLayout();
        setSelection(layout.getLineStart(line), layout.getLineEnd(line));
//				Selection.setSelection(getText(),m.start(),m.end()-1);
        return true;
    }

    private void getBlockText(int start, int end) {
        Layout layout = getLayout();
        startLine = layout.getLineForOffset(start);
        endLine = layout.getLineForOffset(end);

        blockStart = layout.getLineStart(startLine);
        blockEnd = layout.getLineEnd(endLine);
        if (blockEnd == 0)
            blockEnd = layout.getText().length();
        //		if(blockStart<blockEnd)
        blockText = layout.getText().subSequence(blockStart, blockEnd).toString();
//		Log.d("l", blockText + "," + start + "," + end);
//		return layout.getText().subSequence(start, end).toString();
    }

    private void getOnScreenText() {
        Layout layout = getLayout();
        startLine = layout.getLineForVertical(getScrollY());
        endLine = layout.getLineForVertical(getScrollY() + getHeight());

        blockStart = layout.getLineStart(startLine);
        blockEnd = layout.getLineEnd(endLine);
        if (blockEnd == 0)
            blockEnd = layout.getText().length();
//		Log.d("l", w + "," + start + "," + end);
        blockText = layout.getText().subSequence(blockStart, blockEnd).toString();
//		return layout.getText().subSequence(start, end).toString();
    }


    void init(Context context) {
        setPadding(getLineHeight(), 0, getLineHeight() / 3, 0);
        setHorizontallyScrolling(true);
        setGravity(Gravity.LEFT | Gravity.TOP);
        setBackgroundColor(Color.argb(0, 0, 0, 0));
        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        setLineColor(Color.GRAY);
        mPaint.setTextSize(30);

//		helper = new Helper();
        mScroller = new Scroller(getContext());
        setFocusable(true);
        setWillNotDraw(false);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    private int getLineMaxWidth() {
        // TODO: Implement this method
        getOnScreenText();
        int maxWidth = 0;
        Layout layout = getLayout();
        for (int i = startLine; i < endLine; i++) {
            maxWidth = (int) Math.max(maxWidth, layout.getLineWidth(i));
        }
        return maxWidth;
    }

    public void fling(int velocityX, int velocityY) {

        if (getLineCount() > 0) {
            mScroller.fling(getScrollX(), getScrollY(), velocityX * 2, velocityY, 0, getLineMaxWidth() - getWidth() + getPaddingLeft() + getPaddingRight(), 0,
                    getLineHeight() * getLineCount() - getHeight());
//			final boolean movingDown = velocityY > 0;
            awakenScrollBars(mScroller.getDuration());
            invalidate();
        }
    }

    private void obtainVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN
                && event.getEdgeFlags() != 0) {
            return false;
        }

        obtainVelocityTracker(event);
        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastMotionY = y;
                mLastMotionX = x;
                mLastScrollY = getScrollY();
                mLastScrollX = getScrollX();
                flingOrientation = FLINGED;
                if (x < getPaddingLeft()) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (x < getPaddingLeft()) {
                    scrollTo(getScrollX(), Math.min(getLineHeight() * getLineCount() - getHeight(), Math.max(0, (int) (getLineCount() * getLineHeight() * (y / getHeight())))));
                    return true;
                }
                int deltaX = (int) (mLastMotionX - x);
                int deltaY = (int) (mLastMotionY - y);
                if (flingOrientation == FLINGED) {
                    if (Math.abs(deltaX) >= Math.abs(deltaY))
                        flingOrientation = HORIZONTAL;
                    else
                        flingOrientation = VERTICAL;
                }
//				Log.d("oriention",""+flingOrientation);

                break;
            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int initialVelocityY = (int) velocityTracker.getYVelocity();
                int initialVelocityX = (int) velocityTracker.getXVelocity();
                if (flingOrientation == HORIZONTAL)
                    initialVelocityY = 0;
                else if (flingOrientation == VERTICAL)
                    initialVelocityX = 0;
                if ((Math.max(Math.abs(initialVelocityX), Math.abs(initialVelocityY)) > mMinimumVelocity)
                        && getLineCount() > 0) {
                    fling(-initialVelocityX, -initialVelocityY);
                }
                releaseVelocityTracker();
                flingOrientation = FLINGED;
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
            return;
        }
        if (flingOrientation != HORIZONTAL)
            refreshHightlight();
//		flingOrientation=0;
    }


    @Override
    public void scrollTo(int x, int y) {
        // TODO: Implement this method
        if (flingOrientation == VERTICAL)
            x = mLastScrollX;
        if (flingOrientation == HORIZONTAL)
            y = mLastScrollY;
//		Log.d("to",""+flingOrientation);
        super.scrollTo(x, y);
    }

}
*/
