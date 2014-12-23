/**
 *
 */
package com.dexetra.securekeys.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.dexetra.securekeys.R;
import com.dexetra.securekeys.utils.LoadFonts;


/**
 * @author Nithin John
 */
public class WebviewActivity extends SecureKeysBaseActivity {

    Context mContext;
    Activity mActivity;
    String mTitle="Help";

    public static Intent getLaunchIntent(Context context) {
        Intent intent = new Intent(context, WebviewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
        mContext = this;
        mActivity = this;
        setContentView(R.layout.activity_webview);

        setupActionBar();

        initWebView("file:///android_asset/index.html");
    }

    @Override
    public void setTypeface(LoadFonts f) {

    }

    protected void setupActionBar() {
        try {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            //if (mTitle != null)
            //    actionBar.setTitle(mTitle);
            //else
                actionBar.setTitle("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initWebView(final String url) {
        final WebView webView = (WebView) findViewById(R.id.webview_compontent);
        webView.clearFormData();
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(true);
        webView.clearCache(true);
        webView.clearHistory();
        webView.getSettings().setSavePassword(false);
        webView.loadUrl(url);


        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    setSupportProgressBarVisibility(false);
                } else {
                    setSupportProgressBarVisibility(true);
                    // Normalize our progress along the progress bar's scale
                    setSupportProgress((Window.PROGRESS_END - Window.PROGRESS_START)
                            / 100 * progress);
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH
                || keyCode == KeyEvent.FLAG_LONG_PRESS
                || (event.getFlags() & KeyEvent.FLAG_LONG_PRESS) == KeyEvent.FLAG_LONG_PRESS)
            return true;
        else {
            return super.onKeyDown(keyCode, event);
        }
    }

}
