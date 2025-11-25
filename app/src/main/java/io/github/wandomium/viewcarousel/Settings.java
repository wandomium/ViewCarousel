package io.github.wandomium.viewcarousel;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class Settings
{
    private static SharedPreferences mPerfs;
    private static Settings mInstance;

    private static final String IS_FIRST_RUN = "IS_FIRST_RUN";

    private Settings(@NonNull Context ctx) {
        mPerfs = ctx.getSharedPreferences(R.string.app_name + "_settings", Context.MODE_PRIVATE);
    }
    public static Settings getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new Settings(ctx);
        }
        return mInstance;
    }
    public boolean isFirstRun() {
        boolean firstRun = mPerfs.getBoolean(IS_FIRST_RUN, true);
        if (firstRun) {
            mPerfs.edit().putBoolean(IS_FIRST_RUN, false).apply();
        }
        return firstRun;
    }
}
