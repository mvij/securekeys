package com.dexetra.securekeys.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.dexetra.securekeys.SecurekeysApplication;
import com.dexetra.securekeys.utils.LoadFonts;
import com.dexetra.securekeys.utils.ThemeAndFontListener;

/**
 * Created by vijith on 3/6/14.
 */
public abstract class SecureKeysBaseActivity extends ActionBarActivity implements ThemeAndFontListener {


    boolean IsViewGenerated = false;

    // public abstract boolean isActionbarOverlay();
    private int ID_Fonts = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IsViewGenerated = false;
        ((SecurekeysApplication) getApplication()).mTListeners.add(this);
    }

    @Override
    protected void onDestroy() {
        ((SecurekeysApplication) getApplication()).mTListeners.remove(this);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        IsViewGenerated = true;
        LoadFonts f = LoadFonts.getInstance();

        if (f != null) {
            if (ID_Fonts == -1 || ID_Fonts != f.ID) {
                setTypeface(f);
                ID_Fonts = f.ID;
            }
        } else
            ((SecurekeysApplication) getApplication()).initFonts();
        super.onStart();
    }

    public abstract void setTypeface(LoadFonts f);

    @Override
    public void onFontInitialized(LoadFonts fonts) {
        if (isValid()) {
            if (IsViewGenerated) {
                if (ID_Fonts == -1 || ID_Fonts != fonts.ID) {
                    setTypeface(fonts);
                    ID_Fonts = fonts.ID;
                }
            }
        }
    }

    public final boolean isValid() {
        return !(isFinishing() || ((Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR1) && isDestroyed()));
    }
}
