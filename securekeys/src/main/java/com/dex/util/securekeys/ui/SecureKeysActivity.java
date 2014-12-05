package com.dex.util.securekeys.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.dex.util.securekeys.R;
import com.dex.util.securekeys.utils.LoadFonts;
import com.dex.util.securekeys.utils.TypefaceSpan;


public class SecureKeysActivity extends SecureKeysBaseActivity {

    SecureKeysFragment mSecureKeysFragment;
    BackKeyListener mBackKeyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        actionBar.setTitle(R.string.app_name);
    }

    private void setActionBarTitleTypeface(LoadFonts f){
        try {
            ActionBar actionBar = getSupportActionBar();
            SpannableString s = new SpannableString(actionBar.getTitle());
            s.setSpan(new TypefaceSpan(f.getBold(), getResources()
                            .getDimension(R.dimen.text_primary)), 0,
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.secure_keys, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
