package com.dexetra.securekeys.utils;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class TypefaceSpan extends MetricAffectingSpan {

    private Typeface mTypeface;
    private float mSize;

    public TypefaceSpan(Typeface font, float size) {
        mTypeface = font;
        mSize = size;

    }

    @Override
    public void updateMeasureState(TextPaint p) {
        p.setTypeface(mTypeface);
        p.setTextSize(mSize);

// Note: This flag is required for proper typeface rendering
        p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    @Override
    public void updateDrawState(TextPaint p) {
        p.setTypeface(mTypeface);
        p.setTextSize(mSize);

// Note: This flag is required for proper typeface rendering
        p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }
}