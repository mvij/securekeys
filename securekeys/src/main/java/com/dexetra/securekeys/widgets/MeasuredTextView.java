package com.dexetra.securekeys.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.dexetra.securekeys.R;

/**
 * Created by Vijith Menon on 9/12/14.
 */
public class MeasuredTextView extends View {
    private final String TAG = MeasuredTextView.class.getName();

    private final float MIN_TEXT_SIZE = 14;//in sp
    private final float INTERVAL_TEXT_SIZE = 1;//in sp
    private float mSuggestedTextSizeMultiplier;
    private float mTextSize;
    private float mTextSizeInterval;
    private Paint mTextPaint;

    public CharSequence getText() {
        return mText;
    }

    public void setText(CharSequence mText) {
        this.mText = mText;
        requestLayout();
    }

    private CharSequence mText;
    private String family;
    private int colorResId;
    private boolean mAttached = false;
    private float[] measuredWidth;

    public MeasuredTextView(Context context) {
        this(context, null);
    }

    public MeasuredTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setColorResId(int colorResId,int shadowColorId) {
        this.colorResId = colorResId;
        mTextPaint.setColor(getResources().getColor(colorResId));
        if(shadowColorId>0) {
            int shadowColor = getResources().getColor(shadowColorId) /*Color.parseColor("#B0000000")*/;
            mTextPaint.setShadowLayer(1.0f, 0, 0.4f, shadowColor);
        }

        invalidate();
    }

    public MeasuredTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if(!this.isInEditMode()) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MeasuredTextView,
                    defStyleAttr, 0);
            mText = a.getText(R.styleable.MeasuredTextView_mtext);
            family = a.getString(R.styleable.MeasuredTextView_fontfamily);
            mSuggestedTextSizeMultiplier = a.getFloat(
                    R.styleable.MeasuredTextView_suggestedTextSizeMultiplier, 0.5f);
            colorResId = a.getResourceId(R.styleable.MeasuredTextView_tColor,R.color.light_accent);
            a.recycle();
        }

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, MIN_TEXT_SIZE, dm);
        mTextSizeInterval = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                INTERVAL_TEXT_SIZE, dm);

        mTextPaint = new Paint();
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(context.getResources().getColor(colorResId));
        mTextPaint.setAntiAlias(true);
        //mTextPaint.getColor()

    }

    public Typeface getTypeface() {
        return mTypeface;
    }

    public void setTypeface(Typeface mTypeface) {
        this.mTypeface = mTypeface;
    }

    Typeface mTypeface;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        checkAndSetTextSize(widthSize);
        checkHeightAndSetTextSize(heightSize);
        int height =
                (int) (-mTextPaint.ascent() + mTextPaint.descent());
        setMeasuredDimension(widthSize, height);
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void checkHeightAndSetTextSize(int heightSize) {
        if (heightSize == 0) return;
        if(mText==null)return;
        int height = (int) (-mTextPaint.ascent() + mTextPaint.descent());
        if (height > 0 && heightSize < height) {
            // decrease text size
            mTextSize -= mTextSizeInterval;
            mTextPaint.setTextSize(mTextSize);
            checkHeightAndSetTextSize(heightSize);
        } else return;
    }

    private void checkAndSetTextSize(int widthSize) {
        if (widthSize == 0) return;
        if(mText==null)return;
        float[] widths = new float[mText.length()];
        int acceptableChars = mTextPaint.breakText(mText, 0, mText.length(), true,
                widthSize, null);
        mTextPaint.getTextWidths(mText, 0, mText.length(), widths);
        int measuredWidth = getSumOfArray(widths);
        if (acceptableChars < mText.length()) {
            // decrease text size
            mTextSize -= mTextSizeInterval;
            mTextPaint.setTextSize(mTextSize);
        } else if (measuredWidth > 0 && measuredWidth < widthSize - 80) {
            // increase text size
            mTextSize += mTextSizeInterval;
            mTextPaint.setTextSize(mTextSize);
        } else return;
        checkAndSetTextSize(widthSize);
    }

    private int getSumOfArray(float[] widths) {
        int sum = 0;
        for (float value : widths) {
            sum += value;
        }
        return sum;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mText!=null) {
            if(mTypeface!=null)
                mTextPaint.setTypeface(mTypeface);
            canvas.drawText(mText, 0, mText.length(), getWidth() / 2, -mTextPaint.ascent(),
                    mTextPaint);
        }
    }
}
