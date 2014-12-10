package com.dexetra.securekeys.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.dexetra.securekeys.SecurekeysApplication;
import com.dexetra.securekeys.utils.LoadFonts;
import com.dexetra.securekeys.utils.ThemeAndFontListener;

/**
 * Created by vijith on 4/6/14.
 */
public abstract class SecureKeysBaseFragment extends Fragment implements ThemeAndFontListener{


    private int ID_Fonts = -1;
    protected SecureKeysBaseFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((SecurekeysApplication) getActivity().getApplication()).mTListeners.add(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        LoadFonts f = LoadFonts.getInstance();

        if (f != null) {
            if (ID_Fonts == -1 || ID_Fonts != f.ID) {
                setTypeface(f);
                ID_Fonts = f.ID;
            }
        } else
            ((SecurekeysApplication) getActivity().getApplication()).initFonts();
    }

    @Override
    public void onDestroy() {
        ((SecurekeysApplication) getActivity().getApplication()).mTListeners.remove(this);
        super.onDestroy();
    }

    public abstract void setTypeface(LoadFonts fonts);

    @Override
    public void onFontInitialized(LoadFonts fonts) {
        setTypeface(fonts);
    }
}
