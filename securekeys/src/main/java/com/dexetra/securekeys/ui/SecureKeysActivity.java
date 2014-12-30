package com.dexetra.securekeys.ui;

import android.os.Bundle;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.ActionBar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;

import com.dexetra.securekeys.R;
import com.dexetra.securekeys.utils.LoadFonts;
import com.dexetra.securekeys.utils.ThemeHelper;
import com.dexetra.securekeys.utils.TypefaceSpan;


public class SecureKeysActivity extends SecureKeysBaseActivity {

    SecureKeysFragment mSecureKeysFragment;
    BackKeyListener mBackKeyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        ThemeHelper.onActivityCreateSetTheme(this);
        setContentView(R.layout.activity_secure_keys);
        if (savedInstanceState == null) {
            mSecureKeysFragment = new SecureKeysFragment();
            mBackKeyListener = mSecureKeysFragment;
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mSecureKeysFragment)
                    .commit();
        }
        setActionBar();
    }

    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
    }

    private void setActionBarTitleTypeface(LoadFonts f){
        try {
            ActionBar actionBar = getSupportActionBar();
            SpannableStringBuilder s = new SpannableStringBuilder("  Secure");
            s.setSpan(new TypefaceSpan(f.getBold(), getResources()
                            .getDimension(R.dimen.text_primary)), 0,
                    s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            int index = s.length();
            s.append("keys");
            s.setSpan(new TypefaceSpan(f.getLight(), getResources()
                            .getDimension(R.dimen.text_primary)), index,
                    s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            actionBar.setTitle(s);
        } catch (Exception e) {
        }
    }

    @Override
    public void setTypeface(LoadFonts f) {
        setActionBarTitleTypeface(f);
    }

    @Override
    public void onBackPressed() {
        Log.d("TEST","onback pressed "+SecureKeysFragment.isPasswordShown);
        if(mBackKeyListener!=null && SecureKeysFragment.isPasswordShown) {
            mBackKeyListener.onBackPressed();
            return;
        }
        super.onBackPressed();
    }


    public interface BackKeyListener{
        public void onBackPressed();
    }
}
