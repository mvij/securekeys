/**
 *
 */
package com.dexetra.securekeys.utils;

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
    public Typeface mLight;
    public Typeface mRegular;

    AssetManager mAss;

    private LoadFonts(Context c) {
        mAss = c.getAssets();
        ID = new Random().nextInt(1000);
        mBold = Typeface.createFromAsset(mAss,
                "Roboto-Bold.ttf");
        mLight = Typeface.createFromAsset(mAss,
                "Roboto-Light.ttf");
        mRegular = Typeface.createFromAsset(mAss,
                "Roboto-Regular.ttf");
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


    public Typeface getLight() {
        return mLight;
    }

    public void setLight(Typeface light) {
        this.mLight = light;
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
