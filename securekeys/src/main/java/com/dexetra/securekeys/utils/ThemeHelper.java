package com.dexetra.securekeys.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.dexetra.securekeys.R;

/**
 * Created by Vijith Menon on 29/12/14.
 */
public class ThemeHelper {

    private static Theme current = Theme.LIGHT;
    private Context mContext;

    public enum Theme {
        LIGHT, DARK;
        public int iconResId;
        public int bgColorId;
        public int seekbarResId;
        public int seekbarThumbId;
        public int accentColorId;
        public int normalColorId;
        public int genButtonResId;
        public int shadowColorId;
        public int edtBgColorId;
        public int passwordBgColorId;

        public static void init(Context context) {
            LIGHT.iconResId = R.drawable.ic_app_light;
            LIGHT.bgColorId = R.color.light_background;
            LIGHT.seekbarResId = R.drawable.scrubber_progress_horizontal_holo_light;
            LIGHT.seekbarThumbId = R.drawable.scrubber_control_selector_holo_light;
            LIGHT.accentColorId = R.color.light_accent;
            LIGHT.normalColorId = R.color.light_normal;
            LIGHT.genButtonResId = R.drawable.btn_generate_light;
            LIGHT.shadowColorId =  R.color.light_text_shadow;
            LIGHT.edtBgColorId = R.color.light_edt_bg_color;
            LIGHT.passwordBgColorId =R.color.light_background_translucent;

            DARK.iconResId = R.drawable.ic_app_dark;
            DARK.bgColorId = R.color.dark_background;
            DARK.seekbarResId = R.drawable.scrubber_progress_horizontal_holo_dark;
            DARK.seekbarThumbId = R.drawable.scrubber_control_selector_holo_dark;
            DARK.accentColorId = R.color.dark_accent;
            DARK.normalColorId = R.color.dark_normal;
            DARK.genButtonResId = R.drawable.btn_generate_dark;
            DARK.shadowColorId = R.color.dark_text_shadow;
            DARK.edtBgColorId = R.color.dark_edt_bg_color;
            DARK.passwordBgColorId =R.color.dark_background_translucent;
        }
    }

    public static ThemeHelper instance(Context context) {
        return new ThemeHelper(context);
    }

    public ThemeHelper(Context mContext) {
        this.mContext = mContext;
        initTheme();
        Theme.init(mContext);
    }

    public Theme getCurrentTheme() {
        return current;
    }

    private void initTheme() {
        try {
            if (mContext != null && mContext.getApplicationContext() != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                        mContext.getApplicationContext());
                if (prefs != null) {
                    String theme = prefs.getString("current_theme", null);
                    if (theme != null && theme.equals(Theme.DARK.name())) {
                        current = Theme.DARK;
                    } else current = Theme.LIGHT;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toggleTheme() {
        try {
            if (mContext != null && mContext.getApplicationContext() != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                        mContext.getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                if (prefs != null) {
                    String theme = prefs.getString("current_theme", null);
                    if (theme != null && theme.equals(Theme.DARK.toString())) {
                        editor.putString("current_theme", Theme.LIGHT.name());
                        current = Theme.LIGHT;
                    } else {
                        editor.putString("current_theme", Theme.DARK.name());
                        current = Theme.DARK;
                    }
                    editor.commit();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void changeToTheme(Activity activity) {
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
    }

    public static void onActivityCreateSetTheme(Activity activity) {
        switch (current) {
            case DARK:
                activity.setTheme(R.style.SecureKeys_Dark);
                break;
            default:
                activity.setTheme(R.style.SecureKeys_Light);
                break;
        }
    }


}
