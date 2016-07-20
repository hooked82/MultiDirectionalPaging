package com.hookedroid.multidirectionalpaging.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class ToolbarPagerTitleStrip extends ViewGroup {
    private static String TAG = "PagerTitleStrip";

    ViewPager mPager;
    TextView mPrevText;
    TextView mCurrText;
    TextView mNextText;

    private int mLastKnownCurrentPage = -1;
    private float mLastKnownPositionOffset = -1;
    private int mScaledTextSpacing;
    private int mGravity;

    private boolean mUpdatingText;
    private boolean mUpdatingPositions;

    private PageListener mPageListener = new PageListener();

    private WeakReference<PagerAdapter> mWatchingAdapter;

    private static int[] ATTRS = new int[] {
            android.R.attr.textAppearance,
            android.R.attr.textSize,
            android.R.attr.textColor,
            android.R.attr.gravity
    };

    private static int[] TEXT_ATTRS = new int[] {
            0x0101038c // android.R.attr.textAllCaps
    };

    private static float SIDE_ALPHA = 0.5f;
    private static int TEXT_SPACING = 16; // dip

    private int mNonPrimaryAlpha;
    int mTextColor;

    interface PagerTitleStripImpl {
        void setSingleLineAllCaps(TextView text);
    }

    static class PagerTitleStripImplBase implements PagerTitleStripImpl {
        public void setSingleLineAllCaps(TextView text) {
            text.setSingleLine();
        }
    }

    private static PagerTitleStripImpl IMPL;
    static {
        IMPL = new PagerTitleStripImplBase();
    }

    private static void setSingleLineAllCaps(TextView text) {
        IMPL.setSingleLineAllCaps(text);
    }

    public ToolbarPagerTitleStrip(Context context) {
        this(context, null);
    }

    public ToolbarPagerTitleStrip(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);

        mPrevText = new TextView(context);
        mCurrText = new TextView(context);
        mNextText = new TextView(context);

        int textAppearance = a.getResourceId(0, 0);
        if (textAppearance != 0) {
            mPrevText.setTextAppearance(context, textAppearance);
            mCurrText.setTextAppearance(context, textAppearance);
            mNextText.setTextAppearance(context, textAppearance);
        }

        addView(mPrevText);
        addView(mCurrText);
        addView(mNextText);

        int textSize = a.getDimensionPixelSize(1, 0);
        if (textSize != 0) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        }
        if (a.hasValue(2)) {
            int textColor = a.getColor(2, 0);
            mPrevText.setTextColor(textColor);
            mCurrText.setTextColor(textColor);
            mNextText.setTextColor(textColor);
        }
        mGravity = a.getInteger(3, Gravity.BOTTOM);
        a.recycle();

        mTextColor = mCurrText.getTextColors().getDefaultColor();
        setNonPrimaryAlpha(SIDE_ALPHA);

        mPrevText.setEllipsize(TextUtils.TruncateAt.END);
        mCurrText.setEllipsize(TextUtils.TruncateAt.END);
        mNextText.setEllipsize(TextUtils.TruncateAt.END);

        boolean allCaps = false;
        if (textAppearance != 0) {
            TypedArray ta = context.obtainStyledAttributes(textAppearance, TEXT_ATTRS);
            allCaps = ta.getBoolean(0, false);
            ta.recycle();
        }

        if (allCaps) {
            setSingleLineAllCaps(mPrevText);
            setSingleLineAllCaps(mCurrText);
            setSingleLineAllCaps(mNextText);
        } else {
            mPrevText.setSingleLine();
            mCurrText.setSingleLine();
            mNextText.setSingleLine();
        }

        float density = context.getResources().getDisplayMetrics().density;
        mScaledTextSpacing = (int) (TEXT_SPACING * density);
    }

    /**
     * Set the required spacing between title segments.
     *
     * @param spacingPixels Spacing between each title displayed in pixels
     */
    public void setTextSpacing(int spacingPixels) {
        mScaledTextSpacing = spacingPixels;
        requestLayout();
    }

    /**
     * @return The required spacing between title segments in pixels
     */
    public int getTextSpacing() {
        return mScaledTextSpacing;
    }

    /**
     * Set the alpha value used for non-primary page titles.
     *
     * @param alpha Opacity value in the range 0-1f
     */
    public void setNonPrimaryAlpha(@FloatRange(from=0.0, to=1.0) float alpha) {
        mNonPrimaryAlpha = (int) (alpha * 255) & 0xFF;
        int transparentColor = (mNonPrimaryAlpha << 24) | (mTextColor & 0xFFFFFF);
        mPrevText.setTextColor(transparentColor);
        mNextText.setTextColor(transparentColor);
    }

    /**
     * Set the color value used as the base color for all displayed page titles.
     * Alpha will be ignored for non-primary page titles. See {@link #setNonPrimaryAlpha(float)}.
     *
     * @param color Color hex code in 0xAARRGGBB format
     */
    public void setTextColor(@ColorInt int color) {
        mTextColor = color;
        mCurrText.setTextColor(color);
        int transparentColor = (mNonPrimaryAlpha << 24) | (mTextColor & 0xFFFFFF);
        mPrevText.setTextColor(transparentColor);
        mNextText.setTextColor(transparentColor);
    }

    /**
     * Set the default text size to a given unit and value.
     * See {@link TypedValue} for the possible dimension units.
     *
     * <p>Example: to set the text size to 14px, use
     * setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);</p>
     *
     * @param unit The desired dimension unit
     * @param size The desired size in the given units
     */
    public void setTextSize(int unit, float size) {
        mPrevText.setTextSize(unit, size);
        mCurrText.setTextSize(unit, size);
        mNextText.setTextSize(unit, size);
    }

    /**
     * Set the {@link Gravity} used to position text within the title strip.
     * Only the vertical gravity component is used.
     *
     * @param gravity {@link Gravity} constant for positioning title text
     */
    public void setGravity(int gravity) {
        mGravity = gravity;
        requestLayout();
    }

    public void setViewPager(ViewPager viewPager) {
        PagerAdapter adapter = viewPager.getAdapter();

        viewPager.setOnPageChangeListener(mPageListener);
        mPager = viewPager;
        updateAdapter(mWatchingAdapter != null ? mWatchingAdapter.get() : null, adapter);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mPager != null) {
            updateAdapter(mPager.getAdapter(), null);
            mPager.setOnPageChangeListener(null);
            mPager = null;
        }
    }

    void updateText(int currentItem, PagerAdapter adapter) {
        int itemCount = adapter != null ? adapter.getCount() : 0;
        mUpdatingText = true;

        CharSequence text = null;
        if (currentItem >= 1 && adapter != null) {
            text = adapter.getPageTitle(currentItem - 1);
        }
        mPrevText.setText(text);

        mCurrText.setText(adapter != null && currentItem < itemCount ?
                adapter.getPageTitle(currentItem) : null);

        text = null;
        if (currentItem + 1 < itemCount && adapter != null) {
            text = adapter.getPageTitle(currentItem + 1);
        }
        mNextText.setText(text);

        // Measure everything
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int maxWidth = Math.max(0, (int) (width * 0.8f));
        int childWidthSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST);
        int childHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        int maxHeight = Math.max(0, childHeight);
        int childHeightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        mPrevText.measure(childWidthSpec, childHeightSpec);
        mCurrText.measure(childWidthSpec, childHeightSpec);
        mNextText.measure(childWidthSpec, childHeightSpec);

        mLastKnownCurrentPage = currentItem;

        if (!mUpdatingPositions) {
            updateTextPositions(currentItem, mLastKnownPositionOffset, false);
        }

        mUpdatingText = false;
    }

    @Override
    public void requestLayout() {
        if (!mUpdatingText) {
            super.requestLayout();
        }
    }

    void updateAdapter(PagerAdapter oldAdapter, PagerAdapter newAdapter) {
        if (oldAdapter != null) {
            oldAdapter.unregisterDataSetObserver(mPageListener);
            mWatchingAdapter = null;
        }
        if (newAdapter != null) {
            newAdapter.registerDataSetObserver(mPageListener);
            mWatchingAdapter = new WeakReference<PagerAdapter>(newAdapter);
        }
        if (mPager != null) {
            mLastKnownCurrentPage = -1;
            mLastKnownPositionOffset = -1;
            updateText(mPager.getCurrentItem(), newAdapter);
            requestLayout();
        }
    }

    void updateTextPositions(int position, float positionOffset, boolean force) {
        if (position != mLastKnownCurrentPage) {
            updateText(position, mPager.getAdapter());
        } else if (!force && positionOffset == mLastKnownPositionOffset) {
            return;
        }

        mUpdatingPositions = true;

        int prevWidth = mPrevText.getMeasuredWidth();
        int currWidth = mCurrText.getMeasuredWidth();
        int nextWidth = mNextText.getMeasuredWidth();
        int halfCurrWidth = currWidth / 2;

        int stripWidth = getWidth();
        int stripHeight = getHeight();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int textPaddedLeft = paddingLeft + halfCurrWidth;
        int textPaddedRight = paddingRight + halfCurrWidth;
        int contentWidth = stripWidth - textPaddedLeft - textPaddedRight;

        float currOffset = positionOffset + 0.5f;
        if (currOffset > 1.f) {
            currOffset -= 1.f;
        }
        int currCenter = stripWidth - textPaddedRight - (int) (contentWidth * currOffset);
        int currLeft = currCenter - currWidth / 2;
        int currRight = currLeft + currWidth;

        int prevBaseline = mPrevText.getBaseline();
        int currBaseline = mCurrText.getBaseline();
        int nextBaseline = mNextText.getBaseline();
        int maxBaseline = Math.max(Math.max(prevBaseline, currBaseline), nextBaseline);
        int prevTopOffset = maxBaseline - prevBaseline;
        int currTopOffset = maxBaseline - currBaseline;
        int nextTopOffset = maxBaseline - nextBaseline;
        int alignedPrevHeight = prevTopOffset + mPrevText.getMeasuredHeight();
        int alignedCurrHeight = currTopOffset + mCurrText.getMeasuredHeight();
        int alignedNextHeight = nextTopOffset + mNextText.getMeasuredHeight();
        int maxTextHeight = Math.max(Math.max(alignedPrevHeight, alignedCurrHeight),
                alignedNextHeight);

        int vgrav = mGravity & Gravity.VERTICAL_GRAVITY_MASK;

        int prevTop;
        int currTop;
        int nextTop;
        switch (vgrav) {
            default:
            case Gravity.TOP:
                prevTop = paddingTop + prevTopOffset;
                currTop = paddingTop + currTopOffset;
                nextTop = paddingTop + nextTopOffset;
                break;
            case Gravity.CENTER_VERTICAL:
                int paddedHeight = stripHeight - paddingTop - paddingBottom;
                int centeredTop = (paddedHeight - maxTextHeight) / 2;
                prevTop = centeredTop + prevTopOffset;
                currTop = centeredTop + currTopOffset;
                nextTop = centeredTop + nextTopOffset;
                break;
            case Gravity.BOTTOM:
                int bottomGravTop = stripHeight - paddingBottom - maxTextHeight;
                prevTop = bottomGravTop + prevTopOffset;
                currTop = bottomGravTop + currTopOffset;
                nextTop = bottomGravTop + nextTopOffset;
                break;
        }

        mCurrText.layout(currLeft, currTop, currRight,
                currTop + mCurrText.getMeasuredHeight());

        int prevLeft = Math.min(paddingLeft, currLeft - mScaledTextSpacing - prevWidth);
        mPrevText.layout(prevLeft, prevTop, prevLeft + prevWidth,
                prevTop + mPrevText.getMeasuredHeight());

        int nextLeft = Math.max(stripWidth - paddingRight - nextWidth,
                currRight + mScaledTextSpacing);
        mNextText.layout(nextLeft, nextTop, nextLeft + nextWidth,
                nextTop + mNextText.getMeasuredHeight());

        mLastKnownPositionOffset = positionOffset;
        mUpdatingPositions = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Must measure with an exact width");
        }

        int heightPadding = getPaddingTop() + getPaddingBottom();
        int childHeightSpec = getChildMeasureSpec(heightMeasureSpec,
                heightPadding, LayoutParams.WRAP_CONTENT);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthPadding = (int) (widthSize * 0.2f);
        int childWidthSpec = getChildMeasureSpec(widthMeasureSpec,
                widthPadding, LayoutParams.WRAP_CONTENT);

        mPrevText.measure(childWidthSpec, childHeightSpec);
        mCurrText.measure(childWidthSpec, childHeightSpec);
        mNextText.measure(childWidthSpec, childHeightSpec);

        int height;
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            int textHeight = mCurrText.getMeasuredHeight();
            int minHeight = getMinHeight();
            height = Math.max(minHeight, textHeight + heightPadding);
        }

        int childState = ViewCompat.getMeasuredState(mCurrText);
        int measuredHeight = ViewCompat.resolveSizeAndState(height, heightMeasureSpec,
                childState << ViewCompat.MEASURED_HEIGHT_STATE_SHIFT);
        setMeasuredDimension(widthSize, measuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mPager != null) {
            float offset = mLastKnownPositionOffset >= 0 ? mLastKnownPositionOffset : 0;
            updateTextPositions(mLastKnownCurrentPage, offset, true);
        }
    }

    int getMinHeight() {
        int minHeight = 0;
        Drawable bg = getBackground();
        if (bg != null) {
            minHeight = bg.getIntrinsicHeight();
        }
        return minHeight;
    }

    private class PageListener extends DataSetObserver implements ViewPager.OnPageChangeListener {
        private int mScrollState;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (positionOffset > 0.5f) {
                // Consider ourselves to be on the next page when we're 50% of the way there.
                position++;
            }
            updateTextPositions(position, positionOffset, false);
        }

        @Override
        public void onPageSelected(int position) {
            if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                // Only update the text here if we're not dragging or settling.
                updateText(mPager.getCurrentItem(), mPager.getAdapter());

                float offset = mLastKnownPositionOffset >= 0 ? mLastKnownPositionOffset : 0;
                updateTextPositions(mPager.getCurrentItem(), offset, true);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mScrollState = state;
        }

        @Override
        public void onChanged() {
            updateText(mPager.getCurrentItem(), mPager.getAdapter());

            float offset = mLastKnownPositionOffset >= 0 ? mLastKnownPositionOffset : 0;
            updateTextPositions(mPager.getCurrentItem(), offset, true);
        }
    }
}
