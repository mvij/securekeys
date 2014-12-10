package com.dexetra.securekeys;

import android.app.Application;
import android.os.Handler;

import com.dexetra.securekeys.utils.LoadFonts;
import com.dexetra.securekeys.utils.ThemeAndFontListener;

import java.util.ArrayList;

/**
 * Created by vijith on 3/6/14.
 */
public class SecurekeysApplication extends Application {
    Handler mHandler = new Handler();
    public ArrayList<ThemeAndFontListener> mTListeners = new ArrayList<ThemeAndFontListener>(
            7);


    @Override
    public void onCreate() {
        super.onCreate();
        initFonts();
    }

    public void initFonts() {
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        final LoadFonts f = LoadFonts
                                .createInstance(getApplicationContext());
                        mHandler
                                .post(new Runnable() {

                                    @Override
                                    public void run() {
                                        for (ThemeAndFontListener lis : mTListeners)
                                            lis.onFontInitialized(f);
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            };
            t.start();
    }
}
