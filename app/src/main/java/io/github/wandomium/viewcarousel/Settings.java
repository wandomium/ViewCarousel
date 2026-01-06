package io.github.wandomium.viewcarousel;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class Settings
{
    @SuppressWarnings("unused")
    private static final String CLASS_TAG = Settings.class.getSimpleName();

    private static SharedPreferences mPerfs;
    private static Settings mInstance;

    private static final String IS_FIRST_RUN = "IS_FIRST_RUN";
    private static final String SHOW_BTNS = "SHOW_BTNS";
    private static final String CONFIG_FILE = "CONFIG_FILE";

    private Settings(@NonNull Context ctx) {
        mPerfs = ctx.getApplicationContext().getSharedPreferences(R.string.app_name + ".settings", Context.MODE_PRIVATE);
    }
    public static Settings getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new Settings(ctx);
        }
        return mInstance;
    }
//    public boolean isFirstRun() {
//        boolean firstRun = mPerfs.getBoolean(IS_FIRST_RUN, true);
//        if (firstRun) {
//            mPerfs.edit().putBoolean(IS_FIRST_RUN, false).apply();
//        }
//        return firstRun;
//    }
    public boolean showBtns() {
        return mPerfs.getBoolean(SHOW_BTNS, true);
    }
    public void setShowBtns(boolean show) {
        mPerfs.edit().putBoolean(SHOW_BTNS, show).apply();
    }
    public String configFile() {
        return mPerfs.getString(CONFIG_FILE, "config.json");
    }
    public void setConfigFile(String configFile) {
        mPerfs.edit().putString(CONFIG_FILE, configFile).apply();
    }
}
