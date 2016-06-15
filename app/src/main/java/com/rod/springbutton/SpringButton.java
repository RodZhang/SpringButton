package com.rod.springbutton;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * No pains, no gains.
 * Created by Rod on 16/6/14.
 */

public class SpringButton extends LinearLayout {

    private static final int INVALID_INDEX = -1;
    private static final int DEFAULT_BUTTON_COLOR = 0x00000000;
    private static final int[] ATTRS = new int[]{
            android.R.attr.textSize,
            android.R.attr.textColor
    };

    private float mTopLeftRadio;
    private float mTopRightRadio;
    private float mBottomRightRadio;
    private float mBottomLeftRadio;

    private float mTextSize = 12;
    private ColorStateList mTextColor = ColorStateList.valueOf(0xffffffff);
    private int mButtonDividerPadding = 0;
    private int mButtonDividerWidth = 1;
    private int mButtonDividerColor = 0xffffffff;
    private ColorStateList mButtonColor = ColorStateList.valueOf(DEFAULT_BUTTON_COLOR);

    private OnButtonClickListener mButtonClickListener;
    private String[] mButtonTexts = new String[0];
    private float[] mWeight = new float[0];

    private BoundsRadio mBoundsRadio = BoundsRadio.DEFAULT_BOUNDS_RADIO;
    private Paint mDividerPaint;

    public SpringButton(Context context) {
        super(context);
        init();
    }

    public SpringButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttrs(attrs);
        init();
    }

    public SpringButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseAttrs(attrs);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setOrientation(HORIZONTAL);

        initDividerPaint();
    }

    private void parseAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, ATTRS);
        mTextSize = typedArray.getDimension(0, mTextSize);

        ColorStateList textColor = typedArray.getColorStateList(1);
        if (textColor != null) {
            mTextColor = textColor;
        }
        typedArray.recycle();

        typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.SpringButton);
        mButtonDividerPadding = typedArray.getDimensionPixelOffset(R.styleable.SpringButton_buttonDividerPadding, mButtonDividerPadding);
        mButtonDividerWidth = typedArray.getDimensionPixelOffset(R.styleable.SpringButton_buttonDividerWidth, mButtonDividerWidth);
        mButtonDividerColor = typedArray.getColor(R.styleable.SpringButton_buttonDividerColor, mButtonDividerColor);
        ColorStateList buttonColor = typedArray.getColorStateList(R.styleable.SpringButton_buttonColor);
        if (buttonColor != null) {
            mButtonColor = buttonColor;
        }

        mTopLeftRadio = typedArray.getDimension(R.styleable.SpringButton_topLeftCornerRadio, mTopLeftRadio);
        mTopRightRadio = typedArray.getDimension(R.styleable.SpringButton_topRightCornerRadio, mTopRightRadio);
        mBottomRightRadio = typedArray.getDimension(R.styleable.SpringButton_bottomRightCornerRadio, mBottomRightRadio);
        mBottomLeftRadio = typedArray.getDimension(R.styleable.SpringButton_bottomLeftCornerRadio, mBottomLeftRadio);

        mBoundsRadio = new BoundsRadio(mTopLeftRadio, mTopRightRadio, mBottomRightRadio, mBottomLeftRadio);
        typedArray.recycle();
    }

    private void initDividerPaint() {
        mDividerPaint = new Paint();
        mDividerPaint.setAntiAlias(true);
        mDividerPaint.setStrokeWidth(mButtonDividerWidth);
        mDividerPaint.setColor(mButtonDividerColor);
    }

    public void setBoundsRadio(BoundsRadio boundsRadio) {
        if (boundsRadio == null) {
            mBoundsRadio = BoundsRadio.DEFAULT_BOUNDS_RADIO;
        } else {
            mBoundsRadio = boundsRadio;
        }

        updateTexts();
    }

    public void setButtonColor(ColorStateList buttonColor) {
        if (buttonColor == null) {
            // TODO: need default color state list
            return;
        }
        mButtonColor = buttonColor;
        updateTexts();
    }

    public void setOnButtonClickListener(OnButtonClickListener buttonClickListener) {
        mButtonClickListener = buttonClickListener;
    }

    public void setButtons(String[] texts) {
        setButtons(texts, null);
    }

    public void setButtons(String[] texts, float[] weights) {
        if (texts == null) {
            // TODO: add log
            return;
        }

        if (weights == null) {
            resetWeights(texts.length);
        } else if (weights.length != texts.length) {
            throw new IllegalArgumentException(String.format("button texts size is %d, but weights size is %d", texts.length, weights.length));
        }

        mButtonTexts = texts;
        updateTexts();
    }

    private void resetWeights(int len) {
        mWeight = new float[len];
        for (int i = 0; i < len; i++) {
            mWeight[i] = 1;
        }
    }

    private void updateTexts() {
        removeAllViews();
        for (int i = 0; i < mButtonTexts.length; i++) {
            addButton(i);
        }
    }

    private void addButton(int index) {
        TextView child = new TextView(getContext());
        child.setTextColor(mTextColor);
        child.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        child.setText(mButtonTexts[index]);
        child.setTag(mButtonTexts[index]);
        StateListDrawable bg = getItemBGDrawable(index);
        child.setBackgroundDrawable(bg);

        LinearLayout.LayoutParams lp = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, mWeight[index]);
        child.setLayoutParams(lp);
        child.setGravity(Gravity.CENTER);
        addView(child);

        child.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = getChildIndex(v);
                if (index == INVALID_INDEX) {
                    return;
                }

                if (mButtonClickListener != null) {
                    mButtonClickListener.onButtonClick(v, index);
                }
            }
        });
    }

    @NonNull
    private StateListDrawable getItemBGDrawable(int index) {
        if (index == 0) {
            return getFirstItemBg();
        } else if (index == mButtonTexts.length - 1) {
            return getLastItemBg();
        } else {
            return getNormalItemBg();
        }
    }

    private StateListDrawable getFirstItemBg() {
        return getItemBgStateListDrawable(mBoundsRadio.getTopLeftRadio(), 0, 0, mBoundsRadio.getBottomLeftRadio());
    }

    private StateListDrawable getNormalItemBg() {
        return getItemBgStateListDrawable(0, 0, 0, 0);
    }

    private StateListDrawable getLastItemBg() {
        return getItemBgStateListDrawable(0, mBoundsRadio.getTopRightRadio(), mBoundsRadio.getBottomRightRadio(), 0);
    }

    @NonNull
    private StateListDrawable getItemBgStateListDrawable(final float topLeftRadio, final float topRightRadio, final float bottomRightRadio, final float bottomLeftRadio) {
        Drawable normalDrawable = getItemBgDrawable(mButtonColor.getColorForState(new int[]{}, DEFAULT_BUTTON_COLOR), topLeftRadio, topRightRadio, bottomRightRadio, bottomLeftRadio);
        Drawable pressedDrawable = getItemBgDrawable(mButtonColor.getColorForState(new int[]{android.R.attr.state_pressed}, DEFAULT_BUTTON_COLOR), topLeftRadio, topRightRadio, bottomRightRadio, bottomLeftRadio);
        return getStateListDrawable(normalDrawable, pressedDrawable);
    }

    @NonNull
    private Drawable getItemBgDrawable(final int color, final float topLeftRadio, final float topRightRadio, final float bottomRightRadio, final float bottomLeftRadio) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadii(new float[]{topLeftRadio, topLeftRadio, topRightRadio, topRightRadio, bottomRightRadio, bottomRightRadio, bottomLeftRadio, bottomLeftRadio});
        return drawable;
    }

    @NonNull
    private StateListDrawable getStateListDrawable(Drawable normalDrawable, Drawable pressedDrawable) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
        stateListDrawable.addState(new int[]{}, normalDrawable);
        return stateListDrawable;
    }

    private int getChildIndex(View view) {
        int result = INVALID_INDEX;
        for (int i = 0, size = getChildCount(); i < size; i++) {
            if (view == getChildAt(i)) {
                result = i;
                break;
            }
        }

        return result;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        final int height = getHeight();
        for (int i = 0, childCount = getChildCount(); i < childCount - 1; i++) {
            View child = getChildAt(i);
            int right = child.getRight();
            canvas.drawLine(right, mButtonDividerPadding, right, height - mButtonDividerPadding, mDividerPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    interface OnButtonClickListener {
        void onButtonClick(View view, int pos);
    }

    public static class BoundsRadio {
        public static final BoundsRadio DEFAULT_BOUNDS_RADIO = new BoundsRadio(0, 0, 0, 0);

        private final float mTopLeftRadio;
        private final float mTopRightRadio;
        private final float mBottomRightRadio;
        private final float mBottomLeftRadio;

        public BoundsRadio(float topLeftRadio, float topRightRadio, float bottomRightRadio, float bottomLeftRadio) {
            mTopLeftRadio = topLeftRadio;
            mTopRightRadio = topRightRadio;
            mBottomRightRadio = bottomRightRadio;
            mBottomLeftRadio = bottomLeftRadio;
        }

        public float getTopLeftRadio() {
            return mTopLeftRadio;
        }

        public float getTopRightRadio() {
            return mTopRightRadio;
        }

        public float getBottomRightRadio() {
            return mBottomRightRadio;
        }

        public float getBottomLeftRadio() {
            return mBottomLeftRadio;
        }
    }
}
