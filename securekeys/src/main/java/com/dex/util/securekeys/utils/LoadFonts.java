/**
 *
 */
package com.dex.util.securekeys.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.util.Random;


/**
 * @author awesomeeby
 */
public class LoadFonts {
    private static LoadFonts S = null;
    //    private static Context mContext = null;
    public int ID = -1;
    public Typeface mBold;
    public Typeface mMedium;
    public Typeface mRegular;

    AssetManager mAss;

    private LoadFonts(Context c) {
        mAss = c.getAssets();
        ID = new Random().nextInt(1000);
        mBold = Typeface.createFromAsset(mAss,
                "FiraSans-Bold.otf");
        mMedium = Typeface.createFromAsset(mAss,
                "FiraSans-Medium.otf");
        mRegular = Typeface.createFromAsset(mAss,
                "FiraSans-Regular.otf");
    }

    public static LoadFonts createInstance(Context c) {
        S = new LoadFonts(c.getApplicationContext());
        return S;
    }

    public static LoadFonts getInstance(Context c) {
        if (S == null)
            S = new LoadFonts(c.getApplicationContext());
        return S;
    }

    public static LoadFonts getInstance() {
        return S;
    }


    public Typeface getMedium() {
        return mMedium;
    }

    public void setMedium(Typeface mMedium) {
        this.mMedium = mMedium;
    }

    public Typeface getRegular() {
        return mRegular;
    }

    public void setRegular(Typeface mRegular) {
        this.mRegular = mRegular;
    }

    public Typeface getBold() {
        return mBold;
    }

    public void setBold(Typeface mBold) {
        this.mBold = mBold;
    }

}
