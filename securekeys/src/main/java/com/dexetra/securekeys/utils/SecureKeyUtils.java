package com.dexetra.securekeys.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by vijith on 14/7/14.
 */
public class SecureKeyUtils {

    public static int dpToPx(Context context, int size) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                size, context.getResources().getDisplayMetrics());
    }
}
