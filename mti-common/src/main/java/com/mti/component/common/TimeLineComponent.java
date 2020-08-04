package com.mti.component.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.mti.component.common.util.DisplayTool;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @anthor created by jzw
 * @date 2020/5/22
 * @change
 * @describe 时间轴组件
 **/
public class TimeLineComponent extends View {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LineOrientation.HORIZONTAL, LineOrientation.VERTICAL})
    public @interface LineOrientation {
        int HORIZONTAL = 0;
        int VERTICAL = 1;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LineType.NORMAL, LineType.START, LineType.END, LineType.ONLYONE})
    private @interface LineType {
        int NORMAL = 0;
        int START = 1;
        int END = 2;
        int ONLYONE = 3;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LineStyle.NORMAL, LineStyle.DASHED})
    public @interface LineStyle {
        int NORMAL = 0;
        int DASHED = 1;
    }

    private Drawable mMarker;
    private int mMarkerSize;
    private int mMarkerPaddingLeft;
    private int mMarkerPaddingTop;
    private int mMarkerPaddingRight;
    private int mMarkerPaddingBottom;
    private boolean mMarkerInCenter;
    private Paint mLinePaint = new Paint();
    private boolean mDrawStartLine = false;
    private boolean mDrawEndLine = false;
    private float mStartLineStartX, mStartLineStartY, mStartLineStopX, mStartLineStopY;
    private float mEndLineStartX, mEndLineStartY, mEndLineStopX, mEndLineStopY;
    private int mStartLineColor;
    private int mEndLineColor;
    private int mLineWidth;
    private int mLineOrientation;
    private int mLineStyle;
    private int mLineStyleDashLength;
    private int mLineStyleDashGap;
    private int mLinePadding;

    private Rect mBounds;

    public TimeLineComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TimeLineComponent);
        mMarker = typedArray.getDrawable(R.styleable.TimeLineComponent_marker);
        mMarkerSize = typedArray.getDimensionPixelSize(R.styleable.TimeLineComponent_markerSize, DisplayTool.dip2px(getContext(), 20));
        mMarkerPaddingLeft = typedArray.getDimensionPixelSize(R.styleable.TimeLineComponent_markerPaddingLeft, 0);
        mMarkerPaddingTop = typedArray.getDimensionPixelSize(R.styleable.TimeLineComponent_markerPaddingTop, 0);
        mMarkerPaddingRight = typedArray.getDimensionPixelSize(R.styleable.TimeLineComponent_markerPaddingRight, 0);
        mMarkerPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.TimeLineComponent_markerPaddingBottom, 0);
        mMarkerInCenter = typedArray.getBoolean(R.styleable.TimeLineComponent_markerInCenter, true);
        mStartLineColor = typedArray.getColor(R.styleable.TimeLineComponent_startLineColor, getResources().getColor(android.R.color.darker_gray));
        mEndLineColor = typedArray.getColor(R.styleable.TimeLineComponent_endLineColor, getResources().getColor(android.R.color.darker_gray));
        mLineWidth = typedArray.getDimensionPixelSize(R.styleable.TimeLineComponent_lineWidth, DisplayTool.dip2px(getContext(), 2));
        mLineOrientation = typedArray.getInt(R.styleable.TimeLineComponent_lineOrientation, LineOrientation.VERTICAL);
        mLinePadding = typedArray.getDimensionPixelSize(R.styleable.TimeLineComponent_linePadding, 0);
        mLineStyle = typedArray.getInt(R.styleable.TimeLineComponent_lineStyle, LineStyle.NORMAL);
        mLineStyleDashLength = typedArray.getDimensionPixelSize(R.styleable.TimeLineComponent_lineStyleDashLength, DisplayTool.dip2px(getContext(), 8f));
        mLineStyleDashGap = typedArray.getDimensionPixelSize(R.styleable.TimeLineComponent_lineStyleDashGap, DisplayTool.dip2px(getContext(), 4f));
        typedArray.recycle();

        if (isInEditMode()) {
            mDrawStartLine = true;
            mDrawEndLine = true;
        }

        if (mMarker == null) {
            mMarker = getResources().getDrawable(R.drawable.marker);
        }

        initTimeline();
        initLinePaint();

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //Width measurements of the width and height and the inside view of child controls
        int w = mMarkerSize + getPaddingLeft() + getPaddingRight();
        int h = mMarkerSize + getPaddingTop() + getPaddingBottom();

        // Width and height to determine the final view through a systematic approach to decision-making
        int widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
        int heightSize = resolveSizeAndState(h, heightMeasureSpec, 0);

        setMeasuredDimension(widthSize, heightSize);
        initTimeline();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        initTimeline();
    }

    private void initTimeline() {

        int pLeft = getPaddingLeft();
        int pRight = getPaddingRight();
        int pTop = getPaddingTop();
        int pBottom = getPaddingBottom();

        int width = getWidth();// Width of current custom view
        int height = getHeight();

        int cWidth = width - pLeft - pRight;// Circle width
        int cHeight = height - pTop - pBottom;

        int markSize = Math.min(mMarkerSize, Math.min(cWidth, cHeight));

        if (mMarkerInCenter) { //Marker in center is true
            int left = (width / 2) - (markSize / 2);
            int top = (height / 2) - (markSize / 2);
            int right = (width / 2) + (markSize / 2);
            int bottom = (height / 2) + (markSize / 2);

            switch (mLineOrientation) {
                case LineOrientation.HORIZONTAL: {
                    left += mMarkerPaddingLeft - mMarkerPaddingRight;
                    right += mMarkerPaddingLeft - mMarkerPaddingRight;
                    break;
                }
                case LineOrientation.VERTICAL: {
                    top += mMarkerPaddingTop - mMarkerPaddingBottom;
                    bottom += mMarkerPaddingTop - mMarkerPaddingBottom;
                    break;
                }
            }

            if (mMarker != null) {
                mMarker.setBounds(left, top, right, bottom);
                mBounds = mMarker.getBounds();
            }

        } else { //Marker in center is false

            int left = pLeft;
            int top = pTop;
            int right = pLeft + markSize;
            int bottom = pTop;

            switch (mLineOrientation) {
                case LineOrientation.HORIZONTAL: {
                    top = (height / 2) - (markSize / 2);
                    bottom = (height / 2) + (markSize / 2);
                    left += mMarkerPaddingLeft - mMarkerPaddingRight;
                    right += mMarkerPaddingLeft - mMarkerPaddingRight;
                    break;
                }
                case LineOrientation.VERTICAL: {
                    top += mMarkerPaddingTop - mMarkerPaddingBottom;
                    bottom += markSize + mMarkerPaddingTop - mMarkerPaddingBottom;
                    break;
                }
            }

            if (mMarker != null) {
                mMarker.setBounds(left, top, right, bottom);
                mBounds = mMarker.getBounds();
            }
        }

        if (mLineOrientation == LineOrientation.HORIZONTAL) {

            if (mDrawStartLine) {
                mStartLineStartX = pLeft;
                mStartLineStartY = mBounds.centerY();
                mStartLineStopX = mBounds.left - mLinePadding;
                mStartLineStopY = mBounds.centerY();
            }

            if (mDrawEndLine) {
                if (mLineStyle == LineStyle.DASHED) {
                    mEndLineStartX = getWidth() - mLineStyleDashGap;
                    mEndLineStartY = mBounds.centerY();
                    mEndLineStopX = mBounds.right + mLinePadding;
                    mEndLineStopY = mBounds.centerY();
                } else {
                    mEndLineStartX = mBounds.right + mLinePadding;
                    mEndLineStartY = mBounds.centerY();
                    mEndLineStopX = getWidth();
                    mEndLineStopY = mBounds.centerY();
                }
            }
        } else {

            if (mDrawStartLine) {
                mStartLineStartX = mBounds.centerX();
                mStartLineStartY = pTop;
                mStartLineStopX = mBounds.centerX();
                mStartLineStopY = mBounds.top - mLinePadding;
            }

            if (mDrawEndLine) {
                if (mLineStyle == LineStyle.DASHED) {
                    mEndLineStartX = mBounds.centerX();
                    mEndLineStartY = getHeight() - mLineStyleDashGap;
                    mEndLineStopX = mBounds.centerX();
                    mEndLineStopY = mBounds.bottom + mLinePadding;
                } else {
                    mEndLineStartX = mBounds.centerX();
                    mEndLineStartY = mBounds.bottom + mLinePadding;
                    mEndLineStopX = mBounds.centerX();
                    mEndLineStopY = getHeight();
                }
            }
        }

        Log.d("timeline", "mEndLineStartX:" + mEndLineStartX
                + ",mEndLineStartY" + mEndLineStartY
                + ",mEndLineStopX:" + mEndLineStopX
                + ",mEndLineStopY:" + mEndLineStopY);
        invalidate();
    }

    private void initLinePaint() {
        mLinePaint.setAlpha(0);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(mStartLineColor);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(mLineWidth);

        if (mLineStyle == LineStyle.DASHED)
            mLinePaint.setPathEffect(new DashPathEffect(new float[]{(float) mLineStyleDashLength, (float) mLineStyleDashGap}, 0.0f));
        else
            mLinePaint.setPathEffect(new PathEffect());

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mMarker != null) {
            mMarker.draw(canvas);
        }

        if (mDrawStartLine) {
            mLinePaint.setColor(mStartLineColor);
            canvas.drawLine(mStartLineStartX, mStartLineStartY, mStartLineStopX, mStartLineStopY, mLinePaint);
        }

        if (mDrawEndLine) {
            mLinePaint.setColor(mEndLineColor);
            canvas.drawLine(mEndLineStartX, mEndLineStartY, mEndLineStopX, mEndLineStopY, mLinePaint);
        }
    }

    /**
     * 设置标记
     *
     * @param marker will set marker drawable to timeline
     */
    public void setMarker(Drawable marker) {
        mMarker = marker;
        initTimeline();
    }

    public Drawable getMarker() {
        return mMarker;
    }

    /**
     * 设置标记
     *
     * @param marker will set marker drawable to timeline
     * @param color  with a color
     */
    public void setMarker(Drawable marker, int color) {
        mMarker = marker;
        mMarker.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        initTimeline();
    }

    /**
     * 设置标记颜色
     *
     * @param color the color
     */
    public void setMarkerColor(int color) {
        mMarker.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        initTimeline();
    }

    /**
     * 设置起始 line.color
     *
     * @param color    the color of the start line
     * @param viewType the view type
     */
    public void setStartLineColor(int color, int viewType) {
        mStartLineColor = color;
        initLine(viewType);
    }

    public int getStartLineColor() {
        return mStartLineColor;
    }

    /**
     * 设置结束线。
     *
     * @param color    the color of the end line
     * @param viewType the view type
     */
    public void setEndLineColor(int color, int viewType) {
        mEndLineColor = color;
        initLine(viewType);
    }

    public int getEndLineColor() {
        return mEndLineColor;
    }

    /**
     * 设置标记大小
     *
     * @param markerSize the marker size
     */
    public void setMarkerSize(int markerSize) {
        mMarkerSize = markerSize;
        initTimeline();
    }

    public int getMarkerSize() {
        return mMarkerSize;
    }

    /**
     * 设置标记的左 padding
     *
     * @param markerPaddingLeft the left padding to marker, works only in vertical orientation
     */
    public void setMarkerPaddingLeft(int markerPaddingLeft) {
        mMarkerPaddingLeft = markerPaddingLeft;
        initTimeline();
    }

    public int getMarkerPaddingLeft() {
        return mMarkerPaddingLeft;
    }

    /**
     * 设置标记顶部 padding
     *
     * @param markerPaddingTop the top padding to marker, works only in horizontal orientation
     */
    public void setMarkerPaddingTop(int markerPaddingTop) {
        mMarkerPaddingTop = markerPaddingTop;
        initTimeline();
    }

    public int getMarkerPaddingTop() {
        return mMarkerPaddingTop;
    }

    /**
     * 设置标记右 padding
     *
     * @param markerPaddingRight the right padding to marker, works only in vertical orientation
     */
    public void setMarkerPaddingRight(int markerPaddingRight) {
        mMarkerPaddingRight = markerPaddingRight;
        initTimeline();
    }

    public int getMarkerPaddingRight() {
        return mMarkerPaddingRight;
    }

    /**
     * 设置标记底部 padding
     *
     * @param markerPaddingBottom the bottom padding to marker, works only in horizontal orientation
     */
    public void setMarkerPaddingBottom(int markerPaddingBottom) {
        mMarkerPaddingBottom = markerPaddingBottom;
        initTimeline();
    }

    public int getMarkerPaddingBottom() {
        return mMarkerPaddingBottom;
    }

    public boolean isMarkerInCenter() {
        return mMarkerInCenter;
    }

    /**
     * 设置标记在中心
     *
     * @param markerInCenter the marker position
     */
    public void setMarkerInCenter(boolean markerInCenter) {
        this.mMarkerInCenter = markerInCenter;
        initTimeline();
    }

    /**
     * 设置线宽
     *
     * @param lineWidth the line width
     */
    public void setLineWidth(int lineWidth) {
        mLineWidth = lineWidth;
        initTimeline();
    }

    public int getLineWidth() {
        return mLineWidth;
    }

    /**
     * 设置线条 padding
     *
     * @param padding the line padding
     */
    public void setLinePadding(int padding) {
        mLinePadding = padding;
        initTimeline();
    }

    public int getLineOrientation() {
        return mLineOrientation;
    }

    /**
     * 设置线方向
     *
     * @param lineOrientation the line orientation i.e horizontal or vertical
     */
    public void setLineOrientation(int lineOrientation) {
        this.mLineOrientation = lineOrientation;
    }

    public int getLineStyle() {
        return mLineStyle;
    }

    /**
     * 设置线条样式
     *
     * @param lineStyle the line style i.e normal or dashed
     */
    public void setLineStyle(int lineStyle) {
        this.mLineStyle = lineStyle;
        initLinePaint();
    }

    public int getLineStyleDashLength() {
        return mLineStyleDashLength;
    }

    /**
     * 设置虚线长度
     *
     * @param lineStyleDashLength the dashed line length
     */
    public void setLineStyleDashLength(int lineStyleDashLength) {
        this.mLineStyleDashLength = lineStyleDashLength;
        initLinePaint();
    }

    public int getLineStyleDashGap() {
        return mLineStyleDashGap;
    }

    /**
     * 设置虚线间隙
     *
     * @param lineStyleDashGap the dashed line gap
     */
    public void setLineStyleDashGap(int lineStyleDashGap) {
        this.mLineStyleDashGap = lineStyleDashGap;
        initLinePaint();
    }

    public int getLinePadding() {
        return mLinePadding;
    }

    private void showStartLine(boolean show) {
        mDrawStartLine = show;
    }

    private void showEndLine(boolean show) {
        mDrawEndLine = show;
    }

    /**
     * 初始化时间轴组件
     *
     * @param viewType the view type
     */
    public void initLine(int viewType) {
        if (viewType == LineType.START) {
            showStartLine(false);
            showEndLine(true);
        } else if (viewType == LineType.END) {
            showStartLine(true);
            showEndLine(false);
        } else if (viewType == LineType.ONLYONE) {
            showStartLine(false);
            showEndLine(false);
        } else {
            showStartLine(true);
            showEndLine(true);
        }

        initTimeline();
    }

    /**
     * 获取时间轴类型
     *
     * @param position  the position of current item view
     * @param totalSize the total size of the items
     * @return the timeline view type
     */
    public static int getTimeLineComponentType(int position, int totalSize) {
        if (totalSize == 1) {
            return LineType.ONLYONE;
        } else if (position == 0) {
            return LineType.START;
        } else if (position == totalSize - 1) {
            return LineType.END;
        } else {
            return LineType.NORMAL;
        }
    }
}
