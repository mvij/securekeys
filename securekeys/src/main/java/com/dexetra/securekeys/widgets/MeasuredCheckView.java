package com.dexetra.securekeys.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.Checkable;

import com.dexetra.securekeys.R;

/**
 * Created by Vijith Menon on 10/12/14.
 */
public class MeasuredCheckView extends View implements Checkable {
    private final String TAG = MeasuredCheckView.class.getName();

    private final float MIN_TEXT_SIZE = 14;//in sp
    private final float INTERVAL_TEXT_SIZE = 1;//in sp
    private int mBackgroundColor;
    private int mUnCheckedColor;
    private int mCheckedColor;
    private CharSequence mText;
    private String family;
    private float mTextSize;
    private float mTextSizeInterval;
    private Paint mTextPaint;
    private float mSuggestedTextSizeMultiplier;
    private boolean isChecked = false;

    public MeasuredCheckView(Context context) {
        this(context, null);
    }

    public MeasuredCheckView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MeasuredCheckView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //if(!this.isInEditMode()) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MeasuredCheckView,
                    defStyleAttr, 0);
            mText = a.getText(R.styleable.MeasuredCheckView_ctext);
            family = a.getString(R.styleable.MeasuredCheckView_cfontfamily);
            mSuggestedTextSizeMultiplier = a.getFloat(
                    R.styleable.MeasuredCheckView_cSuggestedTextSizeMultiplier, 0.5f);
            a.recycle();
        //}

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, MIN_TEXT_SIZE, dm);
        mTextSizeInterval = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                INTERVAL_TEXT_SIZE, dm);

        mTextPaint = new Paint();
        if(family!=null) {
            Typeface tf = Typeface.create(family, Typeface.NORMAL);
            if (tf != null) {
                mTextPaint.setTypeface(tf);
            }
        }else{
            Typeface tf = Typeface.create("sans-serif", Typeface.NORMAL);
            if (tf != null) {
                mTextPaint.setTypeface(tf);
            }
        }
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setAntiAlias(true);
        int shadowColor = Color.parseColor("#B0000000");
        mTextPaint.setShadowLayer(2.0f,0,0,shadowColor);

        mCheckedColor = context.getResources().getColor(R.color.light_accent);
        mUnCheckedColor = context.getResources().getColor(R.color.light_normal);
        mBackgroundColor = context.getResources().getColor(R.color.light_background);
    }

    public CharSequence getText() {
        return mText;
    }

    public void setText(CharSequence mText) {
        this.mText = mText;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        mTextSize = widthSize/2;
        mTextPaint.setTextSize(mTextSize);
        checkAndSetTextSize(widthSize);
        //checkHeightAndSetTextSize(heightSize);
        int height =
                (int) (-mTextPaint.ascent() + mTextPaint.descent());
        setMeasuredDimension(widthSize, height);
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void checkHeightAndSetTextSize(int heightSize) {
        if (heightSize == 0) return;
        if(mText==null) return;
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
        if(mText==null) return;
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
        canvas.drawColor(mBackgroundColor);
        if(mText!=null) {
            mTextPaint.setColor(isChecked ? mCheckedColor : mUnCheckedColor);
            canvas.drawText(mText, 0, mText.length(), getWidth() / 2, -mTextPaint.ascent(),
                    mTextPaint);
        }
    }

    @Override
    public void setChecked(boolean checked) {
        if (isChecked != checked) {
            isChecked = checked;
            requestLayout();
        }
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void toggle() {
        setChecked(!isChecked);
    }
}
